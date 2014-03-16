package hats.client.gui;

import cpw.mods.fml.relauncher.*;

//Taken from iChun Util

//If your GUI has sliders you need to implement this interface.
@SideOnly(Side.CLIENT)
public interface ISlider 
{
	void onChangeSliderValue(GuiSlider slider);	
}
