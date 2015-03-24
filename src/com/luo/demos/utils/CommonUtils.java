package com.luo.demos.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.luo.demos.wifidemo.R;

public class CommonUtils {
	public static final String PIC_EDIT = "intent_extra_edit_pic_path";
	public final static long ONE_HOUR_IN_MILL = 60 * 60 * 1000;
	public final static String PREFS_RANDOM = "prefs_random";
	private static File m;
	public static String tempfile = "this_is_a_temp_file";

	/**
	 * isStringEmpty
	 * <p>
	 * Check if the given string is empty
	 * 
	 * @return return true if the string is null or len-of-zero
	 */
	public static boolean isStringEmpty(String str) {
		return (str == null || TextUtils.isEmpty(str));
	}

	/**
	 * isPathExist
	 * <p>
	 * Check if the given path is a file and exist
	 * 
	 * @param path
	 * @return return true if the given path is a file and exist
	 */
	public static boolean isPathExist(String path) {
		if (isStringEmpty(path)) {
			return false;
		}
		File file = new File(path);
		return ((file != null) && file.exists());
	}

	/**
	 * Check if the given url is a valid-url
	 * <p>
	 */
	public static boolean isUrl(String url) {
		if (!isStringEmpty(url) && url.startsWith("http")) {
			return true;
		}
		return false;
	}

	public static boolean isPathOrUrl(String path) {
		return ((isPathExist(path)) || (isUrl(path)));
	}

	/**
	 * deleteFileByPath
	 * <p>
	 * delete the file by given path
	 * 
	 * @param path
	 * @return return true if delete success, false if failed
	 */
	public static boolean deleteFileByPath(String path) {
		return deleteFile(new File(path));
	}

	/**
	 * deleteFile
	 * <p>
	 * delete the file by given file
	 * 
	 * @param path
	 * @return return true if delete success, false if failed
	 */
	public static boolean deleteFile(File file) {
		if (!isFileExist(file)) {
			return true;
		}
		return file.delete();
	}

	/**
	 * isFileExist
	 * <p>
	 * Check if the given file is existed
	 * 
	 * @param path
	 * @return return true if the given file is not-null and exist
	 */
	public static boolean isFileExist(File file) {
		return ((file != null) && file.exists());
	}

	/**
	 * Check the file's size of the given path
	 * 
	 * @return true if the file's size > 0
	 */
	public static boolean isFileHasContent(String path) {
		return (path != null) && isFileHasContent(new File(path));
	}

	/**
	 * Check the file's size
	 * 
	 * @return true if the file's size > 0
	 */
	public static boolean isFileHasContent(File file) {
		return isFileExist(file) && file.length() > 5000;
	}

	/**
	 * show KB when size is less then 0.01MB
	 */
	public static String getSizeWithUnit(long size) {
		if (size <= 1024 * 10.24) {
			String KiloBytes = "";
			Float kb = new Float(size);
			kb = kb / 1024;
			KiloBytes = kb.toString();
			int index = KiloBytes.indexOf(".");
			if (index > 0 && index < KiloBytes.length() - 1) {
				int endIndex = KiloBytes.length() >= index + 3 ? index + 3 : KiloBytes.length();
				KiloBytes = KiloBytes.substring(0, endIndex);
			}
			KiloBytes += " KB";
			return KiloBytes;
		} else {
			String megaBytes = "";
			Float mb = new Float(size);
			mb = mb / (1024 * 1024);
			megaBytes = mb.toString();
			int index = megaBytes.indexOf(".");
			if (index > 0 && index < megaBytes.length() - 1) {
				int endIndex = megaBytes.length() >= index + 3 ? index + 3 : megaBytes.length();
				megaBytes = megaBytes.substring(0, endIndex);
			}
			megaBytes += " MB";
			return megaBytes;
		}
	}

	/**
	 * Check if network connect
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}


	/**
	 * check if the phone use Chinese as first language
	 * @param context
	 * @return
	 */
	public static boolean isChineseLaguage(Context context) {
		if (context.getResources().getConfiguration().locale.getCountry().equals("CN") || context.getResources().getConfiguration().locale.getCountry().equals("TW")) {
			return true;
		} else {
			return false;
		}
	}

	public static Bitmap saveTempBitmap(Bitmap btm) {
		String time = callTime();
		if (btm != null) {
			try {
				File f = new File(Environment.getExternalStorageDirectory(), tempfile);
				if (!f.exists()) {
					f.mkdirs();
				}
				m = null;
				m = new File(f, time + ".jpg");
				FileOutputStream bos = new FileOutputStream(m);
				btm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				bos.flush();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return btm;
	}

	public static String getTemppath() {
		return m.toString();
	}

	public static void CleanTempBitmap() {
		File file = new File(Environment.getExternalStorageDirectory(), tempfile);
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				(childFiles[i]).delete();
			}
			return;
		}
	}

	public static Bitmap getBitmapFromPathNewSize(String path, int maxnewwidth, int maxnewhight) {
		Bitmap tempBitmap = null;
		if (!CommonUtils.isPathExist(path)) {
			return null;
		}
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		try {
			tempBitmap = BitmapFactory.decodeFile(path, op);
			int wRatio = (int) Math.ceil(op.outWidth / (float) maxnewwidth);
			int hRatio = (int) Math.ceil(op.outHeight / (float) maxnewhight);
			if (wRatio > 1 && hRatio > 1) {
				if (wRatio > hRatio) {
					op.inSampleSize = wRatio;
				} else {
					op.inSampleSize = hRatio;
				}
			}
			op.inJustDecodeBounds = false;
			tempBitmap = BitmapFactory.decodeFile(path, op);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return tempBitmap;
	}

	public static String callTime() {
		long backTime = new Date().getTime();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(backTime));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int date = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		String time = "" + year + month + date + hour + minute + second;
		Logger.i("CurrentTime", "^^^^^^^^^^^^^" + time + "^^^^^^^^^^^^^");
		return time;
	}
}
