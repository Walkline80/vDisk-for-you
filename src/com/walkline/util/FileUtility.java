package com.walkline.util;

import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

public class FileUtility
{
	protected static final String FILE_PREFIX = "file:///";

	public static boolean createRecursively(String folderPath)
	{
		boolean created = false;
		boolean parentCreated = false;

		if ((folderPath == null) || folderPath.equals("") || !folderPath.trim().startsWith(FILE_PREFIX))
		{
			return false;
		} else {
			folderPath = folderPath.trim();

			if (isRoot(folderPath))
			{
				created = true;
			} else {
				parentCreated = createRecursively(parentOf(folderPath));
				
				if (parentCreated)
				{
					created = createDir(folderPath);
				}
			}
		}

		return created;
	}

	protected static boolean createDir(String folderName)
	{
		boolean created = false;

		try {
			folderName = folderName.trim();

			if (!folderName.endsWith("/")) {folderName += "/";}

			FileConnection fc = (FileConnection) Connector.open(folderName);

			if (fc.exists())
			{
				created = true;
			} else {
				fc.mkdir();
				created = true;
			}
		} catch (Throwable t) {}

		return created;
	}

	public static String parentOf(String inStr)
	{
		String result = null;

		if ((inStr != null) && !inStr.trim().equals(""))
		{
			inStr = inStr.trim();
			int index = inStr.lastIndexOf('/');
			if (index != -1) {result = inStr.substring(0, index);}
		}

		return result;
	}

	public static String getFilename(String inStr)
	{
		String result = null;

		if ((inStr != null) && !inStr.trim().equals(""))
		{
			inStr = inStr.trim();
			int index = inStr.lastIndexOf('/');
			if (index != -1) {result = inStr.substring(index+1);}
		}
		
		return result;
	}
	
	public static boolean isRoot(String pFileName)
	{
		boolean output = false;
		Enumeration e = FileSystemRegistry.listRoots();
		String fileName = pFileName.trim() + "/";

		while (e.hasMoreElements())
		{
			String thisRoot = (String) e.nextElement();
			output = fileName.equals(FILE_PREFIX + thisRoot);
			if (output) {break;}
		}

		return output;
	}
}