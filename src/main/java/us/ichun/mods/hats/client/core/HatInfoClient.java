package us.ichun.mods.hats.client.core;

import us.ichun.mods.hats.common.core.HatInfo;
import us.ichun.mods.hats.common.core.HatInfo;

public class HatInfoClient extends HatInfo
{
	public int recolour;
	
	public boolean doNotRender;
	
	public int prevColourR;
	public int prevColourG;
	public int prevColourB;
    public int prevAlpha;
	
	public HatInfoClient()
	{
		super();
	}
	
	public HatInfoClient(String string) 
	{
		super(string);
	}
	
	public HatInfoClient(String string, int i, int j, int k, int alpha)
	{
		super(string, i, j, k, alpha);
	}

	public void inherit(HatInfoClient info)
	{
		prevColourR = info.colourR;
		prevColourG = info.colourG;
		prevColourB = info.colourB;
        prevAlpha = info.alpha;
		if(info.hatName.equalsIgnoreCase(hatName) && !(prevColourR == colourR && prevColourG == colourG && prevColourB == colourB && prevAlpha == alpha))
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
