package com.zy.zyplayer;

import java.io.Serializable;

import com.zy.libs.zymedia.MediaFolder;

public class GridViewData {
	GridViewData() {
	}

	/**
	 * abstract base class of item operation classes.
	 */
	public static abstract class ItemOperation {
	}

	/**
	 * item operation class for folder navigation.<br>
	 * construct the object of ItemOperationNavigate with an object of
	 * MediaFolder that is the folder containing the media and a File array of
	 * the files located in the media folder.<br>
	 * to associate the grid view item with an object of ItemOperationNavigate
	 * which will be passed to GridView.OnItemClickListener as the item is
	 * clicked.<br>
	 * GridView.OnItemClickListener is responsible for navigating to the
	 * specified folder.
	 */
	public static class ItemOperationNavigate extends ItemOperation implements Serializable {
		/**
		 * serialVersionUID is generated automatically.
		 */
		private static final long serialVersionUID = 4933711639409466201L;
		public MediaFolder mMediaFolder;

		public ItemOperationNavigate(MediaFolder mediaFolder) {
			super();
			mMediaFolder = mediaFolder;
		}
	}

	/**
	 * item operation class for playing the specified media files.<br>
	 * construct the object of ItemOperationPlay with an array of media files
	 * and the file played from.<br>
	 * to associate the grid view item with an object of ItemOperationPlay which
	 * will be passed to GridView.OnItemClickListener as the item is clicked.<br>
	 * GridView.OnItemClickListener is responsible for playing the specified
	 * media files.
	 */
	public static class ItemOperationPlay extends ItemOperation implements Serializable {
		/**
		 * serialVersionUID is generated automatically.
		 */
		private static final long serialVersionUID = 2559205861942344087L;

		public ItemOperationPlay(String[] files, String filename) {
			super();
			mFiles = files;
			mFilename = filename;
		}

		public String[] mFiles;
		public String mFilename;
	}

	/**
	 * the thumbnail to be shown in the grid view.
	 */
	public String mThumbnail;
	/**
	 * the label of the thumbnail.
	 */
	public String mLabel;
	/**
	 * the operation to be taken as the item clicked.
	 */
	public ItemOperation mOperation;
}
