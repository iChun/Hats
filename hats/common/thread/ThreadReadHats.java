package hats.common.thread;

import hats.common.Hats;
import hats.common.core.CommonProxy;

import java.io.File;

public class ThreadReadHats extends Thread 
{
	
	public File hatsFolder;
	
	public CommonProxy proxy;

	public ThreadReadHats(File dir, CommonProxy prox)
	{
		setName("Hats Mod Hat Hunting Thread");
		setDaemon(true);
		
		hatsFolder = dir;
		proxy = prox;
	}
	
	@Override
	public void run()
	{
		if(!hatsFolder.exists())
		{
			return;
		}
		
		int hatCount = 0;
		
		File[] files = hatsFolder.listFiles();
		for(File file : files)
		{
			if(file.getName().endsWith(".tcn"))
			{
				String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
				proxy.hatNames.put(file, hatName);
				hatCount++;
			}
		}

		proxy.postGetHats();
		
		Hats.console("Loaded " + Integer.toString(hatCount) + (hatCount == 1 ? " hat" : " hats"));
	}
}
