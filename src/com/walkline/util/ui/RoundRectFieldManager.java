package com.walkline.util.ui;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class RoundRectFieldManager extends VerticalFieldManager
{
	private int CORNER_RADIUS;

	public RoundRectFieldManager(boolean inputStyle)
	{
		super(USE_ALL_WIDTH);

		if (inputStyle)
		{
			CORNER_RADIUS = 12;
			setMargin(5, 5, 5, 5);
			setPadding(3, 5, 5, 5);
		} else {
			CORNER_RADIUS = 15;
			setMargin(10, 0, 0, 0);
			setPadding(5, 0, 5, 5);
		}
	}

	public void paintBackground(Graphics g)
	{
		int oldColor = g.getColor();

		try {
			g.setColor(Color.WHITE);
			g.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
			g.setColor(Color.LIGHTGRAY);
			g.drawRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
		} catch (Exception e) {
		} finally {
			g.setColor(oldColor);	
		}
	}
}