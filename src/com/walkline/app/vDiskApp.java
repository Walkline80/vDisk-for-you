package com.walkline.app;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.UiApplication;

import com.walkline.screen.LoginFullScreen;
import com.walkline.screen.vDiskScreen;
import com.walkline.util.Enumerations.ShortcutKey;
import com.walkline.vdisk.ApplicationSettings;
import com.walkline.vdisk.vDiskConfig;
import com.walkline.vdisk.vDiskSDK;

public class vDiskApp extends UiApplication
{
	private final String NEXT_URL = vDiskConfig.authorizeURL;
	private final String APPLICATION_ID = vDiskConfig.client_ID;
	private final String APPLICATION_SECRET = vDiskConfig.client_SECRET;

	private vDiskSDK _vDisk;
	private vDiskAppConfig _appConfig;

	public static void main(String[] args)
	{
		if (args != null && args.length > 0)
		{
			if (args[0].equals("startup"))
			{
				String newAppName = vDiskAppConfig.APP_TITLE;
				vDiskAppConfig config = new vDiskAppConfig();
				config.initialize(null);
				int keyIndex = config.getShortcutKey();

				if (keyIndex > 0)
				{
					String shortcut_key = ShortcutKey.choicesShortcutKeys[keyIndex];
					newAppName += "(" + shortcut_key + vDiskAppConfig.UNDERLINE + ")";

					HomeScreen.setName(newAppName);
				}
			} else if (args[0].equals("clean"))
			{
				vDiskAppConfig config = new vDiskAppConfig();
				
				config.clean();
			}
    	} else {
    		vDiskApp theApp = new vDiskApp();
    		theApp.enterEventDispatcher();
    	}
	}

	public vDiskApp()
	{
		_vDisk = vDiskSDK.getInstance(new ApplicationSettings(NEXT_URL, APPLICATION_ID, APPLICATION_SECRET));

		synchronized (Application.getEventLock())
		{
			_appConfig = new vDiskAppConfig();
			_appConfig.initialize(_vDisk);
		}

		_vDisk.setAppConfig(_appConfig);

		if (_appConfig.isAccountEmpty() && _appConfig.isAutoMode())
		{
			pushScreen(new LoginFullScreen(_vDisk, _appConfig));
		} else {
			pushScreen(new vDiskScreen(_vDisk, _appConfig));
		}
	}
}