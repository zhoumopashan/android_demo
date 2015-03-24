package com.luo.demos.wifidemo.p2pdemo.ui;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.luo.demos.utils.Logger;
import com.luo.demos.wifidemo.R;
import com.luo.demos.wifidemo.p2pdemo.controller.WifiP2pActivityListener;
import com.luo.demos.wifidemo.p2pdemo.controller.WifiP2pService;
import com.luo.demos.wifidemo.p2pdemo.controller.WifiP2pService.WifiP2pServiceBinder;
import com.luo.demos.wifidemo.p2pdemo.module.PeerInfo;
import com.luo.demos.wifidemo.p2pdemo.module.Utility;
import com.luo.demos.wifidemo.p2pdemo.module.WiFiPeerListAdapter;
import com.luo.demos.wifidemo.p2pdemo.module.WifiP2pConfigInfo;

/**
 * A simple wifi p2p demo
 * 
 * @author luochenxun
 *
 */
public class WifiP2pDemoActivity extends Activity implements WifiP2pActivityListener{
	
	private final static String TAG = "WifiP2pDemoActivity";

	/** WifiP2pService and the Binder */
	private WifiP2pService mP2pService;
	public WifiP2pService getP2pService() {
		return mP2pService;
	}
	/**
	 * ServiceConnection , Use bind interact with service
	 */
	private ServiceConnection mServiceConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			Logger.d(TAG, "bind service success");
			
			WifiP2pServiceBinder binder = (WifiP2pServiceBinder) service;
			mP2pService = binder.getService();
			
			// register the listener to service
			mP2pService.registerAcitivity(WifiP2pDemoActivity.this);
		}

		public void onServiceDisconnected(ComponentName name) {
			Logger.d("ServiceConnection", "unbind service success");
		}
	};
	
	/** the peers after discovery */
	private ListView mDiscoveryPeersListView;
	private WiFiPeerListAdapter mDiscoveryPeersAdapter;
	
	/** device's detail's info */
	private WifiP2pDevice mRemoteDevice;
	private WifiP2pDevice mLocalDevice;
	private WifiP2pInfo mWifiP2pInfo;
	private boolean bConnected = false;
	private View mDetailView;
	private TextView mAddressTv;
	private TextView mInfoTv;
	private TextView mOwnerTv;
	private TextView mIPTv;
	
	/**
	 * ui's object
	 */
	private ProgressDialog mProgressDialog = null;
	
	/**
	 * Inner Class
	 */
	static private class ActivityHandler extends Handler {
		private static final String TAG = "ActivityHandler";
		private WifiP2pDemoActivity mSelf;

		ActivityHandler(WifiP2pDemoActivity activity) {
			this.mSelf = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			Logger.d(TAG, "handleMessage()  msg.what:" + msg.what);
			switch (msg.what) {
			case WifiP2pConfigInfo.MSG_RECV_PEER_INFO:
				mSelf.showToastTips(R.string.wifip2p_rcv_peer_address);
				mSelf.showSendFileVeiw();
				break;
			case WifiP2pConfigInfo.MSG_SEND_RECV_FILE_BYTES:
				// inc send byte & rec byte
				mSelf.getP2pService().getSendImageController().incSendBytes(msg.arg1);
				mSelf.getP2pService().getSendImageController().incRecvBytes(msg.arg2);
				int progress1 = 0;
				int progress2 = 0;
				long sendSize = mSelf.getP2pService().getSendImageController().getSendBytes();
				long sendFileSize = mSelf.getP2pService().getSendImageController().getSendFileSize();
				long recvSize = mSelf.getP2pService().getSendImageController().getRecvBytes();
				long recvFileSize = mSelf.getP2pService().getSendImageController().getRecvFileSize();
				if (sendFileSize != 0){
					progress1 = (int) (sendSize * 100 / (sendFileSize));
				}
				
				if (recvFileSize != 0){
					progress2 = (int) (recvSize * 100 / (recvFileSize));
				}
				
				String tips = "\n send:" + progress1 + "(%) data(kb):" + sendSize / 1024 + "\n recv:" + progress2 + "(%) data(kb):" + recvSize / 1024;

				mSelf.showStatus(tips);
				break;

			case WifiP2pConfigInfo.MSG_VERIFY_RECV_FILE_DIALOG:
				mSelf.verifyRecvFile();
				break;

			case WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT:
				if (msg.arg1 == 0){
					mSelf.showToastTips(R.string.wifip2p_rcv_file_success);
				}else{
					mSelf.showToastTips(R.string.wifip2p_rcv_file_failed);
				}
				break;

			case WifiP2pConfigInfo.MSG_REPORT_SEND_FILE_RESULT:
				if (msg.arg1 == 0){
					mSelf.showToastTips(R.string.wifip2p_send_file_success);
				}else{
					mSelf.showToastTips(R.string.wifip2p_send_file_failed);
				}
				mSelf.getP2pService().getSendImageController().onSendFileEnd();
				break;
			case WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT:
				if (msg.arg1 == 0){
					mSelf.showToastTips(R.string.wifip2p_send_peerinfo_success);
				}else{
					mSelf.showToastTips(R.string.wifip2p_send_peerinfo_failed);
				}
				break;
			case WifiP2pConfigInfo.MSG_SEND_STRING:
				if (msg.arg1 == -1)
					mSelf.showToastTips("send string failed.");
				else
					mSelf.showToastTips("send string successed, length " + msg.arg1 + ".");
				break;
			case WifiP2pConfigInfo.MSG_REPORT_RECV_PEER_LIST:
				mSelf.showToastTips("receive peer list.");
			case WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT:
				if (msg.arg1 == 0){
					Logger.d(TAG,"send stream sucess");
//					mSelf.showToastTips();
				} else{
					Logger.d(TAG,"send stream failed");
//					mSelf.showToastTips("send stream failed.");
				}
				break;
			default:
				mSelf.showToastTips("error msg id.");
			}
			super.handleMessage(msg);
		}
	}

	private Handler mHandler = new ActivityHandler(this);
	@Override
	public void sendMessage(Message msg) {
		mHandler.sendMessage(msg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initEnvironment();
		initLayoutAndViews();
	}
	
	@Override
	protected void onDestroy() {
		Logger.d(TAG, "onDestroy   unbindService service.");
		releaseEnvironment();
		super.onDestroy();
	}
	
	/**
	 * You can do sth when the device is <br>
	 * scanning peers, like show a progressdialog
	 */
	@Override
	public void showDiscoverPeers() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(this, 
				getString(R.string.wifip2p_p2p_scanning_title), 
				getString(R.string.wifip2p_p2p_scanning), 
				true, true, 
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Logger.d(TAG, "onCancel discovery cancel.");
					}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User has picked an image. Transfer it to group owner i.e peer using
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE) {
			if (data == null) {
				Logger.e(this.getClass().getName(), "onActivityResult data == null, no choice.");
				return;
			}
			Uri uri = data.getData();
			mP2pService.getSendImageController().sendFile(uri , mP2pService);
		}
	}
	
	/**
	 * Remove all peers and clear all fields. This is called on
	 * BroadcastReceiver receiving a state change event.
	 */
	@Override
	public void resetPeers() {
		// clear the deviceListview
		mDiscoveryPeersListView.setEnabled(false);
		mDiscoveryPeersListView.setVisibility(View.GONE);
		
		// clear the detail's view
		if( mDetailView != null ){
			mDetailView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDisconnect() {
		mDetailView.setVisibility(View.GONE);
	}
	
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mWifiP2pInfo = info;
		bConnected = true;

		/* Update ui */
		mDetailView.setVisibility(View.VISIBLE);
		// Update GroupOwner
		TextView view = (TextView) findViewById(R.id.group_owner);
		view.setText( getString(R.string.wifip2p_group_owner_text) + info.isGroupOwner );
		// Update deviceInfo
		view = (TextView) findViewById(R.id.device_info);
		view.setText("Group Owner IP - "
				+ info.groupOwnerAddress.getHostAddress() + "\n local ip:"
				+ Utility.getLocalIpAddress());

		Logger.d(this.getClass().getName(), "new connect device's info:" + info);
		if (info.groupFormed && info.isGroupOwner) {
			// Group don't need to report ip
			findViewById(R.id.btn_send_ip).setVisibility( View.VISIBLE);
		} else if (info.groupFormed) {
			showSendFileVeiw();
			showReportIPVeiw();
		}

		// hide the connect button
		findViewById(R.id.btn_connect).setVisibility(View.GONE);		
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
        }
		mDiscoveryPeersListView.setEnabled(false);
		mDiscoveryPeersListView.setVisibility(View.GONE);
		mDiscoveryPeersAdapter.setDeviceList(peers.getDeviceList());
		mDiscoveryPeersListView.setEnabled(true);
		mDiscoveryPeersListView.setVisibility(View.VISIBLE);
		mDiscoveryPeersListView.setAdapter(mDiscoveryPeersAdapter);		
	}
	
	/**
	 * initEnvironment <br>
	 * init Service and managers
	 */
	private void initEnvironment() {
		// add necessary intent values to be matched.
		bindService(new Intent(this, WifiP2pService.class), mServiceConn, BIND_AUTO_CREATE);
	}
	
	/**
	 * Release the environment that init before
	 */
	private void releaseEnvironment(){
		if (mServiceConn != null) {
			unbindService(mServiceConn);
			mServiceConn = null;
		}
	}

	/**
	 * initLayoutAndViews
	 */
	private void initLayoutAndViews() {
		setContentView(R.layout.wifip2p_main);
		
		// Scan btn
		((Button)findViewById(R.id.wifip2p_main_discovery_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mP2pService.discoverPeers();				
			}
		});
		
		// Scan result listview
		mDiscoveryPeersAdapter = new WiFiPeerListAdapter(this, null);
		mDiscoveryPeersListView = (ListView)findViewById(R.id.wifip2p_main_scan_result_listview);
		mDiscoveryPeersListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 WifiP2pDevice device = (WifiP2pDevice) mDiscoveryPeersAdapter.getItem(position);
			     showDetailView(device);
			}
		});
		
		// Init detail view
		mDetailView = findViewById(R.id.wifip2p_main_detail_view);
		mAddressTv = (TextView)findViewById(R.id.device_address);
		mInfoTv = (TextView)findViewById(R.id.device_info);
		mIPTv = (TextView)findViewById(R.id.group_owner);
		mOwnerTv = (TextView)findViewById(R.id.group_ip);
	}
	
	/**
	 * Update local device's info
	 */
	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		mLocalDevice = device;
		TextView view = (TextView) findViewById(R.id.my_name);
		if (device.deviceName.contains("Android_3f82"))
			view.setText("SamSung");
		else if (device.deviceName.contains("Android_c023"))
			view.setText("htc");
		else if (device.deviceName.contains("Android_e8bf"))
			view.setText("HTC");
		else if (device.deviceName.contains("Android_1bf5"))
			view.setText("HUAWEI");
		else if (device.deviceName.contains("Android_a38e"))
			view.setText("HTC-W");
		else if (device.deviceName.contains("Android_bc2d"))
			view.setText("HTC-ONE");
		else
			view.setText(device.deviceName);

		view = (TextView)findViewById(R.id.my_status);
		view.setText(getDeviceStatus(device.status));
	}
	
	/**
	 * Convert device's status
	 */
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE://3
                return "Available";
            case WifiP2pDevice.INVITED://1
                return "Invited";
            case WifiP2pDevice.CONNECTED://0
                return "Connected";
            case WifiP2pDevice.FAILED://2
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
	
	/** Show Report view */
	public void showReportIPVeiw() {
		showToastTips(R.string.wifip2p_report_ip_tips);
		findViewById(R.id.btn_send_ip).setVisibility(View.VISIBLE);
	}
	
	/**
	 * Show the detail info of the given device
	 * @param device
	 */
	private void showDetailView(WifiP2pDevice device) {
		mRemoteDevice = device;
		mDetailView.setVisibility(View.VISIBLE);
		
		/* device's address */
		mAddressTv.setText("Device's address : " + device.deviceAddress);
		
		/* device's info */
		mInfoTv.setText("------  Device's info -----------\n" + device.toString() + "\n--------------------\n");

		/* connect's btn */
		((Button)findViewById(R.id.btn_connect)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = mRemoteDevice.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mProgressDialog = ProgressDialog.show(WifiP2pDemoActivity.this, 
						getString(R.string.wifip2p_connecting_cancel), getString(R.string.wifip2p_connecting) + mRemoteDevice.deviceAddress, true, true
						, new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Toast.makeText(WifiP2pDemoActivity.this, getString(R.string.wifip2p_connecting_canceled), Toast.LENGTH_SHORT).show();
								cancelConnect();
							}
						});
				connect(config);
			}
		});

		/* disconnect */
		((Button)findViewById(R.id.btn_disconnect)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnect();
			}
		});

		/* Send a image  */
		((Button)findViewById(R.id.btn_start_client)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Allow user to pick an image from Gallery or other registered apps
				if (mWifiP2pInfo.isGroupOwner)
					// if is groupOwner, must select which peer to send
					showSelectPeerDialog(); 
				else {
					// TODO showSelectMediaDialog ...
					// getWiFiDirectActivity().showSelectMediaDialog();
					startSelectImage();
				}
			}
		});

		/* report ip */
		((Button)findViewById(R.id.btn_send_ip)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (bConnected) {
//					getWiFiDirectActivity().reportPeerInfo();
//					Toast.makeText(WifiP2pDemoActivity.this, "Send peer's info to server ...", Toast.LENGTH_SHORT).show();
//				} else{
//					Toast.makeText(WifiP2pDemoActivity.this, "Sorry, this button just for the connected peer.", Toast.LENGTH_SHORT).show();
//				}
			}
		});
	}
	
	/** Show the status area */
	public void showStatus(String text) {
		TextView view = (TextView) findViewById(R.id.status_text);
		view.setText(text);
	}
	
	/** Show a image Select dialog, let user to select a image to send */
	public void startSelectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
	}
	
	/**
	 * Show a dialog to alow GroupOwner to select which peer to send file
	 */
	public void showSelectPeerDialog() {
		mP2pService.getSendImageController().setSelectHost(null);
		
		// Show select dialog
		AlertDialog.Builder selectDialog = new AlertDialog.Builder(this);
		selectDialog.setTitle(R.string.wifip2p_select_peer_dialog_title);
		selectDialog.setIcon(android.R.drawable.ic_dialog_info);

		// Mark all host of the peerList
		final ArrayList<String> items = new ArrayList<String>();
		Iterator<PeerInfo> it = mP2pService.getPeerInfoList().iterator();
		while (it.hasNext()){
			items.add(it.next().host);
		}
		String[] strHosts = new String[items.size()];// size > 0;
		items.toArray(strHosts);
		selectDialog.setSingleChoiceItems(strHosts, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which < items.size() - 1) {
					mP2pService.getSendImageController().setSelectHost(items.get(which));
					Logger.d(TAG, "selectHost:" + mP2pService.getSendImageController().getSelectHost());
					dialog.dismiss();
					
					// Show image select dialog
					startSelectImage();
				}
			}
		});
		selectDialog.setNegativeButton("CANCEL", null);
		selectDialog.show();
	}
	
	/** Show the sendFile's btn */
	private void showSendFileVeiw() {
		// The other device acts as the client. In this case, we enable the
		// get file button.
		findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
	}
	
	/** When Receive a file, Show a dialog to user to sure download or not  */
	private void verifyRecvFile() {
		
		String recvFileName = mP2pService.getSendImageController().getRecvFileName();
		long recvFileSize = mP2pService.getSendImageController().getRecvFileSize();
		
		AlertDialog.Builder normalDia = new AlertDialog.Builder(this);
		normalDia.setIcon(R.drawable.ic_launcher);
		normalDia.setTitle("Verify Receive File");
		normalDia.setMessage("Receive a file " + recvFileName + "\nSIZE:" + ( recvFileSize) / 1024 + "KB\nFROM:" + mP2pService.getRemoteSockAddress());

		normalDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mP2pService.getSendImageController().setbVerifyRecvFile(true);
				mP2pService.getSendImageController().verifyRecvFile(); 
			}
		});
		normalDia.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mP2pService.getSendImageController().setbVerifyRecvFile(false);
				mP2pService.getSendImageController().verifyRecvFile(); 
			}
		});
		normalDia.create().show();
	}
	
	/** Invoke the service to connect the device by the given config */
	private void connect(WifiP2pConfig config) {
		mP2pService.connect(config);
	}

	/** RemoveGroup */
	private void disconnect() {
		mDetailView.setVisibility(View.GONE);
		mP2pService.removeGroup();
		// reflash the peers' list
		mP2pService.discoverPeers();
	}
	
	/** Invoke the service to cancel the connect */
	public void cancelConnect() {
		/*
		 * A cancel abort request by user. <br>
		 * RemoveGroup if already connected.  <br>
		 * Else, request WifiP2pManager to abort the ongoing request
		 */
		Logger.e(TAG, "cancelDisconnect.");
		if (mP2pService.isWifiP2pAviliable()) {
			if (mRemoteDevice == null || mRemoteDevice.status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (mRemoteDevice.status == WifiP2pDevice.AVAILABLE || mRemoteDevice.status == WifiP2pDevice.INVITED) {
				mP2pService.cancelDisconnect();
			}
		}

	}
	
	
	/**
	 * Show a toast,call by selfHandler
	 */
	public void showToastTips(String tips) {
		Toast toast = Toast.makeText(this, tips, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	/** Show a toast,call by selfHandler by StringSourceId */
	public void showToastTips(int tipsID) {
		Toast toast = Toast.makeText(this, tipsID, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public Activity getActivity() {
		return this;
	}

}
