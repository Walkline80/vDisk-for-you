package com.walkline.screen;

import localization.vDiskSDKResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.picker.FilePicker;

import com.walkline.util.ui.VerticalButtonFieldSet;

public class UploadFilePopupScreen extends PopupScreen implements FieldChangeListener, vDiskSDKResource
{
	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	ButtonField _buttonFile;
	ButtonField _buttonPicture;
	ButtonField _buttonMusic;
	ButtonField _buttonVideo;
	ButtonField _buttonRingtone;
	ButtonField _buttonVoicNote;
	
	private int _result = -1;

	public UploadFilePopupScreen()
	{
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLL_MASK));
		
		LabelField headLine = new LabelField(getResString(UPLOAD_FILE_TITLE));

		_buttonFile = new ButtonField(getResString(UPLOAD_FILE_TYPE_FILE), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonFile.setChangeListener(this);
		_buttonPicture = new ButtonField(getResString(UPLOAD_FILE_TYPE_PICTURE), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonPicture.setChangeListener(this);
		_buttonMusic = new ButtonField(getResString(UPLOAD_FILE_TYPE_MUSIC), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonMusic.setChangeListener(this);
		_buttonVideo = new ButtonField(getResString(UPLOAD_FILE_TYPE_VIDEO), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonVideo.setChangeListener(this);
		_buttonRingtone = new ButtonField(getResString(UPLOAD_FILE_TYPE_RINGTONE), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonRingtone.setChangeListener(this);
		_buttonVoicNote = new ButtonField(getResString(UPLOAD_FILE_TYPE_VOICENOTE), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		_buttonVoicNote.setChangeListener(this);

		VerticalButtonFieldSet vbf = new VerticalButtonFieldSet(USE_ALL_WIDTH);
		vbf.add(_buttonFile);
		vbf.add(_buttonPicture);
		vbf.add(_buttonMusic);
		vbf.add(_buttonVideo);
		vbf.add(_buttonRingtone);
		vbf.add(_buttonVoicNote);
		
		add(headLine);
		add(new SeparatorField());
		add(vbf);
	}
	
	public boolean onClose()
	{
		UiApplication.getUiApplication().popScreen(this);					
		
		return true;
	}
	
	protected boolean keyChar(char character, int status, int time)
	{
		switch (character)
		{
			case Characters.LATIN_CAPITAL_LETTER_F:
			case Characters.LATIN_SMALL_LETTER_F:
				_result = FilePicker.VIEW_ALL;
				onClose();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_P:
			case Characters.LATIN_SMALL_LETTER_P:
				_result = FilePicker.VIEW_PICTURES;
				onClose();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_M:
			case Characters.LATIN_SMALL_LETTER_M:
				_result = FilePicker.VIEW_MUSIC;
				onClose();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_V:
			case Characters.LATIN_SMALL_LETTER_V:
				_result = FilePicker.VIEW_VIDEOS;
				onClose();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_R:
			case Characters.LATIN_SMALL_LETTER_R:
				_result = FilePicker.VIEW_RINGTONES;
				onClose();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_N:
			case Characters.LATIN_SMALL_LETTER_N:
				_result = FilePicker.VIEW_VOICE_NOTES;
				onClose();
				return true;
			case Characters.ESCAPE:
				onClose();
				return true;
		}
		
		return super.keyChar(character, status, time);
	}

	private String getResString(int key) {return _bundle.getString(key);}

	public int getSelection() {return _result;}
	
	public void fieldChanged(Field field, int context)
	{
		if (field instanceof ButtonField)
		{
			if (field.equals(_buttonFile)) {
				_result = FilePicker.VIEW_ALL;
			} else if (field.equals(_buttonPicture)) {
				_result = FilePicker.VIEW_PICTURES;
			} else if (field.equals(_buttonMusic)) {
				_result = FilePicker.VIEW_MUSIC;
			} else if (field.equals(_buttonVideo)) {
				_result = FilePicker.VIEW_VIDEOS;
			} else if (field.equals(_buttonRingtone)) {
				_result = FilePicker.VIEW_RINGTONES;
			} else if (field.equals(_buttonVoicNote)) {
				_result = FilePicker.VIEW_VOICE_NOTES;
			}
			
			onClose();
		}
	}
}