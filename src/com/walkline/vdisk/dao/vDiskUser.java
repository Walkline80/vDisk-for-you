package com.walkline.vdisk.dao;

import org.json.me.JSONObject;

import com.walkline.util.Function;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.QuotaInfo;
import com.walkline.vdisk.inf.User;

public class vDiskUser extends vDiskObject implements User
{
	private String _uid = null;
	private String _sinaUID = null;
	private boolean _verified = false;
	private String _screenName = null;
	private String _userName = null;
	private String _location = null;
	private String _profileImageUrl = null;
	private String _avatarLargeUrl = null;
	private String _gender = null;
	private QuotaInfo _quotaInfo = null;
	private byte[] _avatarData = null;

	public vDiskUser(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		super(pvDisk, pJsonObject);
		
		_uid = jsonObject.optString("uid");
		_sinaUID = jsonObject.optString("sina_uid");
		_verified = jsonObject.optBoolean("verified");
		_screenName = jsonObject.optString("screen_name");
		_userName = jsonObject.optString("user_name");
		_location = jsonObject.optString("location");
		_profileImageUrl = jsonObject.optString("profile_image_url");
		_avatarLargeUrl = jsonObject.optString("avatar_large");
		_gender = jsonObject.optString("gender");
		
		try {
			JSONObject jo = jsonObject.optJSONObject("quota_info");
			if ((jo != null) && (jo.length() > 0)) {
				_quotaInfo = vDisk.getQuotaInfo(jo);
			}
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		}
	}

	public String getUID() {return _uid;}

	public String getSinaUID() {return _sinaUID;}

	public boolean getVerified() {return _verified;}

	public String getScreenName() {return _screenName;}

	public String getUserName() {return _userName;}

	public String getLocation() {return _location;}

	public String getProfileImageUrl() {return _profileImageUrl;}

	public String getAvatarLargeUrl() {return _avatarLargeUrl;}

	public String getGender() {return _gender;}

	public QuotaInfo getQuotaInfo() {return _quotaInfo;}

	public byte[] getAvatarData() {return _avatarData;}

	public void setAvatarData(byte[] data) {_avatarData = data;}

	public String details()
	{
		return "UID: " + getUID() +
			   "\nSina UID: " + getSinaUID() +
			   "\nQuota: " + getQuotaInfo().getQuota() +
			   "\nReadable Quota: " + getQuotaInfo().getReadableQuota() +
			   "\nConsumed: " + getQuotaInfo().getConsumed() +
			   "\nReadable Consumed: " + getQuotaInfo().getReadableConsumed() +
			   "\nVerified: " + String.valueOf(getVerified()) +
			   "\nScreen Name: " + getScreenName() +
			   "\nUser Name: " + getUserName() +
			   "\nLocation: " + getLocation() +
			   "\nImage URL: " + getProfileImageUrl() +
			   "\nAvatar URL: " + getAvatarLargeUrl() +
			   "\nGender: " + getGender();
	}
}