package com.luo.wifidemo.p2pdemo.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.luo.wifidemo.R;
import com.luo.wifidemo.p2pdemo.module.PeerInfo;
import com.luo.wifidemo.p2pdemo.module.WifiP2pConfigInfo;
import com.luo.wifidemo.p2pdemo.module.WrapRunable;
import com.luo.wifidemo.util.Logger;

/**********************
 *   Main interface
 *********************/


/**
 * 
 * @author luochenxun
 *
 */
public class WifiP2pService extends Service implements ChannelListener, WifiP2pServiceListener {
	private static final String TAG = "AppNetService";
	private static final int RETRY_CHANNEL_TIMES = 3;
	
	/** For connect failed retryOnce */
	private int mRetryChannelTime = RETRY_CHANNEL_TIMES;
	
	/** Binder class , use to bind the service */
	private WifiP2pServiceBinder mBinder = new WifiP2pServiceBinder();
	public class WifiP2pServiceBinder extends Binder {
		public WifiP2pService getService() {
			return WifiP2pService.this;
		}
	}
	
	/** A ThreadPool bind with this service, main handle the socket of the device, work as a serverSocket */
	private ThreadPoolManager mThreadPoolManager = null;
	
	/** The wifi p2p manager  */
	private WifiP2pManager mWifiP2pManager = null;
	
	/** @see android.net.wifi.p2p.WifiP2pManager.Channel */
	private Channel mChannel = null;
	private WifiP2pDevice mLocalDevice = null;
	
	/**  WifiP2p BroadcastReceiver*/
	private BroadcastReceiver mWifiP2pReceiver = null;
	private final IntentFilter intentFilter = new IntentFilter();
	
	/** The p2p device's list */
	private List<WifiP2pDevice> mP2pDeviceList = new ArrayList<WifiP2pDevice>();
	public List<WifiP2pDevice> getP2pDeviceList() {
		return mP2pDeviceList;
	}
	
	/** Peerinfo's list */
	private ArrayList<PeerInfo> mPeerInfoList = new ArrayList<PeerInfo>();
	final public ArrayList<PeerInfo> getPeerInfoList() {
		return mPeerInfoList;
	}
	
	/** 
	 *  Manager wifiP2p-Group's info
	 */
	private WifiP2pInfo mWifiP2pInfo;
	public final String getHostAddress() {
		return mWifiP2pInfo.groupOwnerAddress.getHostAddress();
	}
	public final boolean isPeer() {
		return !mWifiP2pInfo.isGroupOwner;
	}
	public final boolean isGroupOwner() {
		return mWifiP2pInfo.isGroupOwner;
	}

	/** Getter & Setter & stateChecker  */
	public boolean isWifiP2pAviliable() {
		return mWifiP2pManager != null;
	}
	public boolean isWifiP2pManager() {
		return mWifiP2pManager != null;
	}
	public boolean isWifiP2pChannel() {
		return mChannel != null;
	}
	
	/** Peers's operation Class  */
	/** SendImageController */
	private static SendImageController mSendImageCtrl;
	public SendImageController getSendImageController(){
		return mSendImageCtrl;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "P2p Service IBinder~~~");
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "P2p Service onUnbind~~~");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "P2p Service onCreate~~~");
		initEnvironment();
	}
	
	@Override
	public void onDestroy() {
		Logger.d(TAG, "P2p Service onDestroy~~~");
		unregisterReceiver(mWifiP2pReceiver);
		mThreadPoolManager.destory();
		super.onDestroy();
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (isWifiP2pAviliable() && mRetryChannelTime-- != 0) {
			Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
			resetPeers();
			mChannel = initialize(this, getMainLooper(), this);
		} else {
			Toast.makeText(this, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
		}
	}
	
	/***********************
	 * Public Methods
	 *********************/
	
	public boolean discoverPeers() {
		if (mActivity != null) {
			if (!isWifiP2pEnabled) {
				Toast.makeText(this, R.string.wifip2p_p2p_not_open, Toast.LENGTH_SHORT).show();
			} else {
				// callback activity
				mActivity.showDiscoverPeers();
				// do discoverPeers
				mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Toast.makeText(WifiP2pService.this, R.string.wifip2p_discovery_sucess, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(WifiP2pService.this, String.format(getResources().getString(R.string.wifip2p_discovery_failed), reasonCode), 
								Toast.LENGTH_SHORT).show();
					}
				});
				return true;
			}
		}
		return false;

	}
	
	
	/***********************
	 * Private Methods
	 *********************/
	
	/**
	 * Init the environment of this service
	 */
	private void initEnvironment() {
		// Get wifiP2p manager
		mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		
		// Init channel
		mChannel = initialize(this, getMainLooper(), this);
		
		// init thread Pool manager
		try {
			mThreadPoolManager = new ThreadPoolManager(this, WifiP2pConfigInfo.LISTEN_PORT, 5);
		} catch (IOException ex) {
			Logger.e("NetworkService", "onActivityCreated() IOException ex", ex);
		}
		
		// Add wifi p2p state broadcastReceiver
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mWifiP2pReceiver = new WifiP2pBroadcastReceiver(this, this);
		registerReceiver(mWifiP2pReceiver, intentFilter);
	}

	private void initServiceThread() {
		Logger.d(TAG, "initServiceThread.");
		mThreadPoolManager.init();
	}

	private void uninitServiceThread() {
		Logger.d(TAG, "uninitServiceThread.");
		mThreadPoolManager.uninit();
	}

	/**
	 * This object is use to pass the callback of peer-event back to activity
	 */
	private WifiP2pActivityListener mActivity = null;
	WifiP2pActivityListener getActivity() {
		return mActivity;
	}
	public void registerAcitivity(WifiP2pActivityListener activity) {
		this.mActivity = activity;
		// Bind the operation's controller
		mSendImageCtrl = new SendImageController(mActivity);
		// Update UI
		if (mLocalDevice != null){
			updateLocalDevice(mLocalDevice);
		}
		// discoverPeers
		discoverPeers();
	}

	/**  Mark that is wifiP2p enable  */
	private boolean isWifiP2pEnabled = false;
	final public void setIsWifiP2pEnabled(boolean isEnabled) {
		this.isWifiP2pEnabled = isEnabled;
		if (isWifiP2pEnabled) {
			initServiceThread();
		} else {
			uninitServiceThread();
		}
	}

	/**  Connect a device by the given config */
	public void connect(WifiP2pConfig config) {
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiP2pBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(WifiP2pService.this, R.string.wifip2p_connecting_failed, Toast.LENGTH_SHORT).show();
			}
		});
	}

	
	/**  */
	public void cancelDisconnect() {
		mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(WifiP2pService.this, R.string.wifip2p_connecting_canceled, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reasonCode) {
				Toast.makeText(WifiP2pService.this, String.format(getResources().getString(R.string.wifip2p_connecting_cancel_failed), reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private WifiP2pManager.Channel initialize(Context srcContext, Looper srcLooper, WifiP2pManager.ChannelListener listener) {
		return mWifiP2pManager.initialize(srcContext, srcLooper, listener);
	}

	/**
	 * Request the current list of peers
	 * @param listener
	 */
	public void requestPeers(WifiP2pManager.PeerListListener listener) {
		mWifiP2pManager.requestPeers(mChannel, listener);
	}
	
	public void removeGroup() {
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onFailure(int reasonCode) {
				Logger.e(TAG, "Disconnect failed. Reason :" + reasonCode);
				// reason The reason for failure could be one of P2P_UNSUPPORTED
				// 1, ERROR 0 or BUSY 2.
			}

			@Override
			public void onSuccess() {
				mActivity.onDisconnect();
			}
		});
	}

	public void resetPeers() {
		mP2pDeviceList.clear();
		if (mActivity != null){
			mActivity.resetPeers();
		}
	}
	

	public void requestConnectionInfo(WifiP2pManager.ConnectionInfoListener listener) {
		mWifiP2pManager.requestConnectionInfo(mChannel, listener);
	}



//	private boolean bVerifyRecvFile = false;
//
//	public boolean isbVerifyRecvFile() {
//		return bVerifyRecvFile;
//	}
//
//	public void setbVerifyRecvFile(boolean bVerifyRecvFile) {
//		this.bVerifyRecvFile = bVerifyRecvFile;
//	}
//
	public void postRecvPeerList(int count) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_PEER_LIST;
		msg.arg1 = count;
		mActivity.sendMessage(msg);
	}

	public void postSendStringResult(int sendBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_STRING;
		msg.arg1 = sendBytes;// send;
		mActivity.sendMessage(msg);
	}

	/** Send send-peer-infor-result to ui */
	public void postSendPeerInfoResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT;
		msg.arg1 = result;
		mActivity.sendMessage(msg);
	}
	
	/** Send recv-peer-infor-result to ui */
	public void postRecvPeerInfo(PeerInfo info) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_RECV_PEER_INFO;
		mActivity.sendMessage(msg);
	}

	public void postRecvFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT;
		msg.arg1 = result;
		mActivity.sendMessage(msg);
	}

	public void postSendStreamResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT;
		msg.arg1 = result;
		mActivity.sendMessage(msg);
	}

	private SocketAddress remoteSockAddr;
	public void setRemoteSockAddress(SocketAddress sockAddr) {
		remoteSockAddr = sockAddr;
	}
	public SocketAddress getRemoteSockAddress() {
		return remoteSockAddr;
	}

//	public boolean handleRecvPeerList(InputStream ins) {
//		try {
//			mPeerInfoList.clear();
//			int peerListSize = ins.read();
//			for (int i = 0; i < peerListSize; ++i) {
//				int bufferLen = ins.read();
//				byte[] buffer = new byte[256];
//				ins.read(buffer, 0, bufferLen);
//				String strBuffer = new String(buffer, 0, bufferLen);
//				int offset1 = strBuffer.indexOf("peer:");
//				int offset2 = strBuffer.indexOf("port:");
//				Logger.d(WiFiDirectActivity.TAG, "recvPeerSockAddr strBuffer:" + strBuffer);
//				if (offset1 != -1 && offset2 != -1) {
//					assert (offset1 < offset2);
//					String host = strBuffer.substring(offset1 + 5, offset2);
//					int port = Integer.parseInt(strBuffer.substring(offset2 + 5, strBuffer.length()));
//					mPeerInfoList.add(new PeerInfo(host, port));
//					Logger.d(WiFiDirectActivity.TAG, "peerInfoList.add(...). size:" + mPeerInfoList.size());
//				}
//			}
//			postRecvPeerList(mPeerInfoList.size());
//			return true;
//		} catch (IOException e) {
//			Logger.e(WiFiDirectActivity.TAG, e.getMessage());
//			return false;
//		}
//	}

	/**
	 * Handle when receive a peerInfo 
	 */
	public boolean handleRecvPeerInfo(InputStream ins) {
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("peer:");
			int offset2 = strBuffer.indexOf("port:");
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);
			
			if (offset1 != -1 && offset2 != -1) {
				String host = strBuffer.substring(offset1 + 5, offset2);
				int port = Integer.parseInt(strBuffer.substring(offset2 + 5, strBuffer.length()));
				Logger.d(TAG, "new host:" + host);

				PeerInfo info = new PeerInfo(host, port);
				//send the event to ui
				postRecvPeerInfo(info); 
				
				for (Iterator<PeerInfo> iter = mPeerInfoList.iterator(); iter.hasNext();) {
					PeerInfo peer = iter.next();
					if (peer.host.equals(host)){
						Logger.d(TAG, "Peer is exist already");
						return true;
					}
				}
				// add peer if not exist
				mPeerInfoList.add(info);
				Logger.d(TAG, "peerInfoList.add(...). size:" + mPeerInfoList.size());
			}
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

//	public boolean recvFileAndSave(InputStream ins, String extName) {
//		try {
//			final File recvFile = new File(Environment.getExternalStorageDirectory() + "/wifi-direct/wifip2pshared-" + System.currentTimeMillis() + extName);
//
//			File dirs = new File(recvFile.getParent());
//			if (!dirs.exists())
//				dirs.mkdirs();
//			recvFile.createNewFile();
//
//			Logger.d(WiFiDirectActivity.TAG, "server: copying files " + recvFile.toString());
//			FileOutputStream fileOutS = new FileOutputStream(recvFile);
//
//			byte buf[] = new byte[1024];
//			int len;
//			while ((len = ins.read(buf)) != -1) {
//				fileOutS.write(buf, 0, len);
//				// ֪ͨ���淢��/�����ļ���ȡ�
//				postSendRecvBytes(0, len);
//
//			}
//			fileOutS.close();
//			String strFile = recvFile.getAbsolutePath();
//			if (strFile != null) {
//				// Go, let's go and test a new cool & powerful method.
//				Utility.openFile(mActivity, recvFile);
//			}
//			return true;
//		} catch (IOException e) {
//			Logger.e(WiFiDirectActivity.TAG, "IOException", e);
//			e.printStackTrace();
//			return false;
//		}
//	}

	public void handleSendPeerInfo() {
		mThreadPoolManager.execute(WrapRunable.getSendPeerInfoRunable(
				new PeerInfo(getHostAddress(), WifiP2pConfigInfo.LISTEN_PORT),  this));
	}

	/**  Send file to the given device(host,port)  */
	public void handleSendFile(String host, int port, Uri uri) {
		Logger.d(this.getClass().getName(), "handleSendFile");
		mThreadPoolManager.execute(WrapRunable.getSendFileRunable(host, port, uri, this));
	}
	
	/**  handleRecvFile from other peers  */
	public void handleRecvFile(InputStream ins) {
		mSendImageCtrl.handleRecvFile(ins);
	}
	
	/**  handleRecvFileInfo from other peers  */
	public void handleRecvFileInfo(InputStream ins) {
		mSendImageCtrl.handleRecvFileInfo(ins);
	}
	
	/**  handleRecvPeerList from other peers  */
	public boolean handleRecvPeerList(InputStream ins) {
		try {
			mPeerInfoList.clear();
			int peerListSize = ins.read();
			for (int i = 0; i < peerListSize; ++i) {
				int bufferLen = ins.read();
				byte[] buffer = new byte[256];
				ins.read(buffer, 0, bufferLen);
				String strBuffer = new String(buffer, 0, bufferLen);
				int offset1 = strBuffer.indexOf("peer:");
				int offset2 = strBuffer.indexOf("port:");
				Log.d(TAG, "recvPeerSockAddr strBuffer:"
						+ strBuffer);
				if (offset1 != -1 && offset2 != -1) {
					String host = strBuffer.substring(offset1 + 5, offset2);
					int port = Integer.parseInt(strBuffer.substring(offset2 + 5,
							strBuffer.length()));
					mPeerInfoList.add(new PeerInfo(host, port));
				}
			}
			postRecvPeerList(mPeerInfoList.size());
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	/**
	 * After the network reConnect, a group owner send broadcast <br>
	 *  to all the peer in the its mPeerInfoList
	 */
	public void handleBroadcastPeerList() {
		if (isGroupOwner()) {
			ByteArrayOutputStream outs = new ByteArrayOutputStream();
			// ArrayList<PeerInfo> peerInfoLists = getPeerInfoList();
			outs.write(mPeerInfoList.size());
			for (PeerInfo peerInfo : mPeerInfoList) {
				String tmp = peerInfo.toString();
				// String tmp = "peer:" + peerInfo.host + "port:" + peerInfo.port;
				outs.write(tmp.length());
				try {
					outs.write(tmp.getBytes());
				} catch (IOException e) {
					Logger.e(TAG, " e:" + e);
					e.printStackTrace();
				}
			}

			ByteArrayInputStream ins = new ByteArrayInputStream(outs.toByteArray());
			Logger.d(TAG, " ins's length:" + ins.available());
			
			for (PeerInfo peerInfo : mPeerInfoList) {
				handleSendStream(peerInfo.host, peerInfo.port, ins);
			}
		}
	}

	/**
	 * Send the stream
	 */
	public void handleSendStream(String host, int port, InputStream ins) {
		// let's go and test ...
		mThreadPoolManager.execute(WrapRunable.getSendStreamRunnable(host, port, ins, this));
	}

	public void handleSendString(String host, int port, String data) {
		mThreadPoolManager.execute(WrapRunable.getSendStringRunable(host, port, data, this));
	}

	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		mLocalDevice = device;
		if (mActivity != null){
			mActivity.updateLocalDevice(device);
		}
	}

	/**
	 * Callback by discoveryPeers, return the peers that discovery
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		mP2pDeviceList.clear();
		mP2pDeviceList.addAll(peers.getDeviceList());

		if (mP2pDeviceList.size() == 0) {
			Logger.e(TAG, "No devices found");
		}

		if (mActivity != null){
			mActivity.onPeersAvailable(peers);
		}
	}
	
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Logger.d(TAG," onConnectionInfoAvailable ");
		mWifiP2pInfo = info;
		if (info.groupFormed && info.isGroupOwner) {
			Logger.d(TAG, "I'm the group's owner");
			handleBroadcastPeerList();
		} else if (info.groupFormed) {
			handleSendPeerInfo();
			Logger.d(TAG, "peer - info.groupFormed.");
		}

		if (mActivity != null){
			mActivity.onConnectionInfoAvailable(info);
		}
	}
}
