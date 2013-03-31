package hats.common.thread;

import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.CommonProxy;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

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
				boolean hasTexture = false;
				try
				{
					ZipFile zipFile = new ZipFile(file);
					Enumeration entries = zipFile.entries();
					
					while(entries.hasMoreElements())
					{
						ZipEntry entry = (ZipEntry)entries.nextElement();
						if(!entry.isDirectory())
						{
							if(entry.getName().endsWith(".png"))
							{
								hasTexture = true;
							}
						}
					}
					
					zipFile.close();

				}
				catch(EOFException e1)
				{
					Hats.console("Failed to load: " + file.getName() + " is corrupted!", true);
				}
				catch(IOException e1)
				{
					Hats.console("Failed to load: " + file.getName() + " cannot be read!", true);
				} 
				catch (Exception e1) 
				{
					Hats.console("Failed to load: " + file.getName() + " threw a generic exception!", true);
					e1.printStackTrace();
				}
				
				if(hasTexture)
				{
					String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
					proxy.hatNames.put(file, hatName);
					hatCount++;
				}
				else
				{
					Hats.console("Failed to load: " + file.getName() + " has no texture!", true);
				}
			}
		}

		proxy.postGetHats();
		
		Hats.console("Loaded " + Integer.toString(hatCount) + (hatCount == 1 ? " hat" : " hats"));
	}
}
