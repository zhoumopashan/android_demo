package com.luo.wifidemo.wifitools.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.luo.wifidemo.R;
import com.luo.wifidemo.WifiUtil;
import com.luo.wifidemo.util.Logger;

public class WifiToolsActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/

	// log tag
	private final static String TAG = "WifiToolsActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/* layout and views */
	Button mOpenWifiBtn;
	Button mCloseWifiBtn;
	ListView mListView;

	/** wifi util */
	WifiUtil mWifiUtil;
	/** wifi names */
	private ArrayList<String> mWifiNameList = new ArrayList<String>();

	/******************************
	 * InnerClass <br>
	 ******************************/

	/**************************************
	 * Message Hander
	 **************************************/
	private MainHandler mMainHandler;

	// MainHandler Definition
	class MainHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// case MSG_DOWNLOAD_PROCESS_CHANGE:
			// getUpdateHelper().setDialogProcess(msg.arg1);
			// break;
			default:
				break;
			}
		}
	}

	/******************************
	 * Constructor <br>
	 ******************************/

	/******************************
	 * implement Methods <br>
	 ******************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initWindow();
		initLayoutsAndViews();
		initEnvironment();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		releaseEnvironment();

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_wifi_btn:
			mWifiUtil.openWifi();
			break;
		case R.id.close_wifi_btn:
			mWifiUtil.closeWifi();
			break;
		case R.id.scan_and_log_btn:
			mWifiUtil.openWifi();
			mWifiNameList = mWifiUtil.getScanResult();
			this.updateWifiList();
			break;
		default:
			break;
		}
	}

	/******************************
	 * public Methods <br>
	 ******************************/

	/******************************
	 * private Methods <br>
	 ******************************/

	/**
	 * Init the Frame of the window
	 */
	private void initWindow() {
		setContentView(R.layout.tools_activity_main);
	}

	/**
	 * Init View and layout of the window
	 */
	private void initLayoutsAndViews() {
		mOpenWifiBtn = (Button) findViewById(R.id.open_wifi_btn);
		mOpenWifiBtn.setOnClickListener(this);

		mCloseWifiBtn = (Button) findViewById(R.id.close_wifi_btn);
		mCloseWifiBtn.setOnClickListener(this);

		((Button) findViewById(R.id.scan_and_log_btn)).setOnClickListener(this);

		mListView = (ListView) findViewById(R.id.wifi_list);
		mListView.setVisibility(View.GONE);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String ssid = mWifiNameList.get(position);
				Logger.d(TAG, "ssid:" + ssid);
				
				LayoutInflater inflater = LayoutInflater.from(WifiToolsActivity.this);
				final View textEntryView = inflater.inflate(R.layout.dialoglayout, null);
				final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
				final AlertDialog.Builder builder = new AlertDialog.Builder(WifiToolsActivity.this);
				builder.setCancelable(true);
				builder.setTitle("请输入密码");
				builder.setView(textEntryView);
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Logger.d(TAG, "pass word :" + edtInput.getText().toString());
						mWifiUtil.connectNetwork( ssid , edtInput.getText().toString());
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
//						setTitle("");
					}
				});
				builder.show();
			}
		});
	}

	/**
	 * Init the environment of the activity
	 */
	private void initEnvironment() {
		// Init Main Handler
		mMainHandler = new MainHandler();

		// Init Wifi Util
		mWifiUtil = new WifiUtil(this);
	}

	/**
	 * Release the environment of the activity
	 */
	private void releaseEnvironment() {

	}
	
	private void updateWifiList(){
		mListView.setVisibility(View.GONE);
		mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mWifiNameList ));
		mListView.setVisibility(View.VISIBLE);
	}
}
