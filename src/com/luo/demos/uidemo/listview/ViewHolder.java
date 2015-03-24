package com.luo.demos.uidemo.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luo.demos.uidemo.listview.ImageLoader.Type;

/**
 *  A General ViewHolder
 *  
 * @author luochenxun
 */
public class ViewHolder {
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;

	private ViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		// setTag
		mConvertView.setTag(this);
	}

	/**
	 * Get a ViewHolder Objs
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static ViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			holder = new ViewHolder(context, parent, layoutId, position);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.mPosition = position;
		}
		return holder;
	}
	
	public static ViewHolder getForce(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
		ViewHolder holder = new ViewHolder(context, parent, layoutId, position);
		return holder;
	}

	public View getConvertView() {
		return mConvertView;
	}

	/**
	 * Get View in viewHolder by ViewId
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	/**
	 * Set text of textView
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, String text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}
	
	public ViewHolder setSelected(int viewId , boolean isSelect){
		View view = getView(viewId);
		view.setSelected(isSelect);
		return this;
	}

	/**
	 * Set image of ImageView
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int drawableId) {
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);

		return this;
	}

	/**
	 *  Set image of ImageView
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}
	
	/**
	 *  Set image of ImageView
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageByUrl(int viewId, String url) {
		ImageLoader.getInstance(3, Type.LIFO).loadImage(url, (ImageView) getView(viewId));
		return this;
	}

	/**
	 *  Set image of ImageView
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageByUrl(int viewId, String url , int defaultDrawableId) {
		ImageLoader.getInstance(3, Type.LIFO).loadImage(url, (ImageView) getView(viewId) ,defaultDrawableId);
		return this;
	}

	public int getPosition() {
		return mPosition;
	}
	
	public void setPosition(int position){
		mPosition = position;
	}

}
