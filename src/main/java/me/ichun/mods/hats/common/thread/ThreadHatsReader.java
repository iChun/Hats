package me.ichun.mods.hats.common.thread;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.ichunutil.common.module.tabula.client.formats.ImportList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;


public class ThreadHatsReader extends Thread
{
    public final File hatsFolder;

    public final boolean shouldDownload;

    public final boolean loadGuiOnEnd;

    public ThreadHatsReader(File hatsFolder, boolean shouldDownload, boolean loadGuiOnEnd)
    {
        this.hatsFolder = hatsFolder;
        this.shouldDownload = shouldDownload;
        this.loadGuiOnEnd = loadGuiOnEnd;

        this.setName("Hats Download/Read Hats Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        if(shouldDownload)
        {
            int hatDownloaded = 0;
            try
            {
                URL var1 = new URL("http://www.creeperrepo.net/ichun/static/hatstabula.xml");

                DocumentBuilderFactory var2 = DocumentBuilderFactory.newInstance();
                DocumentBuilder var3 = var2.newDocumentBuilder();
                //Add a timeout of 60 seconds to getting the list, MC stalls without sound for some users.
                URLConnection con = var1.openConnection();
                con.setConnectTimeout(60000);
                con.setReadTimeout(60000);
                Document var4 = var3.parse(con.getInputStream());
                NodeList var5 = var4.getElementsByTagName("File");

                for(int var6 = 0; var6 < 2; ++var6)
                {
                    for(int var7 = 0; var7 < var5.getLength(); ++var7)
                    {
                        Node var8 = var5.item(var7);

                        if(var8.getNodeType() == 1)
                        {
                            Element var9 = (Element)var8;
                            String var10 = var9.getElementsByTagName("Path").item(0).getChildNodes().item(0).getNodeValue();
                            long var11 = Long.parseLong(var9.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());
                            String url = var9.getElementsByTagName("URL").item(0).getChildNodes().item(0).getNodeValue();

                            if(var11 > 0L)
                            {
                                url = url.replaceAll(" ", "%20");
                                if(downloadResource(new URL(url), new File(hatsFolder, var10), var11))
                                {
                                    hatDownloaded++;
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            if(hatDownloaded != 0)
            {
                Hats.console("Downloaded " + hatDownloaded + " hats from Creeperhost Hat Repository");
            }
        }

        int hatCount = 0;

        HatHandler.reloadingHats = true;
        Hats.proxy.clearAllHats();

        //Handle favourites
        File fav = new File(hatsFolder, "/Favourites");
        if(!fav.exists())
        {
            fav.mkdirs();
        }
        File[] favs = fav.listFiles();
        for(File file : favs)
        {
            if(!file.isDirectory() && ImportList.isFileSupported(file))
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

        int contribHats = 0;
        for(File file : files)
        {
            if(file.isDirectory() && !file.getName().equalsIgnoreCase("Disabled"))
            {
                if(file.getName().equalsIgnoreCase("Contributors"))
                {
                    contribHats += HatHandler.loadCategory(file);
                    hatCount += contribHats;
                }
                else
                {
                    hatCount += HatHandler.loadCategory(file);
                }
            }
        }

        Hats.console((loadGuiOnEnd ? "Reloaded " : "Loaded ") + Integer.toString(hatCount) + (hatCount == 1 ? " hat" : " hats. " + contribHats + " are contributor hats."));

        if(loadGuiOnEnd)
        {
            HatHandler.reloadAndOpenGui();
        }

        HatHandler.reloadingHats = false;
    }

    public boolean downloadResource(URL par1URL, File par2File, long size) throws IOException
    {
        if(par2File.exists())
        {
            if(par2File.length() == size || HatHandler.isHatReadable(par2File))
            {
                return false;
            }
        }
        else if(!par2File.getParentFile().exists())
        {
            par2File.getParentFile().mkdirs();
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
                return true;
            }

            var7.write(var5, 0, var9);
        }
    }
}
