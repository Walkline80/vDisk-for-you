package com.walkline.vdisk.dao;

import org.json.me.JSONObject;

import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.QuotaInfo;

public class vDiskQuotaInfo extends vDiskObject implements QuotaInfo
{
	public vDiskQuotaInfo(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		super(pvDisk, pJsonObject);
	}

	public String getQuota()
	{
		return jsonObject.optString("quota");
	}

	public String getReadableQuota()
	{
		return StringUtility.formatSize(Double.parseDouble(jsonObject.optString("quota")), 1);
	}

	public String getConsumed()
	{
		return jsonObject.optString("consumed");
	}

	public String getReadableConsumed()
	{
		return StringUtility.formatSize(Double.parseDouble(jsonObject.optString("consumed")), 1);
	}
}