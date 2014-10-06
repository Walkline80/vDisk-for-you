package com.walkline.util.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.ui.UiApplication;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.FileUtility;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskConfig;
import com.walkline.vdisk.vDiskSDK;

public class NetworkThreadDownload extends Thread implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private ProgressListener _ourObserver;
	private vDiskSDK _vDisk;
	private Hashtable _params;
	private String _root;
	private String _path;
	private boolean _stopRequest = false;
	private String _download_uri;

	private ConnectionFactory cf;
	//private Logger log;

	public NetworkThreadDownload(vDiskSDK vDisk, String path, ProgressListener observer)
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
		_params = new Hashtable();
		_root = _vDisk.getRoot();
		_download_uri = _vDisk.getDownloadURI();
		_path = path;
		_ourObserver = observer;
	}

	public void stop()
	{
		observerError(ProgressListener.CANCELLED, getResString(MESSAGE_INFO_USER_CANCELLED));
		_stopRequest = true;

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
		HttpConnection httpConn = null;
		FileConnection fileConn = null;
		InputStream input = null;
		OutputStream output = null;
		StringBuffer buffer = new StringBuffer();

		try {
			byte[] bytesReceived = _vDisk.doRequestRAW(vDiskConfig.getDownloadFileURL, _params, _root, StringUtility.encode(_path), true);

			if (bytesReceived != null && bytesReceived.length > 0)
			{
				try {
					if (!FileUtility.createRecursively(_download_uri))
					{
						observerError(ProgressListener.CANCELLED, getResString(MESSAGE_ERROR_UNABLE_CREATE_FOLDER));
					}

					FileConnection file;
					file = (FileConnection) Connector.open(_download_uri + FileUtility.getFilename(_path));

					if (!file.exists()) {file.create();}
					file.setWritable(true);

					output = file.openOutputStream();

					ConnectionDescriptor connd = cf.getConnection(new String(bytesReceived));
					httpConn = (HttpConnection) connd.getConnection();

					int resCode = 0;
					String resMessage = "";

					try {
						resCode = httpConn.getResponseCode();
						resMessage = httpConn.getResponseMessage();

						//log.info("HTTP-GET (" + transportTypeName + "):  " + httpConn.getURL());
						//log.info("HTTP-GET Response:  " + resCode + " " + resMessage);
					} catch (IOException e) {
						observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_DOWNLOAD_EXCEPTION));
					}

					switch (resCode)
					{
						case HttpConnection.HTTP_OK:
						{
							long totalBytes = Long.parseLong(httpConn.getHeaderField(HttpProtocolConstants.HEADER_CONTENT_LENGTH));
							long sentBytes = 0;
							int percentPre = 0;

							input = httpConn.openInputStream();

							byte[] temp = new byte[1024];
							int len = 0;

							while ((len = input.read(temp)) > -1)
							{
								if (_stopRequest)
								{
									//log.info("Download cancelled by user.");
									observerError(ProgressListener.CANCELLED, getResString(MESSAGE_INFO_USER_CANCELLED));
									return;
								}

								output.write(temp, 0, len);	

								sentBytes += len;
								int percentageFinished = (int) ((sentBytes * 100) / totalBytes);
								percentageFinished = Math.min(percentageFinished, 99); 

								if (percentageFinished != percentPre)
								{
									observerStatusUpdate(percentageFinished, StringUtility.formatSize(sentBytes, 1) + " / " + StringUtility.formatSize(totalBytes, 1));	
								}

								percentPre = percentageFinished;
							}

							output.flush();
							output.close();

							observerStatusUpdate(100, getResString(MESSAGE_INFO_DOWNLOAD_SUCCESS));

							UiApplication.getUiApplication().invokeAndWait(new Runnable()
							{
								public void run()
								{
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {}		
								}
							});

							observerResponse(getResString(MESSAGE_INFO_DOWNLOAD_SUCCESS) + "\n\n" + _download_uri + FileUtility.getFilename(_path));
							break;
						}
						case HttpConnection.HTTP_BAD_REQUEST:
						case HttpConnection.HTTP_NOT_FOUND:
						case HttpConnection.HTTP_UNAUTHORIZED:
						{
							InputStream inputStream = httpConn.openInputStream();
							int c;

							while ((c = inputStream.read()) != -1) {buffer.append((char) c);}

							inputStream.close();

							observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_DOWNLOAD_EXCEPTION));
							break;
						}
						case HttpConnection.HTTP_TEMP_REDIRECT:
						case HttpConnection.HTTP_MOVED_TEMP:
						case HttpConnection.HTTP_MOVED_PERM: {
							observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_DOWNLOAD_EXCEPTION));
							break;
						}
						case HttpConnection.HTTP_FORBIDDEN:
						{
							observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_DOWNLOAD_EXCEPTION));
							break;
						}
						default:
							break;
					}
				} catch (IOException e) {
					observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_DOWNLOAD_EXCEPTION));
				}
			}
		}catch (Throwable t)
		{
			Function.errorDialog(t.toString());
			//log.error("New Thread Throwable: " + t.getMessage());
		} finally {
			if (input != null) {try {input.close();} catch (IOException e) {}}
			if (fileConn != null) {try {fileConn.close();} catch (IOException e) {}}
			if (output != null) {try {output.close();} catch (IOException e) {}}
			if (httpConn != null) {try {httpConn.close();} catch (IOException e) {}}
		}

		_stopRequest = true;
		_ourObserver = null;

		observerStatusUpdate(100, "Finished"); // Tell Observer we have finished
		observerResponse("Succeeded");
	}

	private String getResString(int key) {return _bundle.getString(key);}
}