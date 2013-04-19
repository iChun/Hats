package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
			
			if(Hats.safeLoad == 1 && !isSafe)
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
					favFile.delete();
					favs.remove(i);
					break;
				}
			}
		}
	}
	
	public static void requestHat(String name, EntityPlayer player)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bytes);

        try
        {
        	stream.writeUTF(name);
        	
        	if(player != null)
        	{
        		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)1, bytes.toByteArray()), (Player)player);
        	}
        	else
        	{
        		PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)1, bytes.toByteArray()));
        	}
        }
        catch(IOException e)
        {}
	}
	
	public static void receiveHatData(DataInputStream stream, EntityPlayer player, boolean isServer)
	{
		if(Hats.allowReceivingOfHats != 1)
		{
			return;
		}
		try
		{
			String hatName = stream.readUTF();
			byte packets = stream.readByte();
			byte packetNumber = stream.readByte();
			int bytesSize = stream.readInt();
			
			byte[] byteValues = new byte[bytesSize];
			
			stream.read(byteValues);
			
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
									sendHat(hatName, player);
								}
							}
						}
						
						if(newHat)
						{
							Hats.console("Received " + file.getName() + " from " + player.username);
						}
						else
						{
							Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
							Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.username : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
							if(!file.delete())
							{
								Hats.console("Failed to delete file! We're doomed!", true);
							}
							
							HatInfo info = Hats.proxy.playerWornHats.get(player.username);
							Hats.proxy.playerWornHats.put(player.username, new HatInfo(HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase(), info.colourR, info.colourG, info.colourB));
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
							
							Hats.proxy.tickHandlerClient.availableHats.clear();
							Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
							while(ite.hasNext())
							{
								Entry<File, String> e = ite.next();
								Hats.proxy.tickHandlerClient.availableHats.add(e.getKey().getName().substring(0, e.getKey().getName().length() - 4));
							}
							Collections.sort(Hats.proxy.tickHandlerClient.availableHats);
						}
						else
						{
							Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
							Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.username : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
							if(!file.delete())
							{
								Hats.console("Failed to delete file! We're doomed!", true);
							}
						}
					}
				}
				else
				{
					Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.username : "server") + "! SafeLoad is on, and the Model file contains files which are not XML or PNG files.", true);
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
		if(Hats.allowSendingOfHats != 1)
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
			try
			{
				
				int fileSize = (int)file.length();
				
				if(fileSize > 250000)
				{
					Hats.console("Unable to send " + file.getName() + ". It is above the size limit!", true);
				}
				
				FileInputStream fis = new FileInputStream(file);
				int packetCount = 0;
				
				Hats.console("Sending " + file.getName() + " to " + (player == null ? "the server" : player.username));
				
				while(fileSize > 0)
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream = new DataOutputStream(bytes);
	
			        stream.writeByte(2); //id
			        stream.writeUTF(file.getName().substring(0, file.getName().length() - 4)); //name
			        stream.writeByte((int)Math.ceil((float)fileSize / 32000F)); //number of overall packets to send
			        stream.writeByte(packetCount); // packet number being sent
			        stream.writeInt(fileSize > 32000 ? 32000 : fileSize); //byte size
			        
			        byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
			        fis.read(fileBytes);
			        stream.write(fileBytes); //hat info

			        packetCount++;
			        fileSize -= 32000;
			        
			        if(player != null)
			        {
			        	PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
			        }
			        else
			        {
			        	PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("Hats", bytes.toByteArray()));
			        }
				}
				
				fis.close();
			}
			catch(Exception e)
			{
				
			}
			
			//byte size should be 32000
		}
		else if(player != null)
		{
			ArrayList<String> queuedLists = queuedHats.get(hatName.toLowerCase());
			if(queuedLists == null)
			{
				queuedLists = new ArrayList<String>();
				queuedHats.put(hatName, queuedLists);
			}
			queuedLists.add(player.username);
		}
	}
	
	public static boolean hasHat(String name)
	{
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
	
	@SideOnly(Side.CLIENT)
	public static void reloadAndOpenGui()
	{
		Hats.proxy.tickHandlerClient.availableHats.clear();
		Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			Hats.proxy.tickHandlerClient.availableHats.add(e.getKey().getName().substring(0, e.getKey().getName().length() - 4));
		}
		Collections.sort(Hats.proxy.tickHandlerClient.availableHats);
		
		if(Hats.playerHatsMode == 3)
		{
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bytes);

	        try
	        {
	        	stream.writeByte(0);
	        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)2, bytes.toByteArray()));
	        }
	        catch(IOException e)
	        {}
		}
		else
		{
			Hats.proxy.openHatsGui();
		}
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

}
