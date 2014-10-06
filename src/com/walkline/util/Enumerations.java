package com.walkline.util;

public class Enumerations
{
	/**
	 * An enumeration of file actions for {@linkplain com.blackberry.util.ui.ListStyleButtonField ListStyleButtonField}
	 */
	public class FileAction
	{
		public static final int POPUPMENU = 0;
		public static final int DOWNLOAD = 1;
		public static final int PREVIEW = 2;
		public static final int PROPERTY = 3;
		public static final int SHARE = 4;
		public static final int UNSHARE = 5;
		public static final int GETDIERCTLINK = 6;

		public static final int DEFAULT_ACTION = POPUPMENU;
	}

	/**
	 * An enumeration of preview thumbnails size
	 */
	public static class ThumbnailSize
	{
		public static final String[] choicesPreviewSize = {"32 x 32", "60 x 60", "100 x 100", "191 x 191", "640 x 480", "1024 x 768"};
		public static final String[] choicesPreviewValue = {"small", "s", "m", "large", "l", "xl"};

		public static final int SIZE_32x32 = 0;
		public static final int SIZE_60x60 = 1;
		public static final int SIZE_100x100 = 2;
		public static final int SIZE_191x191 = 3;
		public static final int SIZE_640x480 = 4;
		public static final int SIZE_1024x768 = 5;

		public static final int DEFAULT_SIZE = 4;
	}

	public static class UploadMethod
	{
		public static final String[] choicesUploadMethod = {"PUT", "POST"};

		public static final int PUT = 0;
		public static final int POST = 1;

		public static final int DEFAULT_METHOD = PUT;
	}

	public static class ShortcutKey
	{
		public static final String[] choicesShortcutKeys = {"", "E", "F", "G", "I", "J", "K", "Q", "R", "W", "X", "Y", "Z"};
		
		public static final int DEFAULT_KEY = 6;
	}

	public static class SectionSize
	{
		public static final String[] choicesSectionSize = {"0.95 MB", "1 MB", "2 MB", "4 MB"};
		public static final int[] choicesSectionValue = {1000*1000, 1024*1024, 2*1024*1024, 4*1024*1024};

		public static final int SIZE_950KB = 0;
		public static final int SIZE_1M = 1;
		public static final int SIZE_2M = 2;
		public static final int SIZE_4M = 3;

		public static final int DEFAULT_SIZE = SIZE_950KB;
	}

	public static class StorageType
	{
		public static final String[] choicesDownloadUri = {"file:///store/home/user/vDisk/Download/", "file:///SDCard/BlackBerry/vDisk/Download/"};

		public static final int STORE = 0;
		public static final int SDCARD = 1;

		public static final int DEFAULT_TYPE = STORE;
		public static final String DEFAULT_URI = choicesDownloadUri[DEFAULT_TYPE];
	}
}