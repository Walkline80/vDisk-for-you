package com.walkline.vdisk.inf;

import java.util.Vector;

public interface UploadFileInfo extends com.walkline.vdisk.inf.Object
{
	public String getUploadID();

	public String getUploadKey();

	public Vector getPartSigns();

	public String details();
}