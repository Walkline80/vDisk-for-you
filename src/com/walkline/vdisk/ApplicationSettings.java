package com.walkline.vdisk;

public class ApplicationSettings
{
	protected String nextUrl = null;
	protected String applicationId = null;
	protected String applicationSecret = null;

	public ApplicationSettings(String pNextUrl, String pApplicationId, String pApplicationSecret) {
		nextUrl = pNextUrl;
		applicationId = pApplicationId;
		applicationSecret = pApplicationSecret;
	}

	public String getNextUrl() {return nextUrl;}
	public void setNextUrl(String pNextUrl) {nextUrl = pNextUrl;}

	public String getApplicationId() {return applicationId;}
	public void setApplicationId(String pApplicationId) {applicationId = pApplicationId;}

	public String getApplicationSecret() {return applicationSecret;}
	public void setApplicationSecret(String pApplicationSecret) {applicationSecret = pApplicationSecret;}
}