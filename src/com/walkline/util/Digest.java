package com.walkline.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.file.FileConnection;

import net.rim.device.api.crypto.MD5Digest;

public class Digest
{
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String md5Hash(String str)
	{
		MD5Digest md5 = new MD5Digest();
		md5.reset();

		byte[] bytes = str.getBytes();
		md5.update(bytes, 0, bytes.length);

		return new String(convertToHexStr(md5.getDigest()).getBytes());
	}

	public static String md5Hash(byte[] bytes)
	{
		MD5Digest md5 = new MD5Digest();
		md5.reset();
		
		md5.update(bytes, 0, bytes.length);
		
		return convertToHexStr(md5.getDigest());
	}

	public static String md5Hash(byte[] bytes, int length)
	{
		MD5Digest digest = new MD5Digest();
		ByteArrayInputStream bais= new ByteArrayInputStream(bytes);
		byte[] buffer = new byte[128*1024];
		int read = 0;
		int count = 0;
		
		digest.reset();
		while ((read = bais.read(buffer, 0, buffer.length)) > 0)
		{
			count += read;
			digest.update(buffer, 0, read);
			// 到达该块末尾 //Reach the end of the buffer
			if (count == length) {break;}
		}
		
		return toHexString(digest.getDigest());
	}

	public static String md5Hash(FileConnection fconn)
	{
		MD5Digest md5 = new MD5Digest();
		md5.reset();
		
		try {		
			InputStream is = fconn.openInputStream();		
			byte[] bytes = new byte[512];
			int count = 0;

			while ((count = is.read(bytes)) != -1)
			{
				md5.update(bytes, 0, count);
			}
		} catch (IOException e) {}
		
		return convertToHexStr(md5.getDigest());
	}

	private static String convertToHexStr(byte[] data)
	{
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < data.length; i++)
		{
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
				{
					buf.append((char) ('0' + halfbyte));					
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		
		return buf.toString();
	}
	
	private static String toHexString(byte[] b)
	{
		StringBuffer sb = new StringBuffer(b.length * 2);
		
		for (int i=0; i<b.length; i++)
		{
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}

		return sb.toString();
	}
}