package com.walkline.util.ui;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.InputConnection;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldNavigationRequestHandler;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResourceRequestHandler;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.util.StringProvider;

import org.w3c.dom.Document;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;

public class BrowserScreen extends MainScreen implements BrowserFieldNavigationRequestHandler, BrowserFieldResourceRequestHandler
{
	//protected Logger log = Logger.getLogger(getClass());

	protected String _url;
	protected ConnectionFactory cf;
	protected BrowserFieldConfig bfc;
	protected BrowserField _bf;
	protected MyBrowserFieldListener _listener;

	protected VerticalFieldManager vfm;
	protected EvenlySpacedHorizontalFieldManager hfm1;
	protected EvenlySpacedHorizontalFieldManager hfm2;
	protected ProgressAnimationField spinner;

	private VerticalFieldManager managerBrowswer = new VerticalFieldManager(VERTICAL_SCROLL | USE_ALL_HEIGHT);

	//private LoadingScreen _loadingScreen = new LoadingScreen();
	private BitmapField _authorizeBitmap = new BitmapField(Bitmap.getBitmapResource("authorize.png"), FIELD_VCENTER); //, LabelField.VCENTER | LabelField.HCENTER);

	public BrowserScreen(String pUrl)
	{
		this(pUrl, new ConnectionFactory());
	}

	public BrowserScreen(String pUrl, ConnectionFactory pcf)
	{
		super(NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS | USE_ALL_HEIGHT);

		//UiApplication.getUiApplication().invokeLater(new Runnable()
		//{
		//	public void run()
		//	{
		//		UiApplication.getUiApplication().pushScreen(_loadingScreen);
		//	}
		//});

		//_spinner.setMargin(15, 15, 15, 15);

		hfm1 = new EvenlySpacedHorizontalFieldManager(USE_ALL_WIDTH);
		hfm1.add(_authorizeBitmap);

		hfm2 = new EvenlySpacedHorizontalFieldManager(USE_ALL_WIDTH);
		spinner = new ProgressAnimationField(Bitmap.getBitmapResource("spinner2.png"), 6, Field.FIELD_HCENTER);
		spinner.setMargin(15, 15, 15, 15);
		hfm2.add(spinner);

		vfm = new VerticalFieldManager(USE_ALL_WIDTH);
		vfm.add(hfm1);
		vfm.add(hfm2);

		showLoading();

		_url = pUrl;
		cf = pcf;

		bfc = new BrowserFieldConfig();
		bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		bfc.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(Display.getWidth()));
		bfc.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, cf);

		_bf = new BrowserField(bfc);
		_listener = new MyBrowserFieldListener();
		_bf.addListener(_listener);
		((ProtocolController) _bf.getController()).setNavigationRequestHandler("http", this);
		((ProtocolController) _bf.getController()).setResourceRequestHandler("http", this);
		((ProtocolController) _bf.getController()).setNavigationRequestHandler("https", this);
		((ProtocolController) _bf.getController()).setResourceRequestHandler("https", this);

		managerBrowswer.add(_bf);

		attachTransition(TransitionContext.TRANSITION_FADE);
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(0xe2e5e7));

		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run() {fetch();}
		});
	}

	protected void showLoading()
	{
		try {
			if (_authorizeBitmap.getScreen() == null)
			{
				synchronized (Application.getEventLock())
				{
					//add(_authorizeBitmap);
					//add(_loading);
					add(vfm);
				}
			}
		} catch (Throwable t) {}
	}

	protected void showContent()
	{
		try {
			if (managerBrowswer.getScreen() == null)
			{
				synchronized (Application.getEventLock())
				{
					add(managerBrowswer);
				}
			}

			if (vfm.getScreen() != null)
			{
				synchronized (Application.getEventLock())
				{
					//delete(_authorizeBitmap);
					//delete(_loading);
					delete(vfm);
				}
			}
			//if (_loadingScreen != null)
			//{
			//	_loadingScreen.close();
			//	_loadingScreen = null;
			//}
		} catch (Throwable t) {t.printStackTrace();}
	}

	protected void fetch() {_bf.requestContent(_url);}
	protected boolean onSavePrompt() {return true;}

	protected void dismiss() {
		if (isDisplayed()) {
			synchronized (Application.getEventLock()) {
				UiApplication.getUiApplication().popScreen(this);
			}
		}
	}

	public void handleNavigation(BrowserFieldRequest request) throws Exception
	{
		//log.info("BF-Navigate: " + request.getURL());
		
		if (shouldFetchContent(request))
		{
			request.setURL(StringUtility.fixHttpsUrlPrefix(request.getURL()));
			_bf.displayContent(handleResource(request), request.getURL());
		}
	}

	public InputConnection handleResource(BrowserFieldRequest request) throws Exception
	{
		InputConnection conn = _bf.getConnectionManager().makeRequest(request);

		if ((conn != null) && (conn instanceof HttpConnection))
		{
			processHttpConnection((HttpConnection) conn);
		} else if ((conn != null) && (conn instanceof HttpsConnection)) {
			processHttpsConnection((HttpsConnection) conn);
		}

		return conn;
	}

	MenuItem menuRefresh = new MenuItem(new StringProvider("Refresh"), 100, 10)
	{
		public void run()
		{
			_bf.refresh();
		}
	};
	
	MenuItem menuGetUrl = new MenuItem(new StringProvider("Get URL"), 100, 20)
	{
		public void run()
		{
			Function.errorDialog(_bf.getDocumentUrl());
		}
	};
	
	protected void makeMenu(Menu menu, int instance)
	{
		menu.add(menuRefresh);
		menu.addSeparator();
		menu.add(menuGetUrl);

		super.makeMenu(menu, instance);
	}
	
	protected boolean processHttpConnection(HttpConnection conn) {return true;}
	protected boolean processHttpsConnection(HttpsConnection conn) {return true;}
	protected boolean shouldFetchContent(BrowserFieldRequest request) {return true;}
	protected boolean shouldShowContent(BrowserField pbf, Document pdoc) {return true;}
	protected boolean postProcessing(BrowserField pbf, Document pdoc) {return true;}

	protected class MyBrowserFieldListener extends BrowserFieldListener
	{
		public void documentLoaded(BrowserField browserField, Document document) throws Exception
		{
			if (shouldShowContent(browserField, document)) {showContent();}
			
			postProcessing(browserField, document);
		}

		public void documentAborted(BrowserField browserField, Document document) throws Exception {}
		public void documentCreated(BrowserField browserField, Document document) throws Exception {}
		public void documentError(BrowserField browserField, Document document) throws Exception {}
		public void documentUnloading(BrowserField browserField, Document document) throws Exception {}
		public void downloadProgress(BrowserField browserField, Document document) throws Exception {}
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
		synchronized (Application.getEventLock())
		{
			UiApplication.getUiApplication().popScreen(this);	
		}
		
		return true;
	}

	protected void attachTransition(int transitionType)
	{
		UiEngineInstance engine = Ui.getUiEngineInstance();
		TransitionContext pushAction = null;
		TransitionContext popAction = null;

		switch (transitionType) {

		case TransitionContext.TRANSITION_FADE:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_FADE);
			popAction = new TransitionContext(TransitionContext.TRANSITION_FADE);
			pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			break;

		case TransitionContext.TRANSITION_NONE:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
			popAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
			break;

		case TransitionContext.TRANSITION_SLIDE:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
			popAction = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
			pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			pushAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
			popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			popAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
			break;

		case TransitionContext.TRANSITION_WIPE:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_WIPE);
			popAction = new TransitionContext(TransitionContext.TRANSITION_WIPE);
			pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			pushAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
			popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
			popAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
			break;

		case TransitionContext.TRANSITION_ZOOM:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
			popAction = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
			pushAction.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
			popAction.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
			break;

		default:
			pushAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
			popAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
		}

		engine.setTransition(null, this, UiEngineInstance.TRIGGER_PUSH, pushAction);
		engine.setTransition(this, null, UiEngineInstance.TRIGGER_POP, popAction);
	}
	
	public class LoadingScreen extends PopupScreen
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