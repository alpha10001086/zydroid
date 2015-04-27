package com.zy.libs.zymedia;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;

public class ZyMedia {

	public FileUtils fileUtils = null;
	private Context mContext = null;

	/**
	 * to construct the object of ZyMedia with application context.
	 * 
	 * @param context
	 *            the context of the application.
	 */
	public ZyMedia(Context context) {
		mContext = context;
		fileUtils = new FileUtils(mContext);
	}

	/**
	 * get the directory of sd card.
	 * 
	 * @return the directory of sd card, or null if sd card does not exist.
	 */
	public static final File getSDCardDirectory() {
		String externalStorageState = Environment.getExternalStorageState();
		if (externalStorageState.equals(Environment.MEDIA_MOUNTED)
				|| externalStorageState
						.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
	}

	/**
	 * get the directories of usb disk.
	 * 
	 * @return the directories of usb disk.
	 */
	public static final List<File> getUSBDirectories() {
		final File emulatedStorage = new File("/storage/emulated");
		File storagePath = new File("/storage");
		File[] storages = storagePath.listFiles();
		Vector<File> usbFolders = new Vector<File>();
		if (storages != null) {
			for (File storage : storages) {
				if(storage.getAbsolutePath().equals(emulatedStorage.getAbsolutePath())) {
					continue;
				}
				try {
					if(FileUtils.isSymlink(storage)) {
						continue;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				usbFolders.add(storage);
			}
		}
		return usbFolders;
	}
}
