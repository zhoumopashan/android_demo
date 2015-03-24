package com.luo.demos.wifidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.luo.demos.wifidemo.p2pdemo.ui.WifiP2pDemoActivity;
import com.luo.demos.wifidemo.wifitools.ui.WifiToolsActivity;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifidemo_main);

		/* wifi p2p demo */
		((Button) findViewById(R.id.p2p_demo_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, WifiP2pDemoActivity.class));
			}
		});

		/* wifi util demo */
		((Button) findViewById(R.id.wifi_util_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, WifiToolsActivity.class));
			}
		});
	}
}
