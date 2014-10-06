package com.walkline.vdisk.dao;

import java.util.Vector;

import org.json.me.JSONObject;

import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.File;
import com.walkline.vdisk.inf.UploadFileInfo;
import com.walkline.vdisk.inf.UploadFilePartSign;

public class vDiskUploadFileInfo extends vDiskObject implements UploadFileInfo
{
	private String _uploadKey;
	private String _uploadID;
	private Vector _partSigns = new Vector();
	private int _partCounts = 0;
	
	//public HashMap<Integer, String> partSigns;// collection of each segment's uri.
	public String s3Host;
	public String md5s;// all segment's md5 splited by ",".
	public String sha1;// sha1 of file.
	public int point;// identify which segment will to be uploaded，start from zero.
	public long expireTime;// upload key will expire in 2 days.
	public long segmentLength; // the length of each upload segment.
	public int segmentNum;

	public File metadata;
	/**
	 * If true, it means there's a same file already on server, you needn't
	 * upload it, server can copy one to your vdisk cloud.
	 */
	public boolean isBlitzUpload;

	public String srcPath; // local file path.
	public String desPath; // target server path.
	public String id; // the key to identify the file.

	public vDiskUploadFileInfo(vDiskSDK pvDisk, JSONObject pJsonObject, int partCounts)	throws vDiskException
	{
		super(pvDisk, pJsonObject);
		
		_uploadID = jsonObject.optString("upload_id");
		_uploadKey = jsonObject.optString("upload_key");
		
		JSONObject jsonSigns = jsonObject.optJSONObject("part_sign");
		if (jsonSigns != null && jsonSigns.length() > 0)
		{
			for (int i=1; i<partCounts+1; i++)
			{
				JSONObject jsonSign = jsonSigns.optJSONObject(Integer.toString(i));
				
				if (jsonSign != null && jsonSign.length() > 0)
				{
					_partSigns.addElement(jsonSign);	
				}
			}
		}

		_partCounts = partCounts;
	}

	public String getUploadID() {return _uploadID;}

	public String getUploadKey() {return _uploadKey;}

	public Vector getPartSigns() {return _partSigns;}
	
	/**
	 * Get number of part signs
	 * @return number of part signs 
	 */
	public int getCounts() {return _partCounts;}

	public String details()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("Upload ID: ").append(getUploadID());
		buffer.append("\nUpload Key: ").append(getUploadKey());

		for (int i=0; i<getCounts(); i++)
		{
			try {
				UploadFilePartSign partSign = vDisk.getUploadFilePartSign((JSONObject) getPartSigns().elementAt(i));
				buffer.append("\n[Part Number: ").append(partSign.getPartNumber());
				buffer.append("\n URI: ").append(partSign.getURI()).append("]");
			} catch (vDiskException e) {}
		}

		return buffer.toString();
	}

		/*
		protected VDiskUploadFileInfo(Map<String, Object> map, int segmentNum,
				String s3Host, long segmentLength, String sha1, String srcPath,
				String desPath) throws VDiskException {
			this.s3Host = s3Host;
			this.segmentLength = segmentLength;
			this.sha1 = sha1;
			this.segmentNum = segmentNum;
			this.srcPath = srcPath;
			this.desPath = desPath;
			setFileId();

			uploadKey = getFromMapAsString(map, "upload_key");
			uploadId = getFromMapAsString(map, "upload_id");

			if (uploadKey == null || uploadId == null) {
				metadata = new Entry(map);
				isBlitzUpload = true;
			} else {
				partSigns = new HashMap<Integer, String>();
				Object sectionInfo = map.get("part_sign");

				@SuppressWarnings("unchecked")
				Map<String, Object> sectionMap = (Map<String, Object>) sectionInfo;

				try {
					for (int i = 1; i <= segmentNum; i++) {
						@SuppressWarnings("unchecked")
						Map<String, Object> section = (Map<String, Object>) sectionMap
								.get(String.valueOf(i));
						String number = getFromMapAsString(section,
								"part_number");
						String uri = getFromMapAsString(section, "uri");
						partSigns.put(Integer.parseInt(number), uri);
					}

					expireTime = System.currentTimeMillis() + 2 * 24 * 60 * 60
							* 1000;
				} catch (NumberFormatException e) {
					throw new VDiskParseException(
							"Invalid segment info from server when uploading large file.");
				}
			}
		}

		private void setFileId() throws VDiskException {
			this.id = DigestFactory.md5String(srcPath + desPath);
		}

	}
*/
}