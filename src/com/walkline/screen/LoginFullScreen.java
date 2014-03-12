package com.walkline.screen;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.EmailAddressEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.ui.RoundRectFieldManager;
import com.walkline.vdisk.vDiskAppConfigException;
import com.walkline.vdisk.vDiskSDK;

public class LoginFullScreen extends FullScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private vDiskSDK _vDisk;
	private vDiskAppConfig _appConfig;

	private EmailAddressEditField _username;
	private PasswordEditField _password;
	private CheckboxField _autoLogin;
	private ButtonField _buttonLogin;

	public LoginFullScreen(vDiskSDK vDisk, vDiskAppConfig appConfig)
	{
		super(NO_VERTICAL_SCROLL);

		setBackground(vDiskAppConfig.bgColor_GradientBlue);

		_vDisk = vDisk;
		_appConfig = appConfig;

		_username = new EmailAddressEditField(getResString(LOGIN_INPUT_USERNAME), null, EditField.DEFAULT_MAXCHARS, EditField.NO_NEWLINE | EditField.NON_SPELLCHECKABLE);
		_password = new PasswordEditField(getResString(LOGIN_INPUT_PASSWORD), null, EditField.DEFAULT_MAXCHARS, EditField.NO_NEWLINE | EditField.NON_SPELLCHECKABLE);

		_autoLogin = new CheckboxField(getResString(LOGIN_CHECKBOX_SEMIAUTO_LOGIN), true, FIELD_LEFT);
		_autoLogin.setEnabled(false);

		_buttonLogin = new ButtonField(getResString(LOGIN_BUTTON_LOGIN), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_RIGHT);
		_buttonLogin.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				saveData();
			}
		});
		_buttonLogin.setMinimalWidth(100);

		LabelField labelSignUp = new LabelField("Create an account", FIELD_RIGHT | FOCUSABLE) {
			protected boolean invokeAction(int action)
			{
				UiApplication.getUiApplication().pushScreen(new SignUpFullScreen());

				return true;
			}
		};
		labelSignUp.setFont(Font.getDefault().derive(Font.ITALIC, Font.getDefault().getHeight(Ui.UNITS_pt), Ui.UNITS_pt));
		labelSignUp.setEnabled(false);

		RoundRectFieldManager roundUsername = new RoundRectFieldManager(false);
		roundUsername.add(_username);

		RoundRectFieldManager roundPassword = new RoundRectFieldManager(false);
		roundPassword.add(_password);

		BitmapField titleIcon = new BitmapField(Bitmap.getBitmapResource("login.png"), FIELD_HCENTER);

		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_HEIGHT | FIELD_HCENTER | FIELD_VCENTER);
		vfm.setPadding(10, 20, 0, 20);
		vfm.add(titleIcon);
		vfm.add(roundUsername);
		vfm.add(roundPassword);
		vfm.add(_autoLogin);
		vfm.add(_buttonLogin);
		//vfm.add(new LabelField());
		//vfm.add(labelSignUp);
		add(vfm);

		_username.setFocus();
	}

	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);

		if (_appConfig.isAccountEmpty()) {System.exit(0);}

		UiApplication.getUiApplication().pushScreen(new vDiskScreen(_vDisk, _appConfig));
		return true;
	}

	protected boolean keyChar(char character, int status, int time)
	{
		switch (character)
		{
			case Characters.ENTER:
				if (_username.isFocus())
				{
					_password.setFocus();
					_password.setCursorPosition(_password.getTextLength());
					return true;
				} else if (_password.isFocus())
				{
					_buttonLogin.setFocus();
					return true;
				}
		}

		return super.keyChar(character, status, time);
	}

	private String getResString(int key) {return _bundle.getString(key);}

	private void saveData()
	{
		if (_appConfig != null)
		{
			if (_username.getText().trim().equals(""))
			{
				_username.setText("");
				_username.setFocus();
				return;
			}

			if (_password.getText().trim().equals(""))
			{
				_password.setText("");
				_password.setFocus();
				return;
			}

			_appConfig.setUsername(_username.getText());
			_appConfig.setPassword(_password.getText());
			_appConfig.setAutoMode(_autoLogin.getChecked());
			_appConfig.save(null, false);

			_vDisk.setAutoMode(_appConfig.isAutoMode(), _appConfig.getUsername(), _appConfig.getPassword());

			onClose();
		} else {
			try {
				throw new vDiskAppConfigException(getResString(MESSAGE_ERROR_CANNOT_SAVE_SETTINGS));
			} catch (vDiskAppConfigException e) {}
		}
	}
}