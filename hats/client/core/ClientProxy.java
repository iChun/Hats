package hats.client.core;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import hats.common.Hats;
import hats.common.core.CommonProxy;

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
					System.out.println(entry);
							
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
	
}
