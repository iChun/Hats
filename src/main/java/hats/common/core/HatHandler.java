package hats.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import hats.common.packet.*;
import ichun.common.core.network.PacketHandler;
import ichun.common.core.util.MD5Checksum;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HatHandler 
{

	public static boolean readHatFromFile(File file)
	{
		return readHatFromFile(file, false);
	}
	
	public static boolean readHatFromFile(File file, boolean category)
	{
		String md5 = MD5Checksum.getMD5Checksum(file);
		
		if(HatHandler.checksums.get(md5) == null)
		{
			HatHandler.checksums.put(md5, file);
		}
		else
		{
			if(!category)
			{
				Hats.console("Rejecting " + file.getName() + "! Identical to " + HatHandler.checksums.get(md5).getName(), true);
			}
			else
			{
				return false;
			}
			return true;
		}

		if(file.getName().endsWith(".tcn"))
		{
			boolean hasTexture = false;
			boolean isSafe = true;
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
						else if(!entry.getName().endsWith(".xml"))
						{
							isSafe = false;
						}
					}
				}
				
				zipFile.close();

			}
			catch(EOFException e1)
			{
				Hats.console("Failed to load: " + file.getName() + " is corrupted!", true);
				return false;
			}
			catch(IOException e1)
			{
				Hats.console("Failed to load: " + file.getName() + " cannot be read!", true);
				return false;
			} 
			catch (Exception e1) 
			{
				Hats.console("Failed to load: " + file.getName() + " threw a generic exception!", true);
				e1.printStackTrace();
				return false;
			}
			
			if(Hats.config.getInt("safeLoad") == 1 && !isSafe)
			{
				Hats.console("Rejecting " + file.getName() + "! It contains files which are not XML or PNG files!", true);
				return false;
			}
			
			if(hasTexture)
			{
				Hats.proxy.loadHatFile(file);
				return true;
			}
			else
			{
				Hats.console("Failed to load: " + file.getName() + " has no texture!", true);
			}
		}
		return false;
	}
	
	public static int loadCategory(File dir)
	{
		int hatCount = 0;
		if(dir.isDirectory())
		{
			ArrayList<String> hatsToLoad = new ArrayList<String>();
			ArrayList<String> categoryHats = new ArrayList<String>();
			File[] files = dir.listFiles();
			for(File file : files)
			{
				if(file.getName().endsWith(".tcn"))
				{
					String hatName = file.getName().substring(0, file.getName().length() - 4);
					hatsToLoad.add(hatName.toLowerCase());
					categoryHats.add(hatName);
					for(Map.Entry<File, String> e : HatHandler.hatNames.entrySet())
					{
						String hatEntryName = e.getValue();
						for(int i = hatsToLoad.size() - 1; i >= 0; i--)
						{
							String hatCategoryEntryName = hatsToLoad.get(i);
							if(hatCategoryEntryName.equalsIgnoreCase(hatEntryName))
							{
								hatsToLoad.remove(i);
								break;
							}
						}
					}
					if(!file.isDirectory() && HatHandler.readHatFromFile(file, true) && !dir.getName().equalsIgnoreCase("Favourites"))
					{
						hatCount++;	
					}
				}
			}
			
			categories.put(dir.getName(), categoryHats);
		}
		return hatCount;
	}
	
	public static void deleteHat(String hatName, boolean disable)
	{
		deleteHat(hatsFolder, hatName, disable);
	}
	
	public static void deleteHat(File dir, String hatName, boolean disable)
	{
		File[] files = dir.listFiles();
		for(File file : files)
		{
			if(!file.isDirectory() && file.getName().equalsIgnoreCase(hatName + ".tcn"))
			{
				if(disable)
				{
					File disabledDir = new File(dir, "/Disabled");
					if(!disabledDir.exists())
					{
						disabledDir.mkdirs();
					}
					File disabledName = new File(disabledDir, hatName + ".tcn");
					if(disabledName.exists())
					{
						disabledName.delete();
					}
					if(!file.renameTo(disabledName))
					{
						Hats.console("Failed to disable hat: " + file.getName());
					}
				}
				else
				{
					if(!file.delete())
					{
						Hats.console("Failed to delete hat: " + file.getName());
					}
				}
				break;
			}
		}
		files = dir.listFiles();
		for(File file : files)
		{
			if(file.isDirectory() && !file.getName().equalsIgnoreCase("Disabled"))
			{
				deleteHat(file, hatName, disable);
			}
		}
	}
	
	public static boolean isInFavourites(String hatName)
	{
		return isInCategory(hatName, "Favourites");
	}
	
	public static boolean isInCategory(String hatName, String category)
	{
		if(category.equalsIgnoreCase("Contributors"))
		{
			return false;
		}
		ArrayList<String> favs = categories.get(category);
		if(favs != null)
		{
			for(String s : favs)
			{
				if(s.equalsIgnoreCase(hatName))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isContributor(String hatName)
	{
		ArrayList<String> favs = categories.get("Contributors");
		if(favs != null)
		{
			for(String s : favs)
			{
				if(s.equalsIgnoreCase(hatName))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static void addToCategory(String hatName, String category)
	{
		ArrayList<String> favs = categories.get(category);
		if(favs != null)
		{
			boolean contained = false;
			for(int i = favs.size() - 1; i >= 0; i--)
			{
				String fav = favs.get(i);
				if(fav.equalsIgnoreCase(hatName))
				{
					contained = true;
					break;
				}
			}
			if(!contained)
			{
				for(Map.Entry<File, String> e : hatNames.entrySet())
				{
					if(hatName.toLowerCase().equalsIgnoreCase(e.getValue()))
					{
						File favFile = new File(hatsFolder, "/" + category + "/" + hatName + ".tcn");
						
				    	InputStream inStream = null;
				    	OutputStream outStream = null;
	
				    	try
				    	{
				    		inStream = new FileInputStream(e.getKey());
				    		outStream = new FileOutputStream(favFile);
				    		
				    		byte[] buffer = new byte[1024];
				    		
				    		int length;
				    		
				    		while ((length = inStream.read(buffer)) > 0)
				    		{
				    	    	outStream.write(buffer, 0, length);
				    	    }
				    	}
				    	catch(Exception e1){}
				    	
				    	try
				    	{
					    	if(inStream != null)
					    	{
					    		inStream.close();
					    	}
				    	}
				    	catch(IOException e1){}
				    	try
				    	{
				    		if(outStream != null)
				    		{
				    			outStream.close();
				    		}
				    	}
				    	catch(IOException e1){}
	
						favs.add(hatName);
						break;
					}
				}
			}
		}
	}
	
	public static void removeFromCategory(String hatName, String category)
	{
		ArrayList<String> favs = categories.get(category);
		if(favs != null)
		{
			for(int i = favs.size() - 1; i >= 0; i--)
			{
				String fav = favs.get(i);
				if(fav.equalsIgnoreCase(hatName))
				{
					File favFile = new File(hatsFolder, "/" + category +"/" + hatName + ".tcn");
					File hatFile = new File(hatsFolder, hatName + ".tcn");
					if(!hatFile.exists())
					{
						favFile.renameTo(hatFile);
					}
					else
					{
						favFile.delete();
					}
					favs.remove(i);
					break;
				}
			}
		}
	}
	
	public static void requestHat(String name, EntityPlayer player)
	{
        if(player != null)
        {
            PacketHandler.sendToPlayer(Hats.channels, new PacketRequestHat(name), player);
        }
        else
        {
            PacketHandler.sendToServer(Hats.channels, new PacketRequestHat(name));
        }
	}

    //TODO make sure hats are sent accurately!
	public static void receiveHatData(ByteBuf buffer, EntityPlayer player, boolean isServer)
	{
		if(Hats.config.getInt("allowReceivingOfHats") != 1)
		{
			return;
		}
		try
		{
            String hatName = ByteBufUtils.readUTF8String(buffer);
            byte packets = buffer.readByte();
            byte packetNumber = buffer.readByte();
            int bytesSize = buffer.readInt();

            byte[] byteValues = new byte[bytesSize];

            buffer.readBytes(byteValues);

            ArrayList<byte[]> byteArray = hatParts.get(hatName);
            if(byteArray == null)
            {
                byteArray = new ArrayList<byte[]>();

                hatParts.put(hatName, byteArray);

                for(int i = 0; i < packets; i++)
                {
                    byteArray.add(new byte[0]);
                }
            }

            byteArray.set(packetNumber, byteValues);

            boolean hasAllInfo = true;

            for(int i = 0; i < byteArray.size(); i++)
            {
                byte[] byteList = byteArray.get(i);
                if(byteList.length == 0)
                {
                    hasAllInfo = false;
                }
            }

            if(hasAllInfo)
            {
                File file = new File(hatsFolder, hatName + ".tcn");

                if(file.exists())
                {
                    Hats.console(file.getName() + " already exists! Will not save.");
                    return;
                }

                FileOutputStream fis = new FileOutputStream(file);

                for(int i = 0; i < byteArray.size(); i++)
                {
                    byte[] byteList = byteArray.get(i);
                    fis.write(byteList);
                }

                fis.close();

                String md5 = MD5Checksum.getMD5Checksum(file);

                boolean newHat = HatHandler.checksums.get(md5) == null;

                if(readHatFromFile(file))
                {
                    if(isServer)
                    {
                        ArrayList<String> queuedLists = queuedHats.get(hatName.toLowerCase());
                        if(queuedLists != null)
                        {
                            queuedHats.remove(hatName);
                            for(String name : queuedLists)
                            {
                                EntityPlayer player1 = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(name);
                                if(player1 != null)
                                {
                                    sendHat(hatName, player1);
                                }
                            }
                        }

                        if(newHat)
                        {
                            Hats.console("Received " + file.getName() + " from " + player.getCommandSenderName());
                        }
                        else
                        {
                            Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
                            Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
                            if(!file.delete())
                            {
                                Hats.console("Failed to delete file! We're doomed!", true);
                            }

                            HatInfo info = Hats.proxy.playerWornHats.get(player.getCommandSenderName());
                            Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo(HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase(), info.colourR, info.colourG, info.colourB));
                        }

                        Hats.proxy.saveData(DimensionManager.getWorld(0));

                        Hats.proxy.sendPlayerListOfWornHats(player, false);
                    }
                    else
                    {
                        Hats.proxy.tickHandlerClient.requestedHats.remove(hatName.toLowerCase());

                        if(newHat)
                        {
                            Hats.console("Received " + file.getName() + " from server.");
                            HatHandler.repopulateHatsList();
                        }
                        else
                        {
                            Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
                            Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
                            if(!file.delete())
                            {
                                Hats.console("Failed to delete file! We're doomed!", true);
                            }
                        }
                    }
                }
                else
                {
                    Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! SafeLoad is on, and the Model file contains files which are not XML or PNG files.", true);
                    if(!file.delete())
                    {
                        Hats.console("Failed to delete file! We're doomed!", true);
                    }
                }
            }
		}
		catch(IOException e)
		{
		}
	}

	public static void sendHat(String hatName, EntityPlayer player)
	{
		if(Hats.config.getInt("allowSendingOfHats") != 1)
		{
			return;
		}

		File file = null;
		
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(hatName))
			{
				file = e.getKey();
			}
		}

		if(file != null)
		{
            int fileSize = (int)file.length();

            if(fileSize > 250000)
            {
                Hats.console("Unable to send " + file.getName() + ". It is above the size limit!", true);
                return;
            }
            else if(fileSize == 0)
            {
                Hats.console("Unable to send " + file.getName() + ". The file is empty!", true);
                return;
            }

            Hats.console("Sending " + file.getName() + " to " + (player == null ? "the server" : player.getCommandSenderName()));

            try
            {
                FileInputStream fis = new FileInputStream(file);

                String hatFullName = file.getName().substring(0, file.getName().length() - 4);
                int packetsToSend = (int)Math.ceil((float)fileSize / 32000F);

                int packetCount = 0;
                while(fileSize > 0)
                {
                    byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
                    fis.read(fileBytes);

                    if(player != null)
                    {
                        PacketHandler.sendToPlayer(Hats.channels, new PacketHatFragment(hatFullName, packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes), player);
                    }
                    else
                    {
                        PacketHandler.sendToServer(Hats.channels, new PacketHatFragment(hatFullName, packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes));
                    }

                    packetCount++;
                    fileSize -= 32000;
                }

                fis.close();
            }
            catch(IOException e)
            {
            }
		}
		else if(player != null)
		{
			ArrayList<String> queuedLists = queuedHats.get(hatName.toLowerCase());
			if(queuedLists == null)
			{
				queuedLists = new ArrayList<String>();
				queuedHats.put(hatName, queuedLists);
			}
			queuedLists.add(player.getCommandSenderName());
		}
	}
	
	public static boolean hasHat(String name)
	{
		if(name.equalsIgnoreCase(""))
		{
			return true;
		}
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public static String getHatStartingWith(String name)
	{
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().toLowerCase().startsWith(name.toLowerCase()))
			{
				return e.getValue();
			}
		}
		return name;
	}
	
	public static HatInfo getRandomHat()
	{
		ArrayList<String> hatNameList = new ArrayList<String>();
		
		Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			if(e.getValue().startsWith("(C)".toLowerCase()) && rand.nextFloat() < ((float)Hats.config.getInt("useRandomContributorHats") / 100F))
			{
				continue;
			}
			hatNameList.add(e.getValue());
		}
		
		if(hatNameList.size() <= 0)
		{
			return new HatInfo();
		}
		
		return new HatInfo(hatNameList.get((new Random()).nextInt(hatNameList.size())), 255, 255, 255);
	}
	
	public static ArrayList<String> getAllHatsAsList()
	{
		ArrayList<String> hatNameList = new ArrayList<String>();
		
		Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
			hatNameList.add(name);
		}
		Collections.sort(hatNameList);
		
		return hatNameList;
	}
	
	public static String[] getAllHatsAsArray()
	{
		ArrayList<String> hatNameList = getAllHatsAsList();
		
		String[] hatNameArray = new String[hatNameList.size()];
		
		hatNameList.toArray(hatNameArray);
		
		return hatNameArray;
	}
	
	public static void unlockHat(EntityPlayer player, String hat) 
	{
		if(player == null)
		{
			return;
		}
		ArrayList<String> hats = Hats.proxy.tickHandlerServer.playerHats.get(player.getCommandSenderName());
		if(hats != null)
		{
			for(String s : hats)
			{
				if(s.equalsIgnoreCase(hat))
				{
					return;
				}
			}
			
			Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
			while(ite.hasNext())
			{
				Entry<File, String> e = ite.next();
				String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
				if(name.equalsIgnoreCase(hat))
				{
					hats.add(name);
					
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < hats.size(); i++)
					{
						sb.append(hats.get(i));
						if(i < hats.size() - 1)
						{
							sb.append(":");
						}
					}
					
					Hats.proxy.saveData.setString(player.getCommandSenderName() + "_unlocked", sb.toString());
					Hats.proxy.saveData(DimensionManager.getWorld(0));

                    PacketHandler.sendToPlayer(Hats.channels, new PacketString(0, name), player);


					break;
				}
			}
		}		
	}
	
	@SideOnly(Side.CLIENT)
	public static void reloadAndOpenGui()
	{
		repopulateHatsList();
		if(Hats.config.getSessionInt("playerHatsMode") == 3)
		{
            PacketHandler.sendToServer(Hats.channels, new PacketPing(0, false));
		}
		else if(Hats.config.getSessionInt("playerHatsMode") != 2)
		{
			Hats.proxy.openHatsGui();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void repopulateHatsList()
	{
		Hats.proxy.tickHandlerClient.availableHats.clear();
		Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			Hats.proxy.tickHandlerClient.availableHats.add(e.getKey().getName().substring(0, e.getKey().getName().length() - 4));
		}
		Collections.sort(Hats.proxy.tickHandlerClient.availableHats);
	}
	
	@SideOnly(Side.CLIENT)
	public static void populateHatsList(String s)
	{
		Hats.proxy.tickHandlerClient.availableHats.clear();
		
		String[] split = s.split(":");
		for(String name : split)
		{
			if(!name.trim().equalsIgnoreCase(""))
			{
				Hats.proxy.tickHandlerClient.availableHats.add(name.trim());
			}
		}
		
		Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
			if(name.startsWith("(C)") && name.toLowerCase().contains(Minecraft.getMinecraft().thePlayer.getCommandSenderName().toLowerCase())
					|| name.equalsIgnoreCase("(C) iChun") && Minecraft.getMinecraft().thePlayer.getCommandSenderName().equalsIgnoreCase("ohaiiChun") //special casing for initial contrib hats.
					|| name.equalsIgnoreCase("(C) Mr. Haz") && Minecraft.getMinecraft().thePlayer.getCommandSenderName().equalsIgnoreCase("damien95")
					|| name.equalsIgnoreCase("(C) Fridgeboy") && Minecraft.getMinecraft().thePlayer.getCommandSenderName().equalsIgnoreCase("lacsap32"))
			{
				if(!Hats.proxy.tickHandlerClient.availableHats.contains(name))
				{
					Hats.proxy.tickHandlerClient.availableHats.add(name);
				}
			}
		}

		Collections.sort(Hats.proxy.tickHandlerClient.availableHats);
		
		Hats.proxy.tickHandlerClient.serverHats = new ArrayList<String>(Hats.proxy.tickHandlerClient.availableHats);
	}

	public static boolean canMobHat(EntityLivingBase ent)
	{
		return !ent.isDead && !ent.isChild() && getRenderHelper(ent.getClass()) != null && getRenderHelper(ent.getClass()).canWearHat(ent);
	}
	
	public static RenderOnEntityHelper getRenderHelper(Class clz)
	{
		if(EntityLivingBase.class.isAssignableFrom(clz) && clz != EntityLivingBase.class)
		{
			RenderOnEntityHelper helper = CommonProxy.renderHelpers.get(clz);
			if(helper == null)
			{
				return getRenderHelper(clz.getSuperclass());
			}
			return helper;
		}
		return null;
	}
	
	public static boolean threadLoadComplete = true;
	public static boolean threadContribComplete = true;
	
	public static boolean reloadingHats;
	
	public static File hatsFolder;
	
	public static HashMap<String, ArrayList<String>> queuedHats = new HashMap<String, ArrayList<String>>();
	
	public static HashMap<String, ArrayList<byte[]>> hatParts = new HashMap<String, ArrayList<byte[]>>();
	
	public static HashMap<File, String> hatNames = new HashMap<File, String>();
	
	public static HashMap<String, File> checksums = new HashMap<String, File>();
	
	public static HashMap<String, ArrayList<String>> categories = new HashMap<String, ArrayList<String>>();
	
	public static Random rand = new Random();
	}
