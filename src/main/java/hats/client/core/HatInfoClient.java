package hats.client.core;

import hats.common.core.HatInfo;

public class HatInfoClient extends HatInfo 
{
	public int recolour;
	
	public boolean doNotRender;
	
	public int prevColourR;
	public int prevColourG;
	public int prevColourB;
	
	public HatInfoClient()
	{
		super();
	}
	
	public HatInfoClient(String string) 
	{
		super(string);
	}
	
	public HatInfoClient(String string, int i, int j, int k) 
	{
		super(string, i, j, k);
	}

	public void inherit(HatInfoClient info)
	{
		prevColourR = info.colourR;
		prevColourG = info.colourG;
		prevColourB = info.colourB;
		if(info.hatName.equalsIgnoreCase(hatName) && !(prevColourR == colourR && prevColourG == colourG && prevColourB == colourB))
		{
			recolour = 20;
		}
	}
	
	public void tick()
	{
		if(recolour > 0)
		{
			recolour--;
		}
	}
}
