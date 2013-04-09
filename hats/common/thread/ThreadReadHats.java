package hats.common.thread;

import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.CommonProxy;
import hats.common.core.HatHandler;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
		
		try
		{
			InputStream in = Hats.class.getResourceAsStream("/hats.zip");
			if(in != null)
			{
				ZipInputStream zipStream = new ZipInputStream(in);
				ZipEntry entry = null;
				
				int extractCount = 0;
				
				while((entry = zipStream.getNextEntry()) != null)
				{
					File file = new File(hatsFolder, entry.getName());
					if(file.exists() && file.length() > 3L)
					{
						continue;
					}
					FileOutputStream out = new FileOutputStream(file);
					
					byte[] buffer = new byte[8192];
					int len;
					while((len = zipStream.read(buffer)) != -1)
					{
						out.write(buffer, 0, len);
					}
					out.close();
					
					extractCount++;
				}
				zipStream.close();
				
				if(extractCount > 0)
				{
					Hats.console("Extracted " + Integer.toString(extractCount) + (extractCount == 1 ? " hat" : " hats" + " from mod zip."));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
		int hatCount = 0;
		
		File[] files = hatsFolder.listFiles();
		for(File file : files)
		{
			if(HatHandler.readHatFromFile(file))
			{
				hatCount++;	
			}
		}

		Hats.console("Loaded " + Integer.toString(hatCount) + (hatCount == 1 ? " hat" : " hats"));
	}
}
