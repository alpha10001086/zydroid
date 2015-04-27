package com.zy.zyplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.zy.zyplayer.GridViewData.ItemOperationPlay;
import com.zy.zyplayer.ImageDecoder.ImageDecoderListener;

/**
 * ZySlideshow is an activity that plays the photo slide show. it implements the
 * handler handleImageDecoded() of interface ImageDecoderListener to handle the
 * decoded image. as the image decoded, it sets the decoded image to the hidden
 * ImageView and switches to it with transition effect.
 */
public class ZySlideshow extends Activity implements ImageDecoderListener {

	public enum Transition {
		CLOCKWISE, COUNTERCLOCKWISE, CROSS_FADE, CROSS_ZOOM, SLIDE_TO_LEFT, SLIDE_TO_RIGHT
	}

	private class SlideshowInfo {
		String[] files;
		protected int index = 0;
		protected int interval = 3000; // milliseconds
		protected Transition effect = Transition.CROSS_FADE;
		protected boolean playing = true;

		protected int getNextIndex() {
			int nextIndex = index + 1;
			return (nextIndex >= files.length) ? 0 : nextIndex;
		}
	}

	protected static class SlideshowHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * SlideshowTimerTask decodes the image to be displayed and schedules the
	 * next image.
	 */
	protected class SlideshowTimerTask extends TimerTask {
		@Override
		public void run() {
			showImage(mSlideshowInfo.index);
		}
	}

	final private float degrees = 90.0f;
	final private float maxScale = 16f;
	private float scaling = 1f;
	private int rotation = 0;

	private Context mContext = null;
	private Handler mHandler = null;
	private ImageDecoder mImageDecoder = null;
	private ProgressBar progressBar = null;
	private static SlideshowInfo mSlideshowInfo;
	private Timer mTimer = null;
	private ViewSwitcher mViewSwitcher = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slideshow);
		initialize();

		ItemOperationPlay actionPlay = null;
		actionPlay = (ItemOperationPlay) getIntent().getSerializableExtra(
				ItemOperationPlay.class.getName());
		if (actionPlay != null) {
			// set the files to slideshowInfo
			mSlideshowInfo.files = actionPlay.mFiles;
			// get the first playing index.
			int index = 0;
			for (String s : actionPlay.mFiles) {
				if (s.equals(actionPlay.mFilename)) {
					mSlideshowInfo.index = index;
					break;
				}
				index++;
			}
		}

		mTimer.schedule(new SlideshowTimerTask(), 10);
	}

	@Override
	public void finish() {
		mTimer.cancel();
		mImageDecoder.stopThread();
		super.finish();
	}

	@Override
	public void handleImageDecoded(ViewSwitcher viewSwitcher, final View view,
			final Bitmap bitmap, final String filename) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// stop displaying loading icon.
				if (View.GONE != progressBar.getVisibility()) {
					progressBar.setVisibility(View.GONE);
				}

				// set image to ImageView and show it.
				ImageView imageView = (ImageView) view;
				imageView.setImageBitmap(bitmap);
				mViewSwitcher.showNext();
			}

		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case 23:
			mSlideshowInfo.playing = !mSlideshowInfo.playing;
			if (mSlideshowInfo.playing) {
				// reset scaling
				ImageView imageView = getCurrentImageView();
				imageView.setScaleX(1);
				imageView.setScaleY(1);

				// reset rotation
				float fromDegrees = degrees * (rotation % 4);
				float toDegrees = (360 - fromDegrees) % 360;
				rotation = 0;
				imageView.setRotation(toDegrees);

				mTimer = new Timer();
				showImage(mSlideshowInfo.index);
			} else {
				mTimer.cancel();
			}
			break;
		case KeyEvent.KEYCODE_PROG_RED:
			ImageView tip = (ImageView) findViewById(R.id.imageViewBottomTip);
			int visibility = tip.getVisibility();
			tip.setVisibility((visibility == View.GONE) ? View.VISIBLE
					: View.GONE);
			break;
		case KeyEvent.KEYCODE_PROG_GREEN:
			if (!mSlideshowInfo.playing) {
				float fromDegrees = degrees * (rotation % 4);
				float toDegrees = fromDegrees + degrees;
				rotation++;
				RotateAnimation rotateAnimation = new RotateAnimation(
						fromDegrees, toDegrees,
						RotateAnimation.RELATIVE_TO_SELF, 0.5f,
						RotateAnimation.RELATIVE_TO_SELF, 0.5f);
				rotateAnimation.setDuration(500);
				rotateAnimation.setFillAfter(true);
				ImageView imageView = getCurrentImageView();
				imageView.startAnimation(rotateAnimation);
			}
			break;
		case KeyEvent.KEYCODE_PROG_YELLOW:
			if (!mSlideshowInfo.playing) {
				if (scaling < maxScale) {
					scaling *= 2f;
					ImageView imageView = getCurrentImageView();
					imageView.setScaleX(scaling);
					imageView.setScaleY(scaling);
				}
			}
			break;
		case KeyEvent.KEYCODE_PROG_BLUE:
			if (!mSlideshowInfo.playing) {
				if (scaling > 1 / maxScale) {
					scaling /= 2f;
					ImageView imageView = getCurrentImageView();
					imageView.setScaleX(scaling);
					imageView.setScaleY(scaling);
				}
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_slideshow, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemInterval3:
			mSlideshowInfo.interval = 3 * 1000;
			break;
		case R.id.itemInterval5:
			mSlideshowInfo.interval = 5 * 1000;
			break;
		case R.id.itemInterval10:
			mSlideshowInfo.interval = 10 * 1000;
			break;
		case R.id.itemTransitionClockwise:
			setTransition(Transition.CLOCKWISE);
			break;
		case R.id.itemTransitionCounterclockwise:
			setTransition(Transition.COUNTERCLOCKWISE);
			break;
		case R.id.itemTransitionCrossFade:
			setTransition(Transition.CROSS_FADE);
			break;
		case R.id.itemTransitionSlideToRight:
			setTransition(Transition.SLIDE_TO_RIGHT);
			break;
		case R.id.itemTransitionSlideToLeft:
			setTransition(Transition.SLIDE_TO_LEFT);
			break;
		case R.id.itemTransitionCrossZoom:
			setTransition(Transition.CROSS_ZOOM);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initialize() {
		mContext = this;
		mTimer = new Timer();
		mHandler = new SlideshowHandler();
		mImageDecoder = new ImageDecoder(this);
		mImageDecoder.start();
		progressBar = (ProgressBar) findViewById(R.id.progressBarDecoding);
		mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherSlideshow);
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		View view0 = inflater
				.inflate(R.layout.view_photo, mViewSwitcher, false);
		View view1 = inflater
				.inflate(R.layout.view_photo, mViewSwitcher, false);
		mViewSwitcher.addView(view0);
		mViewSwitcher.addView(view1);
		mViewSwitcher.setDisplayedChild(0);

		if (mSlideshowInfo == null)
			mSlideshowInfo = new SlideshowInfo();
		setTransition(mSlideshowInfo.effect);
	}

	private ImageView getCurrentImageView() {
		View view = mViewSwitcher.getCurrentView();
		ImageView imageView = (ImageView) view
				.findViewById(R.id.imageViewPhoto);
		return imageView;
	}

	private void setTransition(Transition effect) {
		mSlideshowInfo.effect = effect;
		switch (mSlideshowInfo.effect) {
		case CLOCKWISE:
			mViewSwitcher.setOutAnimation(mContext, R.animator.clockwise_out);
			mViewSwitcher.setInAnimation(mContext, R.animator.clockwise_in);
			break;
		case COUNTERCLOCKWISE:
			mViewSwitcher.setOutAnimation(mContext,
					R.animator.counterclockwise_out);
			mViewSwitcher.setInAnimation(mContext,
					R.animator.counterclockwise_in);
			break;
		case CROSS_FADE:
			mViewSwitcher.setOutAnimation(mContext, R.animator.fade_out);
			mViewSwitcher.setInAnimation(mContext, R.animator.fade_in);
			break;
		case CROSS_ZOOM:
			mViewSwitcher.setOutAnimation(mContext, R.animator.zoom_out);
			mViewSwitcher.setInAnimation(mContext, R.animator.zoom_in);
			break;
		case SLIDE_TO_RIGHT:
			mViewSwitcher.setOutAnimation(mContext,
					R.animator.slide_to_right_out);
			mViewSwitcher
					.setInAnimation(mContext, R.animator.slide_to_right_in);
			break;
		case SLIDE_TO_LEFT:
			mViewSwitcher.setOutAnimation(mContext,
					R.animator.slide_to_left_out);
			mViewSwitcher.setInAnimation(mContext, R.animator.slide_to_left_in);
			break;
		default:
			break;
		}
	}

	private void showImage(int index) {
		View nextView = mViewSwitcher.getNextView();
		ImageView imageView = (ImageView) nextView
				.findViewById(R.id.imageViewPhoto);
		mImageDecoder.decodeThumbnail(mSlideshowInfo.files[index], imageView,
				mViewSwitcher);

		/*
		 * proceed to the next
		 */
		mSlideshowInfo.index = mSlideshowInfo.getNextIndex();
		mTimer.schedule(new SlideshowTimerTask(), mSlideshowInfo.interval);
	}
}
