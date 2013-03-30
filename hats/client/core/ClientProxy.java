package hats.client.core;

import hats.client.model.ModelHat;
import hats.client.render.RenderHat;
import hats.common.Hats;
import hats.common.core.CommonProxy;
import hats.common.entity.EntityHat;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy 
{

	@Override
	public void initRenderersAndTextures() 
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHat.class, new RenderHat());
	}

	@Override
	public void initTickHandlers() 
	{
		tickHandlerClient = new TickHandlerClient();
		TickRegistry.registerTickHandler(tickHandlerClient, Side.CLIENT);
	}

	
	@Override
	public void postGetHats()
	{
		Iterator<Entry<File,String>> hatFiles = hatNames.entrySet().iterator();
		
		while(hatFiles.hasNext())
		{
			Entry<File, String> e = hatFiles.next();
			try
			{
				ZipFile zipFile = new ZipFile(e.getKey());
				Enumeration entries = zipFile.entries();
				
				while(entries.hasMoreElements())
				{
					ZipEntry entry = (ZipEntry)entries.nextElement();
					if(!entry.isDirectory())
					{
						if(entry.getName().endsWith(".png"))
						{
							InputStream stream = zipFile.getInputStream(entry);
							BufferedImage image = ImageIO.read(stream);
							
							bufferedImages.put(e.getValue(), image);
							bufferedImageID.put(image, -1);
							
						}
						if(entry.getName().endsWith(".xml"))
						{
							InputStream stream = zipFile.getInputStream(entry);
							
							DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							
							Document doc = builder.parse(stream);
							
							models.put(e.getValue(), new ModelHat(doc));	
							
						}
					}
				}
				
				zipFile.close();
			}
			catch(EOFException e1)
			{
				Hats.console("Failed to load: " + e.getKey().getName() + " is corrupted!", true);
			}
			catch(IOException e1)
			{
				Hats.console("Failed to load: " + e.getKey().getName() + " cannot be read!", true);
			} 
			catch (Exception e1) 
			{
				Hats.console("Failed to load: " + e.getKey().getName() + " threw a generic exception!", true);
				e1.printStackTrace();
			}
		}
	}
	
	public static HashMap<BufferedImage, Integer> bufferedImageID = new HashMap<BufferedImage, Integer>();
	public static HashMap<String, BufferedImage> bufferedImages = new HashMap<String, BufferedImage>();
	public static HashMap<String, ModelHat> models = new HashMap<String, ModelHat>();
	
}
