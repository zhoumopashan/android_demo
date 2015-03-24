package com.luo.demos;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

public class MainApplication extends Application {
	/*-----------------------------
	 *    Macros 
	 *-----------------------------*/
	private static final String TAG = MainApplication.class.getSimpleName();

	/*-----------------------------
	 *    private Members 
	 *-----------------------------*/

	private List<Activity> mActivityList = new LinkedList<Activity>();

	/*-----------------------------
	 *    Inner classes 
	 *-----------------------------*/

	/** Singleton Methods */
	private static MainApplication mInstance;

	public static MainApplication getInstance() {
		if (mInstance == null) {
			synchronized (MainApplication.class) {
				mInstance = new MainApplication();
			}
		}
		return mInstance;
	}

	/*-----------------------------
	 *    Constructions 
	 *-----------------------------*/

	/*-----------------------------
	 *    Implement Methods
	 *-----------------------------*/

	@Override
	public void onCreate() {
		super.onCreate();

		/* save the application global instance */
		mInstance = this;

		initEnvironment();
	}

	@Override
	public void onTerminate() {
		releaseEnvironment();

		super.onTerminate();
	}

	
	/*-----------------------------
	 *    Public Methods
	 *-----------------------------*/

	/** Add activity in the RunTime-Manage-activity-list */
	public void addActivity(Activity activity) {
		if (activity != null && !mActivityList.contains(activity)) {
			mActivityList.add(activity);
		}
	}

	/** Remove activity from the RunTime-Manage-activity-list */
	public void removeActivity(Activity activity) {
		if (activity != null && mActivityList.contains(activity)) {
			mActivityList.remove(activity);
		}
	}

	/** traves the RunTime-Manage-activity-list and finish all of them */
	public void exitApp() {
		for (Activity act : mActivityList) {
			if (act != null)
				act.finish();
		}
		System.exit(0);
	}

	/*-----------------------------
	 *    Private Methods
	 *-----------------------------*/
	
	/**
	 * Init the global information of the application here
	 */
	private void initEnvironment() {

		/* download the configuration */
		downloadGlobalConfiguration();
	}

	/**
	 * release the global information of the application here
	 */
	private void releaseEnvironment() {
		// TODO
	}

	/**
	 * downloadGlobalConfiguration <br>
	 * Start a Thread to download the global-configuration <br>
	 */
	private void downloadGlobalConfiguration() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO you can download your configure here
			}
		}).start();
	}
}
