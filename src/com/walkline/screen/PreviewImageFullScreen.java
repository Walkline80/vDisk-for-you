package com.walkline.screen;

import localization.vDiskSDKResource;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.w3c.dom.Document;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Function;
import com.walkline.util.ui.ProgressAnimationField;
import com.walkline.vdisk.BasicAsyncCallback;
import com.walkline.vdisk.vDiskSDK;

public class PreviewImageFullScreen extends FullScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private BrowserFieldConfig bfc;
	private BrowserField _browserField;
	//private LoggableConnectionFactory lcf;
	ConnectionFactory cf;

	private vDiskSDK _vDisk;
	private String _filePath;
	private LoadingImageThread _loadingImage = new LoadingImageThread();
	private LoadingScreen _loadingScreen = new LoadingScreen();
	private FullScreen _main;

	public PreviewImageFullScreen(vDiskSDK vDisk, String filePath)
	{
		super(VERTICAL_SCROLL | HORIZONTAL_SCROLL | NO_SYSTEM_MENU_ITEMS);
		_main = this;

		_vDisk = vDisk;
		_filePath = filePath;

		cf = new ConnectionFactory();
		cf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		cf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		cf.setTimeoutSupported(true);
		cf.setAttemptsLimit(10);
		cf.setRetryFactor(2000);
		cf.setConnectionTimeout(120000);

		bfc = new BrowserFieldConfig();
		bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		bfc.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(Display.getWidth()));
		bfc.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, cf);

		_browserField = new BrowserField(bfc);
		_browserField.addListener(new MyBrowserFieldListener());

		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				UiApplication.getUiApplication().pushScreen(_loadingScreen);
				_loadingImage.start();
			}
		});
	}
	
	protected boolean keyChar(char character, int status, int time)
	{
		switch (character)
		{
			case Characters.ESCAPE:
				onClose();
				return true;
		}
		return super.keyChar(character, status, time);
	}
	
	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);
		
		return true;
	}

	private void showContent()
	{
		if (_browserField.getScreen() == null)
		{
			synchronized (UiApplication.getEventLock())
			{
				_main.add(_browserField);
			}
		}
		
		if (_loadingScreen != null)
		{
			_loadingScreen.onClose();
			_loadingScreen = null;
		}
	}
	
	private String getResString(int key)
	{
		return _bundle.getString(key);
	}

	public class LoadingImageThread extends Thread
	{
		public void run()
		{
			BasicAsyncCallback basicCallback = new BasicAsyncCallback()
			{
				public void onComplete(String value, final Object state)
				{
					if (value != null)
					{
						_browserField.requestContent(value);
					} else {
						if (_loadingScreen != null)
						{
							_loadingScreen.onClose();
							_loadingScreen = null;
						}

						Function.errorDialog(getResString(MESSAGE_ERROR_GET_PREVIEW_IMAGE_URL_FAILED));
					}
				}

				public void onException(Exception e, final Object state)
				{
					Function.errorDialog(e.toString());
				}
			};

			try {
				_vDisk.getPreviewImageUrl(_filePath, basicCallback, null);	
			} catch (Exception e) {
				Function.errorDialog(e.toString());
			}
		}
	}
	
	private class MyBrowserFieldListener extends BrowserFieldListener
	{
		public void documentLoaded(BrowserField browserField, Document document) throws Exception
		{
			showContent();
		}

		public void documentAborted(BrowserField browserField, Document document) throws Exception {}
		public void documentError(BrowserField browserField, Document document) throws Exception {}
		public void documentUnloading(BrowserField browserField, Document document) throws Exception {}
	}
	
	private class LoadingScreen extends PopupScreen
	{
		private ProgressAnimationField spinner;
		
		public LoadingScreen()
		{
			super(new VerticalFieldManager());
			
			setBorder(vDiskAppConfig.border_Transparent);
			setBackground(vDiskAppConfig.bg_Transparent);

			spinner = new ProgressAnimationField(Bitmap.getBitmapResource("spinner2.png"), 6, 0);
			spinner.setMargin(15, 15, 15, 15);

			add(spinner);
		}
		
		protected void paintBackground(Graphics g) {}
		
		protected boolean keyChar(char character, int status, int time)
		{
			switch (character)
			{
				case Characters.ESCAPE:
					onClose();
					return true;
			}

			return super.keyChar(character, status, time);
		}
		
		public boolean onClose()
		{
			synchronized (Application.getEventLock())
			{
				UiApplication.getUiApplication().popScreen(this);	
			}

			return true;
		}
	}
}