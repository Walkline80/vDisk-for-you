package com.walkline.screen;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class NewFolderPopupScreen extends PopupScreen implements vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private LabelField _labelTitle;
	private LabelField _labelNotice;
	private EditField _blockField;
	private ButtonField _buttonOk;

	public NewFolderPopupScreen()
	{
		super(new VerticalFieldManager());

		_labelTitle = new LabelField(getResString(NEWFOLDER_CREATE_NEW_FOLDER), Field.FIELD_HCENTER);
		_labelTitle.setMargin(0,  0, 5, 0);
		_blockField=new EditField(getResString(NEWFOLDER_LABEL_FOLDER), "", 200, EditField.NO_NEWLINE);
		_labelNotice = new LabelField(null, LabelField.NON_FOCUSABLE);

		_buttonOk = new ButtonField(getResString(NEWFOLDER_BUTTON_OK), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonOk.setChangeListener(new FieldChangeListener()
		{
			public void fieldChanged(Field field, int context)
			{
				close();
			}
		});
		_buttonOk.setMinimalWidth(100);

		ButtonField buttonCancel = new ButtonField(getResString(NEWFOLDER_BUTTON_CANCEL), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		buttonCancel.setChangeListener(new FieldChangeListener()
		{
			public void fieldChanged(Field field, int context) 
			{
				_blockField.setText("");
				onClose();
			}
		});
		buttonCancel.setMinimalWidth(100);

		add(_labelTitle);
		add(_blockField);
		add(_labelNotice);
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		hfm.add(_buttonOk);
		hfm.add(buttonCancel);
		add(hfm);
	}

	private String getResString(int key) {return _bundle.getString(key);}

	public String getFolderName() {return _blockField.getText();}

	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);
		return true;
	}

	protected boolean onSavePrompt() {return true;}
	
	protected boolean keyChar(char character, int status, int time)
	{
		switch (character)
		{
			case Characters.ESCAPE:
				_blockField.setText("");
				onClose();
				return true;
			case Characters.ENTER:
				if (_blockField.isFocus())
				{
					_buttonOk.setFocus();
					return true;
				} 				
		}

		return super.keyChar(character, status, time);
	}
}