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
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.ui.UiApplication;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;

public class NetworkThreadUploadPUT extends Thread implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private ProgressListener _ourObserver;
	private String _targetURL;
	private Hashtable _params;
	private String _fileURI;
	private boolean _stopRequest = false;

	private ConnectionFactory cf;
	//private Logger log;
	
	public NetworkThreadUploadPUT(String requestURL, Hashtable params, String fileURI, ProgressListener observer)
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

		_targetURL = requestURL;
		_params = params;
		_fileURI = fileURI;
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

	private void observerResponse(byte [] reply) 
	{
		if (!_stopRequest) {_ourObserver.processResponse(reply);}
	}

	public void run ()
	{
		HttpConnection httpConn = null;
		FileConnection fileConn = null;
		InputStream input = null;
		OutputStream output = null;
		StringBuffer responeBuffer = new StringBuffer();

		try {
			if ((_targetURL == null) || _targetURL.equalsIgnoreCase("") || (cf == null))
			{
				if (!_stopRequest)
				{
					_ourObserver.processError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}
			}

			StringBuffer urlBuffer = new StringBuffer(StringUtility.makeGoodPath(_targetURL));
			urlBuffer.append('?').append(StringUtility.encodeUrlParameters(_params));

			ConnectionDescriptor connd = cf.getConnection(urlBuffer.toString());
			String transportTypeName = TransportInfo.getTransportTypeName(connd.getTransportDescriptor().getTransportType());
			httpConn = (HttpConnection) connd.getConnection();

			if (httpConn != null)
			{
				fileConn = (FileConnection)Connector.open(_fileURI, Connector.READ);
				long totalBytes = fileConn.fileSize();
				if (totalBytes == -1)
				{
					//throw new IOException(getResString(MESSAGE_ERROR_FILE_NOT_AVAILABLE) + "\n\n" + _fileURI);
					_ourObserver.processError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_FILE_NOT_AVAILABLE) + "\n\n" + _fileURI);
				}

				try {
					httpConn.setRequestMethod(HttpProtocolConstants.HTTP_METHOD_PUT);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONNECTION, HttpProtocolConstants.HEADER_KEEP_ALIVE);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CACHE_CONTROL,"no-cache, no-store, no-transform");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, Long.toString(totalBytes));
					output = httpConn.openOutputStream();

					observerStatusUpdate(1, "Started");

					long sentBytes = 0;
					int percentPre = 0;

					input = fileConn.openInputStream();
					byte[] temp = new byte[1024 * 8];
					int len = 0;

					while ((len = input.read(temp)) > -1)
					{
						if (_stopRequest)
						{
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
				} catch (IOException e)
				{
					observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}

				//log.info("HTTP-PUT (" + transportTypeName + "): " + httpConn.getURL());

				int resCode = 0;
				String resMessage = "";

				try {
					resCode = httpConn.getResponseCode();
					resMessage = httpConn.getResponseMessage();

					//log.info("HTTP-PUT Response: " + resCode + " " + resMessage);
				} catch (IOException e) {
					observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}

				switch (resCode)
				{
					case HttpConnection.HTTP_OK:
					{
						InputStream inputStream;
						int c;

						try {
							inputStream = httpConn.openInputStream();
							while ((c = inputStream.read()) != -1)
							{
								responeBuffer.append((char) c);
							}

							inputStream.close();
						} catch (IOException e)
						{
							Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
						}

						observerStatusUpdate(100, getResString(MESSAGE_INFO_UPLOAD_SUCCESS));

						UiApplication.getUiApplication().invokeAndWait(new Runnable()
						{
							public void run()
							{
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {}		
							}
						});

						observerResponse(responeBuffer.toString().getBytes());
						break;
					}
					case HttpConnection.HTTP_BAD_REQUEST:
					{
						InputStream inputStream;
						int c;

						try {
							inputStream = httpConn.openInputStream();
							while ((c = inputStream.read()) != -1)
							{
								responeBuffer.append((char) c);
							}

							inputStream.close();
						} catch (Exception e)
						{
							Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
							observerError(ProgressListener.ERROR, e.getMessage());
						}

						observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));

						break;
					}
					case HttpConnection.HTTP_TEMP_REDIRECT:
					case HttpConnection.HTTP_MOVED_TEMP:
					case HttpConnection.HTTP_MOVED_PERM:
					{
						observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
						break;
					}
					case HttpConnection.HTTP_INTERNAL_ERROR:
					{
						observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
						break;
					}
				}
			}
			//log.info("HTTP-PUT Body: " + httpConn.getType() + "(" + responeBuffer.length() + ")");
			//log.debug(responeBuffer.toString());
		} catch (Throwable t)
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
		observerResponse("Succeeded".getBytes());
	}

	private String getResString(int key) {return _bundle.getString(key);}
}