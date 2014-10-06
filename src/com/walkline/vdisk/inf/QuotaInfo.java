package com.walkline.vdisk.inf;

public interface QuotaInfo extends com.walkline.vdisk.inf.Object
{
	public String getQuota();
	
	public String getReadableQuota();
	
	public String getConsumed();
	
	public String getReadableConsumed();
}