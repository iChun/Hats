package hats.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.*;

//Taken from iChun Util

@SideOnly(Side.CLIENT)
public class GuiSlider extends GuiButton
{
    /** The value of this slider control. */
    public double sliderValue = 1.0F;
    
    public String dispString = "";
    
    public String suffix = "";

    /** Is this slider control being dragged. */
    public boolean dragging = false;

    public double minValue = 0.0D;
    public double maxValue = 5.0D;
    
    public ISlider parent = null;
    
    public boolean drawString = true;

    public GuiSlider(int id, int xPos, int yPos, String displayStr, double minVal, double maxVal, double currentVal, ISlider par)
    {
        super(id, xPos, yPos, 88, 20, displayStr);
        minValue = minVal;
        maxValue = maxVal;
        sliderValue = (currentVal - minValue) / (maxValue - minValue);
        dispString = displayStr;
        parent = par;
        
        String val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
        
        displayString = dispString + val;
    }
    
    public GuiSlider(int id, int xPos, int yPos, String displayStr, double minVal, double maxVal, double currentVal, ISlider par, String suf)
    {
    	this(id, xPos, yPos, displayStr, minVal, maxVal, currentVal, par);
    	suffix = suf;
    	
    	String val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
    	
    	displayString = dispString + val + suffix;
    }
    
    public GuiSlider(int id, int xPos, int yPos, String displayStr, double minVal, double maxVal, double currentVal, ISlider par, String suf, boolean drawStr, int width)
    {
    	super(id, xPos, yPos, width, 20, displayStr);
    	
        minValue = minVal;
        maxValue = maxVal;
        sliderValue = (currentVal - minValue) / (maxValue - minValue);
        dispString = displayStr;
        parent = par;
        
        String val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
        
    	suffix = suf;
    	
    	displayString = dispString + val + suffix;

    	drawString = drawStr;
    	if(!drawString)
    	{
    		displayString = "";
    	}
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    @Override
    protected int getHoverState(boolean par1)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    @Override
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.drawButton)
        {
            if (this.dragging)
            {
                this.sliderValue = (par2 - (this.xPosition + 4)) / (float)(this.width - 8);

                updateSlider();
             }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        if (super.mousePressed(par1Minecraft, par2, par3))
        {
            this.sliderValue = (float)(par2 - (this.xPosition + 4)) / (float)(this.width - 8);

            updateSlider();
            
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void updateSlider()
    {
        if (this.sliderValue < 0.0F)
        {
            this.sliderValue = 0.0F;
        }

        if (this.sliderValue > 1.0F)
        {
            this.sliderValue = 1.0F;
        }

        String val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
        
    	if(drawString)
    	{
            displayString = dispString + val + suffix;
    	}

        parent.onChangeSliderValue(this);
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    @Override
    public void mouseReleased(int par1, int par2)
    {
        this.dragging = false;
    }
    
    public double getValue()
    {
        return sliderValue * (maxValue - minValue) + minValue;
    }
}
