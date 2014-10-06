package com.walkline.screen;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import localization.vDiskSDKResource;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.image.Image;
import net.rim.device.api.ui.image.ImageFactory;
import net.rim.device.api.ui.menu.CommandItem;
import net.rim.device.api.ui.menu.CommandItemProvider;
import net.rim.device.api.ui.menu.DefaultContextMenuProvider;
import net.rim.device.api.ui.menu.SubMenu;
import net.rim.device.api.ui.picker.FilePicker;
import net.rim.device.api.util.StringProvider;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Enumerations.FileAction;
import com.walkline.util.FileUtility;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.util.ui.ForegroundManager;
import com.walkline.util.ui.ListStyleButtonField;
import com.walkline.util.ui.ListStyleButtonSet;
import com.walkline.vdisk.BasicAsyncCallback;
import com.walkline.vdisk.vDiskConfig;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.dao.vDiskFile;
import com.walkline.vdisk.dao.vDiskFolder;
import com.walkline.vdisk.inf.DirectLink;
import com.walkline.vdisk.inf.File;
import com.walkline.vdisk.inf.Folder;
import com.walkline.vdisk.inf.User;

public class vDiskScreen extends MainScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private ForegroundManager _foreground = new ForegroundManager(0);
	private ListStyleButtonSet _listSet = new ListStyleButtonSet();

	private vDiskSDK _vDisk;
	private vDiskAppConfig _appConfig;
	private LabelField _currentFolder;

	private LabelField labelTitleField;
	private FilePicker _filePicker;

	private String _uploadFileURI = "";
	private String _uploadFileName = "";
	private String _currentPath = "";

	private boolean isRefreshing =false;
	private ItemProvider itemProvider = new ItemProvider();

	public vDiskScreen(vDiskSDK vDisk, vDiskAppConfig appConfig)
	{
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | NO_SYSTEM_MENU_ITEMS);

		setContextMenuProvider(new DefaultContextMenuProvider());
		setDefaultClose(false);

		try {
			FontFamily family = FontFamily.forName("BBGlobal Sans");
			Font appFont = family.getFont(Font.PLAIN, 8, Ui.UNITS_pt);
			FontManager.getInstance().setApplicationFont(appFont);
		} catch (ClassNotFoundException e) {}

		_vDisk = vDisk;
		_appConfig = appConfig;
 
		_filePicker = FilePicker.getInstance();
		_filePicker.setListener(new FilePickerListener());

		String titleIconName = Display.getWidth() < 640 ? "titleIcon_small.png" : "titleIcon_large.png";
		BitmapField bmpTitleField = new BitmapField(Bitmap.getBitmapResource(titleIconName));
		bmpTitleField.setSpace(5, 5);
		labelTitleField = new LabelField(vDiskAppConfig.APP_TITLE, LabelField.ELLIPSIS);
		labelTitleField.setFont(vDiskAppConfig.FONT_MAIN_TITLE);

		HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH);
		VerticalFieldManager vfm = new VerticalFieldManager(FIELD_VCENTER);
		vfm.add(labelTitleField);
		hfm.add(bmpTitleField);
		hfm.add(vfm);
		hfm.setBackground(vDiskAppConfig.bgColor_Gradient);
		setTitle(hfm);

		String bitmap = Display.getWidth() < 640 ? "normal_root_small.png" : "normal_small.png";
		BitmapField bitmapFolder =  new BitmapField(Bitmap.getBitmapResource(bitmap));
		bitmapFolder.setSpace(5, 0);
		_currentFolder = new LabelField(getResString(ROOT_FOLDER), LabelField.ELLIPSIS);
		_currentFolder.setFont(Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1,Ui.UNITS_pt));
		_currentFolder.setCommandItemProvider(itemProvider);

		HorizontalFieldManager statusBar = new HorizontalFieldManager(USE_ALL_WIDTH);
		VerticalFieldManager statusContent = new VerticalFieldManager(FIELD_VCENTER);
		statusBar.setBackground(BackgroundFactory.createSolidBackground(0xDDDDDD));
		statusContent.add(_currentFolder);
		statusBar.add(bitmapFolder);
		statusBar.add(statusContent);
		add(statusBar);

		//getMainManager().setBackground(BackgroundFactory.createSolidBackground(0xDDDDDD));
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				try {
					_vDisk.getAccountInfo(new GetAccountInfoCallback(), null);
				} catch (vDiskException e) {Function.errorDialog(e.toString());}	
			}
		});
	}

    MenuItem menuSwitchToPrivateArea = new MenuItem(_bundle, MENU_SWITCH_TO_PRIVATE_AREA, 300, 10)
    {
    	public void run()
    	{
    		_vDisk.setRoot(vDiskConfig.ROOT_SANDBOX);
    		_currentPath = "";
    		refreshCurrentFolder();
    	}
    };

    MenuItem menuSwitchToPublicArea = new MenuItem(_bundle, MENU_SWITCH_TO_PUBLIC_AREA, 300, 20)
    {
    	public void run()
    	{
    		_vDisk.setRoot(vDiskConfig.ROOT_BASIC);
    		_currentPath = "";
    		refreshCurrentFolder();
    	}
    };

    MenuItem menuRefresh = new MenuItem(_bundle, MENU_REFRESH, 100, 10)
    {
    	public void run() {refreshCurrentFolder();}
    };
    
    MenuItem menuUpload = new MenuItem(_bundle, MENU_UPLOAD, 100, 30)
    {
    	public void run() {uploadFile();}
    };

    MenuItem menuNewFolder = new MenuItem(_bundle, MENU_NEW_FOLDER, 100, 35)
    {
    	public void run() {createFolder();}
    };

    MenuItem menuSearch = new MenuItem(_bundle, MENU_SEARCH, 100, 40)
    {
    	public void run() {searchFile();}
    };

    MenuItem menuSettings = new MenuItem(_bundle, MENU_SETTINGS, 100, 50)
    {
    	public void run() {showSettingsScreen();}
    };

    MenuItem menuAbout = new MenuItem(_bundle, MENU_ABOUT, 100, 60)
    {
    	public void run() {showAboutScreen();}
    };

    MenuItem menuRunInBackground = new MenuItem(_bundle, MENU_RUN_IN_BACKGROUND, 100, 70)
    {
    	public void run() {UiApplication.getUiApplication().requestBackground();}
    };

    MenuItem menuExit = new MenuItem(_bundle, MENU_EXIT, 100, 80)
    {
    	public void run() {System.exit(0);}
    };

    MenuItem menuToolsRefreshToken = new MenuItem(_bundle, MENU_LOGIN_AGAIN, 200, 10)
    {
    	public void run()
    	{
    		try {
    			_vDisk.refreshAccessToken(true);
    		} catch (vDiskException e) {}
    	}
    };
    
    protected void makeMenu(Menu menu, int instance)
    {
    	SubMenu menuTools = new SubMenu(null, _bundle, MENU_TOOLS, 100, 45);
    	menuTools.add(menuToolsRefreshToken);
    	menuTools.addSeparator();

    	SubMenu menuToolsSwitchTo = new SubMenu(null, _bundle, MENU_SWITCH_TO, 200, 20);
    	menuToolsSwitchTo.add(menuSwitchToPrivateArea);
    	menuToolsSwitchTo.add(menuSwitchToPublicArea);

    	menuTools.add(menuToolsSwitchTo);

    	menu.add(menuRefresh);
    	menu.addSeparator();
    	menu.add(menuUpload);
    	menu.addSeparator();
    	menu.add(menuNewFolder);
    	menu.addSeparator();
    	//menu.add(menuSearch);
    	//menu.addSeparator();
    	menu.add(menuTools);
    	menu.add(menuSettings);
    	menu.addSeparator();
    	menu.add(menuAbout);
    	menu.addSeparator();
    	//menu.add(menuRunInBackground);
    	//menu.addSeparator();
    	menu.add(menuExit);

    	super.makeMenu(menu, instance);
    }

    public boolean onClose()
    {
    	UiApplication.getUiApplication().requestBackground();

    	return true;
    }

    protected boolean keyChar(char character, int status, int time)
    {
    	switch (character)
    	{
			case Characters.BACKSPACE:
				deleteObjectPrompt();	
				return true;
			case Characters.LATIN_CAPITAL_LETTER_R:
			case Characters.LATIN_SMALL_LETTER_R:
				refreshCurrentFolder();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_T:
			case Characters.LATIN_SMALL_LETTER_T:
				if (_listSet.getFieldCount() > 0) {_listSet.getField(0).setFocus();}
				return true;
			case Characters.LATIN_CAPITAL_LETTER_B:
			case Characters.LATIN_SMALL_LETTER_B:
				if (_listSet.getFieldCount() > 0) {_listSet.getField(_listSet.getFieldCount()-1).setFocus();}
				return true;
			case Characters.LATIN_CAPITAL_LETTER_N:
			case Characters.LATIN_SMALL_LETTER_N:
				createFolder();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_I:
			case Characters.LATIN_SMALL_LETTER_I:
				AccountInfoPopupScreen accountInfoScreen = new AccountInfoPopupScreen(_vDisk);
				UiApplication.getUiApplication().pushScreen(accountInfoScreen);
				return true;
			case Characters.LATIN_CAPITAL_LETTER_D:
			case Characters.LATIN_SMALL_LETTER_D:
				downloadFile();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_E:
			case Characters.LATIN_SMALL_LETTER_E:
				getDirectLink();
				return true;
			case Characters.ESCAPE:
				if (!_currentPath.equals("") && !_currentPath.equals("/"))
				{
					refreshFolder(FileUtility.parentOf(_currentPath));
					return true;							
				}
				break;
			case Characters.LATIN_CAPITAL_LETTER_U:
			case Characters.LATIN_SMALL_LETTER_U:
				uploadFile();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_A:
			case Characters.LATIN_SMALL_LETTER_A:
				showAboutScreen();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_X:
			case Characters.LATIN_SMALL_LETTER_X:
				System.exit(0);
				return true;
		}

    	return super.keyChar(character, status, time);
    }

    private void uploadFile()
    {
		UploadFilePopupScreen _uploadScreen = new UploadFilePopupScreen();
		UiApplication.getUiApplication().pushModalScreen(_uploadScreen);

		int selection = _uploadScreen.getSelection();

		if (selection == -1) {return;}

		_uploadFileURI = "";
		_uploadFileName = "";

		switch (selection)
		{
			case FilePicker.VIEW_ALL:
				_filePicker.setTitle(getResString(FILEPICKER_TITLE_FILE));
				break;
			case FilePicker.VIEW_PICTURES:
				_filePicker.setTitle(getResString(FILEPICKER_TITLE_PICTURE));
				break;
			case FilePicker.VIEW_MUSIC:
	    		_filePicker.setTitle(getResString(FILEPICKER_TITLE_MUSIC));
				break;
			case FilePicker.VIEW_VIDEOS:
	    		_filePicker.setTitle(getResString(FILEPICKER_TITLE_VIDEO));
				break;
			case FilePicker.VIEW_RINGTONES:
		   		_filePicker.setTitle(getResString(FILEPICKER_TITLE_RINGTONE));
				break;
			case FilePicker.VIEW_VOICE_NOTES:
	    		_filePicker.setTitle(getResString(FILEPICKER_TITLE_VOICENOTE));
				break;
		}

		_uploadScreen = null;
		_filePicker.setView(selection);
		_filePicker.show();
    }
	
	private void downloadFile()
	{
		if (_listSet.getFieldWithFocusIndex() > -1)
		{
    		final ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();
    		
    		if (!item.isFolder())
    		{
    			try {
					_vDisk.downloadFileWithProgress(item.getPath());
				} catch (vDiskException e)
				{
					Function.errorDialog(e.toString());
				}	
    		}
		}	
	}

	private void createFolder()
	{
		NewFolderPopupScreen input = new NewFolderPopupScreen();
		UiApplication.getUiApplication().pushModalScreen(input);
		String path = input.getFolderName();

		if (path != null && !path.equals(""))
		{
			if (!path.startsWith("/")) {path = "/" + path;}

    		try {
				_vDisk.createFolder(_currentPath + path, new CreateFolderCallback(), null);
			} catch (vDiskException e) {
				Function.errorDialog(e.toString());
			}
		}	
	}

	private void deleteObjectPrompt()
	{
		if (_listSet.getFieldWithFocusIndex() > -1)
		{
			String[] choices = getResStringArray(DIALOG_CHOICES_YESNO);
			Dialog yesnoDialog = new Dialog(getResString(DIALOG_MESSAGE_DELETE_OBJECT), choices, null, 1, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), Dialog.GLOBAL_STATUS);
			yesnoDialog.setDialogClosedListener(new DialogClosedListener()
			{
				public void dialogClosed(Dialog dialog, int choice)
				{
					if (choice == 0) {deleteObject();}
				}
			});
			yesnoDialog.show();
		}
	}
	
	private void deleteObject()
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();

		try {
			_vDisk.deleteObject(item.getPath(), new DeleteObjectCallback(item), null);
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		}
	}

	private void renameFile()
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();

		String oldName = item.isFolder() ? item.getFolderName() : item.getFilename();
		RenamePopupScreen input = new RenamePopupScreen(oldName);
		UiApplication.getUiApplication().pushModalScreen(input);
		String newName = input.getFolderName();

		if (newName != null && !newName.equals("") && !newName.equals(oldName))
		{
			if (!newName.startsWith("/") && !_currentPath.equals("/")) {newName = "/" + newName;}

			JSONObject jsonObject;
    		try {
    			jsonObject = _vDisk.moveObject(item.getPath(), _currentPath + newName, null, null);

				if (jsonObject != null && jsonObject.length() > 0)
				{
					refreshCurrentFolder();
					Function.errorDialog(getResString(MESSAGE_INFO_RENAME_SUCCESS));
				}
			} catch (vDiskException e) {
				Function.errorDialog(e.toString());
			}
		}
	}

	private void moveFile()
	{
		Function.errorDialog("Coming soon!");
	}

	private void searchFile()
	{
		Function.errorDialog("Coming soon!");
	}

	private void shareFile(boolean cancel)
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();

		try {
			_vDisk.shareFile(item.getPath(), cancel, new ShareFileCallback(item.getFilename()), null);
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		}
	}

	private void getDirectLink()
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();

		try {
			_vDisk.getFileDirectLink(item.getPath(), new GetDirectLinkCallback(item.getFilename()), null);	
		} catch (Exception e) {
			Function.errorDialog(e.toString());
		}
	}

	private void showSettingsScreen()
	{
		UiApplication.getUiApplication().pushScreen(new SettingsMainScreen(_vDisk, _appConfig));
	}
	
	private void showAboutScreen()
	{
		UiApplication.getUiApplication().pushScreen(new AboutMainScreen());
	}

	private void showFileProperty()
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();
		
		//if (!item.isFolder())
		//{
			Function.errorDialog(item.getProperties());
		//}

	}

	private void showPreviewImage()
	{
		ListStyleButtonField item = (ListStyleButtonField) _listSet.getFieldWithFocus();
		
		if (item.isThumbExists())
		{
			UiApplication.getUiApplication().pushScreen(new PreviewImageFullScreen(_vDisk, item.getPath()));			
		} else if (item.getFilename().toLowerCase().endsWith(".mp3") | item.getFilename().toLowerCase().endsWith(".wma") | item.getFilename().toLowerCase().endsWith(".mp4") | item.getFilename().toLowerCase().endsWith(".3gp")) {
			UiApplication.getUiApplication().pushScreen(new MediaPlayMainScreen(_vDisk, item.getPath()));
		} else {
			Function.errorDialog(getResString(MESSAGE_ALERT_CAN_NOT_PREVIEW_FILE));
		}
	}

	/**
	 * A sub class for <code>refreshFolder()</code>
	 */
	private boolean showContentList(JSONObject metaData)
	{
		boolean result = false;

		try {
			if (metaData.optBoolean("is_dir"))
			{
				Folder folder;

				folder = new vDiskFolder(_vDisk, metaData);
				
				JSONArray jsonArray = folder.getContents();
				if (_listSet != null) {_listSet.deleteAll();}

				for (int i=0; i<jsonArray.length(); i++)
				{
					metaData = jsonArray.getJSONObject(i);
					
					if (metaData.optBoolean("is_dir"))
					{
						final Folder childFolder = new vDiskFolder(_vDisk, metaData);
						
						ListStyleButtonField item = new ListStyleButtonField(childFolder);
						item.setCommandItemProvider(itemProvider);
						item.setChangeListener(new FieldChangeListener()
						{
							public void fieldChanged(Field field, int context)
							{
								refreshFolder(childFolder.getPath());
								_currentPath = childFolder.getPath();
							}
						});
						_listSet.add(item);
					} else {
						final File childFile = new vDiskFile(_vDisk, metaData);

						ListStyleButtonField item = new ListStyleButtonField(childFile, _appConfig);
						item.setCommandItemProvider(itemProvider);
						item.setChangeListener(new FieldChangeListener()
						{
							public void fieldChanged(Field field, int context)
							{
								int fileActon = _appConfig.getFileAction();

								switch (fileActon)
								{
									case FileAction.POPUPMENU:
										break;
									case FileAction.DOWNLOAD:
										downloadFile();
										break;
									case FileAction.PREVIEW:
										showPreviewImage();
										break;
									case FileAction.PROPERTY:
										showFileProperty();
										break;
								}
								//Function.errorDialog("click " + field.toString());
								//selectFileAction(childFile.isThumbExists());
							}
						});

						_listSet.add(item);
					}
				}

				result = true;
			}
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		} catch (JSONException e) {}

		return result;
	}

	private void addListsetManager()
	{
		if (_listSet.getFieldCount() > 0)
		{
			_foreground.add(_listSet);
			add(_foreground);
		}
	}
	
	private void deleteListsetManager()
	{
		if (_foreground.getScreen() != null)
		{
			_listSet.deleteAll();
			_foreground.delete(_listSet);
			delete(_foreground);
			_listSet = new ListStyleButtonSet();
		}
	}

	/**
	 * Update the folder path LabelField on the top of screen
	 */
	private void updateCurrentFolderStatus()
	{
		if (_currentPath.equals("") || _currentPath.equals("/"))
		{
			_currentFolder.setText(getResString(ROOT_FOLDER));
		} else {
			_currentFolder.setText(getResString(ROOT_FOLDER) + (_currentPath.startsWith("/") ? _currentPath : "/" + _currentPath));
		}
	}

	/**
	 * Refresh current folder content list
	 */
	private void refreshCurrentFolder() {refreshFolder(_currentPath);}

	private void refreshFolder(String path)
	{
		if (isRefreshing) {return;}
		isRefreshing = true;

		try {
			deleteListsetManager();
			_vDisk.getMetaData(path, true, false, new RefreshFolderCallback(), null);
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		}
	}

	class CreateFolderCallback extends BasicAsyncCallback
	{
		public void onComplete(Folder value, final Object state)
		{
			if (value != null)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						refreshCurrentFolder();								
					}
				});
			} else {
				Function.errorDialog(getResString(MESSAGE_ERROR_CREATE_FOLDER_FAILED));
			}
		}
		
		public void onException(Exception e, final Object state)
		{
			Function.errorDialog(e.toString());
		}
	}

	class GetDirectLinkCallback extends BasicAsyncCallback
	{
		String _fileName;

		public GetDirectLinkCallback(String fileName) {_fileName = fileName;}

		public void onComplete(DirectLink value, final Object state)
		{
			if (value != null)
			{
				String url = value.getURL();
				String expireTime = value.getExpireTime();

				Function.showFileDirectLinkUrl(_fileName, url, expireTime);
			} else {
				Function.errorDialog("Get file direct link failed.");
			}
		}
		
		public void onException(Exception e, final Object state)
		{
			Function.errorDialog(e.toString());
		}
	}
	
	class ShareFileCallback extends BasicAsyncCallback
	{
		String _fileName;

		public ShareFileCallback(String fileName) {_fileName = fileName;}

		public void onComplete(JSONObject value, final Object state)
		{
			if (value != null)
			{
				String url = value.optString("url");
				
				if (!url.equals(""))
				{
					Function.showSharedFileUrl(_fileName, url);
				} else {
					Function.errorDialog(getResString(MESSAGE_INFO_UNSHARE_SUCCESS));
				}
			} else {
				Function.errorDialog(getResString(MESSAGE_ERROR_SHARE_UNSHARE_FILE_FAILED));
			}
		}
		
		public void onException(Exception e, final Object state)
		{
			Function.errorDialog(e.toString());
		}
	}
	
	class DeleteObjectCallback extends BasicAsyncCallback
	{
		ListStyleButtonField _item;

		public DeleteObjectCallback(ListStyleButtonField item) {_item = item;}

		public void onComplete(JSONObject value, final Object state)
		{
			if (value != null)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						_listSet.delete(_item);

						if (_listSet.getFieldCount() <= 0) {deleteListsetManager();}
					}
				});
			} else {
				Function.errorDialog(getResString(MESSAGE_ERROR_DELETE_OBJECT_FAILED));
			}
		}
		
		public void onException(Exception e, final Object state)
		{
			Function.errorDialog(e.toString());
		}
	}

	class RefreshFolderCallback extends BasicAsyncCallback
	{
		public void onComplete(JSONObject value, final Object state)
		{
			if (value != null)
			{
				synchronized (Application.getEventLock())
				{
					if (showContentList(value))
					{
						_currentPath = value.optString("path");
						updateCurrentFolderStatus();
						
						addListsetManager();
					}
				}
			}

			isRefreshing = false;
		}

		public void onException(Exception e, final Object state)
		{
			isRefreshing = false;

			Function.errorDialog(e.toString());
		}
	}

	class GetAccountInfoCallback extends BasicAsyncCallback
	{
		public void onComplete(final User value, Object state)
		{
			if (value != null)
			{
				UiApplication.getUiApplication().invokeLater(new Runnable()
				{
					public void run()
					{
						if (!value.getScreenName().equalsIgnoreCase(""))
						{
							labelTitleField.setText(vDiskAppConfig.APP_NAME + value.getScreenName());	
						}
						refreshCurrentFolder();
					}
				});
			}
		}
		
		public void onException(Exception e, Object state)
		{
			Function.errorDialog(e.toString());
		}
	}

	private String getResString(int key) {return _bundle.getString(key);}
	private String[] getResStringArray(int key) {return _bundle.getStringArray(key);}

	class FilePickerListener implements FilePicker.Listener
	{
		public void selectionDone(String selected)
		{
			if (selected != null && selected.length() > 0)
			{
				FileConnection fconn = null;
				int fileSize = 0;

				try {
					fconn = (FileConnection)Connector.open(selected);
					_uploadFileURI = fconn.getURL();
					_uploadFileName = fconn.getName();
					fileSize = (int) fconn.fileSize();
					fconn.close();
				} catch (IOException e1) {}
				
				if (fileSize > _vDisk.getMaxUploadFileSize())
				{
					String maxSize = StringUtility.formatSize(_vDisk.getMaxUploadFileSize(), 1);
					Function.errorDialog(getResString(MESSAGE_ALERT_TOO_LARGE_FILE) + maxSize);
				} else {
					if (fileSize > _vDisk.getUploadSectionSize())
					{
						try {
							JSONObject jsonObject;

							jsonObject = _vDisk.uploadFileMultiPartWithProgress(_currentPath + (_currentPath.equals("/") ? "" : "/") + _uploadFileName, _uploadFileURI);

							if (jsonObject != null && jsonObject.length() > 0)
							{
								refreshCurrentFolder();
								Function.errorDialog(getResString(MESSAGE_INFO_UPLOAD_SUCCESS));
							}
						} catch (vDiskException e)
						{
							Function.errorDialog(e.toString());
						}
					} else {
						try {
							JSONObject jsonObject;

							jsonObject = _vDisk.uploadFileWithProgress(_currentPath + (_currentPath.equals("/") ? "" : "/") + _uploadFileName, _uploadFileURI);

							if (jsonObject != null && jsonObject.length() > 0)
							{
								refreshCurrentFolder();
								Function.errorDialog(getResString(MESSAGE_INFO_UPLOAD_SUCCESS));
							}
						} catch (vDiskException e)
						{
							Function.errorDialog(e.toString());
						}
					}
				}
			}
		}	
	}

	class ItemProvider implements CommandItemProvider
	{
        public Object getContext(Field field) {return field;}

        public Vector getItems(Field field)
        {
        	Vector items = new Vector();

        	Image myIcon = ImageFactory.createImage(Bitmap.getBitmapResource("titleIcon_large.png"));
        	Image iconDelete = ImageFactory.createImage(Bitmap.getBitmapResource("delete.png"));
        	Image iconPreview = ImageFactory.createImage(Bitmap.getBitmapResource("preview.png"));
        	Image iconDownload = ImageFactory.createImage(Bitmap.getBitmapResource("download.png"));
        	Image iconMove = ImageFactory.createImage(Bitmap.getBitmapResource("move.png"));
        	Image iconProperty = ImageFactory.createImage(Bitmap.getBitmapResource("property.png"));
        	Image iconShare = ImageFactory.createImage(Bitmap.getBitmapResource("share.png"));
        	Image iconUnShare = ImageFactory.createImage(Bitmap.getBitmapResource("unshare.png"));
        	Image iconRename = ImageFactory.createImage(Bitmap.getBitmapResource("rename.png"));

        	if (field instanceof ListStyleButtonField)
        	{
        		ListStyleButtonField item = (ListStyleButtonField) field;
        		if (item.isFolder())
        		{
        			CommandItem cmdProperty = new CommandItem(new StringProvider(getResString(CONTEXTMENU_PROPERTY)), iconProperty, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						showFileProperty();
    					}
    				}));
            		CommandItem cmdDelete = new CommandItem(new StringProvider(getResString(CONTEXTMENU_DELETE)), iconDelete, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						deleteObjectPrompt();
    					}
    				}));
            		CommandItem cmdMove = new CommandItem(new StringProvider(getResString(CONTEXTMENU_MOVE)), iconMove, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						moveFile();
    					}
    				}));
            		CommandItem cmdRename = new CommandItem(new StringProvider(getResString(CONTEXTMENU_RENAME)), iconRename, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						renameFile();
    					}
    				}));

            		items.addElement(cmdProperty);
            		items.addElement(cmdDelete);
            		items.addElement(cmdRename);
            		items.addElement(cmdMove);
        		} else {
            		CommandItem cmdDownload = new CommandItem(new StringProvider(getResString(CONTEXTMENU_DOWNLOAD)), iconDownload, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						downloadFile();
    					}
    				}));
            		CommandItem cmdPreview = new CommandItem(new StringProvider(getResString(CONTEXTMENU_PREVIEW)), iconPreview, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						showPreviewImage();
    					}
    				}));
            		CommandItem cmdShare = new CommandItem(new StringProvider(getResString(CONTEXTMENU_SHARE)), iconShare, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						shareFile(false);
    					}
    				}));
            		CommandItem cmdUnshare = new CommandItem(new StringProvider(getResString(CONTEXTMENU_UNSHARE)), iconUnShare, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						shareFile(true);
    					}
    				}));
            		CommandItem cmdProperty = new CommandItem(new StringProvider(getResString(CONTEXTMENU_PROPERTY)), iconProperty, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						showFileProperty();
    					}
    				}));
            		CommandItem cmdDelete = new CommandItem(new StringProvider(getResString(CONTEXTMENU_DELETE)), iconDelete, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						deleteObjectPrompt();
    					}
    				}));
            		CommandItem cmdMove = new CommandItem(new StringProvider(getResString(CONTEXTMENU_MOVE)), iconMove, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						moveFile();
    					}
    				}));
            		CommandItem cmdRename = new CommandItem(new StringProvider(getResString(CONTEXTMENU_RENAME)), iconRename, new Command(new CommandHandler() {
    					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
    						renameFile();
    					}
    				}));

            		items.addElement(cmdDownload);
            		items.addElement(cmdShare);
            		items.addElement(cmdPreview);
            		items.addElement(cmdUnshare);
            		items.addElement(cmdProperty);
            		items.addElement(cmdDelete);
            		items.addElement(cmdMove);
            		items.addElement(cmdRename);
        		}
        	} else if (field instanceof LabelField)
        	{
        		CommandItem cmdDownload = new CommandItem(new StringProvider(getResString(CONTEXTMENU_DOWNLOAD)), iconDownload, new Command(new CommandHandler() {
					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
						downloadFile();
					}
				}));
        		CommandItem cmdPreview = new CommandItem(new StringProvider(getResString(CONTEXTMENU_PREVIEW)), iconPreview, new Command(new CommandHandler() {
					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
						showPreviewImage();
					}
				}));
        		CommandItem cmdShare = new CommandItem(new StringProvider(getResString(CONTEXTMENU_SHARE)), iconShare, new Command(new CommandHandler() {
					public void execute(ReadOnlyCommandMetadata metadata, Object context) {
						shareFile(false);
					}
				}));

        		items.addElement(cmdDownload);
        		items.addElement(cmdPreview);
        		items.addElement(cmdShare);
        	}

    		return items;
        }
	}
}