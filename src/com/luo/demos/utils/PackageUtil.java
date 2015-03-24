package com.luo.demos.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.luo.demos.wifidemo.R;

public class PackageUtil {

	private static final String TAG = "PackageUtil";

	/******************************
	 * Inner Class <br>
	 ******************************/
	public static interface PackageInstallObserver {
		public static final int INSTALL_FAILED = -1;
		public static final int INSTALL_SUCCESS = 0;
		public static final int INSTALL_BY_SYSTEM = 1;

		void onInstallApkCallBack(int result);
	}

	/******************************
	 * public Methods <br>
	 ******************************/

	/** get all installed application info */
	public static List<ApplicationInfo> getInstalledPackageList(Context context) {
		PackageManager pm = context.getPackageManager();
		return pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
	}

	/** get uid is system package list */
	public static List<ApplicationInfo> getSystemPackageList(Context context) {
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> installedList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		ApplicationInfo item;
		ArrayList<ApplicationInfo> systemList = new ArrayList<ApplicationInfo>();
		final int N = installedList.size();
		for (int i = 0; i < N; i++) {
			item = installedList.get(i);
			if (item.uid == Process.SYSTEM_UID)
				systemList.add(item);
		}

		return systemList;
	}

	/**
	 * Get the minimum systemApp's path <br>
	 */
	public static String getMinSystemPackagePath(Context context) {
		String path = null;
		List<ApplicationInfo> systemList = getSystemPackageList(context);
		final int N = systemList.size();
		if (N > 0) {
			int min_idx = 0;
			long min_size = getFileSize(systemList.get(0).sourceDir);
			long fileSize;

			for (int i = 1; i < N; i++) {
				fileSize = getFileSize(systemList.get(i).sourceDir);
				if (fileSize != 0 && fileSize < min_size) {
					min_size = fileSize;
					min_idx = i;
				}
			}
			path = systemList.get(min_idx).sourceDir;
		}

		return path;
	}

	/**
	 * Get file's size by giving path <br>
	 */
	public static long getFileSize(String path) {
		long size = 0;
		File file = new File(path);
		if (file.exists()) {
			size = file.length();
		}

		return size;
	}

	/**
	 * Judge if the app is installed already <br>
	 * 
	 * @param packageName
	 * @return Running the intent of the app, null if not exist
	 */
	public static boolean isAppInstallAlready(Context context, String packageName) {
		// get pm
		final PackageManager packageManager = context.getPackageManager();
		// get all install packages
		List<PackageInfo> mPackageInfo = packageManager.getInstalledPackages(0);

		boolean flag = false;
		if (mPackageInfo != null) {
			String tempName = null;
			for (int i = 0; i < mPackageInfo.size(); i++) {
				tempName = mPackageInfo.get(i).packageName;
				if (tempName != null && tempName.equals(packageName)) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * Install apk by system <br>
	 * 
	 * @param packagePath
	 */
	public static void installApkBySystem(Context context, File apkfile) {
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setDataAndType((Uri.fromFile(apkfile)), "application/vnd.android.package-archive");
		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(installIntent);
	}

	/**
	 * Check if giving package is Running Foreground
	 */
	public static boolean isRunningForeground(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
			return true;
		}

		return false;
	}

	/**
	 * get the app while Running Foreground
	 */
	public static String getRunningForegroundPackageName(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName)) {
			return currentPackageName;
		}

		return null;
	}

	public static String getProgramNameByPackageName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		String name = null;
		try {
			name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * isServiceRunning<br>
	 * <p>
	 * Check if giving service is running
	 */
	public static boolean isServiceRunning(Context context, String className) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the app is installed already,if install return it's
	 * launch-intent
	 * 
	 * @param packageName
	 * @return launch-intent of the app, null if not exist
	 */
	public static Intent getLaunchPackageIntent(Context context, String packageName) {
		Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		if (appIntent != null) {
			return appIntent;
		}
		return null;
	}

	/**
	 * 1. Judge sdcard's space first.<br>
	 * 2. Judge if the device is rooted.(use slience install or not)
	 * 
	 * @param item
	 * @param observer
	 */
	public static void installAPK(Context context, String apkPath, PackageInstallObserver observer) {
		// Check if the file is Exist
		File apkFile = new File(apkPath);
		if (apkFile == null || !apkFile.exists()) {
			if (observer != null)
				observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_FAILED);
			return;
		}

		// Check sdcard's space enough or not
		int freeSize = DeviceUtil.getPartitionFreeSizeM(context.getFilesDir().getAbsolutePath());
		int appSize = (int) (apkFile.length() >>> 20) + 10;
		if (freeSize > appSize) {
			// install by su first, if failed, install by system
			installAPKBySUCommand(context, apkPath, observer);
		} else {
			installAPKBySystem(context, apkPath, observer);
		}
	}

	/**
	 * Slience Install
	 * 
	 * @param item
	 *            {@link InstallTask}
	 */
	public static void installAPKBySUCommand(Context context, String apkPath, PackageInstallObserver observer) {
		boolean installSuccess = false;

		// check the install package
		if (!isAPKFileValid(apkPath, context)) {
			if (observer != null)
				observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_FAILED);
			return;
		}

		java.lang.Process process = null;
		boolean needDestroyProcess = true;
		try {
			process = Runtime.getRuntime().exec(context.getString(R.string.su));
			OutputStream os = process.getOutputStream();
			if (os != null) {
				DataOutputStream dos = new DataOutputStream(os);
				// run command
				byte[] command = ("pm install -r \'" + apkPath + "\'\n").getBytes("utf-8");
				// dos.writeUTF("pm install -r " + path + "\n");
				dos.write(command);
				dos.flush();

				dos.writeBytes("exit\n");
				dos.flush();
				int exitValue = process.waitFor();

				// int waitFor() the Process is terminate
				needDestroyProcess = false;
				Logger.i(TAG, "silent install finished, exit value is:" + exitValue);

				if (exitValue == 0) {
					// success
					DataInputStream dis = new DataInputStream(process.getInputStream());
					String line;
					while ((line = dis.readLine()) != null) {
						Logger.i(TAG, "result:" + line);
						if (line.toLowerCase().contains("success")) {
							installSuccess = true;
							break;
						}
					}
					dis.close();

					if (Logger.LOGABLE) {
						DataInputStream error = new DataInputStream(process.getErrorStream());
						while ((line = error.readLine()) != null) {
							Logger.i(TAG, "error:" + line);
						}
						error.close();
					}
				}
				dos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			// su is not exit!
			installSuccess = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			installSuccess = false;
		} catch (Exception e) {
			e.printStackTrace();
			installSuccess = false;
		} finally {
			if (process != null && needDestroyProcess) {
				process.destroy();
			}
		}
		if (!installSuccess) {
			// install failed try system's install
			installAPKBySystem(context, apkPath, observer);
		} else {
			/** install success **/
			if (observer != null)
				observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_SUCCESS);
		}

	}

	/**
	 * Install apk by broadcast
	 */
	public static void installAPKBySystem(Context context, String apkPath, PackageInstallObserver observer) {

		File apkfile = new File(apkPath);

		if (apkfile == null || !apkfile.exists()) {
			if (observer != null)
				observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_FAILED);
			return;
		}

		try {
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setDataAndType((Uri.fromFile(apkfile)), "application/vnd.android.package-archive");
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(installIntent);
		} catch (Exception e) {
			if (observer != null)
				observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_FAILED);
			return;
		}

		if (observer != null)
			observer.onInstallApkCallBack(PackageInstallObserver.INSTALL_BY_SYSTEM);
	}

	/**
	 * Check if the apk giving by path is valid
	 * 
	 * @param path
	 * @param context
	 * @return true if valid
	 */
	public static boolean isAPKFileValid(String path, Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(path, 0);
		return pi != null;
	}

	public static void startPackage(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(packageName);
		if (intent != null)
			context.startActivity(intent);
	}

	public static void runMarketWithSearching(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (intent != null) {
			try {
				context.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(context, "open failed", Toast.LENGTH_LONG).show();
			}
		}
	}

	private static final String SCHEME = "package";
	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
	private static final String APP_PKG_NAME_22 = "pkg";
	// InstalledAppDetails package name
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	// InstalledAppDetails class
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	public static void showInstalledAppDetails(Context context, String packageName) {

		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 19) {
			intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
			intent.putExtra("extra_pkgname", packageName);
		} else if (apiLevel >= 9) { // 2.3(ApiLevel 9)
			// intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			Uri uri = Uri.fromParts(SCHEME, packageName, null);
			intent.setData(uri);
		} else { // 2.3)
			// 2.2 2.1
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}

	/******************************
	 * private Methods <br>
	 ******************************/

	public static void browsePicture(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "image/*");
		/** if caller is service, add new task flag */
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void browsePicture(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
		/** if caller is service, add new task flag */
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static String[] getTopAppInfo(Context context) {
		String[] appInfo = new String[2];
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		RunningTaskInfo rti = am.getRunningTasks(1).get(0);
		String packageName = rti.topActivity.getPackageName();
		String label = getAppLabel(context, packageName);

		appInfo[0] = packageName;
		appInfo[1] = label;

		return appInfo;
	}

	public static String getAppLabel(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		String label = null;
		try {
			label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return label;
	}

}
