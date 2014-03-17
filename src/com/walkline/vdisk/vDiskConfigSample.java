package com.walkline.vdisk;

public class vDiskConfigSample //Chage to vDiskConfig
{
	public static final String client_ID = "";   //App Key
	public static final String client_SERCRET = "";    //App Secret
	public static final String redirect_URI = "";    //Your URL
	public static String SINA_STORAGE_SERVICE_HOST = "up.sinastorage.com";

	/**
	 * Method: <b>POST</b>
	 */
	public static final String accessTokenURL = "https://auth.sina.com.cn/oauth2/access_token";
	public static final String authorizeURL = "https://auth.sina.com.cn/oauth2/authorize";
	public static final String getAccountInfoURL = "https://api.weipan.cn/2/account/info";
	public static final String getPostFileURL = "http://upload-vdisk.sina.com.cn/2/files/";
	public static final String getPostFileSafeURL = "https://upload-vdisk.sina.com.cn/2/files/";
	public static final String getPutFileURL = "http://upload-vdisk.sina.com.cn/2/files_put/";
	public static final String getPutFileSafeURL = "https://upload-vdisk.sina.com.cn/2/files_put/";

	/**
	 * Method: <b>GET</b>
	 */
	public static final String getMetaDataURL = "https://api.weipan.cn/2/metadata/";
	public static final String getCreateFolderURL = "https://api.weipan.cn/2/fileops/create_folder";
	public static final String getDeleteObjectURL = "https://api.weipan.cn/2/fileops/delete";
	public static final String getDownloadFileURL = "https://api.weipan.cn/2/files/";
	public static final String getShareFileURL = "https://api.weipan.cn/2/shares/";

	/**
	 * Method: <b>POST</b>
	 */
	public static final String getMoveObjectURL = "https://api.weipan.cn/2/fileops/move";

	/**
	 * Method: <b>GET</b>
	 */
	public static final String getThumbnailURL = "https://api.weipan.cn/2/thumbnails/";

	/**
	 * Method: <b>GET</b>
	 */
	public static final String getFileDirectLinkURL = "https://api.weipan.cn/2/media/";

	/**
	 * Method: <b>POST</b>
	 */
	public static final String getMultiPartInitURL = "https://api.weipan.cn/2/multipart/init/";

	/**
	 * Method: <b>POST</b>
	 */
	public static final String getMultiPartCompleteURL = "https://api.weipan.cn/2/multipart/complete";

	public static final String ROOT_BASIC = "basic";
	public static final String ROOT_SANDBOX = "sandbox";
	public static final String DEFAULT_ROOT = ROOT_BASIC;

	public static final int MAX_UPLOAD_SIZE = 500 * 1024 * 1024; // 500 MB
	public static final int UPLOAD_DEFAULT_SECTION_SIZE = 1 * 1000 * 1000; // 0.95 MB
	public static final int AUTHORIZE_TIMEDOUT = 2 * 60 * 1000; // 2 minute
}