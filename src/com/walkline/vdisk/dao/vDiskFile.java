package com.walkline.vdisk.dao;

import net.rim.device.api.i18n.SimpleDateFormat;

import org.json.me.JSONObject;

import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.File;

public class vDiskFile extends vDiskObject implements File
{
	public vDiskFile(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		super(pvDisk, pJsonObject);
	}

	/**
	 * size: "2.81 KB"
	 */
	public String getSize()
	{
		return jsonObject.optString("size"); 
	}

	/**
	 * rev: "112834369"
	 */
	public String getRev()
	{
		return jsonObject.optString("rev"); 
	}

	/**
	 * thumb_exists: false
	 */
	public boolean isThumbExists()
	{
		return jsonObject.optBoolean("thumb_exists"); 
	}

	/**
	 * bytes: "2875"
	 */
	public String getBytes()
	{
		return jsonObject.optString("bytes"); 
	}

	/**
	 * modified: "Wed, 26 Dec 2012 11:34:30 +0000"
	 */
	public String getModified()
	{
		return jsonObject.optString("modified"); 
	}

	/**
	 * get readable date string
	 */
	public String getModifiedDate()
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
		return sdf.formatLocal(StringUtility.parseDate(getModified()).getTime());
	}
	
	/**
	 * path: "/dd/t.txt"
	 */
	public String getPath()
	{
		return jsonObject.optString("path"); 
	}

	/**
	 * current path: "dd"
	 */
	public String getCurrentPath()
	{
		String[] pathStrings = StringUtility.split(getPath(), "/");
		
		return pathStrings[pathStrings.length-2];
	}
	
	/**
	 * finename: "t.txt"
	 */
	public String getFilename()
	{
		String[] pathStrings = StringUtility.split(getPath(), "/");
		
		return pathStrings[pathStrings.length-1];
	}
	
	/**
	 * is_dir: false,
	 */
	public boolean isDir()
	{
		return jsonObject.optBoolean("is_dir"); 
	}

	/**
	 * icon: "page_white_text"
	 */
	public String getIcon()
	{
		return jsonObject.optString("icon"); 
	}

	/**
	 * root: "basic"
	 */
	public String getRoot()
	{
		return jsonObject.optString("root"); 
	}

	/**
	 * mime_type: "text/plain"
	 */
	public String getMimeType()
	{
		return jsonObject.optString("mime_type"); 
	}

	/**
	 * revision: "363379700"
	 */
	public String getRevision()
	{
		return jsonObject.optString("revision"); 
	}

	/**
	 * md5: "b0547b5af57d9f765e42f66a170b39e1"
	 */
	public String getMD5()
	{
		return jsonObject.optString("md5"); 
	}

	/**
	 * sha1: "7df3a1fd477def8a8c071d4a7adbe29d97b0416c"
	 */
	public String getSha1()
	{
		return jsonObject.optString("sha1"); 
	}

	/**
	 * is_deleted: false
	 */
	public boolean isDeleted()
	{
		return jsonObject.optBoolean("is_deleted"); 
	}

	public String details()
	{
		return "Size: " + getSize() +
			 "\nRev: " + getRev() +
			 "\nThumb Exists: " + String.valueOf(isThumbExists()) +
			 "\nBytes: " + getBytes() +
			 "\nModified: " + getModified() +
			 "\nPath: " + getPath() +
			 "\nIs Dir: " + String.valueOf(isDir()) +
			 "\nIcon: " + getIcon() +
			 "\nRoot: " + getRoot() +
			 "\nMime Type: " + getMimeType() +
			 "\nRevision: " + getRevision() +
			 "\nMD5: " + getMD5() +
			 "\nSHA1: " + getSha1() +
			 "\nIs Deleted: " + String.valueOf(isDeleted());
	}
}