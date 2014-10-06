package com.walkline.util.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

public class VerticalButtonFieldSet extends Manager 
{
	public static int DRAWPOSITION_TOP = 0;
    public static int DRAWPOSITION_BOTTOM = 1;
    public static int DRAWPOSITION_MIDDLE = 2;
    public static int DRAWPOSITION_SINGLE = 3;
    private static final int COLOR_BACKGROUND = 0xFFFFFF;
    private static final int COLOR_BORDER = 0xBBBBBB;
    private static final int COLOR_BACKGROUND_FOCUS = 0x186DEF;
    private static final int CORNER_RADIUS = 16;
	
	private int _drawPosition = -1;
	
    public VerticalButtonFieldSet()
    {
        super(Field.FIELD_HCENTER);
    } 
    
    public VerticalButtonFieldSet(long style) 
    {
        super(style);
    }
    
    protected void paintBackground( Graphics g )
    {
        if( _drawPosition < 0 ) {
            super.paintBackground( g );
            return;
        }
        
        int oldColour = g.getColor();
        int background = g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS) ? COLOR_BACKGROUND_FOCUS : COLOR_BACKGROUND;

        try {
            if( _drawPosition == 0 ) {
                // Top
                g.setColor( background );
                g.fillRoundRect( 0, 0, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
                g.setColor( COLOR_BORDER );
                g.drawRoundRect( 0, 0, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
                g.drawLine( 0, getHeight() - 1, getWidth(), getHeight() - 1 );
            } else if( _drawPosition == 1 ) {
                // Bottom 
                g.setColor( background );
                g.fillRoundRect( 0, -CORNER_RADIUS, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
                g.setColor( COLOR_BORDER );
                g.drawRoundRect( 0, -CORNER_RADIUS, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
            } else if( _drawPosition == 2 ) {
                // Middle
                g.setColor( background );
                g.fillRoundRect( 0, -CORNER_RADIUS, getWidth(), getHeight() + 2 * CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
                g.setColor( COLOR_BORDER );
                g.drawRoundRect( 0, -CORNER_RADIUS, getWidth(), getHeight() + 2 * CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS );
                g.drawLine( 0, getHeight() - 1, getWidth(), getHeight() - 1 );
            } else {
                // Single
                g.setColor( background );
                g.fillRoundRect( 0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS );
                g.setColor( COLOR_BORDER );
                g.drawRoundRect( 0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS );
            }
        } finally {
            g.setColor( oldColour );
        }
    }
    
    protected void sublayout(int width, int height) 
    {
        int maxWidth   = 0;
        int numChildren = this.getFieldCount();
        
        if(isStyle(USE_ALL_WIDTH))
        {
            maxWidth = width;
        } else {
            for(int i = 0; i < numChildren; i++)
            {
                Field currentField = getField(i);
                int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
                maxWidth  = Math.max(maxWidth, currentPreferredWidth);
            }
        }
        
        int prevTopMargin = 0;
        int usedHeight = 0;
        int x;
        
        for(int i = 0; i < numChildren; i++)
        {
            Field currentField = getField( i );
            int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
            
            if( currentPreferredWidth < maxWidth )
            {
                int newPadding = ( maxWidth - currentPreferredWidth ) / 2; 
                currentField.setPadding( currentField.getPaddingTop(), newPadding, currentField.getPaddingBottom(), newPadding );
            }
            
            layoutChild( currentField, maxWidth, height );
            
            usedHeight += Math.max( prevTopMargin, currentField.getMarginBottom() );
            x = ( maxWidth - currentField.getWidth() ) / 2;
            setPositionChild( currentField, x, usedHeight );
            usedHeight += currentField.getHeight();
            prevTopMargin = currentField.getMarginBottom();
        }

        setExtent(maxWidth, usedHeight);
    }

    public void setDrawPosition(int drawPosition)
    {
        _drawPosition = drawPosition;
    }

    protected boolean navigationMovement(int dx, int dy, int status, int time) 
    {
        int focusIndex = getFieldWithFocusIndex();                   
        if ( dx < 0 && focusIndex == 0 )
        {
            return true;
        }
        
        if( dx > 0 && focusIndex == getFieldCount()-1 )
        {
            return true;
        }
        
        return super.navigationMovement( dx, dy, status, time );
    }    
}