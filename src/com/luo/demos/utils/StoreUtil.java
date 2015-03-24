package com.luo.demos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import com.luo.demos.wifidemo.R;

public class StoreUtil {

	/*****************
	 * Macros
	 ***************/
	private static final String TAG = "StoreUtil";

	/** Default sharePrefereces's name */
	public static final String DEFAULT_SHARE_PREFERENCES_NAME = "default";
	/** Default Boolean Value */
	public static final Boolean DEFAULT_BOOLEAN_VALUE = false;
	public static final int DEFAULT_INT_VALUE = -1;
	public static final long DEFAULT_LONG_VALUE = -1;
	public static final String DEFAULT_STRING_VALUE = null;

	/**
	 * The mCacheIconDir can cache the icons else , use cache's dir of the app
	 */
	private static String mCachePath;
	private static String mTempPath;
	private static String mPhotoWorkingPath;
	private static String mDefaultSdPath;
	/** main working dir */
	public static final String DEFAULT_WORK_PATH = ConfigConstant.APP_NAME;
	public static String mWorkSpacePath = DEFAULT_WORK_PATH;
	/** main saving photo dir */
	public static final String DEFAULT_PHOTO_PATH = "photo";
	/** main caching photo dir */
	public static final String DEFAULT_CACHE_PATH = "cache";
	/** main temp photo dir for community */
	public static final String DEFAULT_TEMP_PATH = "temp";
	/** main save download-app dir */
	public static final String DEFAULT_APP_PATH = "app";
	/** main save cache-icon dir */
	public static final String DEFAULT_APPICON_PATH = "appicon";
	/** test dir */
	public static final String DEFAULT_TEST_PATH = mWorkSpacePath + File.separatorChar + "test";

	/** reset All path */
	public static void resetAllPath() {
		mCachePath = null;
		mPhotoWorkingPath = null;
		mDefaultSdPath = null;
		mWorkSpacePath = DEFAULT_WORK_PATH;
	}

	/**
	 * getCachePath
	 * <p>
	 * Get default cache-screenshot-path <br>
	 * $sdcard$/kacha/cache
	 * 
	 * @return null if there is no sdcard
	 */
	public static String getCachePath(Context context) {
		// Get cache-string if init already
		if (CommonUtils.isPathExist(mCachePath)) {
			return mCachePath;
		}

		// Get sdcard path
		String sdPath = getDefaultSDPath(context);
		if (sdPath == null) {
			return null;
		}

		// Get Photo path
		File cacheFile = new File(sdPath, mWorkSpacePath + File.separatorChar + DEFAULT_CACHE_PATH);
		if (!CommonUtils.isFileExist(cacheFile)) {
			cacheFile.mkdirs();
		}
		mCachePath = cacheFile.getAbsolutePath();
		if (!CommonUtils.isPathExist(mCachePath)) {
			return null;
		}

		if (!mCachePath.endsWith("/")) {
			mCachePath += "/";
		}

		createNoMediaFileIfPathExist(mCachePath);

		return mCachePath;
	}

	/**
	 * getCachePath
	 * <p>
	 * Get default cache-community-path <br>
	 * $sdcard$/kacha/temp
	 * 
	 * @return null if there is no sdcard
	 */
	public static String getTempPath(Context context) {
		// Get cache-string if init already
		if (CommonUtils.isPathExist(mTempPath)) {
			return mTempPath;
		}

		// Get sdcard path
		String sdPath = getDefaultSDPath(context);
		if (sdPath == null) {
			return context.getCacheDir().getAbsolutePath();
		}

		// Get temp path
		File cacheFile = new File(sdPath, mWorkSpacePath + File.separatorChar + DEFAULT_TEMP_PATH);
		if (!CommonUtils.isFileExist(cacheFile)) {
			cacheFile.mkdirs();
		}
		mTempPath = cacheFile.getAbsolutePath();
		if (!CommonUtils.isPathExist(mTempPath)) {
			return null;
		}

		if (!mTempPath.endsWith("/")) {
			mTempPath += "/";
		}

		createNoMediaFileIfPathExist(mTempPath);

		return mTempPath;
	}

	/**
	 * getPhotoPath
	 * <p>
	 * $sdcard$/kacha/photo
	 * 
	 * @param context
	 * @return getCachePath
	 */
	public static String getPhotoPath(Context context) {
		// Get cache-string if init already
		if (CommonUtils.isPathExist(mPhotoWorkingPath)) {
			return mPhotoWorkingPath;
		}

		// Get sdcard path
		String sdPath = getDefaultSDPath(context);
		if (sdPath == null) {
			return null;
		}

		/** remove photo path */
		File photoFile = new File(sdPath, mWorkSpacePath);
		if (!CommonUtils.isFileExist(photoFile)) {
			photoFile.mkdirs();
		}
		mPhotoWorkingPath = photoFile.getAbsolutePath();

		if (!CommonUtils.isPathExist(mPhotoWorkingPath)) {
			return null;
		}

		if (!mPhotoWorkingPath.endsWith("/")) {
			mPhotoWorkingPath += "/";
		}

		return mPhotoWorkingPath;
	}

	public static File getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory();
	}

	public static String getStorageRoot() {
		String rootDir = getExternalStorageDirectory().getAbsolutePath();
		int index = rootDir.indexOf('/', 1);
		String storageRoot = (index > 0) ? rootDir.substring(0, index) + "/" : "/";
		return storageRoot;
	}

	/**
	 * isExternalStorageAvailable <br>
	 * <p>
	 * 
	 * @return if the External Storage is Available <br>
	 */
	public static boolean isExternalStorageAvailable() {
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * getExternalStoragePath <br>
	 * <p>
	 * 
	 * @return return the External Storage's path <br>
	 */
	public static String getExternalStoragePath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * get Default SDPath
	 * 
	 * @param context
	 * @return
	 */
	public static String getDefaultSDPath(Context context) {
		Logger.d(TAG, "getDefaultSDPath");
		// if (CommonUtils.isPathExist(mDefaultSdPath)) {
		// return mDefaultSdPath;
		// } else {
		return initDefaultSDPath(context);
		// }
	}

	/**
	 * Init default sdPath
	 * 
	 * @param context
	 * @return
	 */
	public static String initDefaultSDPath(Context context) {
		Logger.d(TAG, "initDefaultSDPath");

		// Get default sdPath by Environment
		File sdFile = getExternalStorageDirectory();
		if (sdFile != null && isExternalStorageAvailable()) {
			mDefaultSdPath = getExternalStoragePath();
		}
		if (isSdUseful(mDefaultSdPath)) {
			mDefaultSdPath = addFloderSuffix(mDefaultSdPath);
			return mDefaultSdPath;
		}

		// Get sdPath by android common-path : "/sdcard"
		try {
			mDefaultSdPath = new File(context.getString(R.string.sdcard)).getCanonicalPath();
		} catch (IOException e) {
			mDefaultSdPath = context.getString(R.string.sdcard);
		}
		if (isSdUseful(mDefaultSdPath)) {
			mDefaultSdPath = addFloderSuffix(mDefaultSdPath);
			return mDefaultSdPath;
		}

		// Traverse Storage's root
		mDefaultSdPath = traverseGetSDPath(getStorageRoot());
		if (isSdUseful(mDefaultSdPath)) {
			mDefaultSdPath = addFloderSuffix(mDefaultSdPath);
			return mDefaultSdPath;
		}

		// Traverse Storage's parent
		mDefaultSdPath = traverseGetSDPath(sdFile.getParent());
		if (isSdUseful(mDefaultSdPath)) {
			mDefaultSdPath = addFloderSuffix(mDefaultSdPath);
			return mDefaultSdPath;
		}

		return null;
	}

	/**
	 * getDefaultSharePreferences
	 * 
	 * @param context
	 * @return
	 */
	public static SharedPreferences getDefaultSharePreferences(Context context) {
		return context.getSharedPreferences(DEFAULT_SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	/**
	 * Get Boolean-value From Default's prefs
	 * 
	 * @param context
	 * @return false as default
	 */
	public static boolean getBooleanFromDefault(Context context, String key) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getBoolean(key, DEFAULT_BOOLEAN_VALUE);
	}

	public static boolean getBooleanFromDefault(Context context, String key, boolean defaultValue) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getBoolean(key, defaultValue);
	}

	/**
	 * Set boolean-value to Default's prefs
	 * 
	 * @param context
	 */
	public static void setBooleanToDefault(Context context, String key, boolean value) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		prefs.edit().putBoolean(key, value).commit();
	}

	/**
	 * Get Integer-value From Default's prefs
	 * 
	 * @param context
	 * @return -1 as default
	 */
	public static int getIntFromDefault(Context context, String key) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getInt(key, DEFAULT_INT_VALUE);
	}

	public static int getIntFromDefault(Context context, String key, int defaultValue) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getInt(key, defaultValue);
	}

	/**
	 * Set Integer-value to Default's prefs
	 * 
	 * @param context
	 */
	public static void setIntToDefault(Context context, String key, int value) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		prefs.edit().putInt(key, value).commit();
	}

	/**
	 * Get Long-value From Default's prefs
	 * 
	 * @param context
	 * @return -1 as default
	 */
	public static long getLongFromDefault(Context context, String key) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getLong(key, DEFAULT_LONG_VALUE);
	}

	public static long getLongFromDefault(Context context, String key, long value) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getLong(key, value);
	}

	/**
	 * Set Long-value to Default's prefs
	 * 
	 * @param context
	 */
	public static void setLongToDefault(Context context, String key, long value) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		prefs.edit().putLong(key, value).commit();
	}

	/**
	 * Get String-value From Default's prefs
	 * 
	 * @param context
	 * @return null as default
	 */
	public static String getStringFromDefault(Context context, String key) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getString(key, DEFAULT_STRING_VALUE);
	}

	public static String getStringFromDefault(Context context, String key, String defaultValue) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		return prefs.getString(key, defaultValue);
	}

	/**
	 * Set String-value to Default's prefs
	 * 
	 * @param context
	 */
	public static void setStringToDefault(Context context, String key, String value) {
		SharedPreferences prefs = getDefaultSharePreferences(context);
		prefs.edit().putString(key, value).commit();
	}

	/********************
	 * private methods
	 ********************/

	/**
	 * 
	 * @param file
	 * @return
	 */
	private static boolean isPathOnSdcardIndepent(File file) {
		return isPathOnSdcardIndepent(file.getAbsolutePath());
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private static boolean isPathOnSdcardIndepent(String path) {
		boolean result = false;
		// path change to storage dir
		int index = path.indexOf('/');
		index = path.indexOf('/', index + 1);
		index = path.indexOf('/', index + 1);
		String storagePath = index == -1 ? path : path.substring(0, index);
		storagePath += " ";

		// find storage's device index from mounts
		String lineInMounts = "";
		File mounts = new File("/proc/mounts");
		String deviceIndex = "";
		BufferedReader br = null;
		if (mounts.exists()) {
			try {
				br = new BufferedReader(new FileReader(mounts));
				while ((lineInMounts = br.readLine()) != null) {
					if (lineInMounts.contains(storagePath)) {
						String lines[] = lineInMounts.split(" ");
						deviceIndex = lines[0];
						deviceIndex = deviceIndex.substring(deviceIndex.lastIndexOf(':') + 1, deviceIndex.length());
						if (deviceIndex != null && deviceIndex.length() != 0) {
							deviceIndex = " " + deviceIndex + " ";
						}
						break;
					}
				}
			} catch (FileNotFoundException e) {
				Logger.e(TAG, "catch FileNotFoundException: " + e.getMessage());
			} catch (IOException e) {
				Logger.e(TAG, "catch IOException: " + e.getMessage());
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					Logger.e(TAG, "catch IOException: " + e.getMessage());
				}
			}
			if (deviceIndex == null || deviceIndex.length() == 0) {
				result = false;
			} else {
				// is storage's device id mapped to external storage: sdcard
				File partitions = new File("/proc/partitions");
				if (partitions.exists()) {
					try {
						br = new BufferedReader(new FileReader(partitions));
						String lineInPartitions = "";
						while ((lineInPartitions = br.readLine()) != null) {
							if (lineInPartitions.contains(deviceIndex)) {
								if (lineInPartitions.contains("mmcblk1p1") || lineInPartitions.contains("mmcblk1")) {
									result = true;
								}
								break;
							}
						}
					} catch (FileNotFoundException e) {
						Logger.e(TAG, "catch FileNotFoundException: " + e.getMessage());
					} catch (IOException e) {
						Logger.e(TAG, "catch IOException: " + e.getMessage());
					} finally {
						try {
							br.close();
						} catch (IOException e) {
							Logger.d(TAG, "catch IOException: " + e.getMessage());
						}
					}
				} else {
					Logger.d(TAG, "/proc/partitions file does not exist.");
					result = false;
				}
			}
		} else {
			Logger.d(TAG, "/proc/mounts file does not exist.");
			result = false;
		}

		Logger.d(TAG, "path " + path + " on sdcard: " + result);
		return result;
	}

	/**
	 * Check if the given path is a useful SDpath
	 */
	private static boolean isSdUseful(String path) {
		File testFloder = new File(path, mWorkSpacePath);
		if (CommonUtils.isFileExist(testFloder)) {
			return true;
		}
		if (testFloder != null && testFloder.mkdir()) {
			return true;
		}
		return false;
	}

	public static boolean isPathWritable(String path) {
		File testFloder = new File(path, "test_kacha");
		if (CommonUtils.isFileExist(testFloder)) {
			testFloder.delete();
			return true;
		}
		if (testFloder != null && testFloder.mkdir()) {
			testFloder.delete();
			return true;
		}
		return false;
	}

	public static void createNoMediaFileIfPathExist(String pathDir) {
		// param check
		if (!CommonUtils.isPathExist(pathDir)) {
			return;
		}
		File noMediaFile = new File(pathDir, ".nomedia");
		if (!CommonUtils.isFileExist(noMediaFile)) {
			try {
				noMediaFile.createNewFile();
			} catch (IOException e) {
				Logger.e(TAG, "createNoMediaFileIfPathExist error");
			}
		}
	}

	/**
	 * Traverse the given path and search if it has sdPath
	 * 
	 * @param rootPath
	 * @return
	 */
	private static String traverseGetSDPath(String rootPath) {
		if (CommonUtils.isStringEmpty(rootPath))
			return null;

		File rootFile = new File(rootPath);
		File files[] = rootFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (isLink(pathname) || pathname.isDirectory()) && !pathname.isHidden() && pathname.canWrite();
			}
		});
		int filesCount = (null == files) ? 0 : files.length;
		Logger.d(TAG, "get " + filesCount + " files from :" + rootPath);
		int externalStorageInd = -1;
		for (int i = 0; i < filesCount; i++) {
			Logger.d(TAG, "files[" + i + "].getAbsolutePath(): " + files[i].getAbsolutePath());
			if (isPathOnSdcardIndepent(files[i].getAbsolutePath())) {
				externalStorageInd = i;
				Logger.d(TAG, "find external storage: " + i);
				break;
			}
		}

		if (-1 != externalStorageInd) {
			mDefaultSdPath = files[externalStorageInd].getAbsolutePath();
		} else if (filesCount > 0) {
			mDefaultSdPath = files[0].getAbsolutePath();
		} else {
			Logger.w(TAG, "no external storage on :" + rootPath);
			return null;
		}
		return mDefaultSdPath;
	}

	/**
	 * Add a seperator end-of-floder's path
	 * 
	 * @param path
	 * @return
	 */
	private static String addFloderSuffix(String path) {
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		return path;
	}

	/**
	 * decompress raw file to files dir
	 * 
	 * @param resId
	 *            , raw file resource id
	 * @param desFile
	 *            , destination file name
	 * @return boolean, false: failed true: success
	 */
	public static void decompressRawFile(Context context, int rawFileRes, String desFile) throws IOException {
		InputStream is = context.getResources().openRawResource(rawFileRes);
		FileOutputStream fos = context.openFileOutput(desFile, Context.MODE_PRIVATE);
		GZIPInputStream gzpis = new GZIPInputStream(is);
		byte[] buffer = new byte[4096];
		int readbytes;
		while ((readbytes = gzpis.read(buffer)) > 0) {
			fos.write(buffer, 0, readbytes);
		}
		/** close handle */
		gzpis.close();
		fos.flush();
		fos.close();
		buffer = null;
	}

	/**
	 * decompress assetsFile to files dir
	 * 
	 * @param resId
	 *            , assetsFile resource id
	 * @param desFile
	 *            , destination file name
	 * @return boolean, false: failed true: success
	 */
	public static void decompressAssetsFile(Context context, String assetsFile, String desFile) throws IOException {
		InputStream is = context.getAssets().open(assetsFile);
		FileOutputStream fos = context.openFileOutput(desFile, Context.MODE_PRIVATE);
		GZIPInputStream gzpis = new GZIPInputStream(is);
		byte[] buffer = new byte[4096];
		int readbytes;
		while ((readbytes = gzpis.read(buffer)) > 0) {
			fos.write(buffer, 0, readbytes);
		}
		/** close handle */
		gzpis.close();
		fos.flush();
		fos.close();
		buffer = null;
	}

	private static boolean isLink(File file) {
		String cPath = "";
		try {
			cPath = file.getCanonicalPath();
		} catch (IOException ex) {
			return false;
		}
		return !cPath.equals(file.getAbsolutePath());
	}

	public static void getStorageEnvValue() {
		String externallStorage = System.getenv("EXTERNAL_STORAGE");
		String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
		String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");

		Logger.d(TAG, "external storage: " + externallStorage);
		Logger.d(TAG, "emulated storage source: " + emulatedStorageSource);
		Logger.d(TAG, "emulated storage target: " + emulatedStorageTarget);
	}

	public static String getExternalStorageDirectoryPath() {
		String externalPath = null;
		/** handle android 4.2+ multi user external storage path problem */
		if (Build.VERSION.SDK_INT < 17) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		externalPath = System.getenv("EXTERNAL_STORAGE");
		if (externalPath == null)
			externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		return externalPath;
	}

}
