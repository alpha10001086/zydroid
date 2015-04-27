package com.zy.zyplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.zy.zyplayer.R;
import com.zy.zyplayer.ImageDecoder.ImageDecoderListener;

public class GridViewAdapter extends BaseAdapter implements
		ImageDecoderListener {

	private static final int VIEW_LOADING = 0;
	private static final int VIEW_THUMBNAIL = 1;

	private Context mContext = null;
	private OnClickListener mOnClickListener = null;
	private Handler mHandler = null;
	private ImageDecoder mImageDecoder = null;
	private GridViewData[] mData = null;

	public GridViewAdapter(Context context, OnClickListener onClickListener,
			GridViewData[] data) {
		super();
		mContext = context;
		mOnClickListener = onClickListener;
		mData = data;
		mImageDecoder = new ImageDecoder(this);
		mImageDecoder.start();
		mHandler = new Handler();
	}

	@Override
	protected void finalize() throws Throwable {
		mImageDecoder.stopThread();
		super.finalize();
	}

	@Override
	public int getCount() {
		return mData.length;
	}

	@Override
	public Object getItem(int position) {
		return mData[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewSwitcher viewSwitcher = new ViewSwitcher(mContext);
		GridViewData gridViewData = (GridViewData) getItem(position);
		String thumbnail = gridViewData.mThumbnail;
		if (null == convertView) {
			ProgressBar progressBar = new ProgressBar(mContext);
			// FIXME: align the progress bar center in some way.
			progressBar.setLayoutParams(new LayoutParams(200, 200));
			progressBar.setPadding(40, 40, 40, 40);
			LayoutInflater inflater = LayoutInflater.from(mContext);
			TextView thumbView = (TextView) inflater.inflate(
					R.layout.view_thumbnail, viewSwitcher, false);
			thumbView.setLayoutParams(new LayoutParams(200, 200));
			thumbView.setText(gridViewData.mLabel);

			viewSwitcher.setPadding(0, 10, 20, 0);
			viewSwitcher.addView(progressBar);
			viewSwitcher.addView(thumbView);
			viewSwitcher.setOnClickListener(mOnClickListener);
		} else {
			viewSwitcher = (ViewSwitcher) convertView;
		}

		GridViewData tagData = (GridViewData) viewSwitcher.getTag();
		if (tagData == null || !tagData.mThumbnail.equals(thumbnail)) {
			viewSwitcher.setTag(gridViewData);
			viewSwitcher.setDisplayedChild(VIEW_LOADING);
			TextView thumbView = (TextView) viewSwitcher
					.getChildAt(VIEW_THUMBNAIL);
			mImageDecoder.decodeThumbnail(thumbnail, thumbView, viewSwitcher);
		}

		return viewSwitcher;
	}

	private Bitmap getBitmapFromResource(Context context, int resId) {
		return BitmapFactory.decodeResource(context.getResources(), resId);
	}

	@Override
	public void handleImageDecoded(final ViewSwitcher viewSwitcher,
			final View view, final Bitmap bitmap, String filename) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				TextView thumbView = (TextView) view;
				Bitmap bm = bitmap;
				if (bm == null) {
					bm = getBitmapFromResource(mContext,
							R.drawable.alpha_photo_default_thumb);
				}
				Drawable drawable = new BitmapDrawable(mContext.getResources(),
						bm);
				drawable.setBounds(0, 30, 160, 160);
				thumbView.setCompoundDrawables(null, drawable, null, null);
				viewSwitcher.setDisplayedChild(VIEW_THUMBNAIL);
			}
		});
	}
}
