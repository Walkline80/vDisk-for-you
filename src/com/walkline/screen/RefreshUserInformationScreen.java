package com.walkline.screen;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.walkline.util.Function;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.User;

public class RefreshUserInformationScreen extends PopupScreen
{
	vDiskSDK _vDisk;
	User _user;
	Thread _thread=null;
	byte[] _avatarData=null;

	public RefreshUserInformationScreen(vDiskSDK vDisk)
	{
		super(new VerticalFieldManager());
		
		add(new LabelField("Please wait....", Field.FIELD_HCENTER));
		
		_vDisk = vDisk;
		
		_thread=new Thread() 
		{
			public void run()
			{
				try {
					_user = _vDisk.getAccountInfo();
				} catch (vDiskException e) {Function.errorDialog(e.toString());}
				
				if (_user != null)
				{
					try {
						_avatarData = _vDisk.doRequestRAW(_user.getAvatarLargeUrl(), false);
						_user.setAvatarData(_avatarData);
					} catch (vDiskException e) {}
				}
				
				Application.getApplication().invokeLater(new Runnable()
				{
					public void run() {if (_thread != null) {onClose();}}
				});
			}
		};
		_thread.start();
	}
	
	public User getUser() {return _user;}
	
	public boolean onClose()
	{
		if (_thread != null) {try {_thread = null;} catch (Exception e) {}}

		try {
			UiApplication.getUiApplication().popScreen(this);					
		} catch (Exception e) {Function.errorDialog(e.toString());}
		
		return true;
	}
	
	protected boolean keyChar(char key, int status, int time)
	{
		if(key==Characters.ESCAPE)
		{
			onClose();
			
			return true;
		}
		
		return super.keyChar(key, status, time);
	}
}