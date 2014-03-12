package com.walkline.util;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.global.Formatter;

import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.ui.component.Dialog;

public class StringUtility
{
	final static String[] hex = {
	    "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
	    "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
	    "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
	    "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
	    "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
	    "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
	    "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
	    "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
	    "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
	    "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
	    "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
	    "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
	    "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
	    "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
	    "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
	    "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
	    "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
	    "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
	    "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
	    "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
	    "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
	    "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
	    "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
	    "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
	    "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
	    "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
	    "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
	    "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
	    "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
	    "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
	    "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
	    "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
	 };
	
	/**
	 * replace "%2f" to "\"
	 * @param url path
	 * @return fixed good path
	 */
	public static String makeGoodPath(String path)
	{
		return replaceAll(path, "%2f", "/");
	}
	
	public static String encode(String s)
	{
		StringBuffer sbuf = new StringBuffer();
	    int len = s.length();
	    
	    for (int i = 0; i < len; i++)
	    {
	    	int ch = s.charAt(i);

	    	if ('A' <= ch && ch <= 'Z') {		// 'A'..'Z'
	    		sbuf.append((char)ch);
	    	} else if ('a' <= ch && ch <= 'z') {	// 'a'..'z'
	    		sbuf.append((char)ch);
	    	} else if ('0' <= ch && ch <= '9') {	// '0'..'9'
	    		sbuf.append((char)ch);
	    	} else if (ch == ' ') {			// space
	    		sbuf.append('+');
	    	} else if (ch == '-' || ch == '_'		// unreserved
	    			|| ch == '.' || ch == '!'
	    			|| ch == '~' || ch == '*'
	    			|| ch == '\'' || ch == '('
	    			|| ch == ')') {
	    		sbuf.append((char)ch);
	    	} else if (ch <= 0x007f) {		// other ASCII
	    		sbuf.append(hex[ch]);
	    	} else if (ch <= 0x07FF) {		// non-ASCII <= 0x7FF
	    		sbuf.append(hex[0xc0 | (ch >> 6)]);
	    		sbuf.append(hex[0x80 | (ch & 0x3F)]);
	    	} else {					// 0x7FF < ch <= 0xFFFF
	    		sbuf.append(hex[0xe0 | (ch >> 12)]);
	    		sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
	    		sbuf.append(hex[0x80 | (ch & 0x3F)]);
	    	}
	    }
	    
	    return sbuf.toString();
	}
	
	public static String encodeUrlParameters(Hashtable parameters)
	{
		if(parameters == null) {return "";}

		StringBuffer sb = new StringBuffer();
		boolean first = true;

		Enumeration keys=parameters.keys();
		while(keys.hasMoreElements())
		{
			if(first)
			{
				first = false;
			} else {
			    sb.append("&");
			}
			
			String _key=(String) keys.nextElement();
			String _value=(String) parameters.get(_key);
			
			if(_value!=null)
			{
			    try {
					sb.append(new String(_key.getBytes(), "UTF-8") + "=" + new String(_value.getBytes(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {Dialog.alert(e.toString());}
			}
		}
		
		return sb.toString();
	}
	

	/*
	public static Hashtable decodeUrlCode(String s)
	{
		Hashtable params=new Hashtable();
		
		if(s!=null)
		{
			String arrayTemp[]=split(s, "?");
			String array[]=split(arrayTemp[1], "&");
			
			for(int i=0; i<array.length; i++)
			{
				String v[]=split(array[i], "=");
				try {
					params.put(new String(v[0].getBytes(), "UTF-8"), new String(v[1].getBytes(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {Function.errorDialog(e.toString());}
			}
		}
		
		return params;
	}
	*/
	
	public static Date parseDate(String date)
	{
		if (date == null) {return null;}

		return new Date(HttpDateParser.parse(date));
	}
	
	public static String[] split(String strString, String strDelimiter)
	{
		int iOccurrences = 0;
		int iIndexOfInnerString = 0;
		int iIndexOfDelimiter = 0;
		int iCounter = 0;

		if (strString == null) {throw new NullPointerException("Input string cannot be null.");}
		if (strDelimiter.length() <= 0 || strDelimiter == null) {throw new NullPointerException("Delimeter cannot be null or empty.");}

		//if (strString.startsWith(strDelimiter)) {strString = strString.substring(strDelimiter.length());}
		if (!strString.endsWith(strDelimiter)) {strString += strDelimiter;}

		while((iIndexOfDelimiter= strString.indexOf(strDelimiter,iIndexOfInnerString))!=-1)
		{
			iOccurrences += 1;
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
		}

		String[] strArray = new String[iOccurrences];
		iIndexOfInnerString = 0;
		iIndexOfDelimiter = 0;

		while((iIndexOfDelimiter= strString.indexOf(strDelimiter,iIndexOfInnerString))!=-1)
		{
			strArray[iCounter] = strString.substring(iIndexOfInnerString, iIndexOfDelimiter);
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();

			iCounter += 1;
		}
		
		return strArray;
	}
	
	public static String replace(String source, String pattern, String replacement)
	{	
		if (source == null) {return "";}

		StringBuffer sb = new StringBuffer();
		int idx = -1;
		int patIdx = 0;

		idx = source.indexOf(pattern, patIdx);
		while(idx!=-1)
		{
			sb.append(source.substring(patIdx, idx));
			sb.append(replacement);
			patIdx = idx + pattern.length();
			sb.append(source.substring(patIdx));

			idx = source.indexOf(pattern, patIdx);
		}

        if (sb.length()==0)
        {
            return source;
        } else {
            return sb.toString();
        }
	}
	
	public static String replaceAll(String source, String pattern, String replacement)
	{    
	    if (source == null) {return "";}

	    StringBuffer sb = new StringBuffer();
	    int idx = 0;
	    String workingSource = source;
	    
	    while((idx=workingSource.indexOf(pattern, idx))!=-1)
	    {
	        sb.append(workingSource.substring(0, idx));
	        sb.append(replacement);
	        sb.append(workingSource.substring(idx + pattern.length()));
	        
	        workingSource = sb.toString();
	        sb.delete(0, sb.length());
	        idx += replacement.length();
	    }

	    return workingSource;
	}
	
	public static StringBuffer replace(String source, int startIndex, int endIndex, String replacement)
	{
		if(startIndex>endIndex) return new StringBuffer(source);
		
		StringBuffer sb=new StringBuffer();
		String tempBegin=source.substring(0, startIndex);
		String tempEnd=source.substring(endIndex);
		
		return sb.append(tempBegin).append(replacement).append(tempEnd);
	}
	
	/**
	 * 把字节大小转换为KB、MB和GB，带小数位
	 * @param size 需要转换的字节大小
	 * @param decimal 小数点后保留的位数
	 * @return 返回一个合适的字符串以显示文件大小
	 */
    public static String formatSize(double size, int decimal)
    {
    	long SIZE_KB = 1024;
    	long SIZE_MB = SIZE_KB * 1024;
    	long SIZE_GB = SIZE_MB * 1024;

    	if (size < SIZE_KB)
    	{
    		return size + " B";
    	} else if(size < SIZE_MB) {
    		return new Formatter("en").formatNumber(size/SIZE_KB, decimal) + " KB";
    	} else if(size < SIZE_GB) {
    		return new Formatter("en").formatNumber(size/SIZE_MB, decimal) + " MB";
    	} else {
    		return new Formatter("en").formatNumber(size/SIZE_GB, decimal) + " GB";
    	}
    }

	public static String fixHttpsUrlPrefix(String url)
	{
		String result = "";

		if ((url == null) || url.trim().equals(""))
		{
			result = url;
		} else {
			if (url.startsWith("http://") && (url.indexOf(":443") != -1))
			{
				result = "https://" + url.substring(7);
			} else {
				result = url;
			}
		}

		return result;
	}
}