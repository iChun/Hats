package hats.common.core;

import hats.client.core.TickHandlerClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.server.MinecraftServer;

public class CommonProxy 
{
	public void initCommands(MinecraftServer server)
	{
	}
	
	public void initMod()
	{
		getHats();
	}
	
	public void initRenderersAndTextures() {}
	
	public void initSounds() {}
	
	public void initTickHandlers() 
	{
	}
	
	public void getHats()
	{
		((Thread)new ThreadReadHats(hatsFolder, this)).start();
	}
	
	public void postGetHats()
	{
	}
	
	public String getRandomHatName()
	{
		ArrayList<String> hatNameList = new ArrayList<String>();
		
		Iterator<Entry<File, String>> ite = hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			hatNameList.add(e.getValue());
		}
		
		if(hatNameList.size() <= 0)
		{
			return "";
		}
		
		return hatNameList.get((new Random()).nextInt(hatNameList.size()));
	}
	
	public static HashMap<File, String> hatNames = new HashMap<File, String>();
	public static File hatsFolder;
	
	public static TickHandlerClient tickHandlerClient;
	
}
