package com.walkline.util;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

public class Function
{
    public static void errorDialog(final String message)
    {
        UiApplication.getUiApplication().invokeAndWait(new Runnable()
        {
            public void run()
            {
                Dialog.alert(message);
            } 
        });
    }

	/**
	 * Used to show a url of the shared file, 
	 * and copy url to clipboard.
	 *  
	 * @param fileName shared file name
	 * @param url shared url
	 * 
	 * @author <a href="http://blog.csdn.net/Walkline">Walkline</a>
	 */
	public static void showSharedFileUrl(final String fileName, final String url)
	{
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				String[] choices = {"Copy"};
				final Dialog showUrlDialog = new Dialog("Filename: " + fileName + "\n\nShared link: " + url, choices, null, 0, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
				showUrlDialog.setDialogClosedListener(new DialogClosedListener()
				{
					public void dialogClosed(Dialog dialog, int choice)
					{
						if (choice == 0)
						{
							Clipboard cb=Clipboard.getClipboard();
							cb.put(new String(url));
						}
					}
				});
				showUrlDialog.show();
			}
		});
	}

	/**
	 * Used to show file direct link url and expire time of the selected file, 
	 * and copy url to clipboard.
	 *  
	 * @param fileName selected file name
	 * @param url file direct link url
	 * @param expireTime expired time of link
	 * 
	 * @author <a href="http://blog.csdn.net/Walkline">Walkline</a>
	 */
	public static void showFileDirectLinkUrl(final String fileName, final String url, final String expireTime)
	{
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
				String[] choices = {"Copy"};
				final Dialog showUrlDialog = new Dialog("Filename: " + fileName + "\n\nDirect link: " + url + "\nExpire time: " + expireTime, choices, null, 0, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
				showUrlDialog.setDialogClosedListener(new DialogClosedListener()
				{
					public void dialogClosed(Dialog dialog, int choice)
					{
						if (choice == 0)
						{
							Clipboard cb=Clipboard.getClipboard();
							cb.put(new String(url));
						}
					}
				});
				showUrlDialog.show();
			}
		});
	}
}