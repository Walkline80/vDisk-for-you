package com.walkline.util.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.ui.UiApplication;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;

public class NetworkThreadUploadPOST extends Thread implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private static final String twoHyphens = "--";
	private static final String Boundary = "****************256176b82bde4478"; //what_hell_is_that   UIDGenerator.getUniqueScopingValue()
	private static final String lineEnd = "\r\n";

	private ProgressListener _ourObserver;
	private String _targetURL;
	private Hashtable _params;
	private String _fileName;
	private String _fileURI;
	private String _fileType;
	private boolean _stopRequest = false;

	private ConnectionFactory cf;
	//private Logger log;

	private	long postSize = 0;

	public NetworkThreadUploadPOST(String requestURL, Hashtable params, String fileName, String fileURI, ProgressListener observer)
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
		_fileName = fileName;

		String mimeType = MIMETypeAssociations.getMIMEType(_fileName);
		String normalizedType = MIMETypeAssociations.getNormalizedType(mimeType != null ? mimeType : "application/octet-stream");

		_fileType = normalizedType;
		_fileURI = fileURI;
		_ourObserver = observer;

		postSize = getMultipartPostBytesSize(_fileName, _fileType, _fileURI);
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
		StringBuffer buffer = new StringBuffer();
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
				try {
					httpConn.setRequestMethod(HttpConnection.POST);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONNECTION, HttpProtocolConstants.HEADER_KEEP_ALIVE);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CACHE_CONTROL,"no-cache, no-store, no-transform");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_MULTIPART_FORM_DATA + "; boundary=" + Boundary);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, Long.toString(postSize));
					output = httpConn.openOutputStream();

					buffer.append(twoHyphens + Boundary + lineEnd);
					buffer.append("Content-Disposition: form-data; name=\"file" + "\"; filename=\"" + _fileName + "\"" + lineEnd);
					buffer.append("Content-Type: " + _fileType + lineEnd);
					buffer.append(lineEnd);
					output.write(buffer.toString().getBytes());	
					observerStatusUpdate(1, "Started");

					fileConn = (FileConnection)Connector.open(_fileURI, Connector.READ);
					long totalBytes = fileConn.fileSize();
					if (totalBytes == -1)
					{
						_ourObserver.processError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_FILE_NOT_AVAILABLE) + "\n\n" + _fileURI);
						//throw new IOException(getResString(MESSAGE_ERROR_FILE_NOT_AVAILABLE) + "\n\n" + _fileURI);
					}

					long sentBytes = 0;
					int percentPre = 0;

					input = fileConn.openInputStream();
					byte[] temp = new byte[1024];
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

					output.write(lineEnd.getBytes());
					output.write((twoHyphens+Boundary+twoHyphens+lineEnd).getBytes());

					output.flush();
					output.close();
				} catch (IOException e)
				{
					observerError(ProgressListener.ERROR, getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}

				//log.info("HTTP-POST-MULTI (" + transportTypeName + "): " + httpConn.getURL());

				int resCode = 0;
				String resMessage = "";

				try {
					resCode = httpConn.getResponseCode();
					resMessage = httpConn.getResponseMessage();

					//log.info("HTTP-POST-MULTI Response: " + resCode + " " + resMessage);
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

			//log.info("HTTP-POST-MULTI Body: " + httpConn.getType() + "(" + responeBuffer.length() + ")");
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

	private long getMultipartPostBytesSize(String fileName, String fileType, String fileURI)
	{
		StringBuffer buffer = new StringBuffer();
		FileConnection fconn = null;
		long fileSize = 0;

		/*
		 * @multipart post format
		 *	--****************256176b82bde4478\r\n
		 *	Content-Disposition: form-data; name="uploadfile"; filename="fileName"\r\n
		 *	Content-Type: txt/plain\r\n
		 *	\r\n
		 *	[content bytes of upload file]
		 *	\r\n
		 *	--****************256176b82bde4478--\r\n
		*/
		buffer.append(twoHyphens + Boundary + lineEnd);
		buffer.append("Content-Disposition: form-data; name=\"file" + "\"; filename=\"" + fileName + "\"" + lineEnd);
		buffer.append("Content-Type: " + fileType + lineEnd);
		buffer.append(lineEnd);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try
		{
			baos.write(buffer.toString().getBytes());
			baos.write(lineEnd.getBytes());
			baos.write((twoHyphens+Boundary+twoHyphens+lineEnd).getBytes());

		} catch (IOException e)	{
			Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
		}

		try {
			fconn = (FileConnection)Connector.open(fileURI);
			fileSize = fconn.fileSize();
			fconn.close();
		} catch (IOException e) {}

		return baos.toByteArray().length + fileSize;
	}

	private String getResString(int key) {return _bundle.getString(key);}
}