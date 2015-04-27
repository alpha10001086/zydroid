package com.zy.libs.zymedia;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import com.zy.libs.zymedia.R;

public class FileUtils {
	private Context mContext = null;

	/**
	 * media type for image, music and video.
	 */
	public enum MediaType {
		IMAGE, MUSIC, VIDEO, UNKNOWN
	}
	/**
	 * to construct the object of FileUtils with application context.
	 * 
	 * @param context
	 *            the context of the application.
	 */
	public FileUtils(Context context) {
		super();
		mContext = context;
	}

	/**
	 * get the resource id by the media type.
	 * 
	 * @param mediaType
	 *            the media type, one of IMAGE, MUSIC or VIDEO.
	 * @return the resource id of the specified media type.
	 */
	private int getResourIdByMediaType(MediaType mediaType) {
		switch (mediaType) {
		case IMAGE:
			return R.array.image;
		case MUSIC:
			return R.array.music;
		case VIDEO:
			return R.array.video;
		default:
			return -1;
		}
	}

	/**
	 * get the file name filter by the specified media type.
	 * 
	 * @param mediaType
	 *            the media type, one of IMAGE, MUSIC or VIDEO.
	 * @return an array of the objects typed FilenameFilter, used to filter the
	 *         media files.
	 */
	private FilenameFilter[] getFilenameFilter(MediaType mediaType) {
		Resources resources = mContext.getResources();
		FilenameFilter[] filters = null;
		int resourceId = getResourIdByMediaType(mediaType);
		if (resourceId != -1) {
			String[] fileTypes = resources.getStringArray(resourceId);
			filters = new FilenameFilter[fileTypes.length];

			int i = 0;
			for (final String type : fileTypes) {
				filters[i] = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						int extIndex = name.lastIndexOf(".");
						String extName = (extIndex > 0 ? name
								.substring(extIndex + 1) : "");
						return extName.equalsIgnoreCase(type);
					}
				};
				i++;
			}
		}
		return filters;
	}

	/**
	 * find the media folders.
	 * 
	 * @param mediaType
	 *            the media type, one of IMAGE, MUSIC or VIDEO.
	 * @param storagePath
	 *            the storage path.<br>
	 *            use the following helper functions to get the storage path:<br>
	 *            ZyMedia.getSDCardDirectory();<br>
	 *            ZyMedia.getUSBDirectory();<br>
	 * @return
	 */
	public List<MediaFolder> findMediaFolders(MediaType mediaType,
			File storagePath) {
		FilenameFilter[] filters = getFilenameFilter(mediaType);
		return (List<MediaFolder>) findFilesAndFolders(storagePath, filters);
	}

	/**
	 * to find the folders containing the media by the filename filter.
	 * 
	 * @param directory
	 *            the folder to be searched the media files.
	 * @param filters
	 *            an array of the objects typed FilenameFilter, used to filter
	 *            the media files.
	 * @return a collection of found folders which type are MediaFolder.
	 */
	public Collection<MediaFolder> findFilesAndFolders(File directory,
			FilenameFilter[] filters) {
		Vector<MediaFolder> folders = new Vector<MediaFolder>();
		Vector<File> files = new Vector<File>();

		File[] entries = directory.listFiles();

		if (entries != null) {
			for (File entry : entries) {
				if (filters == null) {
					files.addAll(Arrays.asList(entries));
				} else {
					for (FilenameFilter filefilter : filters) {
						if (filefilter.accept(directory, entry.getName())) {
							files.add(entry);
						}
					}
				}
				if (entry.isDirectory() && !entry.isHidden()) {
					folders.addAll(findFilesAndFolders(entry, filters));
				}
			}
		}

		if (files.size() > 0) {
			File[] f = new File[files.size()];
			files.toArray(f);
			MediaFolder folder = new MediaFolder(directory, f);
			folders.add(folder);
		}
		return folders;
	}

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an
	 * actual file.
	 * 
	 * @param file
	 * @return true if the file is a Symbolic Link
	 * @throws IOException
	 */
	public static boolean isSymlink(File file) throws IOException {
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}
}

