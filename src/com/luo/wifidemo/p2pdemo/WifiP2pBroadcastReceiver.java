package com.luo.wifidemo.p2pdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;

import com.luo.wifidemo.util.Logger;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WifiP2pBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WiFiDirectBroadcastReceiver";

    private WifiP2pService mP2pService;
    private WifiP2pServiceListener mP2pServiceListener;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WifiP2pBroadcastReceiver(WifiP2pService service, WifiP2pServiceListener listener) {
        super();
        this.mP2pService = service;
        this.mP2pServiceListener = listener;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
	public void onReceive(Context context, Intent intent) {

		/* Param check */
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (action == null || TextUtils.isEmpty(action)) {
			return;
		}

		/* Intent action filter */
		// Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// UI update to indicate wifi p2p status.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct mode is enabled
				mP2pService.setIsWifiP2pEnabled(true);
				mP2pService.discoverPeers();
			} else {
				mP2pService.setIsWifiP2pEnabled(false);
				mP2pService.resetPeers();

			}
			Logger.d(TAG, "P2P state changed - is wifip2p enable:" + (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) );
		} 
		// Broadcast intent action indicating that the available peer list has changed.
		else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// Request available peers from the wifi p2p manager. <br>
			// This is an asynchronous call and the calling activity <br>
			// is notified with a callback on PeerListListener.onPeersAvailable() <br>
			if (mP2pService.isWifiP2pAviliable()) {
				mP2pService.requestPeers(mP2pServiceListener);
			}
			Logger.d(TAG, "P2P peers changed");
		} 
		// indicating that the state of Wi-Fi p2p connectivity has changed.
		else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

			if (!mP2pService.isWifiP2pAviliable()) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (networkInfo.isConnected()) {
				// we are connected with the other device, request connection
				// info to find group owner IP
				mP2pService.requestConnectionInfo(mP2pServiceListener);
			} else {
				// It's a disconnect
				mP2pService.resetPeers();
				// TODO let's go and test ...
				mP2pService.discoverPeers();
			}
			Logger.d(TAG, "P2P connection changed - networkInfo:" + networkInfo.toString());
		} 
		// Broadcast intent action indicating that this device details have changed.
		else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			mP2pService.updateLocalDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
		} else {
			Logger.d(TAG, "Other P2P change action - " + action);
		}
	}
}