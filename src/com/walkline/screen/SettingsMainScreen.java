package com.walkline.screen;

import localization.vDiskSDKResource;
import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Enumerations.FileAction;
import com.walkline.util.Enumerations.SectionSize;
import com.walkline.util.Enumerations.ShortcutKey;
import com.walkline.util.Enumerations.StorageType;
import com.walkline.util.Enumerations.ThumbnailSize;
import com.walkline.util.Enumerations.UploadMethod;
import com.walkline.util.Function;
import com.walkline.util.ui.ForegroundManager;
import com.walkline.util.ui.RoundRectFieldManager;
import com.walkline.util.ui.VerticalButtonFieldSet;
import com.walkline.vdisk.vDiskAppConfigException;
import com.walkline.vdisk.vDiskSDK;

public class SettingsMainScreen extends MainScreen implements vDiskSDKResource
{
	private ForegroundManager _foreground;
	private CheckboxField checkboxAutoLogin;
	private CheckboxField checkboxSafeMode;
	private CheckboxField checkboxOverwrite;
	private ObjectChoiceField choiceMethod;
	private ObjectChoiceField choiceSectionSize;
	private ObjectChoiceField choiceStorage;
	private ObjectChoiceField choicePreview;
	private ObjectChoiceField choiceShortcutKey;
	private ObjectChoiceField choiceFileAction;
	private EditField editStorageLocation;

	private vDiskSDK _vDisk;
	private vDiskAppConfig _appConfig;

	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	public SettingsMainScreen(vDiskSDK vDisk, vDiskAppConfig appConfig)
	{
		super(USE_ALL_HEIGHT | NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);
		setTitle(getResString(SETTINGS_TITLE));

		_vDisk = vDisk;
		_appConfig = appConfig;

		RoundRectFieldManager managerLogin = new RoundRectFieldManager(true);
		RoundRectFieldManager managerUpload = new RoundRectFieldManager(true);
		RoundRectFieldManager managerStorage = new RoundRectFieldManager(true);
		RoundRectFieldManager managerPreview = new RoundRectFieldManager(true);
		RoundRectFieldManager managerShortcutKey = new RoundRectFieldManager(true);
		RoundRectFieldManager managerFileAction = new RoundRectFieldManager(true);
		
		_foreground=new ForegroundManager(0);

		//Login option start
    	MyTitleLabelField labelLogin = new MyTitleLabelField(getResString(SETTINGS_LOGIN));

    	checkboxAutoLogin = new CheckboxField(getResString(SETTINGS_LOGIN_AUTOLOGIN), true, 134217728 | USE_ALL_WIDTH);
    	checkboxAutoLogin.setEnabled(false);

		MyDescriptionLabelField labelAutoLoginDescription = new MyDescriptionLabelField(getResString(SETTINGS_LOGIN_AUTOLOGIN_DESCRIPTION));

    	VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);
    	ButtonField buttonDeleteAccountInfo = new ButtonField(getResString(SETTINGS_LOGIN_BUTTON_CLEAN_INFORMATION), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
    	buttonDeleteAccountInfo.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				cleanAccountInfo();
			}
		});
    	vbf.add(buttonDeleteAccountInfo);

    	managerLogin.add(labelLogin);
    	managerLogin.add(new SeparatorField());
    	managerLogin.add(checkboxAutoLogin);
    	managerLogin.add(labelAutoLoginDescription);
    	managerLogin.add(vbf);
		//Login option end

    	//Upload option start
    	MyTitleLabelField labelUpload = new MyTitleLabelField(getResString(SETTINGS_UPLOAD));
    	
    	checkboxSafeMode = new CheckboxField(getResString(SETTINGS_UPLOAD_SAFEMODE), false, 134217728 | USE_ALL_WIDTH);

    	MyDescriptionLabelField labelSafeModeDescription = new MyDescriptionLabelField(getResString(SETTINGS_UPLOAD_SAFEMODE_DESCRIPTION));

		String[] choicesMethod = UploadMethod.choicesUploadMethod;
		choiceMethod = new ObjectChoiceField(getResString(SETTINGS_UPLOAD_METHOD), choicesMethod, UploadMethod.DEFAULT_METHOD);

		String[] choicesSectionSize = SectionSize.choicesSectionSize;
		choiceSectionSize = new ObjectChoiceField(getResString(SETTINGS_UPLOAD_SECTIONSIZE), choicesSectionSize, SectionSize.DEFAULT_SIZE);

		MyDescriptionLabelField labelSectionSizeDescription = new MyDescriptionLabelField(getResString(SETTINGS_UPLOAD_SECTIONSIZE_DESCRIPTION));

    	managerUpload.add(labelUpload);
    	managerUpload.add(new SeparatorField());
    	managerUpload.add(checkboxSafeMode);
    	managerUpload.add(labelSafeModeDescription);
    	managerUpload.add(choiceMethod);
    	managerUpload.add(choiceSectionSize);
    	managerUpload.add(labelSectionSizeDescription);
    	//Upload option end

    	//Storage option start
    	MyTitleLabelField labelStorage = new MyTitleLabelField(getResString(SETTINGS_STORAGE));

    	String[] choicesStorageLocation = getResStringArray(SETTINGS_STORAGE_LOCATION_ITEMS);
    	choiceStorage = new ObjectChoiceField(getResString(SETTINGS_STORAGE_LOCATION), choicesStorageLocation, StorageType.DEFAULT_TYPE) {
    		protected void fieldChangeNotify(int context)
    		{
				int selection = choiceStorage.getSelectedIndex();
				
				switch (selection)
				{
					case 0:
						editStorageLocation.setText(StorageType.choicesDownloadUri[StorageType.STORE]);
						break;
					case 1:
						editStorageLocation.setText(StorageType.choicesDownloadUri[StorageType.SDCARD]);
						break;
				}
				
				super.fieldChangeNotify(context);
    		};
		};

    	editStorageLocation = new EditField(getResString(SETTINGS_STORAGE_LOCATION_URI), StorageType.DEFAULT_URI, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NO_NEWLINE | EditField.NON_SPELLCHECKABLE);

    	MyDescriptionLabelField labelStorageDescription = new MyDescriptionLabelField(getResString(SETTINGS_STORAGE_LOCATION_DESCRIPTION));

    	checkboxOverwrite = new CheckboxField(getResString(SETTINGS_STORAGE_OVERWRITE), true, 134217728 | USE_ALL_WIDTH);
    	checkboxOverwrite.setEnabled(false);

    	managerStorage.add(labelStorage);
    	managerStorage.add(new SeparatorField());
    	managerStorage.add(choiceStorage);
    	managerStorage.add(editStorageLocation);
    	managerStorage.add(labelStorageDescription);
    	managerStorage.add(checkboxOverwrite);
    	//Storage option end
    	
    	//Preview option start
    	MyTitleLabelField labelPreview = new MyTitleLabelField(getResString(SETTINGS_PREVIEW));

    	String[] choicesPreviewSize = ThumbnailSize.choicesPreviewSize;
    	choicePreview = new ObjectChoiceField(getResString(SETTINGS_PREVIEW_SIZE), choicesPreviewSize, ThumbnailSize.DEFAULT_SIZE);

    	MyDescriptionLabelField labelPreviewDescription = new MyDescriptionLabelField(getResString(SETTINGS_PREVIEW_SIZE_DESCRIPTION));

    	managerPreview.add(labelPreview);
    	managerPreview.add(new SeparatorField());
    	managerPreview.add(choicePreview);
    	managerPreview.add(labelPreviewDescription);
    	//Preview option start

    	//Shortcut key option start
    	MyTitleLabelField labelShortKey = new MyTitleLabelField(getResString(SETTINGS_SHORTCUT));

    	String[] choicesShortcutKeys = ShortcutKey.choicesShortcutKeys;
    	choicesShortcutKeys[0] = getResString(SETTINGS_SHORTCUT_NONE);
    	choiceShortcutKey = new ObjectChoiceField(getResString(SETTINGS_SHORTCUT_KEY), choicesShortcutKeys, ShortcutKey.DEFAULT_KEY);

    	MyDescriptionLabelField labelShortcutKeysDescription = new MyDescriptionLabelField(getResString(SETTINGS_SHORTCUT_KEY_DESCRIPTION));

    	managerShortcutKey.add(labelShortKey);
    	managerShortcutKey.add(new SeparatorField());
    	managerShortcutKey.add(choiceShortcutKey);
    	managerShortcutKey.add(labelShortcutKeysDescription);
    	//Shortcut key option end

    	//FileAction option start
    	MyTitleLabelField labelFileAction = new MyTitleLabelField(getResString(SETTINGS_FILEACTION));

    	String[] choicesFileAction = getResStringArray(SETTINGS_FILEACTION_ACTION_ITEMS);    	
    	choiceFileAction = new ObjectChoiceField(getResString(SETTINGS_FILEACTION_ACTION), choicesFileAction, FileAction.DEFAULT_ACTION);

		MyDescriptionLabelField labelFileActionDescription = new MyDescriptionLabelField(getResString(SETTINGS_FILEACTION_ACTION_DESCRIPTION));

    	managerFileAction.add(labelFileAction);
    	managerFileAction.add(new SeparatorField());
    	managerFileAction.add(choiceFileAction);
    	managerFileAction.add(labelFileActionDescription);
		//FileAction option end

		_foreground.add(managerLogin);
		_foreground.add(managerUpload);
		_foreground.add(managerStorage);
		_foreground.add(managerPreview);
		_foreground.add(managerShortcutKey);
		_foreground.add(managerFileAction);
		add(_foreground);

		loadData();
	}

	private String getResString(int key) {return _bundle.getString(key);}
	private String[] getResStringArray(int key) {return _bundle.getStringArray(key);}

	private void cleanAccountInfo()
	{
		_appConfig.setUsername("");
		_appConfig.setPassword("");
		_appConfig.save(null, false);

		Function.errorDialog(getResString(MESSAGE_INFO_CLEAN_ACCOUNT_SUCCESS));
	}

	private void loadData()
	{
		if (_appConfig != null)
		{
			checkboxAutoLogin.setChecked(_appConfig.isAutoMode());
			checkboxSafeMode.setChecked(_appConfig.isSafeMode());
			checkboxOverwrite.setChecked(_appConfig.isOverwrite());

			choiceMethod.setSelectedIndex(_appConfig.getUploadMethod());
			choiceSectionSize.setSelectedIndex(_appConfig.getSectionSize());
			choiceStorage.setSelectedIndex(_appConfig.getDownloadLocation());
			choicePreview.setSelectedIndex(_appConfig.getPreviewSize());
			choiceShortcutKey.setSelectedIndex(_appConfig.getShortcutKey());
			choiceFileAction.setSelectedIndex(_appConfig.getFileAction());

			editStorageLocation.setText(_appConfig.getDownloadURI());
		} else{
			try {
				throw new vDiskAppConfigException(getResString(MESSAGE_ERROR_CANNOT_LOAD_SETTINGS));
			} catch (vDiskAppConfigException e) {Function.errorDialog(e.toString());}
		}	
	}

	private void saveData()
	{
		String newAppName = vDiskAppConfig.APP_TITLE;

		_appConfig.setAutoMode(checkboxAutoLogin.getChecked());
		_appConfig.setSafeMode(checkboxSafeMode.getChecked());
		_appConfig.setOverwrite(checkboxOverwrite.getChecked());

		_appConfig.setUploadMethod(choiceMethod.getSelectedIndex());
		_appConfig.setSectionSize(choiceSectionSize.getSelectedIndex());
		_appConfig.setDownloadLocation(choiceStorage.getSelectedIndex());
		_appConfig.setPreviewSize(choicePreview.getSelectedIndex());
		_appConfig.setShortcutKey(choiceShortcutKey.getSelectedIndex());
		_appConfig.setFileAction(choiceFileAction.getSelectedIndex());

		_appConfig.setDownloadURI(editStorageLocation.getText());
		_appConfig.save(_vDisk, true);

		if (_appConfig.getShortcutKey() > 0)
		{
			String shortcut_key = ShortcutKey.choicesShortcutKeys[_appConfig.getShortcutKey()];
			newAppName += "(" + shortcut_key + vDiskAppConfig.UNDERLINE + ")";
		}

		HomeScreen.setName(newAppName);
	}

	public boolean onSave()
	{
		saveData();
    	return true;
	}

	class MyTitleLabelField extends LabelField
	{
		public MyTitleLabelField(String text)
		{
			super(text, USE_ALL_WIDTH | LabelField.ELLIPSIS);

			setFont(vDiskAppConfig.FONT_SETTINGS_TITLE);
		}

		protected void paint(Graphics g)
		{
			g.setColor(Color.DARKGRAY);
			super.paint(g);	
		}
	}

	class MyDescriptionLabelField extends LabelField
	{
		public MyDescriptionLabelField(String text)
		{
			super(text);

			setFont(vDiskAppConfig.FONT_SETTINGS_DESCRIPTION);
		}
	}
}