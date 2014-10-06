package com.walkline.vdisk.inf;

public interface File extends com.walkline.vdisk.inf.Object
{
	public String getSize();
	
	public String getRev();
	
	public boolean isThumbExists();
	
	public String getBytes();

	public String getModified();
	
	public String getModifiedDate();
	
	public String getPath();
	
	public String getCurrentPath();
	
	public String getFilename();
	
	public boolean isDir();
	
	public String getIcon();
	
	public String getRoot();
	
	public String getMimeType();
		
	public String getRevision();
	
	public String getMD5();
	
	public String getSha1();
	
	public boolean isDeleted();
	
	public String details();
}