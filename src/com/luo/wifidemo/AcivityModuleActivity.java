package com.luo.wifidemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class AcivityModuleActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/

	// log tag
	private final static String TAG = "AcivityModuleActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/

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

	}

	/**
	 * Init View and layout of the window
	 */
	private void initLayoutsAndViews() {

	}

	/**
	 * Init the environment of the activity
	 */
	private void initEnvironment() {
		// Init Main Handler
		mMainHandler = new MainHandler();
	}

	/**
	 * Release the environment of the activity
	 */
	private void releaseEnvironment() {

	}
}
