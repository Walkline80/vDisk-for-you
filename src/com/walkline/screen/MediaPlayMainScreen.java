package com.walkline.screen;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import localization.vDiskSDKResource;
import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.File;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.w3c.dom.Document;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.FileUtility;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.util.ui.ProgressAnimationField;
import com.walkline.vdisk.BasicAsyncCallback;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.DirectLink;

public class MediaPlayMainScreen extends MainScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private BrowserFieldConfig bfc;
	private BrowserField _browserField;
	//private LoggableConnectionFactory lcf;
	ConnectionFactory cf;

	private vDiskSDK _vDisk;
	private String _filePath;
	private String _url;
	private GetMediaURLThread _getMedia = new GetMediaURLThread();
	private LoadingScreen _loadingScreen = new LoadingScreen();
	private FullScreen _main;

	public MediaPlayMainScreen(vDiskSDK vDisk, String filePath)
	{
		super(VERTICAL_SCROLL | HORIZONTAL_SCROLL | NO_SYSTEM_MENU_ITEMS);
		_main = this;

		_vDisk = vDisk;
		_filePath = filePath;
		_url = "";

		cf = new ConnectionFactory();
		cf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		cf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		cf.setTimeoutSupported(true);
		cf.setAttemptsLimit(10);
		cf.setRetryFactor(2000);
		cf.setConnectionTimeout(120000);

		HttpHeaders headers = new HttpHeaders();
		headers.addProperty(HttpHeaders.HEADER_CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_TEXT_HTML);
		headers.addProperty(HttpHeaders.HEADER_ACCEPT_CHARSET, "UTF-8");

		bfc = new BrowserFieldConfig();
		bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		bfc.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(Display.getWidth()));
		bfc.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, cf);
		bfc.setProperty(BrowserFieldConfig.HTTP_HEADERS, headers);

		_browserField = new BrowserField(bfc);

		add(_browserField);
		//_browserField.addListener(new MyBrowserFieldListener());

		_getMedia.start();

		/*
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				UiApplication.getUiApplication().pushScreen(_loadingScreen);
				_getMedia.start();
			}
		});
		*/
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
	
	private String getResString(int key) {return _bundle.getString(key);}

	public class GetMediaURLThread extends Thread
	{
		public void run()
		{
			BasicAsyncCallback basicCallback = new BasicAsyncCallback()
			{
				public void onComplete(DirectLink value, final Object state)
				{
					if (value != null)
					{
						_url = value.getURL();

						String mediaPlayer = "<html><head><meta name=\"viewport\" content=\"width=device-width,height=device-height,initial-scale=1.0\"></head><body>Hello<video controls autoplay loop src=\"#url#\" width=\"#width#\" height=\"#height#\">Your browser can't support HTML5 video</video></body></html>";
						mediaPlayer = StringUtility.replace(mediaPlayer, "#url#", _url);

						mediaPlayer = StringUtility.replace(mediaPlayer, "#width#", String.valueOf(Display.getWidth()));
						mediaPlayer = StringUtility.replace(mediaPlayer, "#height#", String.valueOf(Display.getHeight()));

						try {
							FileConnection file;
							OutputStream output = null;
							file = (FileConnection) Connector.open("file:///SDCard/url.html");

							if (!file.exists()) {file.create();}
							file.setWritable(true);

							output = file.openOutputStream();
							output.write(mediaPlayer.getBytes());
							output.flush();
							output.close();
							file.close();
						} catch (Exception e) {
						}

						//Function.errorDialog(mediaPlayer);
						//_browserField.displayContent(mediaPlayer, "");
						
					} else {
						Function.errorDialog("Get file direct link failed.");
					}
				}
				
				public void onException(Exception e, final Object state)
				{
					Function.errorDialog(e.toString());
				}
			};

			try {
				_vDisk.getFileDirectLink(_filePath, basicCallback, null);	
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