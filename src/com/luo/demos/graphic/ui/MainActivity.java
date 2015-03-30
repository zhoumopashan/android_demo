package com.luo.demos.graphic.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.luo.demos.uidemo.listview.CommonAdapter;
import com.luo.demos.uidemo.listview.ViewHolder;
import com.luo.demos.utils.Logger;
import com.luo.demos.wifidemo.R;

public class MainActivity extends Activity implements View.OnClickListener, OnItemClickListener {

	/*-----------------------------
	 *    Macros 
	 *-----------------------------*/

	private final static String TAG = MainActivity.class.getSimpleName();

	/*-----------------------------
	 *    Public Members 
	 *-----------------------------*/

	/*-----------------------------
	 *    Private Members 
	 *-----------------------------*/

	/* layouts and views */
	private ListView mListView;
	private ItemAdapter mAdapter;

	/* main members */
	private HashMap<Integer, Class> mItemMap = new HashMap<Integer, Class>();
	private List<Integer> mItemNameList = new ArrayList<Integer>();
	{
		mItemMap.put(R.string.paint, com.luo.demos.graphic.ui.PaintActivity.class);
		mItemMap.put(R.string.canvas, com.luo.demos.graphic.ui.CanvasActivity.class);

		// init list by the itemName-class-map
		Iterator<Entry<Integer, Class>> iter = mItemMap.entrySet().iterator();
		while (iter.hasNext()) {
			mItemNameList.add(((Entry<Integer, Class>) iter.next()).getKey());
		}
	}

	/*-----------------------------
	 *    InnerClass 
	 *-----------------------------*/

	class ItemAdapter extends CommonAdapter<Integer> implements OnClickListener {

		public ItemAdapter(Context context, List<Integer> mDatas, int itemLayoutId) {
			super(context, mDatas, itemLayoutId);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// case value:
			//
			// break;

			default:
				break;
			}
		}

		@Override
		public void convert(ViewHolder helper, Integer item) {
			helper.setText(R.id.activity_main_lv_item_tv, getString(item));
		}

	}

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

		updateListView();
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
		mListView = (ListView) findViewById(R.id.activity_main_lv);
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

	private void updateListView() {
		synchronized (mItemNameList) {
			if (mAdapter == null) {
				mAdapter = new ItemAdapter(this, mItemNameList, R.layout.activity_main_listview_item);
				mListView.setAdapter(mAdapter);
				mListView.setOnItemClickListener(this);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Logger.d(TAG, "  class is : " + mItemMap.get(mItemNameList.get(position)));
		startActivity(new Intent(this, mItemMap.get(mItemNameList.get(position))));
	}
}
