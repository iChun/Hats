package hats.common.thread;

import hats.client.gui.GuiHatSelection;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.CommonProxy;
import hats.common.core.HatHandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.minecraft.network.packet.Packet131MapData;

import org.w3c.dom.Document;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class ThreadReadHats extends Thread 
{
	
	public File hatsFolder;
	
	public CommonProxy proxy;
	
	public boolean loadGuiOnEnd;

	public ThreadReadHats(File dir, CommonProxy prox, boolean gui)
	{
		setName("Hats Mod Hat Hunting Thread");
		setDaemon(true);
		
		hatsFolder = dir;
		proxy = prox;
		
		loadGuiOnEnd = gui;
	}
	
	@Override
	public void run()
	{
		HatHandler.threadLoadComplete = false;
    	HatHandler.threadContribComplete = false;
		
		if(!hatsFolder.exists())
		{
			return;
		}
		
		if(loadGuiOnEnd)
		{
			HatHandler.reloadingHats = true;
			Hats.proxy.clearAllHats();
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
		
		File fav = new File(hatsFolder, "/Favourites");
		if(!fav.exists())
		{
			fav.mkdirs();
		}
		File[] favs = fav.listFiles();
		for(File file : favs)
		{
			if(!file.isDirectory() && file.getName().endsWith(".tcn"))
			{
				File hat = new File(hatsFolder, file.getName());
				if(!hat.exists())
				{
					//Copy hats to the main folder. We don't want to read the hats in the Favourites, we just want the file name reference.
			    	InputStream inStream = null;
			    	OutputStream outStream = null;

			    	try
			    	{
			    		inStream = new FileInputStream(file);
			    		outStream = new FileOutputStream(hat);
			    		
			    		byte[] buffer = new byte[1024];
			    		
			    		int length;
			    		
			    		while ((length = inStream.read(buffer)) > 0)
			    		{
			    	    	outStream.write(buffer, 0, length);
			    	    }
			    	}
			    	catch(Exception e){}
			    	
			    	try
			    	{
				    	if(inStream != null)
				    	{
				    		inStream.close();
				    	}
			    	}
			    	catch(IOException e){}
			    	try
			    	{
			    		if(outStream != null)
			    		{
			    			outStream.close();
			    		}
			    	}
			    	catch(IOException e){}
				}
			}
		}
		
		File[] files = hatsFolder.listFiles();
		for(File file : files)
		{
			if(!file.isDirectory() && HatHandler.readHatFromFile(file))
			{
				hatCount++;	
			}
		}
		
		for(File file : files)
		{
			if(file.isDirectory() && !file.getName().equalsIgnoreCase("Disabled") && !file.getName().equalsIgnoreCase("Contributors"))
			{
				hatCount += HatHandler.loadCategory(file);	
			}
		}
		
		((Thread)new ThreadGetContributors(loadGuiOnEnd)).start();

		Hats.console((loadGuiOnEnd ? "Reloaded " : "Loaded ") + Integer.toString(hatCount) + (hatCount == 1 ? " hat" : " hats"));
		
		HatHandler.threadLoadComplete = true;
		
		if(loadGuiOnEnd)
		{
			if(HatHandler.threadContribComplete)
			{
				HatHandler.reloadAndOpenGui();
			}
			
			try
			{
				sleep(5000);
			}
			catch(Exception e)
			{
				
			}
			
			if(HatHandler.threadContribComplete)
			{
				HatHandler.reloadingHats = false;
			}
		}
	}
}
