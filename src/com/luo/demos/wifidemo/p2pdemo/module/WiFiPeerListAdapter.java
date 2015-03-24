package com.luo.demos.wifidemo.p2pdemo.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luo.demos.wifidemo.R;

/**
 * Array adapter for ListFragment that maintains WifiP2pDevice list.
 */
public class WiFiPeerListAdapter extends BaseAdapter {

	private List<WifiP2pDevice> mDeivceList;
	private Context mContext;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public WiFiPeerListAdapter(Context context, List<WifiP2pDevice> objects) {
		mDeivceList = objects;
		mContext = context;
		mDeivceList = new ArrayList<WifiP2pDevice>();
	}
	
	/** Getter & Setter */
	public void setDeviceList(Collection<WifiP2pDevice> objects){
		mDeivceList.clear();
		mDeivceList.addAll(objects);
	}
	public List<WifiP2pDevice> getDeviceList(){
		return mDeivceList;
	}
	
	@Override
	public int getCount() {
		return (mDeivceList != null) ? mDeivceList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (mDeivceList != null) ? mDeivceList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		
		if (v == null) {
			v= LayoutInflater.from(mContext).inflate(R.layout.wifip2p_listview_peersitem, null);
		}
		
		WifiP2pDevice device = mDeivceList.get(position);
		
		if (device != null) {
			TextView top = (TextView) v.findViewById(R.id.device_name);
			TextView bottom = (TextView) v.findViewById(R.id.device_details);
			if (top != null) {
				String strTop = device.deviceName;
				if (device.deviceName.contains("Android_3f82"))
					strTop = ("SamSung");
				else if (device.deviceName.contains("Android_c023"))
					strTop = ("htc");
				else if (device.deviceName.contains("Android_e8bf"))
					strTop = ("HTC");
				else if (device.deviceName.contains("Android_1bf5"))
					strTop = ("HUAWEI");
				else if (device.deviceName.contains("Android_a38e"))
					strTop = ("HTC-W");
				else if (device.deviceName.contains("Android_bc2d"))
					strTop = ("HTC-ONE");

				top.setText(strTop);
			}
			if (bottom != null) {
				bottom.setText(getDeviceStatus(device.status));
			}
		}
		
		return v;
	}
	
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
}