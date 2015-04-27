package com.zy.zyplayer;

import java.io.File;
import java.util.List;
import java.util.Vector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.zy.libs.zymedia.FileUtils;
import com.zy.libs.zymedia.MediaFolder;
import com.zy.libs.zymedia.ZyMedia;
import com.zy.zyplayer.GridViewData.ItemOperation;
import com.zy.zyplayer.GridViewData.ItemOperationNavigate;
import com.zy.zyplayer.GridViewData.ItemOperationPlay;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class ZyExplorer extends Activity {

	private ZyMedia zyMedia = null;
	private GridView mGridViewExplorer = null;
	private TextView mTextViewHint = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		zyMedia = new ZyMedia(this);
		setContentView(R.layout.activity_explorer);
		mGridViewExplorer = (GridView) findViewById(R.id.GridExplorer);
		mTextViewHint = (TextView) findViewById(R.id.GridExplorerHint);
		loadGridViewForFolders();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (canGoBack()) {
				loadGridViewForFolders();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean canGoBack() {
		String hint = (String) mTextViewHint.getText();
		String hint_select_folder = getResources().getString(
				R.string.hint_select_folder);
		return !hint.equals(hint_select_folder);
	}

	private GridView.OnItemClickListener gridViewOnClickListener = new GridView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (view instanceof ViewSwitcher) {
				ViewSwitcher viewSwitcher = (ViewSwitcher) view;
				GridViewData gridViewData = (GridViewData) viewSwitcher
						.getTag();
				ItemOperation action = gridViewData.mOperation;
				if (action instanceof ItemOperationNavigate) {
					ItemOperationNavigate actionNavigate = (ItemOperationNavigate) action;
					loadGridViewForImages(actionNavigate);
				} else if (action instanceof ItemOperationPlay) {
					ItemOperationPlay actionPlay = (ItemOperationPlay) action;
					startSlideShow(actionPlay);
				}
			}
		}
	};

	private void loadGridViewForFolders() {
		List<MediaFolder> folders = new Vector<MediaFolder>();
		File devSDCard = ZyMedia.getSDCardDirectory();
		if (devSDCard != null) {
			List<MediaFolder> foldersSDCard = zyMedia.fileUtils
					.findMediaFolders(FileUtils.MediaType.IMAGE, devSDCard);
			folders.addAll(foldersSDCard);
		}
		List<File> devUSBs = ZyMedia.getUSBDirectories();
		for(File devUSB : devUSBs) {
			List<MediaFolder> foldersUSB = zyMedia.fileUtils.findMediaFolders(
					FileUtils.MediaType.IMAGE, devUSB);
			folders.addAll(foldersUSB);
		}

		GridViewData[] data = new GridViewData[0];
		if (folders.size() > 0) {
			data = new GridViewData[folders.size()];
			int i = 0;
			for (MediaFolder mediaFolder : folders) {
				data[i] = new GridViewData();
				data[i].mThumbnail = mediaFolder.mThumbnail;
				data[i].mLabel = mediaFolder.mFolder.getName();
				data[i].mOperation = new ItemOperationNavigate(mediaFolder);
				i++;
			}
		}
		GridViewAdapter thumbnailAdapter = new GridViewAdapter(
				this.getApplicationContext(), null, data);
		mGridViewExplorer.setAdapter(thumbnailAdapter);
		mGridViewExplorer.setOnItemClickListener(gridViewOnClickListener);
		mTextViewHint.setText(R.string.hint_select_folder);
	}

	private void loadGridViewForImages(ItemOperationNavigate actionNavigate) {
		MediaFolder mediaFolder = actionNavigate.mMediaFolder;
		String[] filenames = mediaFolder.mFilenames;
		GridViewData[] data = new GridViewData[0];
		data = new GridViewData[filenames.length];
		int i = 0;
		for (String filename : filenames) {
			data[i] = new GridViewData();
			data[i].mThumbnail = filename;
			data[i].mLabel = (new File(filename)).getName();
			data[i].mOperation = new ItemOperationPlay(filenames, filename);
			i++;
		}
		GridViewAdapter thumbnailAdapter = new GridViewAdapter(
				this.getApplicationContext(), null, data);
		mGridViewExplorer.setAdapter(thumbnailAdapter);
		mGridViewExplorer.setOnItemClickListener(gridViewOnClickListener);
		mTextViewHint.setText(R.string.hint_press_ok_to_play);
	}

	private void startSlideShow(ItemOperationPlay actionPlay) {
		Intent intent = new Intent();
		intent.setClass(this, ZySlideshow.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ItemOperationPlay.class.getName(), actionPlay);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
