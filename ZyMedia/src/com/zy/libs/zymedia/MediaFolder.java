package com.zy.libs.zymedia;

import java.io.File;

public class MediaFolder {
	/**
	 * File instance of the folder.
	 */
	final public File mFolder;
	/**
	 * the media files located in the folder.
	 */
	final public File[] mFiles;
	/**
	 * the names of the media files located in the folder. it is generated
	 * automatically.
	 */
	public String[] mFilenames;
	/**
	 * thumbnail of the folder.
	 */
	public String mThumbnail;

	/**
	 * to construct the object of MediaFolder with the folder containing the
	 * media and a File array of the files located in the media folder.
	 * 
	 * @param folder
	 *            a File object to represent the folder containing the media.
	 * @param files
	 *            a File array contains the files located in the media folder.
	 */
	MediaFolder(File folder, File[] files) {
		mFolder = folder;
		mFiles = files;
		generateFilenames();
		setDefaultThumbnail();
	}

	/**
	 * set the first media as the default thumbnail for the folder.
	 */
	private void setDefaultThumbnail() {
		mThumbnail = mFiles[0].getAbsolutePath();
	}

	/**
	 * generate the string array 'filenames' that contains the file name of the
	 * files stored in the File array 'files'.
	 */
	private void generateFilenames() {
		mFilenames = new String[mFiles.length];
		int i = 0;
		for (File f : mFiles) {
			mFilenames[i++] = f.getAbsolutePath();
		}
	}
}
