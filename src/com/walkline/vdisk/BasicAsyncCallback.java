package com.walkline.vdisk;

import java.util.Date;

import net.rim.device.api.system.Bitmap;

import org.json.me.JSONObject;

import com.walkline.vdisk.inf.DirectLink;
import com.walkline.vdisk.inf.Folder;
import com.walkline.vdisk.inf.User;

public class BasicAsyncCallback implements AsyncCallback
{
	public void onComplete(com.walkline.vdisk.inf.Object[] values, Object state) {}

	public void onComplete(String value, Object state) {}

	public void onComplete(String[] values, Object state) {}

	public void onComplete(int[] values, Object state) {}

	public void onComplete(double[] values, Object state) {}

	public void onComplete(boolean[] values, Object state) {}

	public void onComplete(Date[] values, Object state) {}

	public void onComplete(Bitmap[] values, Object state) {}

	public void onComplete(User value, Object state) {}

	public void onComplete(JSONObject value, Object state) {}

	public void onComplete(Folder value, Object state) {}

	public void onComplete(DirectLink value, Object state) {}

	public void onException(Exception e, Object state) {}
}