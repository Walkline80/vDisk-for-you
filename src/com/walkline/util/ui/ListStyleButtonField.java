package com.walkline.util.ui;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.component.LabelField;

import com.walkline.app.vDiskAppConfig;
import com.walkline.util.StringUtility;
import com.walkline.vdisk.inf.File;
import com.walkline.vdisk.inf.Folder;

public class ListStyleButtonField extends Field
{
    public static final int DRAWPOSITION_TOP = 0;
    public static final int DRAWPOSITION_BOTTOM = 1;
    public static final int DRAWPOSITION_MIDDLE = 2;
    public static final int DRAWPOSITION_SINGLE = 3;
    
    private static final int LISTSTYLE_ALARMCLOCK=0;
    private static final int LISTSTYLE_WORLDCLOCK=1;
    private static final int LISTSTYLE_REMINDER=2;
    
    private static final int CORNER_RADIUS = 12;
    
    private static final int HPADDING = Display.getWidth() <= 320 ? 4 : 6;
    private static final int VPADDING = 4;
    
    private static final boolean USING_LARGE_ICON = Display.getWidth()<640 ? false : true;
    
    private static int COLOR_BACKGROUND = 0xFFFFFF;
    private static int COLOR_BORDER = 0xBBBBBB;
    private static int COLOR_BACKGROUND_FOCUS = 0x186DEF;
    //private static final int COLOR_LIST_DISABLED = Color.DARKGRAY;//0x6D6D6D;//0x6A6A6A;

    private static final String[] _fileExts = {"acc", "ace", "ai", "aiff", "asp", "avi",
    										   "bat", "bin", "bmp",
    										   "ccd", "cmd", "com", "css", "cue",
    										   "dat", "dll", "doc",
    										   "fla",
    										   "gif",
    										   "hqx", "htm", "html",
    										   "ifo", "inf", "ini", "iso",
    										   "jpeg", "jpg",
    										   "mdf", "mds", "midi", "mov", "mp3", "mpeg", "mpeg2",
    										   "ogg",
    										   "pdf", "php", "png", "ppt", "psd",
    										   "rar", "rtf",
    										   "sit", "sitx", "swf",
    										   "tga", "tif", "tiff", "txt",
    										   "wav", "wma", "wmv", "wri",
    										   "xls", "xml",
    										   "zip"};
    //private Bitmap _leftIcon;
    private Bitmap _iconNormal; //=USING_LARGE_ICON ? Bitmap.getBitmapResource("folder_large.png") : Bitmap.getBitmapResource("folder_small.png");
    
    private MyLabelField _labelTitle;
    private MyLabelField _labelDescription;
    //private MyLabelField _labelDate;
    
    private int _rightOffset;
    private int _leftOffset;
    private int _labelHeight;
    //private int _listStyle;
    
    private int _drawPosition = -1;
    
	//private static PersistentObject _store;
	//private static Vector _data;
	//private static int _fontSize=1;

	private static Font FONT_TITLE;
	private static Font FONT_DESCRIPTION;
	//private static Font FONT_DATE;
	
	private String _path = "";
	private String _foldername = "";
	private String _filename = "";
	private boolean _isFolder = false;
	private boolean _isThumbExists = false;
	private String _modifiedDate = "";
	private String _md5 = "";
	private String _size = "";
	private String _bytes = "";
	
	private vDiskAppConfig _appConfig;
	//private boolean needPopupMenu = true;

    public ListStyleButtonField(Folder folder)
    {
        super(USE_ALL_WIDTH | Field.FOCUSABLE);

        _labelTitle = new MyLabelField(folder.getCurrentPath());
        _labelDescription = new MyLabelField(folder.getModifiedDate());
        //_labelDate = new MyLabelField(folder.getParentPath());
        
        _path = folder.getPath();
        _foldername = folder.getCurrentPath();
        _isFolder = folder.isDir();
        _modifiedDate = folder.getModifiedDate();
        _size = folder.getSize();

        setFontSize();

		_iconNormal = USING_LARGE_ICON ? Bitmap.getBitmapResource("normal_large.png") : Bitmap.getBitmapResource("normal_small.png");
    }

    public ListStyleButtonField(File file, vDiskAppConfig appConfig)
    {
        super(USE_ALL_WIDTH | Field.FOCUSABLE);

        _appConfig = appConfig;

        _labelTitle = new MyLabelField(file.getFilename());
        _labelDescription = new MyLabelField(file.getSize() + " / " + file.getModifiedDate());
        //_labelDate = new MyLabelField(file.getCurrentPath());

        _path = file.getPath();
        _filename = file.getFilename();
        _isFolder = file.isDir();
        _isThumbExists = file.isThumbExists();
        _modifiedDate = file.getModifiedDate();
        _md5 = file.getMD5();
        _size = file.getSize();
        _bytes = file.getBytes();

        setFontSize();

        String[] fileExts = StringUtility.split(file.getPath(), ".");
        String fileExt = fileExts[fileExts.length-1];

        _iconNormal = getFileExtIcon(fileExt.toLowerCase());
    }

    public void setDrawPosition(int drawPosition)
    {
        _drawPosition = drawPosition;
    }

    public void layout(int width, int height)
    {
        _leftOffset = _iconNormal.getWidth() + HPADDING*2;
        _rightOffset = HPADDING;
        
        //if(_labelDate!=null) {_labelDate.layout(width- _leftOffset - _rightOffset, height);}
        
        _labelTitle.layout(width - _leftOffset - _rightOffset, height); // - (_labelDate != null ? _labelDate.getWidth() : 0), height);
        _labelHeight = _labelTitle.getHeight();
        
    	if(_labelDescription != null)
    	{
    		_labelDescription.layout(width - _leftOffset - _rightOffset, height); // - (_labelData != null ? _labelDate.getWidth() : 0, height);
    		_labelHeight += _labelDescription.getHeight();
    	}

        setExtent(width, _labelHeight+10);//+2* extraVPaddingNeeded);
    }
    
    public void setFontSize()
    {
  		FONT_TITLE = vDiskAppConfig.FONT_LIST_TITLE;
    	//FONT_DATE = vDiskAppConfig.FONT_DATE_NORMAL;
    	FONT_DESCRIPTION = vDiskAppConfig.FONT_LIST_DESCRIPTION;

    	if(_labelTitle!=null) {_labelTitle.setFont(FONT_TITLE);}
    	//if(_labelDate!=null) {_labelDate.setFont(FONT_DATE);}
    	if(_labelDescription != null) {_labelDescription.setFont(FONT_DESCRIPTION);}
    }

    public String getProperties()
    {
    	StringBuffer buffer = new StringBuffer();
    	
    	if (isFolder())
    	{
        	buffer.append("Type: ").append("Folder").append("\n\n");
        	buffer.append("Name: ").append(getPath()).append("\n");
        	buffer.append("Size: ").append(getSize()).append("\n");
        	buffer.append("Modified: ").append(getModifiedDate()).append("\n");
    	} else {
        	buffer.append("Type: ").append("File").append("\n\n");
        	buffer.append("Name: ").append(getFilename()).append("\n");
        	buffer.append("Size: ").append(getSize()).append("\n");
        	buffer.append("Bytes: ").append(getBytes()).append("\n");
        	buffer.append("Modified: ").append(getModifiedDate()).append("\n");
        	buffer.append("MD5: ").append(getMD5());
    	}

    	return buffer.toString();
    }
    
    /*
    static
	{
    	_store=PersistentStore.getPersistentObject(0xaa041103a4a17b66L); //blackberry_alarmclock_worldclock_settings_id
		
		synchronized(_store)
		{
			if(_store.getContents()==null)
			{
				_store.setContents(new Vector());
				_store.forceCommit();
			}
		}
		
		try {
			_data=new Vector();
			_data=(Vector) _store.getContents();
		} catch (final Exception e) {}
	}
	*/

    protected void paint(Graphics g)
    {
        // Logo Bitmap
   		g.drawBitmap(HPADDING, (getHeight()-_iconNormal.getHeight())/2, _iconNormal.getWidth(), _iconNormal.getHeight(), _iconNormal, 0, 0);
        
        // Title Text
        try
        {
        	g.setFont(FONT_TITLE);
        	g.pushRegion(_leftOffset, VPADDING, _labelTitle.getWidth(), _labelTitle.getHeight(), 0, 0);
            _labelTitle.paint(g);
        } finally {
        	g.popContext();
        }
        
        // Right Text
        //if(_labelDate!=null)
        //{
        //	try
        //	{
        //		g.setFont(FONT_DATE);
        //		g.pushRegion(getWidth() - HPADDING - _labelDate.getWidth(), (getHeight() - _labelDate.getHeight())/2, _labelDate.getWidth(), _labelDate.getHeight(),0,0);
        //		_labelDate.paint(g);
        //	} finally{
        //		g.popContext();
        //	}       	
        //}

        // Description Text
        if(_labelDescription != null)
        {
        	try
        	{
        		if(g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS))
        		{
        			g.setColor(Color.WHITE);
        		} else {
        			g.setColor(Color.GRAY);	
    			}

        		if(_labelDescription!=null)
        		{
            		g.setFont(FONT_DESCRIPTION);
            		g.pushRegion(_leftOffset, getHeight()-_labelDescription.getHeight()-VPADDING, _labelDescription.getWidth(), _labelDescription.getHeight(),0,0);
            		_labelDescription.paint(g);
        		}
        	} finally{
        		g.popContext();
        	}	
        }
    }

    protected void paintBackground(Graphics g)
    {
        if(_drawPosition < 0)
        {
            super.paintBackground(g);
            return;
        }
        
        int oldColour = g.getColor();
        
        switch(0)
        {
			case LISTSTYLE_ALARMCLOCK:
        		COLOR_BACKGROUND_FOCUS = 0x186DEF; //0xfe3434;
        		
        		break;
			case LISTSTYLE_WORLDCLOCK:
        		COLOR_BACKGROUND_FOCUS = 0x0A9000;
        		
        		break;
			case LISTSTYLE_REMINDER:
        		COLOR_BACKGROUND_FOCUS = 0xC72F00;

        		break;
		}

        int background = g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS) ? COLOR_BACKGROUND_FOCUS : COLOR_BACKGROUND;

        try {
            if(_drawPosition == 0)
            {
                // Top
                g.setColor(background);
                g.fillRoundRect(0, 0, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
                g.setColor(COLOR_BORDER);
                g.drawRoundRect(0, 0, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } else if(_drawPosition == 1)
            {
                // Bottom 
                g.setColor(background);
                g.fillRoundRect(0, -CORNER_RADIUS, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
                g.setColor(COLOR_BORDER);
                g.drawRoundRect(0, -CORNER_RADIUS, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
            } else if(_drawPosition == 2)
            {
                // Middle
                g.setColor(background);
                g.fillRoundRect(0, -CORNER_RADIUS, getWidth(), getHeight() + 2 * CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
                g.setColor(COLOR_BORDER);
                g.drawRoundRect(0, -CORNER_RADIUS, getWidth(), getHeight() + 2 * CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } else {
                // Single
                g.setColor(background);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g.setColor(COLOR_BORDER);
                g.drawRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
            }
        } finally {
            g.setColor(oldColour);
        }
    }
    
    protected void drawFocus(Graphics g, boolean on)
    {
        if(_drawPosition < 0)
        {
        } else {
            boolean oldDrawStyleFocus = g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS);
            try {
                if(on)
                {
                	g.setColor(Color.WHITE);
                    g.setDrawingStyle(Graphics.DRAWSTYLE_FOCUS, true);
                }
                paintBackground(g);
                paint(g);
            } finally {
                g.setDrawingStyle(Graphics.DRAWSTYLE_FOCUS, oldDrawStyleFocus);
            }
        }
    }

    protected boolean keyChar(char character, int status, int time) 
    {
    	switch (character)
    	{
			case Characters.ENTER:
	            clickButton();
	            return true;
        }

        return super.keyChar(character, status, time);
    }

    protected boolean navigationUnclick(int status, int time) 
    {
    	if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY)
    	{
    		if (isFolder())
    		{
        		clickButton();
        		return true;
    		} else {
            	if (_appConfig.needPopupMenu())
            	{
            		return false;
            	} else {
            		clickButton();
            		return true;
            	}
    		}
    	}

    	return super.navigationClick(status, time);
    }

    protected boolean trackwheelClick(int status, int time)
    {
    	if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL)
    	{
    		if (isFolder())
    		{
        		clickButton();
        		return true;
    		} else {
            	if (_appConfig.needPopupMenu())
            	{
            		return false;
            	} else {
            		clickButton();
            		return true;
            	}
    		}
    	}

    	return super.trackwheelClick(status, time);
    }

    protected boolean invokeAction(int action) 
    {
    	switch(action)
    	{
    		case ACTION_INVOKE:
        		if (isFolder())
        		{
            		clickButton();
            		return true;
        		} else {
                	if (_appConfig.needPopupMenu())
                	{
                		return false;
                	} else {
                		clickButton();
                		return true;
                	}
        		}
    	}

    	return super.invokeAction(action);
    }

    protected boolean touchEvent(TouchEvent message)
    {
        int x = message.getX(1);
        int y = message.getY(1);

        if (x < 0 || y < 0 || x > getExtent().width || y > getExtent().height) {return false;}

        switch (message.getEvent())
        {
            case TouchEvent.UNCLICK:
        		if (isFolder())
        		{
            		clickButton();
            		return true;
        		} else {
                	if (_appConfig.needPopupMenu())
                	{
                		return false;
                	} else {
                		clickButton();
                		return true;
                	}
        		}
        }

        return super.touchEvent(message);
    }

    public void clickButton() {fieldChangeNotify(0);}

    public void setDirty(boolean dirty) {}
    public void setMuddy(boolean muddy) {}

    private Bitmap getFileExtIcon(String fileExt)
    {
    	Bitmap icon;

    	icon=USING_LARGE_ICON ? Bitmap.getBitmapResource("unknown_large.png") : Bitmap.getBitmapResource("unknown_small.png");

    	for (int i=0; i<_fileExts.length; i++)
    	{
    		if (_fileExts[i].equals(fileExt))
    		{
    			icon = USING_LARGE_ICON ? Bitmap.getBitmapResource(fileExt + "_large.png") : Bitmap.getBitmapResource(fileExt + "_small.png");
    			break;
    		}
    	}

    	return icon;
    }

    public String getPath() {return _path;}

    public String getFolderName() {return _foldername;}

    public String getFilename() {return _filename;}

    public boolean isFolder() {return _isFolder;}

    public boolean isThumbExists() {return _isThumbExists;}

    public String getModifiedDate() {return _modifiedDate;}

    public String getMD5() {return _md5;}

    public String getSize() {return _size;}
    
    public String getBytes() {return _bytes;}
}

class MyLabelField extends LabelField
{
    public MyLabelField(String text) {super(text, LabelField.ELLIPSIS);}

	public void layout(int width, int height) {super.layout(width, height);}   

	public void paint(Graphics g) {super.paint(g);}
}