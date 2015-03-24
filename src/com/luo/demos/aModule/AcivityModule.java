package com.luo.demos.aModule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.luo.demos.wifidemo.R;

public class AcivityModule extends Activity implements View.OnClickListener {

	/*-----------------------------
	 *    Macros 
	 *-----------------------------*/

	private final static String TAG = AcivityModule.class.getSimpleName();

	/*-----------------------------
	 *    Public Members 
	 *-----------------------------*/
	
	/*-----------------------------
	 *    Private Members 
	 *-----------------------------*/
	
	/* layouts and  views */
	private ListView mCommentListView;
	
	/*-----------------------------
	 *    InnerClass 
	 *-----------------------------*/
	
	/*-----------------------------
	 *    Message Hander 
	 *-----------------------------*/
	
	private MainHandler mMainHandler;

	/**
	 * MainHandler
	 * <p>
	 * Process the Msg-Queue of ui in the activity
	 * */
	@SuppressLint("HandlerLeak")
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

	/*-----------------------------
	 *    Implement Methods
	 *-----------------------------*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.initWindow();
		this.initLayoutsAndViews();
		this.initEnvironment();

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

	/*-----------------------------
	 *    Public Methods
	 *-----------------------------*/
	
	
	/*-----------------------------
	 *    Private Methods
	 *-----------------------------*/

	/**
	 * Init the Frame of the window
	 */
	private void initWindow() {
		setContentView(R.layout.activity_main);
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
