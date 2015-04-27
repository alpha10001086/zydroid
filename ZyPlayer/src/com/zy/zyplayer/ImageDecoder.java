package com.zy.zyplayer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ViewSwitcher;

public class ImageDecoder extends Thread {

	/**
	 * the class creates the instance of ThumbnailDecoder should implement the
	 * interface ThumbnailDecoderListener in which the callback
	 * handleThumbnailDecoded() will be called once the image is decoded.
	 */
	public interface ImageDecoderListener {
		void handleImageDecoded(ViewSwitcher viewSwitcher,
				View view, Bitmap bitmap, String filename);
	}

	private ImageDecoderListener mListener = null;
	private Handler mHandler;

	/**
	 * construct the image decoder with a listener that should implement
	 * ThumbnailDecoderListener to handle the decoded image.
	 * 
	 * @param listener
	 *            the listener implements ThumbnailDecoderListener to handle the
	 *            decoded image
	 */
	ImageDecoder(ImageDecoderListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		Looper.loop();
	}

	/**
	 * stop the image decoding thread.
	 */
	public synchronized void stopThread() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Looper.myLooper().quit();
			}

		});
	}

	/**
	 * to get the sample size of the specified image file.
	 * 
	 * @param view
	 *            the view to display the image.
	 * @param filename
	 *            the name of the file to be query.
	 * @return the sample size of the specified image file.
	 */
	private int getSampleSize(final View view, final String filename) {
		int sampleSize = 1;
		int sampleWidth = 0;
		int viewWidth = view.getLayoutParams().width;
		if (viewWidth < 0) {
			DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
			sampleWidth = metrics.widthPixels;
		} else {
			sampleWidth = viewWidth;
		}

		@SuppressWarnings("unused")
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(filename, options);
		if (options.outWidth > sampleWidth) {
			sampleSize = options.outWidth / sampleWidth;
		}
		Log.i("jacob_shih", ">>> " + options.outMimeType + " " + "size: "
				+ options.outWidth + "x" + options.outHeight + " "
				+ "viewWidth: " + viewWidth + " " + "sampleSize: " + sampleSize
				+ " ");
		return sampleSize;
	}

	/**
	 * decode the image.
	 * 
	 * @param filename
	 *            the path of the image file to be decoded.
	 * @param view
	 *            the image view to display the decoded image.
	 */
	public synchronized void decodeThumbnail(final String filename,
			final View view, final ViewSwitcher viewSwitcher) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				try {
					synchronized (view) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = getSampleSize(view, filename);
						Bitmap bitmap = BitmapFactory
								.decodeFile(filename, options);

						thumbnailDecoded(viewSwitcher, view, bitmap, filename);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * called when the image is decoded and ask the UI thread to display the
	 * decoded image on the image view.
	 * 
	 * @param imageView
	 *            the image view to display the decoded image.
	 * @param bitmap
	 *            decoded image.
	 */
	private void thumbnailDecoded(ViewSwitcher viewSwitcher,
			View view, Bitmap bitmap, String filename) {
		if (mListener != null) {
			mListener
					.handleImageDecoded(viewSwitcher, view, bitmap, filename);
		}
	}
}
