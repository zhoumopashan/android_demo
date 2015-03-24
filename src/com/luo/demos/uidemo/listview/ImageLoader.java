package com.luo.demos.uidemo.listview;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import sun.encoder.MD5Util;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.luo.demos.MainApplication;
import com.luo.demos.utils.CommonUtils;
import com.luo.demos.utils.DownloadUtil;
import com.luo.demos.utils.Logger;
import com.luo.demos.utils.StoreUtil;
import com.luo.demos.wifidemo.R;

public class ImageLoader {
	/**
	 * 图片缓存的核心类
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 线程池
	 */
	private ExecutorService mThreadPool;
	/**
	 * 线程池的线程数量，默认为1
	 */
	private int mThreadCount = 1;
	/**
	 * 队列的调度方式
	 */
	private Type mType = Type.LIFO;
	/**
	 * 任务队列
	 */
	private LinkedList<Runnable> mTasks;
	/**
	 * 轮询的线程
	 */
	private Thread mPoolThread;
	private Handler mPoolThreadHander;

	/**
	 * 运行在UI线程的handler，用于给ImageView设置图片
	 */
	private Handler mHandler;

	/**
	 * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
	 */
	private volatile Semaphore mSemaphore = new Semaphore(0);

	/**
	 * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
	 */
	private volatile Semaphore mPoolSemaphore;

	private static ImageLoader mInstance;

	/**
	 * 队列的调度方式
	 * 
	 * @author zhy
	 * 
	 */
	public enum Type {
		FIFO, LIFO
	}

	public static interface ImageLoadCompletedObserver {
	    public abstract void loaded(ImageView imageView, int bitmapWidth, int bitmapHeight);
	}

	/**
	 * 单例获得该实例对象
	 * 
	 * @return
	 */
	public static ImageLoader getInstance() {

		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(1, Type.LIFO);
				}
			}
		}
		return mInstance;
	}

	private ImageLoader(int threadCount, Type type) {
		init(threadCount, type);
	}

	private void init(int threadCount, Type type) {
		// loop thread
		mPoolThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				mPoolThreadHander = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						mThreadPool.execute(getTask());
						try {
							mPoolSemaphore.acquire();
						} catch (InterruptedException e) {
						}
					}
				};
				// 释放一个信号量
				mSemaphore.release();
				Looper.loop();
			}
		};
		mPoolThread.start();

		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			};
		};

		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mPoolSemaphore = new Semaphore(threadCount);
		mTasks = new LinkedList<Runnable>();
		mType = type == null ? Type.LIFO : type;

	}
	
	public void loadImage(final String path, final ImageView imageView) {
		loadImage(path , imageView , -1);
	}
	
	public void loadImage(final String path, final ImageView imageView , int defaultDrawableId) {
		loadImage(path , imageView , defaultDrawableId , false, null);
	}

    public void loadImage(final String path, final ImageView imageView, int defaultDrawableId, ImageLoadCompletedObserver obser) {
        loadImage(path, imageView, defaultDrawableId, false, obser);
    }
	

	/**
	 * 加载图片
	 * 
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView , int defaultDrawableId, final boolean doNotCompress, final ImageLoadCompletedObserver obser) {
		// null check
		if(!CommonUtils.isPathOrUrl(path) || imageView == null){
			// set default src
			this.setImageDefaultSrc(imageView , defaultDrawableId);
			return;
		}
		
		// set tag
//		imageView.setTag(path);
		imageView.setTag(R.id.tag_imageloader, path);
		// UI线程
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					ImageView imageView = holder.imageView;
					Bitmap bm = holder.bitmap;
					ImageLoadCompletedObserver obserCallback = holder.obser;
					if(bm == null){
						return;
					}
					String path = holder.path;
					if (imageView.getTag(R.id.tag_imageloader).toString().equals(path)) {
						imageView.setImageBitmap(bm);
						if (obserCallback != null) {
							/*
							 * add by luochenxun , to avoid use a too long
							 * bitmap's height
							 */
							int MAX_HEIGHT = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.photo_max_len);
							int imageHeight = bm.getHeight();
							if (imageHeight > MAX_HEIGHT) {
								imageHeight = MAX_HEIGHT;
							}
							obserCallback.loaded(imageView, bm.getWidth(), imageHeight);
						}
					}
				}
			};
		}

		Bitmap bm = getBitmapFromLruCache(path);
		if (bm != null) {
			ImgBeanHolder holder = new ImgBeanHolder();
			holder.bitmap = bm;
			holder.imageView = imageView;
			holder.path = path;
			holder.obser = obser;
			Message message = Message.obtain();
			message.obj = holder;
			mHandler.sendMessage(message);
		} else {
			// set default src
			this.setImageDefaultSrc(imageView , defaultDrawableId);
						
			addTask(new Runnable() {
				@Override
				public void run() {

					ImageSize imageSize = getImageViewWidth(imageView);

					int reqWidth = imageSize.width;
					int reqHeight = imageSize.height;

					Bitmap bm = decodeSampledBitmapFromResource(path, reqWidth, reqHeight, doNotCompress);
					addBitmapToLruCache(path, bm);
					ImgBeanHolder holder = new ImgBeanHolder();
					holder.bitmap = getBitmapFromLruCache(path);
					holder.imageView = imageView;
					holder.path = path;
				    holder.obser = obser;
					Message message = Message.obtain();
					message.obj = holder;
					// Log.e("TAG", "mHandler.sendMessage(message);");
					mHandler.sendMessage(message);
					mPoolSemaphore.release();
				}
			});
		}

	}
	
	/**
	 * 加载图片
	 * 
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView , final String defaultPath) {
		// null check
		if(!CommonUtils.isPathOrUrl(path) || imageView == null){
			return;
		}
		
		// set tag
		imageView.setTag(R.id.tag_imageloader, path);
		// UI线程
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					ImageView imageView = holder.imageView;
					Bitmap bm = holder.bitmap;
					if(bm == null){
						return;
					}
					String path = holder.path;
					if (imageView.getTag(R.id.tag_imageloader).toString().equals(path)) {
						imageView.setImageBitmap(bm);
					}
				}
			};
		}

		Bitmap bm = getBitmapFromLruCache(path);
		if (bm != null) {
			ImgBeanHolder holder = new ImgBeanHolder();
			holder.bitmap = bm;
			holder.imageView = imageView;
			holder.path = path;
			Message message = Message.obtain();
			message.obj = holder;
			mHandler.sendMessage(message);
		} else {
			// set default path
			Bitmap defaultBitmap = getBitmapFromLruCache(defaultPath);
			if( defaultBitmap != null ){
				imageView.setImageBitmap(defaultBitmap);
			}else{
				defaultBitmap = decodeSampledBitmapFromResource(defaultPath, 
						getImageViewWidth(imageView).width, getImageViewWidth(imageView).height);
				if( defaultBitmap != null ){
					addBitmapToLruCache(defaultPath, defaultBitmap);
					imageView.setImageBitmap(defaultBitmap);
				}
			}
			// start a task to download the photo
			addTask(new Runnable() {
				@Override
				public void run() {

					ImageSize imageSize = getImageViewWidth(imageView);

					int reqWidth = imageSize.width;
					int reqHeight = imageSize.height;

					Bitmap bm = decodeSampledBitmapFromResource(path, reqWidth, reqHeight);
					addBitmapToLruCache(path, bm);
					ImgBeanHolder holder = new ImgBeanHolder();
					holder.bitmap = getBitmapFromLruCache(path);
					holder.imageView = imageView;
					holder.path = path;
					Message message = Message.obtain();
					message.obj = holder;
					// Log.e("TAG", "mHandler.sendMessage(message);");
					mHandler.sendMessage(message);
					mPoolSemaphore.release();
				}
			});
		}

	}
	
	private void setImageDefaultSrc(ImageView imageview , int drawableId){
		if( imageview != null && drawableId != -1 ){
			imageview.setImageResource(drawableId);
		}
	}

	/**
	 * 添加一个任务
	 * 
	 * @param runnable
	 */
	private synchronized void addTask(Runnable runnable) {
		try {
			// 请求信号量，防止mPoolThreadHander为null
			if (mPoolThreadHander == null)
				mSemaphore.acquire();
		} catch (InterruptedException e) {
		}
		
		mTasks.add(runnable);
		mPoolThreadHander.sendEmptyMessage(0x110);
	}

	/**
	 * 取出一个任务
	 * 
	 * @return
	 */
	private synchronized Runnable getTask() {
		if (mType == Type.FIFO) {
			return mTasks.removeFirst();
		} else if (mType == Type.LIFO) {
			return mTasks.removeLast();
		}
		return null;
	}

	/**
	 * 单例获得该实例对象
	 * 
	 * @return
	 */
	public static ImageLoader getInstance(int threadCount, Type type) {

		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(threadCount, type);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 根据ImageView获得适当的压缩的宽和高
	 * 
	 * @param imageView
	 * @return
	 */
	private ImageSize getImageViewWidth(ImageView imageView) {
		ImageSize imageSize = new ImageSize();
		final DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
		final LayoutParams params = imageView.getLayoutParams();

		int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth(); // Get
																							// actual
																							// image
																							// width
		if (width <= 0)
			width = params.width; // Get layout width parameter
		if (width <= 0)
			width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
																	// maxWidth
																	// parameter
		if (width <= 0)
			width = displayMetrics.widthPixels;
		int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight(); // Get
																								// actual
																								// image
																								// height
		if (height <= 0)
			height = params.height; // Get layout height parameter
		if (height <= 0)
			height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
																		// maxHeight
																		// parameter
		if (height <= 0)
			height = displayMetrics.heightPixels;
		imageSize.width = width;
		imageSize.height = height;
		return imageSize;

	}

	/**
	 * 从LruCache中获取一张图片，如果不存在就返回null。
	 */
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}

	/**
	 * 往LruCache中添加一张图片
	 * 
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToLruCache(String key, Bitmap bitmap) {
		if (getBitmapFromLruCache(key) == null) {
			if (bitmap != null)
				mLruCache.put(key, bitmap);
		}
	}

	/**
	 * 计算inSampleSize，用于压缩图片
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// 源图片的宽度
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;

		if (width > reqWidth && height > reqHeight) {
			// 计算出实际宽度和目标宽度的比率
			int widthRatio = Math.round((float) width / (float) reqWidth);
			int heightRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = Math.max(widthRatio, heightRatio);
		}
		return inSampleSize;
	}
	
	private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
		return decodeSampledBitmapFromResource(pathName , reqWidth , reqHeight , false);
	}

	/**
	 * 根据计算的inSampleSize，得到压缩后图片
	 * 
	 * @param pathName
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight , boolean doNotCompress) {
		
		pathName = this.downloadPhotoIfGetByWeb( pathName );
		
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		
		Bitmap bitmap = null;
		if (doNotCompress) {
			options.inJustDecodeBounds = false;
			options.inSampleSize = 1; // do not compress the image
		} else {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, options);
			// 调用上面定义的方法计算inSampleSize值
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			// 使用获取到的inSampleSize值再次解析图片
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = null;
			options.inPurgeable = true;
			options.inInputShareable = true;
		}
		
		// decode the file
		try {
			bitmap = BitmapFactory.decodeFile(pathName, options);
		} catch (Exception e) {
		} catch(OutOfMemoryError error){
			// if outOfMemory , remove all cache and retry
			mLruCache.evictAll();
			Logger.e("imageloader","OutOfMemoryError");
			try{
			    bitmap = BitmapFactory.decodeFile(pathName, options);
			} catch(OutOfMemoryError retryerr){
				
			}
		}

		return bitmap;
	}
	
	/**
	 * If the path is http-protocol, download it fromWeb
	 * 
	 * @param pathName
	 */
	private String downloadPhotoIfGetByWeb(String pathName){
		/* null check */
		if( CommonUtils.isStringEmpty(pathName) || !pathName.startsWith("http")){
			return pathName;
		}
		
		/* translate to tempFile's path */
		String realFilePath = new File( StoreUtil.getTempPath(MainApplication.getInstance()) , 
				MD5Util.getMD5(pathName) ).getAbsolutePath();
		
		/* download file */
		if( !CommonUtils.isPathExist(realFilePath) ){
			DownloadUtil.downloadFile(pathName, StoreUtil.getTempPath(MainApplication.getInstance()), MD5Util.getMD5(pathName));
		}
		
		return realFilePath;
	}

	private class ImgBeanHolder {
		Bitmap bitmap;
		ImageView imageView;
		String path;
		ImageLoadCompletedObserver obser;
	}

	private class ImageSize {
		int width;
		int height;
	}

	/**
	 * 反射获得ImageView设置的最大宽度和高度
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;

				Log.e("TAG", value + "");
			}
		} catch (Exception e) {
		}
		return value;
	}

}
