package com.walkline.vdisk.dao;

import net.rim.device.api.i18n.SimpleDateFormat;

import org.json.me.JSONObject;

import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.DirectLink;

public class vDiskDirectLink extends vDiskObject implements DirectLink
{
	private String _url = null;
	private String _expireTime = null;

	public vDiskDirectLink(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		super(pvDisk, pJsonObject);
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		_url = jsonObject.optString("url");
		_expireTime = sdf.formatLocal(StringUtility.parseDate(jsonObject.optString("expires")).getTime());
	}

	public String getURL() {return _url;}

	public String getExpireTime() {return _expireTime;}

	public String details()
	{
		return "URL: " + getURL() +
			   "\nExpire Time: " + getExpireTime();
	}
}