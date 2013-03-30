package hats.common.core;

import hats.client.core.TickHandlerClient;
import hats.common.Hats;
import hats.common.thread.ThreadReadHats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class CommonProxy 
{
	public void initCommands(MinecraftServer server)
	{
	}
	
	public void initMod()
	{
		getHats();
	}
	
	public void initRenderersAndTextures() {}
	
	public void initSounds() {}
	
	public void initTickHandlers() 
	{
	}
	
	public void getHats()
	{
		((Thread)new ThreadReadHats(hatsFolder, this)).start();
	}
	
	public void postGetHats()
	{
	}
	
	public String getRandomHatName()
	{
		ArrayList<String> hatNameList = new ArrayList<String>();
		
		Iterator<Entry<File, String>> ite = hatNames.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<File, String> e = ite.next();
			hatNameList.add(e.getValue());
		}
		
		if(hatNameList.size() <= 0)
		{
			return "";
		}
		
		return hatNameList.get((new Random()).nextInt(hatNameList.size()));
	}
	
    public void loadData(WorldServer world)
    {
    	try
    	{
    		File file = new File(world.getChunkSaveLocation(), "hats.dat");
    		if(!file.exists())
    		{
    			saveData = new NBTTagCompound();
    			Hats.console("Save data does not exist!");
    			return;
    		}
    		saveData = CompressedStreamTools.readCompressed(new FileInputStream(file));
    	}
    	catch(EOFException e)
    	{
    		Hats.console("Save data is corrupted! Attempting to read from backup.");
    		try
    		{
	    		File file = new File(world.getChunkSaveLocation(), "hats_backup.dat");
	    		if(!file.exists())
	    		{
	    			saveData = new NBTTagCompound();
	    			Hats.console("No backup detected!");
	    			return;
	    		}
	    		saveData = CompressedStreamTools.readCompressed(new FileInputStream(file));
	    		
	    		File file1 = new File(world.getChunkSaveLocation(), "hats.dat");
	    		file1.delete();
	    		file.renameTo(file1);
	    		Hats.console("Restoring data from backup.");
    		}
    		catch(Exception e1)
    		{
    			saveData = new NBTTagCompound();
    			Hats.console("Even your backup data is corrupted. What have you been doing?!", true);
    		}
    	}
    	catch(IOException e)
    	{
    		saveData = new NBTTagCompound();
    		Hats.console("Failed to read save data!");
    	}
    }
    
    public void sendPlayerListOfWornHats(EntityPlayer player, String playerName)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bytes);

		try
		{
			stream.writeByte(1); //packetID;
			
			if(playerName == null)
			{
				Iterator<Entry<String, String>> ite = Hats.proxy.playerWornHats.entrySet().iterator();
				
				while(ite.hasNext())
				{
					Entry<String, String> e = ite.next();
					
					stream.writeUTF(e.getKey());
					stream.writeUTF(e.getValue());
					
					if(bytes.toByteArray().length > 32000)
					{
						stream.writeUTF("#endPacket");
						PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
						
						bytes = new ByteArrayOutputStream();
						stream = new DataOutputStream(bytes);
						
			        	stream.writeByte(1); //id
					}
				}
			}
			else
			{
				String hat = playerWornHats.get(playerName);
				if(hat == null)
				{
					hat = "";
				}
				
				stream.writeUTF(playerName);
				stream.writeUTF(hat);
			}

			PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
		}
		catch(IOException e)
		{}

    }
    
	public static NBTTagCompound saveData = null;
	
	public static HashMap<String, ArrayList> playerAvailableHats = new HashMap<String, ArrayList>();
	public static HashMap<String, String> playerWornHats = new HashMap<String, String>();
	
	public static HashMap<File, String> hatNames = new HashMap<File, String>();
	public static File hatsFolder;
	
	public static TickHandlerClient tickHandlerClient;
	
}
