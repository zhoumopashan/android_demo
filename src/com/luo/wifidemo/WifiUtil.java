package com.luo.wifidemo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiUtil {

	private final static String TAG = "WifiUtil";
	private final static String DEFULT_WIFI_LOCK_NAME = "default_wifi_lock";
	
	// Managers
	/** WifiManager */
	private WifiManager mWifiManager;
	/** WifiInfo */
	private WifiInfo mWifiInfo;
	/**  wifi lock  */
	private WifiLock mWifiLock;
	
	// wifi states
	/**  ScanResult list */
	private List<ScanResult> mScanResultList;
	/**   */
	private ScanResult mScanResult;
	//
	/** Get wifi state list  */
	private List<WifiConfiguration> mWifiConfigList;
	/** wifi names */
	private ArrayList<String> mWifiNameList = new ArrayList<String>();
	

	/**
	 * Constructor
	 */
	public WifiUtil(Context context) {
		// Get WifiManager
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		// Get WifiInfo
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	public WifiInfo returnWifiInfo() {
		return mWifiInfo;
	}
	
	/**
	 * Open wifi module if not enable<br>
	 */
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	/** Close wifi */
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
	
	/**
	 * Start a wifi-network-scan <br>
	 * 1. startScan <br>
	 * 2. get ScanResult list <br>
	 * 3. get wifiConfiguration list <br>
	 */
	public void startScan() {
		mWifiManager.startScan();
		mScanResultList = mWifiManager.getScanResults();
		mWifiConfigList = mWifiManager.getConfiguredNetworks();
		if(mScanResultList == null){
			Log.e(TAG,"Scan Failed , mScanResultList is null");
		}
	}

	/**
	 * Get the wifi-scan-reslut
	 */
	public ArrayList<String> getScanResult() {
		// start scan
		startScan();
		
		if (mScanResultList != null) {
			for (ScanResult resultItem : mScanResultList) {
				// Add all wifi-point's ssid into wifiNames
				mWifiNameList.add(resultItem.SSID); 
			}
		}
		
		// Print result in log
		printScanResult(mScanResultList);
		
		return mWifiNameList;
	}
	
	/** Get scanresult member */
	public List<ScanResult> getScanResultList() {
		return mScanResultList;
	}
	
	/**
	 * Get ScanRelust's info
	 * @return
	 */
	public void printScanResult(List<ScanResult> resultList) {
		// null check
		if(resultList == null) return;
		
		StringBuilder stringBuilder = new StringBuilder();
		
		for (int i = 0; i < resultList.size(); i++) {
			// get item
			ScanResult result = resultList.get(i);
			// construct msg
			stringBuilder = stringBuilder.append("No.").append(i + 1)
					.append(" BSSID : ").append(result.BSSID)
					.append(" SSID : ").append(result.SSID)
					.append(" capabilities : ").append(result.capabilities)
					.append(" frequency : ").append(result.frequency)
					.append(" level : ").append(result.level)
					.append(" describeContents : ").append(result.describeContents());
		}
		
		Log.d(TAG, "getScanResult :" + stringBuilder.toString());
	}

	/** Create a wifi Lock */
	public void creatWifiLock(String lockName) {
		mWifiLock = mWifiManager.createWifiLock(lockName);
	}
	/** Create a wifi Lock */
	public void creatWifiLock(){
		creatWifiLock(DEFULT_WIFI_LOCK_NAME);
	}

	/**  Acquire a wifiLock*/
	public void acquireWifiLock(String lockName) {
		if(mWifiLock == null){
			creatWifiLock(lockName);
		}
		if( mWifiLock != null){
			mWifiLock.acquire();
		}
	}
	
	public void acquireWifiLock() {
		acquireWifiLock(DEFULT_WIFI_LOCK_NAME);
	}

	/**  Release a wifilock after acqiure */
	public void releaseWifiLock() {
		// only release it when held it
		if (mWifiLock != null && mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	/** Get wifiConfiuration */
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfigList;
	}
	
	/**
	 * Auto Connect the given wifi-ssid
	 */
	public void autoConnect(String ssid) {
		// start scan
		startScan();
		
		if (mScanResultList != null) {
			for (int i = 0; i < mScanResultList.size(); i++) {
				mScanResult = mScanResultList.get(i);
				if (mScanResult.SSID.toString().contains(ssid)) {
					addNetwork(CreateWifiInfo("sh_001", "shenghong", 3));
				}
			}
		}
	}

	/**
	 * Allow a previously configured network to be associated with.
	 * @param index from wifiConfiuration list by mWifiManager.getConfiguredNetworks()
	 */
	public void connectConfiguration(int index) {
		// index check
		if (index > mWifiConfigList.size()) {
			return;
		}
		// Allow a previously configured network to be associated with.
		mWifiManager.enableNetwork(mWifiConfigList.get(index).networkId,true);
	}
	
	/**
	 * Add a network and connect it
	 */
	public void addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
		
		Log.d(TAG,"network id : " + wcgID );
		Log.d(TAG,"Is the network enable : " + b );
	}

	/** disconnectWifi */
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	/**
	 * CreateWifiInfo
	 */
	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}
		if (Type == 1) // WIFICIPHER_NOPASS
		{
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2) // WIFICIPHER_WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3) // WIFICIPHER_WPA
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	/**
	 * Check if the given ssid is exist
	 * @param SSID
	 * @return
	 */
	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}
	
	/**
	 * Get if wifi is Available
	 */
	public static boolean isWifiEnable(Context context){
		ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo  = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		NetworkInfo.State state = networkInfo.getState();
		return networkInfo.isAvailable();
	}
	
	/**
	 * Get wifi state 
	 * 
	 * @return One of WIFI_STATE_DISABLED, WIFI_STATE_DISABLING, WIFI_STATE_ENABLED, WIFI_STATE_ENABLING, WIFI_STATE_UNKNOWN
	 */
	public int getWifiState() {
		return mWifiManager.getWifiState();
	}
	
	/**
	 * GetMacAddress
	 */
	public String getMacAddress() {
		return (mWifiInfo == null) ? null : mWifiInfo.getMacAddress();
	}

	/**
	 * GetBSSID
	 * @return
	 */
	public String getBSSID() {
		return (mWifiInfo == null) ? null : mWifiInfo.getBSSID();
	}

	/**
	 * Get IP
	 * @return
	 */
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	/**
	 * Get NetworkId
	 * @return
	 */
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	/**
	 *  Get WifiInfo's all infomation
	 * @return
	 */
	public String getWifiInfo() {
		return (mWifiInfo == null) ? null : mWifiInfo.toString();
	}

}
