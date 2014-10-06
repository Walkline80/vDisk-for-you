package com.walkline.screen;

import java.io.IOException;

import javax.microedition.content.ContentHandler;
import javax.microedition.content.ContentHandlerException;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import localization.vDiskSDKResource;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.ui.VerticalButtonFieldSet;

public class AboutMainScreen extends PopupScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	public AboutMainScreen()
	{
		super(new VerticalFieldManager(FOCUSABLE | NO_VERTICAL_SCROLL));
		
		setBorder(vDiskAppConfig.border_popup_Transparent);
		setBackground(vDiskAppConfig.bg_popup_Transparent);

    	LabelField labelAbout = new LabelField(getResString(ABOUT_TITLE), USE_ALL_WIDTH | LabelField.ELLIPSIS);
    	labelAbout.setFont(vDiskAppConfig.FONT_ABOUT_TITLE);
    	labelAbout.setPadding(0, 0, 1, 0);

    	LabelField labelTitle = new LabelField(vDiskAppConfig.APP_TITLE, USE_ALL_WIDTH | LabelField.ELLIPSIS);
    	labelTitle.setFont(vDiskAppConfig.FONT_ABOUT_LARGE);

    	LabelField labelVersion = new LabelField(getResString(ABOUT_VERSION) + ApplicationDescriptor.currentApplicationDescriptor().getVersion(), USE_ALL_WIDTH | LabelField.ELLIPSIS | LabelField.RIGHT);
    	labelVersion.setFont(vDiskAppConfig.FONT_ABOUT_SMALL);

		VerticalFieldManager vfmTitle = new VerticalFieldManager(FIELD_VCENTER);
    	vfmTitle.add(labelAbout);
    	vfmTitle.add(new SeparatorField());
    	vfmTitle.add(labelTitle);
    	vfmTitle.add(labelVersion);
    	
		VerticalFieldManager vfmContent = new VerticalFieldManager(VERTICAL_SCROLL);
		HorizontalFieldManager horizontalContactTitle = new HorizontalFieldManager();
		HorizontalFieldManager horizontalShortcutTitle = new HorizontalFieldManager();
		VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);

    	LabelField labelIntro = new LabelField(getResString(ABOUT_INTRO));

    	LabelField labelContact = new LabelField(getResString(ABOUT_CONTACT), USE_ALL_WIDTH | LabelField.ELLIPSIS);
    	labelContact.setFont(vDiskAppConfig.FONT_ABOUT_HEADLINE);

    	LabelField labelAuthor = addLabel(getResString(ABOUT_CONTACT_AUTHOR));
    	LabelField labelEmail = addLabel(getResString(ABOUT_CONTACT_EMAIL));
    	LabelField labelWeibo = addLabel(getResString(ABOUT_CONTACT_WEIBO));

    	LabelField labelShortcut = new LabelField(getResString(ABOUT_SHORTCUTKEYS), USE_ALL_WIDTH | LabelField.ELLIPSIS);
    	labelShortcut.setFont(vDiskAppConfig.FONT_ABOUT_HEADLINE);
    	
    	LabelField shortB=addLabel(getResString(ABOUT_TIP_OF_KEY_B));
    	LabelField shortT=addLabel(getResString(ABOUT_TIP_OF_KEY_T));
    	LabelField shortR=addLabel(getResString(ABOUT_TIP_OF_KEY_R));
    	LabelField shortN=addLabel(getResString(ABOUT_TIP_OF_KEY_N));
    	LabelField shortA=addLabel(getResString(ABOUT_TIP_OF_KEY_A));
    	LabelField shortD=addLabel(getResString(ABOUT_TIP_OF_KEY_D));
    	LabelField shortX=addLabel(getResString(ABOUT_TIP_OF_KEY_X));
    	LabelField shortU=addLabel(getResString(ABOUT_TIP_OF_KEY_U));
    	LabelField shortDel=addLabel(getResString(ABOUT_TIP_OF_KEY_DEL));
    	LabelField shortEsc=addLabel(getResString(ABOUT_TIP_OF_KEY_ESC));
    	
    	ButtonField btnWriteAReview = new ButtonField(getResString(MENU_BBWORLD_WRITE_A_REVIEW), ButtonField.NEVER_DIRTY | ButtonField.CONSUME_CLICK);
    	btnWriteAReview.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context)
			{
				try
	            {
	                openAppWorld(vDiskAppConfig.BBW_APPID);
	            } catch(final Exception e)
	            {
	                UiApplication.getUiApplication().invokeLater(new Runnable()
	                {
	                    public void run()
	                    {
	                    	if(e instanceof ContentHandlerException)
	                    	{
	                    		Dialog.alert("BlackBerry World is not installed!");
	                    	} else {
	                    		Dialog.alert("Problems opening App World: " + e.getMessage());
	                    	}
	                    }
	                });
	            }
			}
		});

    	ButtonField btnBrowseOtherApps = new ButtonField(getResString(ABOUT_BBW_BROWSE_OTHER_APPS), ButtonField.NEVER_DIRTY | ButtonField.CONSUME_CLICK);
    	btnBrowseOtherApps.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context)
			{
				BrowserSession browser=Browser.getDefaultSession();
	    		browser.displayPage("http://appworld.blackberry.com/webstore/vendor/69061");
			}
		});
    	
    	horizontalContactTitle.add(labelContact);
    	horizontalContactTitle.add(new LabelField("", LabelField.FOCUSABLE));
    	vfmContent.add(labelIntro);
    	vfmContent.add(new LabelField());
    	vfmContent.add(horizontalContactTitle);
    	vfmContent.add(labelAuthor);
    	vfmContent.add(labelEmail);
    	vfmContent.add(labelWeibo);
    	vfmContent.add(new LabelField());
    	
    	horizontalShortcutTitle.add(labelShortcut);
    	horizontalShortcutTitle.add(new LabelField("", LabelField.FOCUSABLE));
    	vfmContent.add(horizontalShortcutTitle);
    	vfmContent.add(shortB);
    	vfmContent.add(shortT);
    	vfmContent.add(shortN);
    	vfmContent.add(shortD);
    	vfmContent.add(shortA);
    	vfmContent.add(shortR);
    	vfmContent.add(shortX);
    	vfmContent.add(shortU);
    	vfmContent.add(shortDel);
    	vfmContent.add(shortEsc);
    	vfmContent.add(new LabelField());
    	vbf.add(btnWriteAReview);
    	vbf.add(btnBrowseOtherApps);
    	vfmContent.add(vbf);
    	
    	add(vfmTitle);
    	add(vfmContent);
	}

	private String getResString(int key) {return _bundle.getString(key);}

	protected void openAppWorld(String myContentId) throws IllegalArgumentException, ContentHandlerException, SecurityException, IOException 
    {
        Registry registry = Registry.getRegistry(vDiskScreen.class.getName());
        Invocation invocation = new Invocation( null, null, "net.rim.bb.appworld.Content", true, ContentHandler.ACTION_OPEN );

        invocation.setArgs(new String[] {myContentId});

        boolean mustExit = registry.invoke(invocation);
        if(mustExit) {UiApplication.getUiApplication().popScreen(this);}
    }
	
	private LabelField addLabel(String label)
	{
		return new LabelField(label, USE_ALL_WIDTH | LabelField.ELLIPSIS);
	}

	protected void paintBackground(Graphics g) {}

	protected boolean keyChar(char character, int status, int time)
	{
		switch (character)
		{
			case Characters.ESCAPE:
				UiApplication.getUiApplication().popScreen(this);
				return true;
		}

		return super.keyChar(character, status, time);
	}
}