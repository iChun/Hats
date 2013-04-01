package hats.common.core;

public class HatInfo 
{

	public final String hatName;
	
	public final int colourR;
	public final int colourG;
	public final int colourB;
	
	public HatInfo()
	{
		hatName = "";
		colourR = 0;
		colourG = 0;
		colourB = 0;
	}
	
	public HatInfo(String name)
	{
		hatName = name;
		colourR = 0;
		colourG = 0;
		colourB = 0;
	}
	
	public HatInfo(String name, int r, int g, int b)
	{
		hatName = name;
		colourR = r;
		colourG = g;
		colourB = b;
	}
	
}
