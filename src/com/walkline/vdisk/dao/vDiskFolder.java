package com.walkline.vdisk.dao;

import net.rim.device.api.i18n.SimpleDateFormat;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.Folder;

public class vDiskFolder extends vDiskObject implements Folder
{
	public vDiskFolder(vDiskSDK pvDisk, JSONObject pJsonObject)	throws vDiskException 
	{
		super(pvDisk, pJsonObject);
	}
    
    /**
     * size: "0 bytes"
     */
	public String getSize()
	{
		return jsonObject.optString("size");
	}

	/**
	 * hash: "eb78a93f30b7c902c20be2cadc1c5d11"
	 */
	public String getHash()
	{
		return jsonObject.optString("hash");
	}

	/**
	 * rev: "ab595f5a"
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
	 * bytes: "0"
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
	 * path: "/dd/ee"
	 */
	public String getPath()
	{
		return jsonObject.optString("path");
	}
	
	/**
	 * current path: "ee"
	 */
	public String getCurrentPath()
	{
		String[] pathStrings = StringUtility.split(getPath(), "/");
		
		return pathStrings[pathStrings.length-1];
	}

	public String getParentPath()
	{
		String[] pathStrings = StringUtility.split(getPath(), "/");
		
		return pathStrings[pathStrings.length-2];
	}
	
	/**
	 * is_dir: true
	 */
	public boolean isDir()
	{
		return jsonObject.optBoolean("is_dir");
	}

	/**
	 * root: "basic"
	 */
	public String getRoot() 
	{
		return jsonObject.optString("root");
	}

	/**
	 * icon: "folder"
	 */
	public String getIcon() 
	{
		return jsonObject.optString("icon");
	}

	/**
	 * revision: "115870075"
	 */
	public String getRevision()
	{
		return jsonObject.optString("revision");
	}
    
	/**
	 * is_deleted: false
	 */
	public boolean isDeleted()
	{
		return jsonObject.optBoolean("is_deleted");
	}

	public JSONArray getContents()
	{
		return jsonObject.optJSONArray("contents");
	}

	public String details()
	{
		return "Size: " + getSize() +
			   "\nHash: " + getHash() +
			   "\nRev: " + getRev() +
			   "\nThumb Exists: " + String.valueOf(isThumbExists()) +
			   "\nBytes: " + getBytes() +
			   "\nModified: " + getModified() +
			   "\nPath: " + getPath() +
			   "\nIs Dir: " + String.valueOf(isDir()) +
			   "\nRoot: " + getRoot() +
			   "\nIcon: " + getIcon() +
			   "\nRevision: " + getRevision() +
			   "\nIs Deleted: " + String.valueOf(isDeleted()) + 
			   "\nHas contents: " + String.valueOf(getContents() != null && getContents().length() > 0);
	}
}