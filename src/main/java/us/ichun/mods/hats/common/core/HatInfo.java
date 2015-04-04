package us.ichun.mods.hats.common.core;

public class HatInfo 
{
	public final String hatName;
	
	public final int colourR;
	public final int colourG;
	public final int colourB;
    public final int alpha;
	
	public HatInfo()
	{
		hatName = "";
		colourR = 0;
		colourG = 0;
		colourB = 0;
        alpha = 255;
	}
	
	public HatInfo(String name)
	{
		hatName = name;
		colourR = 255;
		colourG = 255;
		colourB = 255;
        alpha = 255;
	}
	
	public HatInfo(String name, int r, int g, int b, int alp)
	{
		hatName = name;
		colourR = r;
		colourG = g;
		colourB = b;
        alpha = alp;
	}
	
}
