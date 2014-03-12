package com.walkline.vdisk.dao;

import org.json.me.JSONObject;

import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;

public class vDiskObject implements com.walkline.vdisk.inf.Object
{
	protected vDiskSDK vDisk;
	protected JSONObject jsonObject;

	public vDiskObject(vDiskSDK pvDisk, JSONObject pJsonObject) throws vDiskException
	{
		if ((pvDisk == null) || (pJsonObject == null)) {
			throw new vDiskException("Unable to create vDiskSDK vDiskObject.");
		}
		vDisk = pvDisk;
		jsonObject = pJsonObject;
	}

	//public void fetch(boolean force)
	//{
	//	try {
	//		if (isStub() || force) {
	//			String id = getId();
	//			if ((id != null) && !id.trim().equals("")) {
	//				JSONObject jo;
	//				jo = fb.read(id.trim());
	//				if ((jo != null) && (jo.length() > 0)) {
	//					jsonObject = jo;
	//				}
	//			}
	//		}
	//	} catch (vDiskException e) {
	//		e.printStackTrace();
	//	}
	//}

	//public String getId() {
	//	return jsonObject.optString("id");
	//}

	public boolean isStub() {
		return false;
	}

	public void fetch(boolean force) {
		
	}
}