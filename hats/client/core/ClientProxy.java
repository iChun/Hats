package hats.client.core;

import hats.common.Hats;
import hats.common.core.CommonProxy;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClientProxy extends CommonProxy 
{

	@Override
	public void getHats()
	{
		super.getHats();
		
		for(File hatFile : hatFiles)
		{
			try
			{
				ZipFile zipFile = new ZipFile(hatFile);
				Enumeration entries = zipFile.entries();
				while(entries.hasMoreElements())
				{
					ZipEntry entry = (ZipEntry)entries.nextElement();
					if(!entry.isDirectory())
					{
						System.out.println(entry);
					}
				}
				
				zipFile.close();
			}
			catch(EOFException e)
			{
				Hats.console("Failed to load: " + hatFile.getName() + " is corrupted!", true);
			}
			catch(IOException e)
			{
				Hats.console("Failed to load: " + hatFile.getName() + " cannot be read!", true);
			}
		}
	}
	
	public static HashMap<BufferedImage, Integer> bufferedImageID = new HashMap<BufferedImage, Integer>();
	public static HashMap<String, BufferedImage> bufferedImages = new HashMap<String, BufferedImage>();
	
}
