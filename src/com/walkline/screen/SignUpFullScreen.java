package com.walkline.screen;

import com.walkline.app.vDiskAppConfig;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class SignUpFullScreen extends FullScreen
{
	protected ConnectionFactory lcf;
	protected BrowserFieldConfig bfc;
	protected BrowserField _bf;

	private VerticalFieldManager managerBrowswer = new VerticalFieldManager(VERTICAL_SCROLL | USE_ALL_HEIGHT);

	public SignUpFullScreen()
	{
		super(NO_VERTICAL_SCROLL);

		//setBackground(vDiskAppConfig.bgColor_GradientBlue);
		setBackground(BackgroundFactory.createSolidBackground(0xe7e7e7));

		lcf = new ConnectionFactory();
		lcf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		lcf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		lcf.setTimeoutSupported(true);
		lcf.setAttemptsLimit(10);
		lcf.setRetryFactor(2000);
		lcf.setConnectionTimeout(120000);

		bfc = new BrowserFieldConfig();
		bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
		bfc.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		bfc.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(Display.getWidth()));
		bfc.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, lcf);

		_bf = new BrowserField(bfc);
		//_listener = new MyBrowserFieldListener();
		//_bf.addListener(_listener);
		//((ProtocolController) _bf.getController()).setNavigationRequestHandler("http", this);
		//((ProtocolController) _bf.getController()).setResourceRequestHandler("http", this);
		//((ProtocolController) _bf.getController()).setNavigationRequestHandler("https", this);
		//((ProtocolController) _bf.getController()).setResourceRequestHandler("https", this);

		managerBrowswer.add(_bf);
		_bf.requestContent("http://m.weibo.cn/reg/index#email");
		add(managerBrowswer);
	}

	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);

		return true;
	}
}