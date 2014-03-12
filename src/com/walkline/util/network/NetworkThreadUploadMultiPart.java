package com.walkline.util.network;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import localization.vDiskSDKResource;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.ui.UiApplication;

import org.json.me.JSONObject;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Digest;
import com.walkline.util.Function;
import com.walkline.vdisk.vDiskConfig;
import com.walkline.vdisk.vDiskException;
import com.walkline.vdisk.vDiskSDK;
import com.walkline.vdisk.inf.UploadFileInfo;
import com.walkline.vdisk.inf.UploadFilePartSign;

public class NetworkThreadUploadMultiPart extends Thread implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private vDiskSDK _vDisk;
	private ProgressListener _ourObserver;
	private String _filePath;
	private String _fileURI;
	private boolean _stopRequest = false;
	private HttpMultipartRequest _request;

	private ConnectionFactory cf;
	//private Logger log;
	
	public NetworkThreadUploadMultiPart(vDiskSDK vDisk, String filePath, String fileURI, ProgressListener observer)
	{
		super();

		//log = Logger.getLogger(getClass());

		cf = new ConnectionFactory();
		cf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		cf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		cf.setTimeoutSupported(true);
		cf.setAttemptsLimit(10);
		cf.setRetryFactor(2000);
		cf.setConnectionTimeout(120000);

		_vDisk = vDisk;
		_filePath = filePath;
		_fileURI = fileURI;
		_ourObserver = observer;
	}

	public void stop()
	{
		observerError(ProgressListener.CANCELLED, getResString(MESSAGE_INFO_USER_CANCELLED));
		_stopRequest = true;
		if (_request != null) {_request.stop();}

		Thread.currentThread().interrupt();	
	}

	private void observerStatusUpdate(final int status, final String statusString)
	{
		if (!_stopRequest) {_ourObserver.processStatusUpdate(status, statusString);}
	}

	private void observerError(int errorCode, String errorMessage)
	{
		if (!_stopRequest) {_ourObserver.processError(errorCode, errorMessage);}
	}

	private void observerResponse(String reply)
	{
		if (!_stopRequest) {_ourObserver.processResponse(reply);}
	}

	public void run ()
	{
		JSONObject result = null;
		UploadFileInfo uploadFileInfo = null;
		FileConnection fileConn = null;
		StringBuffer bufferMD5 = new StringBuffer();

		try {
			observerStatusUpdate(0, getResString(MESSAGE_INFO_GETTING_SEGMENT_INFO));

			fileConn = (FileConnection)Connector.open(_fileURI, Connector.READ);
			int fileSize = (int) fileConn.fileSize();
			int segmentNum = 1;

			if ((fileSize % _vDisk.getUploadSectionSize()) > 0)
			{
				segmentNum = (int) (fileSize /  _vDisk.getUploadSectionSize() + 1);
			} else {
				segmentNum = (int) (fileSize / _vDisk.getUploadSectionSize());
			}

			uploadFileInfo = _vDisk.getMultiPartFileInfo(_filePath, segmentNum, fileSize);

			InputStream input = fileConn.openInputStream();
			byte[] segmentBytes = new byte[_vDisk.getUploadSectionSize()];
			int len = 0;
			int blockSize = 0;

			observerStatusUpdate(1, getResString(MESSAGE_INFO_UPLOADING_SEGMENT));

			for (int i=0; i<segmentNum; i++)
			{
				if (_stopRequest)
				{
					if (_request != null) {_request.stop();}
					observerError(ProgressListener.CANCELLED, getResString(MESSAGE_INFO_USER_CANCELLED));
				}

				String md5;
				if (i == (segmentNum - 1))
				{
					blockSize = fileSize % _vDisk.getUploadSectionSize();
					segmentBytes = new byte[blockSize];

					len = input.read(segmentBytes);
					md5 = Digest.md5Hash(segmentBytes, len);
				} else {
					len = input.read(segmentBytes);
					md5 = Digest.md5Hash(segmentBytes, len);
				}

				bufferMD5.append(md5);
				if (i != segmentNum-1) {bufferMD5.append(",");}

				UploadFilePartSign segmentPart = _vDisk.getUploadFilePartSign((JSONObject) uploadFileInfo.getPartSigns().elementAt(i));
				String uploadUri = "http://" + vDiskConfig.SINA_STORAGE_SERVICE_HOST + segmentPart.getURI();

				int percentageFinished = (int) (((float) i+1) / ((float) segmentNum) * 100);
				percentageFinished = Math.min(percentageFinished, 99); 
				observerStatusUpdate(percentageFinished, getResString(MESSAGE_INFO_UPLOADING_SEGMENT) + "" + (i+1) + "/" + segmentNum);
				//UiApplication.getUiApplication().invokeLater(new HttpMultipartRequest(uploadUri, segmentBytes, len, null));
				_request = new HttpMultipartRequest(uploadUri, segmentBytes, len, null);
				UiApplication.getUiApplication().invokeAndWait(_request);
			}

			input.close();

			result = _vDisk.completeUploadFileMultiPart(_filePath, uploadFileInfo.getUploadID(), uploadFileInfo.getUploadKey(), bufferMD5.toString());

			observerStatusUpdate(100, getResString(MESSAGE_INFO_UPLOAD_SUCCESS));
			observerResponse(result.toString());
		} catch (IOException e) {
			Function.errorDialog(e.toString());
		} catch (vDiskException e) {
			Function.errorDialog(e.toString());
		} finally {
			if (fileConn != null) {try {fileConn.close(); fileConn = null;} catch (IOException e) {}
			}
		}

		_stopRequest = true;
		_ourObserver = null;

		observerStatusUpdate(100, "Finished"); // Tell Observer we have finished
		observerResponse("Succeeded");
	}

	private String getResString(int key) {return _bundle.getString(key);}
}