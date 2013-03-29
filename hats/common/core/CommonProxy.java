package hats.common.core;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

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
		if(!hatsFolder.exists())
		{
			return;
		}
		File[] files = hatsFolder.listFiles();
		for(File file : files)
		{
			if(file.getName().endsWith(".tcn"))
			{
				String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
				hatNames.add(hatName);
				hatFiles.add(file);
				
			}
		}
	}
	
	public static ArrayList<File> hatFiles = new ArrayList<File>();
	public static ArrayList<String> hatNames = new ArrayList<String>();
	public static File hatsFolder;
	
}
