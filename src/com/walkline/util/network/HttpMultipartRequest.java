package com.walkline.util.network;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

import localization.vDiskSDKResource;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;

public class HttpMultipartRequest implements Runnable, vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private String _targetURL;
	private boolean _stopRequest;
	private ProgressListener _ourObserver;
	private ConnectionFactory cf;
	//private Logger log;
	private byte[] _payload;
	private int _byteLength;
	//private Hashtable _params = new Hashtable();

	public HttpMultipartRequest(String url, byte[] payload, int byteLength, ProgressListener progressListener)
	{
		//log = Logger.getLogger(getClass());

		cf = new ConnectionFactory();
		cf.setPreferredTransportTypes(vDiskAppConfig.preferredTransportTypes);
		cf.setDisallowedTransportTypes(vDiskAppConfig.disallowedTransportTypes);
		cf.setTimeoutSupported(true);
		cf.setAttemptsLimit(10);
		cf.setRetryFactor(2000);
		cf.setConnectionTimeout(120000);

		_stopRequest = false;
		_payload = payload;
		_byteLength = byteLength;
		_targetURL = url;
		_ourObserver = progressListener;
	}

	public void run()
	{
		try {
			send();
		} catch (Exception e) {
			//observerError(ProgressListener.ERROR, "Network problems!");
		}
	}

	public void stop() 
	{
		//observerError(ProgressListener.ERROR, "Cancelled by user.");
		_stopRequest = true;

		Thread.currentThread().interrupt();
	}

	public void send()
	{
		HttpConnection httpConn = null;
		DataOutputStream output = null;
		StringBuffer responeBuffer = new StringBuffer();
		
		if ((_targetURL == null) || _targetURL.equalsIgnoreCase("") || (cf == null)) {return;}

		StringBuffer urlBuffer = new StringBuffer(StringUtility.makeGoodPath(_targetURL));
		//urlBuffer.append('&').append(StringUtility.encodeUrlParameters(_params));

		try {
			ConnectionDescriptor connd = cf.getConnection(urlBuffer.toString());
			String transportTypeName = TransportInfo.getTransportTypeName(connd.getTransportDescriptor().getTransportType());
			httpConn = (HttpConnection) connd.getConnection();

			if (httpConn != null)
			{
				try {
					httpConn.setRequestMethod(HttpProtocolConstants.HTTP_METHOD_PUT);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONNECTION, HttpProtocolConstants.HEADER_KEEP_ALIVE);
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CACHE_CONTROL,"no-cache, no-store, no-transform");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, "application/octet-stream");
					httpConn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, Integer.toString(_byteLength));
					output = httpConn.openDataOutputStream();
					
					//int totalBytes = _payload.length;
					//observerResponse(null, totalBytes);
					//int bPercent = 0;
					//observerStatusUpdate(0, "Started");

					int len = 0;
					long sentBytes = 0;
					//int bPercentPre = 0;

					ByteArrayInputStream input = new ByteArrayInputStream(_payload, 0, _byteLength);
		            byte[] temp = new byte[1024];

		            while ((len = input.read(temp)) > -1)
					{
						if (_stopRequest)
						{
							Thread.currentThread().interrupt();
							//observerError(ProgressListener.CANCELLED, "User canceled.");
							return;
						}

						output.write(temp, 0, len);

						Thread.yield();
						sentBytes += len;
						//bPercent = len/totalBytes;

						//if ((bPercent % 5) == 0 && bPercent != bPercentPre && bPercent != 100) 
						//{
							//observerStatusUpdate((int) (bPercent), "uploading...");
							//bPercentPre = bPercent;
						//}
					}
					
					//observerStatusUpdate(99, "Uploaded.");

		            output.flush();
		            output.close();
					
					//Function.errorDialog("Upload segment total bytes: " + _byteLength + "\nUpload segment sent bytes: " + sentBytes);
				} catch (IOException e)
				{
					Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}

				//log.info("HTTP-PUT Segment (" + transportTypeName + "):  " + httpConn.getURL());

				int resCode = 0;
				String resMessage = "";

				try {
					resCode = httpConn.getResponseCode();
					resMessage = httpConn.getResponseMessage();
					
					//log.info("HTTP-PUT Segment Response:  " + resCode + " " + resMessage);
					//Function.errorDialog("Respone code: " + resCode + "\nMessage: " + resMessage);
				} catch (IOException e) {
					Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
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

						//observerStatusUpdate(100, "Uploaded.");
						//observerResponse("Succeeded".getBytes());
						
						break;
					}
					case HttpConnection.HTTP_BAD_REQUEST:
					{
						InputStream inputStream;
						int c;

						try {
							inputStream =  httpConn.openInputStream();
							while ((c = inputStream.read()) != -1)
							{
								responeBuffer.append((char) c);
							}

							inputStream.close();
						} catch (Exception e)
						{
							Function.errorDialog(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
						}
						
						//observerError(ProgressListener.ERROR, "File transfer problems!");

						break;
					}
					case HttpConnection.HTTP_TEMP_REDIRECT:
					case HttpConnection.HTTP_MOVED_TEMP:
					case HttpConnection.HTTP_MOVED_PERM:
					{
						//url = conn.getHeaderField("Location");
						//responeBuffer = doPostMultipart(url, params, name, fileName, fileType, payload);
						//observerError(ProgressListener.ERROR, "File transfer moved!");
						break;
					}
					case HttpConnection.HTTP_INTERNAL_ERROR:
						throw new vDiskException(getResString(MESSAGE_ERROR_UPLOAD_EXCEPTION));
				}
			}

			//log.info("HTTP-PUT Segment Body:  " + httpConn.getType() + "(" + responeBuffer.length() + ")");
			//log.debug(responeBuffer.toString());
		} catch (Throwable t) {
			Function.errorDialog(t.toString());

			//log.error("New Thread Throwable: " + t.toString());
		} finally {
			if (output != null) {try {output.close();} catch (IOException e) {}}
			if (httpConn != null) {try {httpConn.close();} catch (IOException e) {}}
		}

		_stopRequest = true;
		_ourObserver = null;

		//observerStatusUpdate(100, "Finished"); // Tell Observer we have finished
		//observerResponse("Succeeded".getBytes());
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

	private String getResString(int key) {return _bundle.getString(key);}
}