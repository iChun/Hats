package hats.common.thread;

import hats.common.Hats;
import hats.common.core.HatHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.minecraft.client.Minecraft;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class ThreadGetContributors extends Thread
{
    /** The folder to store the data in. */
    public File contribFolder;
    
    public boolean loadGuiOnEnd;

    public ThreadGetContributors(boolean load)
    {
        this.setName("Hats Contributors Hat Download Thread");
        this.setDaemon(true);
        this.contribFolder = new File(HatHandler.hatsFolder, "Contributors/");

        loadGuiOnEnd = load;
        
        if (!this.contribFolder.exists() && !this.contribFolder.mkdirs())
        {
            throw new RuntimeException("The working directory could not be created: " + this.contribFolder);
        }
    }

    public void run()
    {
    	int hatCount = 0;
        try
        {
            URL var1 = new URL("http://repo.creeperhost.net/static/ichun/hatscontrib.xml");
            DocumentBuilderFactory var2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder var3 = var2.newDocumentBuilder();
            //Add a timeout of 60 seconds to getting the list, MC stalls without sound for some users.
            URLConnection con = var1.openConnection();
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            Document var4 = var3.parse(con.getInputStream());
            NodeList var5 = var4.getElementsByTagName("File");

            for (int var6 = 0; var6 < 2; ++var6)
            {
                for (int var7 = 0; var7 < var5.getLength(); ++var7)
                {
                    Node var8 = var5.item(var7);

                    if (var8.getNodeType() == 1)
                    {
                        Element var9 = (Element)var8;
                        String var10 = var9.getElementsByTagName("Path").item(0).getChildNodes().item(0).getNodeValue();
                        long var11 = Long.parseLong(var9.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());
                        String url = var9.getElementsByTagName("URL").item(0).getChildNodes().item(0).getNodeValue();

                        if (var11 > 0L)
                        {
                        	url = url.replaceAll(" ", "%20");
                            this.downloadResource(new URL(url), new File(contribFolder, var10), var11);

                            if (getIsClosing())
                            {
                                return;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception var13)
        {
            var13.printStackTrace();
        }
        
        hatCount += HatHandler.loadCategory(contribFolder);
        
        Hats.console("Loaded " + Integer.toString(hatCount) + " contributor" + (hatCount == 1 ? " hat" : " hats"));
        
		HatHandler.threadContribComplete = true;
        
		if(loadGuiOnEnd)
		{
			if(HatHandler.threadLoadComplete)
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
			
			if(HatHandler.threadLoadComplete)
			{
				HatHandler.reloadingHats = false;
			}
		}
    }

    /**
     * Downloads the resource and saves it to disk.
     */
    private void downloadResource(URL par1URL, File par2File, long size) throws IOException
    {
    	if(par2File.exists())
    	{
    		if(par2File.length() == size)
    		{
    			return;
    		}
    	}
    	
        byte[] var5 = new byte[4096];
        
        URLConnection con = par1URL.openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        DataInputStream var6 = new DataInputStream(con.getInputStream());
        DataOutputStream var7 = new DataOutputStream(new FileOutputStream(par2File));
        boolean var8 = false;

        while(true)
        {
            int var9;

            if ((var9 = var6.read(var5)) < 0)
            {
                var6.close();
                var7.close();
                return;
            }

            var7.write(var5, 0, var9);
        }
    }
    
    public boolean getIsClosing()
    {
    	boolean closing = false;
    	net.minecraft.util.ThreadDownloadResources thread = null;
    	try
    	{
			thread = (net.minecraft.util.ThreadDownloadResources)ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), new String[] { "X", "field_71430_V", "downloadResourcesThread"}); 
			if(thread != null)
			{
				closing = (Boolean)ObfuscationReflectionHelper.getPrivateValue(net.minecraft.util.ThreadDownloadResources.class, thread, new String[] { "c", "field_74578_c", "closing"				});
			}
    	}
    	catch(Exception e)
    	{
			Hats.console("Forgot to update obfuscation!", true);
			e.printStackTrace();
    	}
    	return closing;
    }
}
