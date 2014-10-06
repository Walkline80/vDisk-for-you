package com.walkline.util.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import localization.vDiskSDKResource;

import net.rim.blackberry.api.browser.PostData;
import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;

import com.walkline.util.Function;
import com.walkline.util.StringUtility;
import com.walkline.vdisk.vDiskException;

public class HttpClient implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	protected ConnectionFactory cf;
	//protected Logger log;

	public HttpClient(ConnectionFactory pcf)
	{
		cf = pcf;
		//log = Logger.getLogger(getClass());
	}

	public StringBuffer doGet(String url, Hashtable args) throws Exception
	{
		StringBuffer urlBuffer = new StringBuffer(StringUtility.makeGoodPath(url));
		urlBuffer.append('?').append(StringUtility.encodeUrlParameters(args));
		
		return doGet(urlBuffer.toString());
	}

	public StringBuffer doGet(String url) throws Exception
	{
		HttpConnection conn = null;
		StringBuffer buffer = new StringBuffer();

		try {
			if ((url == null) || url.equalsIgnoreCase("") || (cf == null)) {return null;}

			ConnectionDescriptor connd = cf.getConnection(url);
			String transportTypeName = TransportInfo.getTransportTypeName(connd.getTransportDescriptor().getTransportType());
			conn = (HttpConnection) connd.getConnection();

			//log.info("HTTP-GET (" + transportTypeName + "): " + conn.getURL());
			int resCode = conn.getResponseCode();
			String resMessage = conn.getResponseMessage();
			//log.info("HTTP-GET Response: " + resCode + " " + resMessage);

			switch (resCode)
			{
				case HttpConnection.HTTP_OK: 
				case HttpConnection.HTTP_BAD_REQUEST:
				case HttpConnection.HTTP_NOT_FOUND:
				case HttpConnection.HTTP_UNAUTHORIZED:
				{
					InputStream inputStream = conn.openInputStream();
					int c;

					while ((c = inputStream.read()) != -1) {buffer.append((char) c);}

					inputStream.close();
					break;
				}
				case HttpConnection.HTTP_TEMP_REDIRECT:
				case HttpConnection.HTTP_MOVED_TEMP:
				case HttpConnection.HTTP_MOVED_PERM: {
					url = conn.getHeaderField("Location");
					buffer.append(url);
					break;
				}
				case HttpConnection.HTTP_FORBIDDEN:
				{
					Function.errorDialog(getResString(MESSAGE_ERROR_ACCESS_FORBIDDEN));
					break;
				}
			}

			//log.info("HTTP-GET Body: " + conn.getType() + "(" + buffer.length() + ")");
			if (!conn.getType().startsWith("image/") && buffer.length() < 500)
			{
				//log.debug(buffer.toString());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null) {try {conn.close(); conn = null;} catch (IOException e) {}}
		}

		return buffer;
	}

	public StringBuffer doPost(String url, Hashtable data) throws Exception
	{
		URLEncodedPostData encoder = new URLEncodedPostData("UTF-8", false);
		Enumeration keysEnum = data.keys();

		while (keysEnum.hasMoreElements()) {
			String key = (String) keysEnum.nextElement();
			String val = (String) data.get(key);
			encoder.append(key, val);
		}

		return doPost(StringUtility.makeGoodPath(url), encoder);
	}

	public StringBuffer doPost(String url, PostData postData) throws Exception
	{
		HttpConnection conn = null;
		OutputStream os = null;
		StringBuffer buffer = new StringBuffer();

		try {
			if ((url == null) || url.equalsIgnoreCase("") || (cf == null)) {return null;}

			ConnectionDescriptor connd = cf.getConnection(url);
			String transportTypeName = TransportInfo.getTransportTypeName(connd.getTransportDescriptor().getTransportType());
			conn = (HttpConnection) connd.getConnection();

			if (conn != null) {
				try {
					if (postData != null) {
						conn.setRequestMethod(HttpConnection.POST);
						conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, postData.getContentType());
						conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(postData.size()));

						os = conn.openOutputStream();
						os.write(postData.getBytes());
					} else {
						conn.setRequestMethod(HttpConnection.GET);
					}
				} catch (Throwable t) {
					//log.error("Throwable: " + t.getMessage());
				}

				//log.info("HTTP-POST (" + transportTypeName + "): " + conn.getURL());
				int resCode = conn.getResponseCode();
				String resMessage = conn.getResponseMessage();
				//log.info("HTTP-POST Response: " + resCode + " " + resMessage);

				switch (resCode)
				{
					case HttpConnection.HTTP_OK:
					case HttpConnection.HTTP_BAD_REQUEST:
					case HttpConnection.HTTP_NOT_FOUND:
					case HttpConnection.HTTP_FORBIDDEN:
					{
						InputStream inputStream = conn.openInputStream();
						int c;

						while ((c = inputStream.read()) != -1) {
							buffer.append((char) c);
						}

						inputStream.close();
						break;
					}
					case HttpConnection.HTTP_TEMP_REDIRECT:
					case HttpConnection.HTTP_MOVED_TEMP:
					case HttpConnection.HTTP_MOVED_PERM: {
						url = conn.getHeaderField("Location");
						buffer = doPost(url, postData);
						break;
					}
					case HttpConnection.HTTP_INTERNAL_ERROR:
						new vDiskException("Internal server error");
						break;
				}
			}
			//log.info("HTTP-POST Body: " + conn.getType() + "(" + buffer.length() + ")");
			if (buffer.length() < 500)
			{
				//log.debug(buffer.toString());
			}
		} catch (Throwable t) {
			//log.error("Throwable: " + t.getMessage());
		} finally {
			if (os != null) {try {os.close(); os = null;} catch (IOException e) {}}
			if (conn != null) {try {conn.close(); conn = null;} catch (IOException e) {}}
		}

		return buffer;
	}

	private String getResString(int key) {return _bundle.getString(key);}
}