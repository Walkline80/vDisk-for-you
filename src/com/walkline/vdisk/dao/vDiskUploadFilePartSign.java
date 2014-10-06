package com.walkline.vdisk.dao;

import org.json.me.JSONObject;

import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.UploadFilePartSign;

public class vDiskUploadFilePartSign extends vDiskObject implements UploadFilePartSign
{
	public vDiskUploadFilePartSign(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		super(pvDisk, pJsonObject);
	}

	public String getPartNumber()
	{
		return jsonObject.optString("part_number");
	}

	public String getURI()
	{
		return jsonObject.optString("uri");
	}
}