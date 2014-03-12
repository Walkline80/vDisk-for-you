package com.walkline.vdisk.inf;

import org.json.me.JSONArray;

public interface Folder extends com.walkline.vdisk.inf.Object
{
	public String getSize();

	public String getHash();

	public String getRev();
	
	public boolean isThumbExists();
	
	public String getBytes();

	public String getModified();
	
	public String getModifiedDate();

	public String getPath();
	
	public String getCurrentPath();
	
	public String getParentPath();
	
	public boolean isDir();
	
	public String getRoot();

	public String getIcon();

	public String getRevision();
	
	public boolean isDeleted();
	
	public JSONArray getContents();

	public String details();
}