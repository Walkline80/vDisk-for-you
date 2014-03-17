package com.walkline.vdisk;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import localization.vDiskSDKResource;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import org.w3c.dom.Document;

import com.walkline.app.vDiskAppConfig;
import com.walkline.screen.PleaseWaitPopupScreen;
import com.walkline.util.Enumerations.SectionSize;
import com.walkline.util.Enumerations.StorageType;
import com.walkline.util.Enumerations.ThumbnailSize;
import com.walkline.util.FileUtility;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.util.network.HttpClient;
import com.walkline.util.network.ThreadResponse;
import com.walkline.util.ui.BrowserScreen;
import com.walkline.vdisk.dao.vDiskDirectLink;
import com.walkline.vdisk.dao.vDiskFolder;
import com.walkline.vdisk.dao.vDiskQuotaInfo;
import com.walkline.vdisk.dao.vDiskUploadFileInfo;
import com.walkline.vdisk.dao.vDiskUploadFilePartSign;
import com.walkline.vdisk.dao.vDiskUser;
import com.walkline.vdisk.inf.DirectLink;
import com.walkline.vdisk.inf.Folder;
import com.walkline.vdisk.inf.QuotaInfo;
import com.walkline.vdisk.inf.UploadFileInfo;
import com.walkline.vdisk.inf.UploadFilePartSign;
import com.walkline.vdisk.inf.User;

public class vDiskSDK implements vDiskSDKResource
{
	//private static final int REVISION_DEFAULT_LIMIT = 10;
	//private static final int SEARCH_DEFAULT_LIMIT = 1000;
	//public static final String DOWNLOAD_TEMP_FILE_SUFFIX = ".vdisktemp";

	//protected Logger log = Logger.getLogger(getClass());

	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	protected Object ACCESS_TOKEN_LOCK = new Object();

	private static final String NULL = "";
	protected ApplicationSettings appSettings;
	protected ConnectionFactory cf;
	//protected LoggableConnectionFactory lcf;
	protected ConnectionFactory lcf;
	protected HttpClient http;
	protected PleaseWaitPopupScreen _waitScreen = null;

	protected String _accessToken;
	protected String _id = "";
	protected String _pwd = "";
	protected boolean _autoMode = true;
	protected String _root = vDiskConfig.DEFAULT_ROOT;
	protected String _downloadURI = StorageType.DEFAULT_URI;
	protected boolean _safeMode = false;
	protected boolean _postMethod = false;
	protected int _maxUploadSize = vDiskConfig.MAX_UPLOAD_SIZE;
	protected int _uploadSectionSize = vDiskConfig.UPLOAD_DEFAULT_SECTION_SIZE;
	protected int _imagePreviewSize = 4;

	public static vDiskSDK getInstance(ApplicationSettings pAppSettings)
	{
		return new vDiskSDK(pAppSettings);
	}

	protected vDiskSDK(ApplicationSettings pAppSettings)
	{
		appSettings = pAppSettings;
		cf = new ConnectionFactory();
		cf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		cf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		cf.setTimeoutSupported(true);
		cf.setAttemptsLimit(10);
		cf.setRetryFactor(2000);
		cf.setConnectionTimeout(120000);
		//lcf = new LoggableConnectionFactory();
		lcf = new ConnectionFactory();
		lcf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		lcf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		lcf.setTimeoutSupported(true);
		lcf.setAttemptsLimit(10);
		lcf.setRetryFactor(2000);
		lcf.setConnectionTimeout(120000);

		http = new HttpClient(cf);
	}

	public void setDownloadURI(String uri) {_downloadURI = uri;}
	
	public String getDownloadURI() {return _downloadURI;}
	
	public void setRoot(String root) {_root = root;}
	
	public String getRoot() {return _root;}
	
	public void setAutoMode(boolean autoMode, String userName, String password)
	{
		_autoMode = autoMode;
		_id = userName;
		_pwd = password;
	}
	
	public boolean isAutoMode() {return _autoMode;}

	public void setSafeMode(boolean safeMode) {_safeMode = safeMode;}
	
	public boolean isSafeMode() {return _safeMode;}

	public void setPostMethod(boolean postMethod) {_postMethod = postMethod;}
	
	public boolean isPostMethod() {return _postMethod;}

	public void setMaxUploadFileSize(int size) {_maxUploadSize = size;}
	
	public int getMaxUploadFileSize() {return _maxUploadSize;}
	
	public void setUploadSectionSize(int size) {_uploadSectionSize = size;}
	
	public int getUploadSectionSize() {return SectionSize.choicesSectionValue[_uploadSectionSize];}
	
	public void setPreviewSize(int size) {_imagePreviewSize = size;}
	
	public String getPreviewSize() {return ThumbnailSize.choicesPreviewValue[_imagePreviewSize];}

	public String getAccessToken() {return _accessToken;}

	public void setAccessToken(String accessToken) {_accessToken = accessToken;}
	
	public boolean hasAccessToken()
	{
		String at = getAccessToken();
		
		if ((at == null) || at.trim().equals(""))
		{
			return false;
		} else {
			return true;
		}
	}

	public void refreshAccessToken(final boolean force) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK)
		{
			if (force) // || !isAccessTokenValid()) {
			{
				setAccessToken(null);
				if (Application.isEventDispatchThread())
				{
					UiApplication.getUiApplication().pushModalScreen(new LoginScreen());

					synchronized (ACCESS_TOKEN_LOCK) {ACCESS_TOKEN_LOCK.notify();}
				} else {
					UiApplication.getApplication().invokeLater(new Runnable() {
						public void run() {
							UiApplication.getUiApplication().pushModalScreen(new LoginScreen());

							synchronized (ACCESS_TOKEN_LOCK) {ACCESS_TOKEN_LOCK.notify();}
						}
					});
				}
			}
		}
	}

	public StringBuffer checkResponse(StringBuffer res) throws vDiskException
	{
		StringBuffer result = null;

		try {
			if ((res != null) && (res.length() > 0)) { //&& (res.length() < 500)) {
				if ((res.charAt(0) == '{') && (res.charAt(res.length() - 1) == '}'))
				{
					JSONObject jo = new JSONObject(new JSONTokener(res.toString()));
					if ((jo != null) && jo.has("error")) {
						JSONObject error = jo.getJSONObject("error");
						String errorType = error.optString("type");
						String errorMessage = error.optString("message");
						if ((errorType != null) && errorType.trim().equals("OAuthException")) {
							throw new OAuthException(errorMessage);
						} else {
							throw new UnknownException(errorMessage);
						}
					}
				}
			}
		} catch (JSONException e) {}

		result = res;

		return result;
	}
	
	private JSONObject doRequest(String methond, String api, Hashtable args, String root, String path) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK) {
			if (!hasAccessToken())
			{
				refreshAccessToken(true);
				try {
					ACCESS_TOKEN_LOCK.wait(vDiskConfig.AUTHORIZE_TIMEDOUT);
				} catch (InterruptedException e) {}
			}
		}

		if (!hasAccessToken())
		{
			final Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
			if (activeScreen instanceof LoginScreen)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						UiApplication.getUiApplication().popScreen(activeScreen);
					}
				});
			}

			throw new vDiskException(getResString(MESSAGE_ERROR_UNABLE_REFRESH_ACCESSTOKEN));
		}

		JSONObject result = null;
		StringBuffer responseBuffer = null;

		if (args.get("access_token") == null) {args.put("access_token", getAccessToken());}

		try {
			if (methond.equals(HttpConnection.POST))
			{
				responseBuffer = checkResponse(http.doPost(api + root + path, args));	
			} else {
				responseBuffer = checkResponse(http.doGet(api + root + path, args));
			}

			if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {result = null;}

			result = new JSONObject(new JSONTokener(responseBuffer.toString()));
			checkErrorCode(result);
		//} catch (OAuthException e) {
		//	if (retry) {
		//		refreshAccessToken(true);
		//		result = doRequest(methond, api, args, root, path, false);
		//	}
		} catch (Exception e) {
			throw new vDiskException(e.getMessage());
		} catch (Throwable t) {
			throw new vDiskException(t.getMessage());
		}

		return result;
	}

	public String doRequestString(String url) throws vDiskException
	{
		return doRequestString(NULL, null, NULL, url);
	}

	private String doRequestString(String api, Hashtable args, String root, String path) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK) {
			if (!hasAccessToken())
			{
				refreshAccessToken(true);
				try {
					ACCESS_TOKEN_LOCK.wait(vDiskConfig.AUTHORIZE_TIMEDOUT);
				} catch (InterruptedException e) {}
			}
		}

		if (!hasAccessToken())
		{
			final Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
			if (activeScreen instanceof LoginScreen)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						UiApplication.getUiApplication().popScreen(activeScreen);
					}
				});
			}

			throw new vDiskException(getResString(MESSAGE_ERROR_UNABLE_REFRESH_ACCESSTOKEN));
		}

		String result = null;
		StringBuffer responseBuffer = null;
		
		try {
			if (args.get("access_token") == null) {args.put("access_token", getAccessToken());}
			responseBuffer = http.doGet(api + root + path, args);

			if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {result = null;}

			try {
				JSONObject errorResult = new JSONObject(new JSONTokener(responseBuffer.toString()));
				checkErrorCode(errorResult);
			} catch (JSONException e) {
				result = responseBuffer.toString();
			}
		//} catch (OAuthException e) {
		//	if (retry) {
		//		refreshAccessToken(true);
		//		result = doRequestString(api, args, root, path, false);
		//	}
		} catch (Exception e) {
			throw new vDiskException(e.getMessage());
		} catch (Throwable t) {
			throw new vDiskException(t.getMessage());
		}

		return result;
	}
	
	public byte[] doRequestRAW(String url, boolean relative) throws vDiskException
	{
		return doRequestRAW(NULL, null, NULL, url, relative);
	}
	
	public byte[] doRequestRAW(String api, Hashtable args, String root, String path, boolean relative) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK) {
			if (!hasAccessToken())
			{
				refreshAccessToken(true);
				try {
					ACCESS_TOKEN_LOCK.wait(vDiskConfig.AUTHORIZE_TIMEDOUT);
				} catch (InterruptedException e) {}
			}
		}

		if (!hasAccessToken())
		{
			final Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
			if (activeScreen instanceof LoginScreen)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						UiApplication.getUiApplication().popScreen(activeScreen);
					}
				});
			}

			throw new vDiskException(getResString(MESSAGE_ERROR_UNABLE_REFRESH_ACCESSTOKEN));
		}

		byte[] result = null;
		StringBuffer responseBuffer = null;
		
		try {
			if (relative)
			{
				if (args.get("access_token") == null) {args.put("access_token", getAccessToken());}
				if (!path.startsWith("/")) {path = "/" + path;}
				
				responseBuffer = checkResponse(http.doGet(api + root + path, args));			
			} else {
				responseBuffer = checkResponse(http.doGet(path, args));				
			}

			if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {result = null;}

			result = responseBuffer.toString().getBytes();
		//} catch (OAuthException e) {
		//	if (retry) {
		//		refreshAccessToken(true);
		//		result = doRequestRAW(api, args, root, path, relative, false);
		//	}
		} catch (Exception e) {
			throw new vDiskException(e.getMessage());
		} catch (Throwable t) {
			throw new vDiskException(t.getMessage());
		}

		return result;
	}

	public UploadFileInfo getMultiPartFileInfo(String path, int partTotal, int fileSize) throws vDiskException
	{
		return getMultiPartFileInfo(path, partTotal, fileSize, null, null);
	}

	private UploadFileInfo getMultiPartFileInfo(final String path, final int partCounts, final int fileSize, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {new Thread() {public void run() {}}.start();
			
			return null;
		} else {
			UploadFileInfo result=null;
			Hashtable args = new Hashtable();
			
			args.put("root", getRoot());
			args.put("path", path);
			args.put("part_total", Integer.toString(partCounts));
			args.put("size", Integer.toString(fileSize));
			args.put("s3host", vDiskConfig.SINA_STORAGE_SERVICE_HOST);
			
			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getMultiPartInitURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = getMultiPartFileInfo(jsonObject, partCounts);
			}
			
			return result;
		}
	}
	
	private UploadFileInfo getMultiPartFileInfo(JSONObject jo, int partCounts) throws vDiskException
	{
		return new vDiskUploadFileInfo(this, jo, partCounts);
	}
	
	public JSONObject completeUploadFileMultiPart(String path, final String uploadID, final String uploadKey, final String md5List) throws vDiskException
	{
		return completeUploadFileMultiPart(path, uploadID, uploadKey, md5List, null, null);
	}

	private JSONObject completeUploadFileMultiPart(final String path, final String uploadID, final String uploadKey, final String md5List, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {new Thread() {public void run() {}}.start();
			
			return null;
		} else {
			JSONObject result=null;
			Hashtable args = new Hashtable();
			
			args.put("root", getRoot());
			args.put("path", path);
			args.put("upload_id", uploadID);
			args.put("upload_key", uploadKey);
			args.put("md5_list", md5List);
			args.put("s3host", vDiskConfig.SINA_STORAGE_SERVICE_HOST);
			
			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getMultiPartCompleteURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0))
			{
				result = null;
			} else {
				result = jsonObject;
			}
			
			return result;
		}
	}
	
	public JSONObject uploadFileMultiPartWithProgress(String filePath, String fileURI) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK) {
			if (!hasAccessToken())
			{
				refreshAccessToken(true);
				try {
					ACCESS_TOKEN_LOCK.wait(vDiskConfig.AUTHORIZE_TIMEDOUT);
				} catch (InterruptedException e) {}
			}
		}

		if (!hasAccessToken())
		{
			final Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
			if (activeScreen instanceof LoginScreen)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						UiApplication.getUiApplication().popScreen(activeScreen);
					}
				});
			}

			throw new vDiskException(getResString(MESSAGE_ERROR_UNABLE_REFRESH_ACCESSTOKEN));
		}

		JSONObject result = null;
		ThreadResponse threadResponse;
		String fileName = FileUtility.getFilename(filePath);

		try {
			_waitScreen = new PleaseWaitPopupScreen(this, filePath, fileName, fileURI);
			threadResponse = _waitScreen.showUploadMultiPart();

			StringBuffer responseBuffer = new StringBuffer();
			if (threadResponse.isValidResponseWithString())
			{
				responseBuffer = checkResponse(new StringBuffer(threadResponse.getResponseString()));
	    	}

			if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {return null;}

			result = new JSONObject(new JSONTokener(responseBuffer.toString()));
			checkErrorCode(result);			
		} catch (Exception e) {
			throw new vDiskException(e.getMessage());
		} catch (Throwable t) {
			throw new vDiskException(t.getMessage());
		}
		
		return result;
	}
	
	public JSONObject uploadFileWithProgress(String filePath, String fileURI) throws vDiskException
	{
		synchronized (ACCESS_TOKEN_LOCK) {
			if (!hasAccessToken())
			{
				refreshAccessToken(true);
				try {
					ACCESS_TOKEN_LOCK.wait(vDiskConfig.AUTHORIZE_TIMEDOUT);
				} catch (InterruptedException e) {}
			}
		}

		if (!hasAccessToken())
		{
			final Screen activeScreen = UiApplication.getUiApplication().getActiveScreen();
			if (activeScreen instanceof LoginScreen)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						UiApplication.getUiApplication().popScreen(activeScreen);
					}
				});
			}

			throw new vDiskException(getResString(MESSAGE_ERROR_UNABLE_REFRESH_ACCESSTOKEN));
		}

		JSONObject result = null;
		String fileName = FileUtility.getFilename(filePath);
		Hashtable data = new Hashtable();
		String api;

		data.put("access_token", getAccessToken());

		try {
			ThreadResponse threadResponse;

			if (isPostMethod())
			{
				if (isSafeMode())
				{
					api = vDiskConfig.getPostFileSafeURL;
				} else {
					api = vDiskConfig.getPostFileURL;
				}

				_waitScreen = new PleaseWaitPopupScreen(api + _root + StringUtility.encode(filePath), data, fileName, fileURI);
	        	threadResponse = _waitScreen.showUploadPOST();
			} else {
				if (isSafeMode())
				{
					api = vDiskConfig.getPutFileSafeURL;
				} else {
					api = vDiskConfig.getPutFileURL;
				}

				_waitScreen = new PleaseWaitPopupScreen(api + _root + StringUtility.encode(filePath), data, fileName, fileURI);
	        	threadResponse = _waitScreen.showUploadPUT();
			}

        	StringBuffer responseBuffer = new StringBuffer();
        	if (threadResponse.isValidResponseWithBytes())
        	{
        		responseBuffer = checkResponse(new StringBuffer(new String(threadResponse.getResponseBytes())));
        	}

			if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {return null;}

			result = new JSONObject(new JSONTokener(responseBuffer.toString()));
			checkErrorCode(result);
		//} catch (OAuthException e) {
		//	if (retry) {
		//		refreshAccessToken(true);
		//		result = uploadFileWithProgress(filePath, fileURI, false);
		//	}
		} catch (Exception e) {
			throw new vDiskException(e.getMessage());
		} catch (Throwable t) {
			throw new vDiskException(t.getMessage());
		}

		return result;
	}

	public Folder createFolder(String path) throws vDiskException
	{
		return createFolder(path, null, null);
	}

	public Folder createFolder(final String path, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						Folder result = null;
						result = createFolder(path);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}	
				}
			}.start();

			return null;
		} else {
			Folder result = null;
			Hashtable args = new Hashtable();

			args.put("root", getRoot());
			args.put("path", path);

			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getCreateFolderURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = createFolder(jsonObject);
			}
			
			return result;
		}
	}
	
	public Folder createFolder(JSONObject jo) throws vDiskException
	{
		return new vDiskFolder(this, jo);
	}

	public JSONObject moveObject(String fromPath, String toPath) throws vDiskException
	{
		return moveObject(fromPath, toPath, null, null);
	}

	public JSONObject moveObject(final String fromPath, final String toPath, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						JSONObject result = null;
						result = moveObject(fromPath, toPath);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}	
				}
			}.start();

			return null;
		} else {
			JSONObject result = null;
			Hashtable args = new Hashtable();

			args.put("root", getRoot());
			args.put("from_path", fromPath);
			args.put("to_path", toPath);

			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getMoveObjectURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = jsonObject;
			}

			return result;
		}
	}

	public User getAccountInfo() throws vDiskException
	{
		return getAccountInfo(null, null);
	}

	public User getAccountInfo(final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						User result = null;
						result = getAccountInfo();
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}
				}
			}.start();

			return null;
		} else {
			User result = null;
			Hashtable args = new Hashtable();

			JSONObject jsonObject = doRequest(HttpConnection.GET, vDiskConfig.getAccountInfoURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = getAccountInfo(jsonObject);
			}

			return result;
		}
	}

	public User getAccountInfo(JSONObject jo) throws vDiskException
	{
		return new vDiskUser(this, jo);
	}

	public void downloadFileWithProgress(String path) throws vDiskException
	{
		downloadFileWithProgress(path, null, null);
	}

	public void downloadFileWithProgress(String path, final AsyncCallback listener, final Object state)throws vDiskException
	{
		if (listener != null) {new Thread() {public void run() {}}.start();
		
			return;
		} else {
			_waitScreen = new PleaseWaitPopupScreen(this, path);
			ThreadResponse threadResponse = _waitScreen.showDownload();

			if (threadResponse.isValidResponseWithString())
			{
				Function.errorDialog(threadResponse.getResponseString());
			}
		}
	}

	/**
	 * Get file/folder information
	 * @param path The file/folder path
	 * @param list Determine whether to display the contents of a folder
	 * @param inlcludeDeleted Determine whether to display the deleted contents
	 * @return A json object contains file/folder information
	 * @throws vDiskException
	 */
	public JSONObject getMetaData(String path, boolean list, boolean includeDeleted) throws vDiskException
	{
		return getMetaData(path, list, includeDeleted, null, null);
	}

	/**
	 * Get file/folder information with callback
	 * @param path The file/folder path
	 * @param list Determine whether to display the contents of a folder
	 * @param inlcludeDeleted Determine whether to display the deleted contents
	 * @param listener Callback listener
	 * @param state Useless
	 * @return A json object contains file/folder information
	 * @throws vDiskException
	 */
	public JSONObject getMetaData(final String path, final boolean list, final boolean includeDeleted, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						JSONObject result = null;
						result = getMetaData(path, list, includeDeleted);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}
				}
			}.start();

			return null;
		} else {
			JSONObject result=null;
			Hashtable args = new Hashtable();

			args.put("list", String.valueOf(list));
			args.put("include_deleted", String.valueOf(includeDeleted));

			JSONObject jsonObject = doRequest(HttpConnection.GET, vDiskConfig.getMetaDataURL, args, _root, StringUtility.encode(path));
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = jsonObject;
			}

			return result;
		}
	}

	public JSONObject shareFile(String path, boolean cancel) throws vDiskException
	{
		return shareFile(path, cancel, null, null);
	}

	public JSONObject shareFile(final String path, final boolean cancel, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						JSONObject result = null;
						result = shareFile(path, cancel);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}	
				}
			}.start();
			
			return null;
		} else {
			JSONObject result=null;
			Hashtable args = new Hashtable();
			
			args.put("cancel", String.valueOf(cancel));
			
			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getShareFileURL, args, _root, StringUtility.encode(path));
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = jsonObject;
			}
			
			return result;
		}
	}
	
	public DirectLink getFileDirectLink(String path) throws vDiskException
	{
		return getFileDirectLink(path, null, null);
	}

	public DirectLink getFileDirectLink(final String path, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						DirectLink result = null;
						result = getFileDirectLink(path);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}	
				}
			}.start();
			
			return null;
		} else {
			DirectLink result=null;
			Hashtable args = new Hashtable();
			
			JSONObject jsonObject = doRequest(HttpConnection.GET, vDiskConfig.getFileDirectLinkURL, args, _root, StringUtility.encode(path));
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = getFileDirectLink(jsonObject);
			}
			
			return result;
		}
	}
	
	public DirectLink getFileDirectLink(JSONObject jo) throws vDiskException
	{
		return new vDiskDirectLink(this, jo);
	}
	
	public String getPreviewImageUrl(String path) throws vDiskException
	{
		return getPreviewImageUrl(path, null, null);
	}

	public String getPreviewImageUrl(final String path, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						String result = null;
						result = getPreviewImageUrl(path);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}	
				}
			}.start();
			
			return null;
		} else {
			String result=null;
			Hashtable args = new Hashtable();
			
			args.put("size", getPreviewSize());
			
			String url = doRequestString(vDiskConfig.getThumbnailURL, args, _root, StringUtility.encode(path));
			if ((url == null) || (url.equals("") || url.length() < 0)) {
				result = null;
			} else {
				result = url;
			}
			
			return result;
		}
	}
	
	public JSONObject deleteObject(String path) throws vDiskException
	{
		return deleteObject(path, null, null);
	}

	public JSONObject deleteObject(final String path, final AsyncCallback listener, final Object state) throws vDiskException
	{
		if (listener != null) {
			new Thread() {
				public void run() {
					try {
						JSONObject result = null;
						result = deleteObject(path);
						listener.onComplete(result, null);
					} catch (Exception e) {
						listener.onException(e, null);
					}
				}
			}.start();
			
			return null;
		} else {
			JSONObject result=null;
			Hashtable args = new Hashtable();

			args.put("root", getRoot());
			args.put("path", path);

			JSONObject jsonObject = doRequest(HttpConnection.POST, vDiskConfig.getDeleteObjectURL, args, NULL, NULL);
			if ((jsonObject == null) || (jsonObject.length() <= 0)) {
				result = null;
			} else {
				result = jsonObject;
			}
			
			return result;
		}
	}
	
	public QuotaInfo getQuotaInfo(JSONObject jo) throws vDiskException
	{
		return new vDiskQuotaInfo(this, jo);
	}

	public UploadFilePartSign getUploadFilePartSign(JSONObject jo) throws vDiskException
	{
		return new vDiskUploadFilePartSign(this, jo);
	}

	private void checkErrorCode(JSONObject result) throws vDiskException
	{
		if (!result.optString("error_code").equals(""))
		{
			String request=result.optString("request");
			String code=result.optString("error_code");
			String detailCode=result.optString("error_detail_code");
			String error=result.optString("error");

			StringBuffer buffer = new StringBuffer();
			String retry = result.optString("retry");

			if (retry != null && !retry.equals("") && retry.length() > 0)
			{
				buffer.append("\n\nData: {");
				buffer.append("\n\tRetry: ").append(request);
				buffer.append("\n}");
			} else {
				JSONArray data = result.optJSONArray("data");
				
				if (data != null && data.length() > 0)
				{
					buffer.append("\n\nData: [");
					for (int i=0; i<data.length(); i++)
					{
						try {
							JSONObject dataObject = data.getJSONObject(i);

							String partNumber = dataObject.optString("part_number");
							String md5 = dataObject.optString("md5");
							
							buffer.append("\n{");
							buffer.append("\n\tpart_number: ").append(partNumber);
							buffer.append("\n\tmd5: ").append(md5);
							
							JSONObject s3_md5 = dataObject.optJSONObject("s3_md5");
							
							Enumeration keys = s3_md5.keys();
							while (keys.hasMoreElements())
							{
								String key = (String) keys.nextElement();
								boolean status = s3_md5.optBoolean(key);
								
								buffer.append("\n\ts3_md5: ");
								buffer.append(key).append(":").append(status);
							}
							
							buffer.append("\n}");
						} catch (JSONException e) {}
					}
				}
			
			}
			
			String dataString = (buffer != null) ? buffer.toString() : "";
			throw new vDiskException("\n\nrequest: " + request +
									 "\nerror_code: " + code +
									 "\nerror_detail_code: " + detailCode +
									 "\nerror: " + error +
									 dataString); 
									}
	}
	
	protected class LoginScreen extends BrowserScreen
	{
		protected LoginScreen()
		{
			super(vDiskConfig.authorizeURL + "?client_id=" + vDiskConfig.client_ID + "&redirect_uri=" + vDiskConfig.redirect_URI + "&display=mobile&response_type=code", lcf);
		}

		protected boolean hasAccessToken(String pUrl)
		{
			boolean result = false;
			String token = getAccessTokenFromUrl(pUrl);
			
			if ((token == null) || token.trim().equals(""))
			{
				//log.info("Access Token not found.");
				result = false;
			} else {
				//log.info("Access Token found !!!");
				setAccessToken(token);
				dismiss();
				result = true;
			}
			return result;
		}

		protected boolean shouldFetchContent(BrowserFieldRequest request)
		{
			return !hasAccessToken(request.getURL());
		}

		protected boolean shouldShowContent(BrowserField pbf, Document pdoc)
		{
			return !hasAccessToken(pdoc.getDocumentURI());
		}

		protected boolean postProcessing(BrowserField pbf, Document pdoc) 
		{
			if (isAutoMode())
			{
				if ((pdoc != null))
				{
					String jsFillId = "document.forms[0].elements['userid'].value = '" + _id + "';";
					String jsFillPwd = "document.forms[0].elements['password'].value = '" + _pwd + "';";
					String jsLogin = "setTimeout(\"var obj=document.getElementsByTagName('a')[0]; var evt_l = document.createEvent('MouseEvents'); evt_l.initEvent('click', true, true); obj.dispatchEvent(evt_l);\",3000);";
					String jsStart = "setTimeout(\"var obj1=document.getElementsByTagName('a')[0]; var evt_s = document.createEvent('MouseEvents'); evt_s.initEvent('click', true, true); obj1.dispatchEvent(evt_s);\",3000);";

					try {
						//log.info("try to auto login with fill username and password");
						pbf.executeScript(jsFillId);
						pbf.executeScript(jsFillPwd);
						//pbf.executeScript(jsLogin);
						//log.info("auto login with fill username and password success!");
					} catch (Exception e) {
						try {
							//log.info("auto login failed, try to login with click \"Start Using\"");
							pbf.executeScript(jsStart);
							//log.info("login with click \"Start Using\" success!");
						} catch (Exception e2) {}
					}
				} else {
					Function.errorDialog("pdoc == null");
					//log.error("AutoMode: Loaded document is null.");
				}
			}

			return true;
		}

		protected String getAccessTokenFromUrl(String url)
		{
			String code = null;
			String at = null;

			if ((url != null) && !url.trim().equals(""))
			{
				int startIndex = url.indexOf("#access_token=");
				if (startIndex > -1)
				{
					startIndex++;
					int stopIndex = url.length();
					if (url.indexOf('&', startIndex) > -1) 
					{
						stopIndex = url.indexOf('&', startIndex);
					} else if (url.indexOf(';', startIndex) > -1) {
						stopIndex = url.indexOf(';', startIndex);
					}
					at = url.substring(url.indexOf('=', startIndex) + 1, stopIndex);
				} else {
					startIndex = url.indexOf("?code=");
					if (startIndex > -1) {
						startIndex++;
						int stopIndex = url.length();
						if (url.indexOf('&', startIndex) > -1) {
							stopIndex = url.indexOf('&', startIndex);
						} else if (url.indexOf(';', startIndex) > -1) {
							stopIndex = url.indexOf(';', startIndex);
						}
						code = url.substring(url.indexOf('=', startIndex) + 1, stopIndex);
						at = getAccessTokenFromCode(code);
					}
				}
			}

			return at;
		}

		protected String getAccessTokenFromCode(String pCode)
		{
			JSONObject result = null;
			String at = null;

			if ((pCode == null) || pCode.trim().equals("")) {return null;}

			Hashtable args = new Hashtable();
			args.put("client_id", vDiskConfig.client_ID);
			args.put("client_secret", vDiskConfig.client_SERCRET);
			args.put("grant_type", "authorization_code");
			args.put("redirect_uri", vDiskConfig.redirect_URI);
			args.put("code", pCode);

			try {
				StringBuffer responseBuffer = http.doPost(vDiskConfig.accessTokenURL, args);

				if ((responseBuffer == null) || (responseBuffer.length() <= 0)) {at = null;}

				result = new JSONObject(new JSONTokener(responseBuffer.toString()));
				checkErrorCode(result);
			} catch (Exception e) {
				e.printStackTrace();

			} catch (Throwable t) {
				t.printStackTrace();
			}

			return result.optString("access_token");
		}
	}

	private String getResString(int key) {return _bundle.getString(key);}
}