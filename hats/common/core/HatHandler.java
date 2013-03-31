package hats.common.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

public class HatHandler 
{

	public static boolean hasHat(String name)
	{
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public static File hatsFolder;
	public static HashMap<File, String> hatNames = new HashMap<File, String>();

}
