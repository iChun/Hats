package me.ichun.mods.hats.common.core;

public class TimeActiveInfo 
{

	public boolean active;
	public int timeLeft;
	public int levels;
	
	public void tick()
	{
		if(active && timeLeft > 0)
		{
			timeLeft--;
		}
	}
}
