package com.walkline.screen;

import java.util.Hashtable;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.walkline.util.FileUtility;
import com.walkline.util.network.NetworkThreadDownload;
import com.walkline.util.network.NetworkThreadUploadMultiPart;
import com.walkline.util.network.NetworkThreadUploadPOST;
import com.walkline.util.network.NetworkThreadUploadPUT;
import com.walkline.util.network.ProgressListener;
import com.walkline.util.network.ThreadResponse;
import com.walkline.util.ui.VerticalButtonFieldSet;
import com.walkline.vdisk.vDiskSDK;

public class PleaseWaitPopupScreen extends PopupScreen implements ProgressListener, vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

    private GaugeField _gaugeField = null;
    private ButtonField _cancelButton = null;
    private LabelField _statusText = null;

    private NetworkThreadUploadPOST _requestThreadUploadPOST;
    private NetworkThreadUploadPUT _requestThreadUploadPUT;
    private NetworkThreadDownload _requestThreadDownload;
    private NetworkThreadUploadMultiPart _requestThreadUploadMultiPart;

    private vDiskSDK _vDisk;
    private String _path;

    private Hashtable _params;
    private String _requestURL;
    private String _filePath;
    private String _fileName;
    private String _fileURI;

    private int _returnCode = ProgressListener.CANCELLED;
    private byte[] _returnBytes;
    private String _returnString;

    /**
     * Download file from server
     * @param vDisk
     * @param path
     */
    public PleaseWaitPopupScreen(vDiskSDK vDisk, String path)
    {
        super(new VerticalFieldManager());

    	HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH);
    	VerticalFieldManager vfm = new VerticalFieldManager(FIELD_VCENTER);
    	VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);
    	
        String titleIconName=Display.getWidth()<640 ? "titleIcon_small.png" : "titleIcon_large.png";
    	BitmapField bmpTitleField=new BitmapField(Bitmap.getBitmapResource(titleIconName));
    	LabelField labelTitleField=new LabelField(getResString(PLEASEWAIT_DOWNLOADING) + FileUtility.getFilename(path));
    	bmpTitleField.setSpace(5, 5);
    	vfm.add(labelTitleField);
    	hfm.add(bmpTitleField);
    	hfm.add(vfm);
        add(hfm);
        
        _gaugeField = new GaugeField(null, 0, 100, 0, GaugeField.PERCENT);//GaugeField.NO_TEXT);
        add(_gaugeField);

        _statusText = new LabelField(null, LabelField.FIELD_RIGHT);
        _statusText.setFont(Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt));
        add(_statusText);
        add(new LabelField());

        _cancelButton = new ButtonField(getResString(PLEASEWAIT_BUTTON_CANCEL), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
        _cancelButton.setChangeListener( new FieldChangeListener()
        {
            public void fieldChanged(Field field, int context)
            {
                if ( _requestThreadDownload != null )
                {
                    if ( _requestThreadDownload.isAlive() )
                    {
                        _requestThreadDownload.stop();
                    }
                } else {
                    throw new RuntimeException("Oppsss");
                }
            }
        });
        vbf.add(_cancelButton);
        add(vbf);
        
        _vDisk = vDisk;
        _path = path;
    }
    
    /**
     * Upload file to server
     * @param requestURL
     * @param params
     * @param fileName
     * @param fileURI
     */
    public PleaseWaitPopupScreen(String requestURL, Hashtable params, String fileName, String fileURI)
    {
        super(new VerticalFieldManager());

    	HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH);
    	VerticalFieldManager vfm = new VerticalFieldManager(FIELD_VCENTER);
    	VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);
    	
        String titleIconName=Display.getWidth()<640 ? "titleIcon_small.png" : "titleIcon_large.png";
    	BitmapField bmpTitleField=new BitmapField(Bitmap.getBitmapResource(titleIconName));
    	bmpTitleField.setSpace(5, 5);
    	LabelField labelTitleField=new LabelField(getResString(PLEASEWAIT_UPLOADING) + fileName);
    	vfm.add(labelTitleField);
    	hfm.add(bmpTitleField);
    	hfm.add(vfm);
        add(hfm);

        _gaugeField = new GaugeField(null, 0, 100, 0, GaugeField.PERCENT);
        add(_gaugeField);

        _statusText = new LabelField(null, LabelField.FIELD_RIGHT);
        _statusText.setFont(Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt));
        add(_statusText);
        add(new LabelField());

        _cancelButton = new ButtonField(getResString(PLEASEWAIT_BUTTON_CANCEL), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
        _cancelButton.setChangeListener( new FieldChangeListener()
        {
            public void fieldChanged(Field field, int context)
            {
                if ( _requestThreadUploadPUT != null )
                {
                    if ( _requestThreadUploadPUT.isAlive() )
                    {
                        _requestThreadUploadPUT.stop();
                    }
                } else {
                    throw new RuntimeException("Oppsss");
                }
            }
        });
        vbf.add(_cancelButton);
        add(vbf);
        
        _requestURL = requestURL;
        _params = params;
        _fileName = fileName;
        _fileURI = fileURI;
    }

    /**
     * Upload file to server via multi-part
     * @param requestURL
     * @param params
     * @param fileName
     * @param fileURI
     */
    public PleaseWaitPopupScreen(vDiskSDK vDisk, String filePath, String fileName, String fileURI)
    {
        super(new VerticalFieldManager());

    	HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH);
    	VerticalFieldManager vfm = new VerticalFieldManager(FIELD_VCENTER);
    	VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);

        String titleIconName=Display.getWidth()<640 ? "titleIcon_small.png" : "titleIcon_large.png";
    	BitmapField bmpTitleField=new BitmapField(Bitmap.getBitmapResource(titleIconName));
    	bmpTitleField.setSpace(5, 5);
    	LabelField labelTitleField=new LabelField(getResString(PLEASEWAIT_UPLOADING) + fileName);
    	vfm.add(labelTitleField);
    	hfm.add(bmpTitleField);
    	hfm.add(vfm);
        add(hfm);

        _gaugeField = new GaugeField(null, 0, 100, 0, GaugeField.PERCENT);
        add(_gaugeField);

        _statusText = new LabelField(null, LabelField.FIELD_RIGHT);
        _statusText.setFont(Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight(Ui.UNITS_pt)-1, Ui.UNITS_pt));
        add(_statusText);
        add(new LabelField());

        _cancelButton = new ButtonField(getResString(PLEASEWAIT_BUTTON_CANCEL), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
        _cancelButton.setChangeListener( new FieldChangeListener()
        {
            public void fieldChanged(Field field, int context)
            {
                if ( _requestThreadUploadMultiPart != null )
                {
                    if ( _requestThreadUploadMultiPart.isAlive() )
                    {
                    	_requestThreadUploadMultiPart.stop();
                    }
                } else {
                    throw new RuntimeException("Oppsss");
                }
            }
        });
        vbf.add(_cancelButton);
        add(vbf);
        
        _vDisk = vDisk;
        _filePath = filePath;
        _fileName = fileName;
        _fileURI = fileURI;
    }

	private String getResString(int key)
	{
		return _bundle.getString(key);
	}

    public ThreadResponse showUploadPOST()
    {
        _requestThreadUploadPOST = new NetworkThreadUploadPOST(_requestURL, _params, _fileName, _fileURI, this);
        _requestThreadUploadPOST.start();
        UiApplication.getUiApplication().pushModalScreen(this);

        return new ThreadResponse(_returnCode, _returnBytes);
    }

    public ThreadResponse showUploadPUT()
    {
        _requestThreadUploadPUT = new NetworkThreadUploadPUT(_requestURL, _params, _fileURI, this);
        _requestThreadUploadPUT.start();
        UiApplication.getUiApplication().pushModalScreen(this);

        return new ThreadResponse(_returnCode, _returnBytes);
    }

    public ThreadResponse showUploadMultiPart()
    {
    	_requestThreadUploadMultiPart = new NetworkThreadUploadMultiPart(_vDisk, _filePath, _fileURI, this);
    	_requestThreadUploadMultiPart.start();
    	UiApplication.getUiApplication().pushModalScreen(this);

    	return new ThreadResponse(_returnCode, _returnString);
    }

    public ThreadResponse showDownload()
    {
    	_requestThreadDownload = new NetworkThreadDownload(_vDisk, _path, this);
    	_requestThreadDownload.start();
    	UiApplication.getUiApplication().pushModalScreen(this);
    	
    	return new ThreadResponse(_returnCode, _returnString);
    }
    
    public void processStatusUpdate(final int status, final String statusString)
    {
    	synchronized (UiApplication.getEventLock())
    	{
    		if (UiApplication.isEventDispatchThread())
    		{
 	           UiApplication.getUiApplication().invokeLater(new Runnable()
	           {
 	        	   public void run ()
	               {
 	        		   _statusText.setText(statusString);
	                    
	                   if ( status > 0 )
	                   {
	                       _gaugeField.setValue(status);
	                   }
	                    
	                   PleaseWaitPopupScreen.this.invalidate();
	               }
	           });
    		} else {
      		   _statusText.setText(statusString);
               
               if ( status > 0 )
               {
                   _gaugeField.setValue(status);
               }
                
               PleaseWaitPopupScreen.this.invalidate();
    		}
		}
    }

    public void processResponse(final byte [] responseBytes)
    {
        _returnCode = ProgressListener.OK;

    	synchronized (UiApplication.getEventLock())
    	{
    		if (UiApplication.isEventDispatchThread())
    		{
    	        UiApplication.getUiApplication().invokeLater(new Runnable()
    	        {
    	            public void run ()
    	            {
    	            	_returnBytes = responseBytes;
    	                UiApplication.getUiApplication().popScreen(PleaseWaitPopupScreen.this);
    	            }
    	        });
    		} else {
    			_returnBytes = responseBytes;
                UiApplication.getUiApplication().popScreen(PleaseWaitPopupScreen.this);
    		}
    	}
    }

    public void processResponse(final String responseString)
    {
        _returnCode = ProgressListener.OK;

    	synchronized (UiApplication.getEventLock())
    	{
    		if (UiApplication.isEventDispatchThread())
    		{
    	        UiApplication.getUiApplication().invokeLater(new Runnable()
    	        {
    	            public void run ()
    	            {
    	            	_returnString = responseString;
    	                UiApplication.getUiApplication().popScreen(PleaseWaitPopupScreen.this);
    	            }
    	        });
    		} else {
    			_returnString = responseString;
                UiApplication.getUiApplication().popScreen(PleaseWaitPopupScreen.this);
    		}
    	}
    }
    
    public void processError(int errorCode, final String errorMessage)
    {
        _returnCode = errorCode;
        
        UiApplication.getUiApplication().invokeLater(new Runnable() 
        {
            public void run ()
            {
                Dialog.alert(errorMessage);
                UiApplication.getUiApplication().popScreen(PleaseWaitPopupScreen.this);
            }
        });
    }
}