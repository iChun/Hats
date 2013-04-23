package hats.common.core;

import hats.client.core.TickHandlerClient;
import hats.common.Hats;
import hats.common.thread.ThreadReadHats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy 
{
	public void initCommands(MinecraftServer server)
	{
		ICommandManager manager = server.getCommandManager();
		if(manager instanceof CommandHandler)
		{
			CommandHandler handler = (CommandHandler)manager;
			handler.registerCommand(new CommandHats());
		}
	}
	
	public void initMod()
	{
		getHats();
	}
	
	public void initRenderersAndTextures() {}
	
	public void initSounds() {}
	
	public void initTickHandlers() 
	{
		tickHandlerServer = new TickHandlerServer();
		TickRegistry.registerTickHandler(tickHandlerServer, Side.SERVER);
	}
	
	public void getHats()
	{
		((Thread)new ThreadReadHats(HatHandler.hatsFolder, this, false)).start();
	}
	
	public void getHatsAndOpenGui()
	{
	}
	
	public void clearAllHats()
	{
		HatHandler.hatNames.clear();
		HatHandler.checksums.clear();
		HatHandler.categories.clear();
	}
	
	public void openHatsGui()
	{
	}
	
	public void loadHatFile(File file)
	{
		String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
		HatHandler.hatNames.put(file, hatName);
	}
	
	public void remap(String duplicate, String original)
	{
		File file = null;
		for(Map.Entry<File, String> e : HatHandler.hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(original))
			{
				file = e.getKey();
				break;
			}
		}
		if(file != null)
		{
			HatHandler.hatNames.put(file, duplicate);
		}
	}
	
	//Left for backwards compatibility. Will be removed next MC version.
	@Deprecated
	public HatInfo getRandomHat()
	{
		return HatHandler.getRandomHat();
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
    
    public void saveData(WorldServer world)
    {
    	if(saveData != null)
    	{
    		for(Map.Entry<String, HatInfo> e : playerWornHats.entrySet())
    		{
    			HatInfo hat = e.getValue();
    			saveData.setString(e.getKey() + "_wornHat", hat.hatName);
    			saveData.setInteger(e.getKey() + "_colourR", hat.colourR);
    			saveData.setInteger(e.getKey() + "_colourG", hat.colourG);
    			saveData.setInteger(e.getKey() + "_colourB", hat.colourB);
    		}
    		
            try
            {
            	if(world.getChunkSaveLocation().exists())
            	{
	                File file = new File(world.getChunkSaveLocation(), "hats.dat");
	                if(file.exists())
	                {
	                	File file1 = new File(world.getChunkSaveLocation(), "hats_backup.dat");
	                	if(file1.exists())
	                	{
	                		if(file1.delete())
	                		{
	                			file.renameTo(file1);
	                		}
	                		else
	                		{
	                			Hats.console("Failed to delete mod backup data!", true);
	                		}
	                	}
	                	else
	                	{
	                		file.renameTo(file1);
	                	}
	                }
	                CompressedStreamTools.writeCompressed(saveData, new FileOutputStream(file));
            	}
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
                throw new RuntimeException("Failed to save hat data");
            }
    	}
    	else
    	{
    		Hats.console("Mod data is null! This is a problem!", true);
    	}
    }
    
    public void sendPlayerListOfWornHats(EntityPlayer player, boolean sendAllPlayerHatInfo)
    {
    	this.sendPlayerListOfWornHats(player, sendAllPlayerHatInfo, true);
    }
    
    public void sendPlayerListOfWornHats(EntityPlayer player, boolean sendAllPlayerHatInfo, boolean ignorePlayer) //if false send the only player's info to all players
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bytes);

		try
		{
			stream.writeByte(1); //packetID;
			
			if(sendAllPlayerHatInfo)
			{
				Iterator<Entry<String, HatInfo>> ite = Hats.proxy.playerWornHats.entrySet().iterator();
				
				while(ite.hasNext())
				{
					Entry<String, HatInfo> e = ite.next();
					
					HatInfo hat = e.getValue();
					
					stream.writeUTF(e.getKey());
					stream.writeUTF(hat.hatName);
					stream.writeInt(hat.colourR);
					stream.writeInt(hat.colourG);
					stream.writeInt(hat.colourB);
					
					if(bytes.toByteArray().length > 32000)
					{
						stream.writeUTF("#endPacket");
						stream.writeUTF("#endPacket");
						
						PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
						
						bytes = new ByteArrayOutputStream();
						stream = new DataOutputStream(bytes);
						
			        	stream.writeByte(1); //id
					}
				}
				PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
			}
			else
			{
				HatInfo hat = playerWornHats.get(player.username);
				if(hat == null)
				{
					hat = new HatInfo();
				}
				
				stream.writeUTF(player.username);
				stream.writeUTF(hat.hatName);
				stream.writeInt(hat.colourR);
				stream.writeInt(hat.colourG);
				stream.writeInt(hat.colourB);

				Packet250CustomPayload packet = new Packet250CustomPayload("Hats", bytes.toByteArray());
				
				for(int i = 0; i < FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.size(); i++)
				{
					EntityPlayer player1 = (EntityPlayer)FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.get(i);
					
					if(player.username.equalsIgnoreCase(player1.username) && ignorePlayer)
					{
						continue;
					}
					
					PacketDispatcher.sendPacketToAllPlayers(packet);
				}
			}

		}
		catch(IOException e)
		{}

    }
    
	public static NBTTagCompound saveData = null;
	
	public static HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	
	public static TickHandlerClient tickHandlerClient;
	public static TickHandlerServer tickHandlerServer;
}
