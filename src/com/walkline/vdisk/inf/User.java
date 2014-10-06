package com.walkline.vdisk.inf;

public interface User extends com.walkline.vdisk.inf.Object
{
	public String getUID();

	public String getSinaUID();

	public QuotaInfo getQuotaInfo();

	public boolean getVerified();

	public String getScreenName();

	public String getUserName();

	public String getLocation();

	public String getProfileImageUrl();

	public String getAvatarLargeUrl();

	public byte[] getAvatarData();
	
	public void setAvatarData(byte[] data);
	
	public String getGender();
	
	public String details();
}