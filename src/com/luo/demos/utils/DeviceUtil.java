package com.luo.demos.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

/**
 * DeviceManager is a class that has a lot of static-method help you to <br>
 * get the information of the device.
 * 
 * @author luochenxun
 */
public class DeviceUtil {

	private static final String TAG = "DeviceManager";

	/**
	 * getDeviceMacAddress
	 * <p>
	 * get the Mac-Address of Devices
	 * 
	 * @param context
	 * @return the Mac-Address of Devices(String)
	 */
	public static String getDeviceMacAddress(Context context) {
		String address = null;
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null) {
				address = info.getMacAddress();
			}
		}

		return address;
	}

	/**
	 * Get the current ipv4-address of the device
	 * 
	 * @return return null if process-error
	 */
	public static String getDeviceIpAddress() {
		String ipAddress = null;
		try {
			String ipv4;
			ArrayList<NetworkInterface> mylist = Collections.list(NetworkInterface.getNetworkInterfaces());

			for (NetworkInterface ni : mylist) {

				ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
						return ipv4;
					}
				}
			}
		} catch (SocketException ex) {

		}

		return ipAddress;
	}

	/**
	 * getAppVersionCode
	 * <p>
	 * get the AppVersionCode by given packageName
	 * </p>
	 * 
	 * @param context
	 * @param packageName
	 * @return the AppVersionCode by given packageName
	 */
	public static int getAppVersionCode(Context context, String packageName) {
		PackageManager nPackageManager = context.getPackageManager();
		int currentVersionCode = -1;
		try {
			PackageInfo nPackageInfo = nPackageManager.getPackageInfo(packageName, 0);
			currentVersionCode = nPackageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return currentVersionCode;
	}

	/**
	 * Get the appVersionCode of the current app
	 * 
	 * @param context
	 * @param packageName
	 * @return the appVersionCode of the current app
	 */
	public static int getAppVersionCode(Context context) {

		return getAppVersionCode(context, context.getPackageName());
	}

	/**
	 * Get the appVersionName of the current app
	 * 
	 * @param context
	 * @return the appVersionName of the current app
	 */
	public static String getAppVersionName(Context context) {
		return getAppVersionName(context, context.getPackageName());
	}

	/**
	 * getAppVersionName
	 * <p>
	 * get the AppVersionName by given packageName
	 * </p>
	 * 
	 * @param context
	 * @param packageName
	 * @return the AppVersionName by given packageName
	 */
	public static String getAppVersionName(Context context, String packageName) {
		String versionName = null;
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packInfo = pm.getPackageInfo(packageName, 0);
			versionName = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return versionName;
	}


	/**
	 * Get the deviceId
	 * 
	 * @param context
	 * @return
	 */
	public static String getDeviceID(Context context) {
		String imei = null;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			imei = tm.getDeviceId();
		}

		return imei;
	}

	/**
	 * Check isNetworkAvailable
	 * 
	 * @param context
	 * @return true if is available; false if not
	 */
	public static boolean isNetworkAvailable(Context context) {
		boolean ret = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null && info.isAvailable())
				return true;
		}

		return ret;
	}

	/**
	 * Check if wifi is available
	 * 
	 * @param context
	 * @return true if is available; false if not
	 */
	public static boolean isWifiAvailable(Context context) {
		WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
		if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Use {@link #getPartitionFreeSize(String)} , Get the root-path of the
	 * system
	 * 
	 * @return
	 */
	public static long getSystemPartitionFreeSize() {
		return getPartitionFreeSize(Environment.getRootDirectory().getPath());
	}

	/**
	 * getPartitionFreeSize. Get the FreeSize of the given path <br>
	 * <p>
	 * FreeSize = (BlockSize * BlockCount)/1024 (div 1024 = right-roll 10 bit)<br>
	 * div 1024 because the return unity is KB
	 * </P>
	 * 
	 * @param path
	 * @return the freeSize of the given-path , unity is KB
	 */
	public static long getPartitionFreeSize(String path) {
		long kSize = 0;
		if (path != null) {
			StatFs sfs = new StatFs(path);
			if (sfs != null) {
				int blockSize = sfs.getBlockSize();
				int availCount = sfs.getAvailableBlocks();
				kSize = ((long) (blockSize * availCount) >>> 10);
			}
		}
		return kSize;
	}

	/**
	 * like {@link #getPartitionFreeSize(String)}, this method return unity is
	 * MB
	 * 
	 * @param path
	 * @return the freeSize of the given-path , unity is MB
	 */
	public static int getPartitionFreeSizeM(String path) {
		long kSize = 0;
		if (path != null) {
			StatFs sfs = new StatFs(path);
			if (sfs != null) {
				long blockSize = sfs.getBlockSize();
				long availCount = sfs.getAvailableBlocks();
				kSize = ((long) (blockSize * availCount) >>> 20);
			}
		}
		return (int) kSize;
	}
	
	public static int getSystemSDKLevel() {
	    return android.os.Build.VERSION.SDK_INT;
	}
	
	public static String getDeviceModel() {
	    return Build.MODEL;
	}

}
