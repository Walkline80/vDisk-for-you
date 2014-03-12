package com.walkline.util.network;

public class ThreadResponse
{
	private int _responseCode = 0;
	private byte[] _responseBytes;
	private String _responseString = "";

	public ThreadResponse(int responseCode, byte[] responseBytes)
	{
		_responseCode = responseCode;
		_responseBytes = responseBytes;
	}

	public ThreadResponse(int responseCode, String responseString)
	{
		_responseCode = responseCode;
		_responseString = responseString;
	}

	public int getResponseCode() {return _responseCode;}

	public String getResponseString() {return _responseString;}

	public byte[] getResponseBytes() {return _responseBytes;}

	public boolean isValidResponseWithBytes()
	{
		return ((getResponseCode() == ProgressListener.OK) && (getResponseBytes() != null) && (getResponseBytes().length > 0));
	}

	public boolean isValidResponseWithString()
	{
		return ((getResponseCode() == ProgressListener.OK) && (!getResponseString().equals("")) && (getResponseString().length() > 0));
	}
}