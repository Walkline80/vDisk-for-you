package com.walkline.screen;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.User;

public class AccountInfoPopupScreen extends PopupScreen
{
	private vDiskSDK _vDisk = null;
	private User _user = null;
	
	private BitmapField _avatar;
	private LabelField _screenName;
	private EditField _consumed;
	private EditField _quota;
	private GaugeField _quotaUsage;
	private EditField _userID;
	private EditField _sinaID;
	private EditField _gender;
	private EditField _verified;
	private EditField _location;
	
	public AccountInfoPopupScreen(vDiskSDK vDisk)
	{
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR));

		_vDisk = vDisk;

		_avatar = new BitmapField(null, BitmapField.FIELD_HCENTER | BitmapField.NON_FOCUSABLE);
		_screenName = new LabelField("Screen Name", LabelField.FIELD_HCENTER);
		_quotaUsage = new GaugeField(null, 0, 100, 0, GaugeField.PERCENT | GaugeField.FIELD_HCENTER | GaugeField.NON_FOCUSABLE);
		_consumed = new EditField("Consumed: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_quota = new EditField("Quota: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_gender = new EditField("Gender: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_location = new EditField("Location: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_userID = new EditField("User ID: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_sinaID = new EditField("Sina ID: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		_verified = new EditField("Verified: ", null, EditField.DEFAULT_MAXCHARS, EditField.READONLY | EditField.NON_FOCUSABLE);
		
		ButtonField buttonClose = new ButtonField("Close", ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | ButtonField.FIELD_HCENTER);
		buttonClose.setChangeListener(new FieldChangeListener()
		{
			public void fieldChanged(Field field, int context)
			{
				UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
			}
		});

		add(_avatar);
		add(_screenName);
		add(new LabelField(null, LabelField.FOCUSABLE));
		add(_quotaUsage);
		add(_consumed);
		add(_quota);
		add(new LabelField());
		add(_gender);
		add(_location);
		add(new LabelField());
		add(_userID);
		add(_sinaID);
		add(_verified);
		add(new LabelField());
		add(buttonClose);
		
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				RefreshUserInformationScreen refreshScreen = new RefreshUserInformationScreen(_vDisk);
				UiApplication.getUiApplication().pushModalScreen(refreshScreen);
				_user = refreshScreen.getUser();
				
				if (_user != null)
				{
					_screenName.setText(_user.getScreenName());
					
					if (_user.getAvatarData() != null)
					{
						_avatar.setBitmap(Bitmap.createBitmapFromBytes(_user.getAvatarData(), 0, -1, 1));
					}
					
					float used = Float.parseFloat(_user.getQuotaInfo().getConsumed());
					float total = Float.parseFloat(_user.getQuotaInfo().getQuota());
					_quotaUsage.setValue((int) ((used / total) * 100));
					_consumed.setText(_user.getQuotaInfo().getReadableConsumed());
					_quota.setText(_user.getQuotaInfo().getReadableQuota());
					
					String gender = _user.getGender();
					if (gender.equals("m")) {
						gender = "男";
					} else if (gender.equals("f")) {
						gender = "女";
					} else {
						gender = "未知";
					}
					_gender.setText(gender);
					_location.setText(_user.getLocation());
					
					_userID.setText(_user.getUID());
					_sinaID.setText(_user.getSinaUID());
					_verified.setText(_user.getVerified() ? "Yes" : "No");
				}
				
				if (refreshScreen != null) {refreshScreen = null;}
				if (_user != null) {_user = null;}
			}
		});
	}
	
	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);
		
		return true;
	}
	
	protected boolean keyChar(char key, int status, int time)
	{
		if (key == Characters.ESCAPE)
		{
			onClose();
			
			return true;
		}
		
		return super.keyChar(key, status, time);
	}
}