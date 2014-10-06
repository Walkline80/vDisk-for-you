package com.walkline.app;

import java.util.Vector;

import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.util.Persistable;

import com.walkline.util.Enumerations.FileAction;
import com.walkline.util.Enumerations.SectionSize;
import com.walkline.util.Enumerations.ShortcutKey;
import com.walkline.util.Enumerations.StorageType;
import com.walkline.util.Enumerations.ThumbnailSize;
import com.walkline.util.Enumerations.UploadMethod;
import com.walkline.vdisk.vDiskConfig;
import com.walkline.vdisk.vDiskSDK;

public class vDiskAppConfig implements Persistable
{
	public static final String APP_NAME = "vDisk for ";
	public static final String APP_TITLE = "vDisk for you";
	public static final String UNDERLINE = "\u0332";
	public static final String BBW_APPID = "48696893";
	//3e95827b174ebfc0 vdisk_for_you_written_by_walkline_wang

	public static int[] preferredTransportTypes = {TransportInfo.TRANSPORT_TCP_WIFI, TransportInfo.TRANSPORT_TCP_CELLULAR, TransportInfo.TRANSPORT_WAP2};
	public static int[] disallowedTransportTypes = {TransportInfo.TRANSPORT_BIS_B, TransportInfo.TRANSPORT_MDS, TransportInfo.TRANSPORT_WAP};

	public static final Background bgColor_Gradient=BackgroundFactory.createLinearGradientBackground(Color.GRAY, Color.GRAY, Color.BLACK, Color.BLACK);
	public static final Background bgColor_GradientBlue=BackgroundFactory.createLinearGradientBackground(0x4992ce, 0x4992ce, 0x05549d, 0x05549d);
	public static final Border border_Transparent=BorderFactory.createRoundedBorder(new XYEdges(16,16,16,16), Color.BLACK, 0, Border.STYLE_FILLED);
	public static final Background bg_Transparent=BackgroundFactory.createSolidTransparentBackground(Color.BLACK, 0);
	public static final Border border_popup_Transparent=BorderFactory.createRoundedBorder(new XYEdges(16,16,16,16), Color.BLACK, 200, Border.STYLE_FILLED);
	public static final Background bg_popup_Transparent=BackgroundFactory.createSolidTransparentBackground(Color.BLACK, 200);

	public static final Font FONT_LIST_TITLE = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt), Ui.UNITS_pt);
	public static final Font FONT_LIST_DESCRIPTION = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt);
	//public static final Font FONT_DATE_NORMAL = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt);
	
	public static final Font FONT_SETTINGS_TITLE = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt);
	public static final Font FONT_SETTINGS_DESCRIPTION = Font.getDefault().derive(Font.ITALIC, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt);

	public static final Font FONT_ABOUT_TITLE = Font.getDefault().derive(Font.BOLD, Font.getDefault().getHeight(Ui.UNITS_pt)+2, Ui.UNITS_pt);
	public static final Font FONT_ABOUT_HEADLINE = Font.getDefault().derive(Font.BOLD | Font.ITALIC, Font.getDefault().getHeight(Ui.UNITS_pt), Ui.UNITS_pt);
	public static final Font FONT_ABOUT_SMALL = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt);
	public static final Font FONT_ABOUT_LARGE = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)+1, Ui.UNITS_pt);

	public static final Font FONT_MAIN_TITLE = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)+2, Ui.UNITS_pt);

	//Elements and Keys
	private Vector _elements;
	private static final int AUTO_LOGIN = 0;
	private static final int SAFE_MODE = 1;
	private static final int OVERWRITE = 2;
	private static final int UPLOAD_METHOD = 3;
	private static final int SECTION_SIZE = 4;
	private static final int DOWNLOAD_LOCATION = 5;
	private static final int DOWNLOAD_URI = 6;
	private static final int PREVIEW_SIZE = 7;
	private static final int USERNAME = 8;
	private static final int PASSWORD = 9;
	private static final int SHORTCUT_KEY = 10;
	private static final int FILE_ACTION = 11;
	private static final int ACCESS_TOKEN = 12;
	private static final int REFRESH_TOKEN = 13;
	private static final int EXPIRES_IN = 14;

	//Default values
	private final int MAXUPLOADFILESIZE = 20 * 1024 * 1024; //20MB
	private final int FALSE = 0;
	private final int TRUE = 1;
	private final String ROOT = vDiskConfig.DEFAULT_ROOT;
	private final String DOWNLOADURI = StorageType.DEFAULT_URI;
	private final int PREVIEWSIZE = ThumbnailSize.DEFAULT_SIZE;
	private final int UPLOADSECTIONSIZE = SectionSize.DEFAULT_SIZE;

	//Persistent objects
	private static Vector _data;
	private static PersistentObject _store;

	private boolean _autoMode = true;
	private boolean _safeMode = false;
	private boolean _overWrite = true;
	private int _uploadMethod = UploadMethod.DEFAULT_METHOD;
	private int _sectionSize = SectionSize.DEFAULT_SIZE;
	private int _downloadLocation = StorageType.DEFAULT_TYPE;
	private int _previewSize = ThumbnailSize.DEFAULT_SIZE;
	private int _shortcutKey = ShortcutKey.DEFAULT_KEY;
	private int _fileAction = FileAction.DEFAULT_ACTION;
	private String _downloadURI = StorageType.DEFAULT_URI;
	private String _username = "";
	private String _password = "";
	private String _accessToken = "";
	private String _refresh_Token = "";
	private long _expiresIn = 0;

	public vDiskAppConfig()
	{
		_elements = new Vector(15);

		for (int i=0; i<_elements.capacity(); i++)
		{
			_elements.addElement(new Object()); //(""));
		}
	}

	private boolean getElementBoolean(int id)
	{
		boolean result = false;
		Object valueObject = _elements.elementAt(id); 

		if (valueObject instanceof Boolean)
		{
			result = ((Boolean) valueObject).booleanValue();
		}

		return result;
	}

	private int getElementInt(int id) {return Integer.parseInt(_elements.elementAt(id).toString());}

	private long getElementLong(int id) {return Long.parseLong(_elements.elementAt(id).toString());}

	private String getElementString(int id) {return (String) _elements.elementAt(id);}

	private void setElement(int id, Object value) {_elements.setElementAt(value, id);}

	public void setAutoMode(boolean value)
	{
		setElement(AUTO_LOGIN, new Boolean(value));
		_autoMode = value;
	}

	public boolean isAutoMode() {return _autoMode;}

	public void setSafeMode(boolean value) 
	{
		setElement(SAFE_MODE, new Boolean(value));
		_safeMode = value;
	}

	public boolean isSafeMode() {return _safeMode;}

	public void setOverwrite(boolean value)
	{
		setElement(OVERWRITE, new Boolean(value));
		_overWrite = value;
	}

	public boolean isOverwrite() {return _overWrite;}

	public void setUploadMethod(int value)
	{
		setElement(UPLOAD_METHOD, new Integer(value));
		_uploadMethod = value;
	}

	public int getUploadMethod() {return _uploadMethod;}

	public void setSectionSize(int value)
	{
		setElement(SECTION_SIZE, new Integer(value));
		_sectionSize = value;
	}

	public int getSectionSize() {return _sectionSize;}

	public void setDownloadLocation(int value)
	{
		setElement(DOWNLOAD_LOCATION, new Integer(value));
		_downloadLocation = value;
	}

	public int getDownloadLocation() {return _downloadLocation;}

	public void setPreviewSize(int value)
	{
		setElement(PREVIEW_SIZE, new Integer(value));
		_previewSize = value;
	}

	public int getPreviewSize() {return _previewSize;}

	public void setShortcutKey(int value)
	{
		setElement(SHORTCUT_KEY, new Integer(value));
		_shortcutKey = value;
	}

	public int getShortcutKey() {return _shortcutKey;}

	public void setFileAction(int value)
	{
		setElement(FILE_ACTION, new Integer(value));
		_fileAction = value;
	}

	public int getFileAction() {return _fileAction;}

	public void setUsername(String value)
	{
		setElement(USERNAME, new String(value));
		_username = value;
	}

	public String getUsername() {return _username;}

	public void setAccessToken(String value)
	{
		setElement(ACCESS_TOKEN, new String(value));
		_accessToken = value;
	}

	public String getAccessToken() {return _accessToken;}

	public void setRefreshToken(String value)
	{
		setElement(REFRESH_TOKEN, new String(value));
		_refresh_Token = value;
	}

	public String getRefreshToken() {return _refresh_Token;}

	public void setExpiresIn(long value)
	{
		setElement(EXPIRES_IN, new Long(value));
		_expiresIn = value;
	}

	public long getExpiresIn() {return _expiresIn;}

	public void setPassword(String value)
	{
		setElement(PASSWORD, new String(value));
		_password = value;
	}

	public String getPassword() {return _password;}

	public void setDownloadURI(String value)
	{
		setElement(DOWNLOAD_URI, new String(value));
		_downloadURI = value;
	}

	public String getDownloadURI() {return _downloadURI;}

	public boolean isAccountEmpty()
	{
		boolean result = false;

		if (_username.equals("") || _username.length() < 0 || _password.equals("") || _password.length() < 0)
		{
			result = true;
		}

		return result;
	}

	public boolean needPopupMenu() {return (_fileAction == FileAction.POPUPMENU);}

	public void clean()
	{
		synchronized (_store)
		{
			_store.setContents(new Vector());
			_store.forceCommit();

			return;
		}
	}

	public void initialize(vDiskSDK vDisk)
	{
		synchronized(_store)
		{
			try {
				_data = (Vector) _store.getContents();

				if (!_data.isEmpty())
				{
					_elements = (Vector) _data.lastElement();

					_username = getElementString(USERNAME);
					_password = getElementString(PASSWORD);

					_accessToken = getElementString(ACCESS_TOKEN);
					_refresh_Token = getElementString(REFRESH_TOKEN);
					_expiresIn = getElementLong(EXPIRES_IN);

					_autoMode = getElementBoolean(AUTO_LOGIN);
					_safeMode = getElementBoolean(SAFE_MODE);
					_overWrite = getElementBoolean(OVERWRITE);

					_downloadLocation = getElementInt(DOWNLOAD_LOCATION);
					_downloadURI = getElementString(DOWNLOAD_URI);
					_uploadMethod = getElementInt(UPLOAD_METHOD);
					_previewSize = getElementInt(PREVIEW_SIZE);
					_shortcutKey = getElementInt(SHORTCUT_KEY);
					_fileAction = getElementInt(FILE_ACTION);
					_sectionSize = getElementInt(SECTION_SIZE);

					if (vDisk != null)
					{
						vDisk.setAutoMode(_autoMode, _username, _password);
						vDisk.setRoot(ROOT);
						vDisk.setDownloadURI(_downloadURI);
						vDisk.setSafeMode(_safeMode);
						vDisk.setPostMethod((_uploadMethod == TRUE));
						//vDisk.setOverwrite(_overWrite);
						vDisk.setPreviewSize(_previewSize);
						vDisk.setUploadSectionSize(_sectionSize);
						vDisk.setAccessToken(_accessToken);
						vDisk.setRefreshToken(_refresh_Token);
						vDisk.setExpiresIn(_expiresIn);
						vDisk.setMaxUploadFileSize(MAXUPLOADFILESIZE);
					}
				} else{
					makeDefaultSettings(vDisk);
				}
			} catch (Exception e) {
				_store.setContents(new Vector());
				_store.forceCommit();
				_data = new Vector();

				makeDefaultSettings(vDisk);
			}
		}
	}

	public void save(vDiskSDK _vDisk, boolean updateSDK)
	{
		setAutoMode(_autoMode);
		setSafeMode(_safeMode);
		setOverwrite(_overWrite);

		setUploadMethod(_uploadMethod);
		setSectionSize(_sectionSize);
		setDownloadLocation(_downloadLocation);
		setPreviewSize(_previewSize);
		setShortcutKey(_shortcutKey);
		setFileAction(_fileAction);

		setUsername(_username);
		setPassword(_password);
		setDownloadURI(_downloadURI);

		setAccessToken(_accessToken);
		setRefreshToken(_refresh_Token);
		setExpiresIn(_expiresIn);

		_data.addElement(_elements);

		synchronized(_store)
		{
			_store.setContents(_data);
			_store.commit();
		}

		if (updateSDK && _vDisk != null)
		{
			_vDisk.setAutoMode(_autoMode, _username, _password);
			_vDisk.setRoot(_vDisk.getRoot());
			_vDisk.setDownloadURI(_downloadURI);
			_vDisk.setSafeMode(_safeMode);
			_vDisk.setPostMethod((_uploadMethod == TRUE));
			//_vDisk.setOverwrite(true);
			_vDisk.setPreviewSize(_previewSize);
			_vDisk.setUploadSectionSize(_sectionSize);
			_vDisk.setAccessToken(_accessToken);
			_vDisk.setRefreshToken(_refresh_Token);
			_vDisk.setExpiresIn(_expiresIn);
			_vDisk.setMaxUploadFileSize(MAXUPLOADFILESIZE);
		}
	}

	private void makeDefaultSettings(vDiskSDK vDisk)
	{
		setAutoMode(true);
		setSafeMode(false);
		setOverwrite(true);

		setUploadMethod(UploadMethod.DEFAULT_METHOD);
		setSectionSize(SectionSize.DEFAULT_SIZE);
		setDownloadLocation(StorageType.DEFAULT_TYPE);
		setPreviewSize(ThumbnailSize.DEFAULT_SIZE);
		setShortcutKey(ShortcutKey.DEFAULT_KEY);
		setFileAction(FileAction.DEFAULT_ACTION);

		setUsername("");
		setPassword("");
		setDownloadURI(StorageType.DEFAULT_URI);

		setAccessToken("");
		setRefreshToken("");
		setExpiresIn(0);

		_data.addElement(_elements);

		synchronized(_store)
		{
			_store.setContents(_data);
			_store.forceCommit();
		}

		if (vDisk != null)
		{
			vDisk.setAutoMode(true, "", "");
			vDisk.setSafeMode(false);
			vDisk.setPostMethod(false);
			vDisk.setRoot(ROOT);
			vDisk.setDownloadURI(DOWNLOADURI);
			vDisk.setAccessToken("");
			vDisk.setRefreshToken("");
			vDisk.setExpiresIn(0);
			//vDisk.setOverwrite(true);
			vDisk.setPreviewSize(PREVIEWSIZE);
			vDisk.setUploadSectionSize(UPLOADSECTIONSIZE);
			vDisk.setMaxUploadFileSize(MAXUPLOADFILESIZE);
		}
	}

	public String details()
	{
		return "Auto Mode: " + isAutoMode() +
				"\nSafe Mode: " + isSafeMode() +
				"\nOverwrite: " + isOverwrite() +
				"\nUpload Method: " + getUploadMethod() +
				"\nSection Size: " + getSectionSize() +
				"\nDownload Location: " + getDownloadLocation() +
				"\nPreview Size: " + getPreviewSize() +
				"\nShortcut Key: " + getShortcutKey() +
				"\nFile Action: " + getFileAction() +
				"\nUsername: " + getUsername() +
				"\nPassword: " + getPassword() +
				"\nDownload URI: " + getDownloadURI() +
				"\nAccess Token: " + getAccessToken() +
				"\nRefresh Token: " + getRefreshToken() +
				"\nExpires In: " + getExpiresIn();
	}

	static
	{
		_store = PersistentStore.getPersistentObject(0x59e8114dd2411b80L); //blackberry_vdisk_written_by_Walkline_Wang
	}
}