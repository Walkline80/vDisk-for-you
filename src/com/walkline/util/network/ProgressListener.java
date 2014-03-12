package com.walkline.util.network;

public interface ProgressListener
{
    public static int CANCELLED = -1;
    public static int ERROR = -2;
    public static int OK = 0;

	public void processStatusUpdate(int status, String statusString);

    public void processResponse(byte [] responseBytes);

    public void processResponse(String responseString);

    public void processError(int errorCode, String errorMessage);
}