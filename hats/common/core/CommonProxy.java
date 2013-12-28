package hats.common.core;

import hats.api.RenderOnEntityHelper;
import hats.client.core.TickHandlerClient;
import hats.client.render.helper.HelperBlaze;
import hats.client.render.helper.HelperChicken;
import hats.client.render.helper.HelperCow;
import hats.client.render.helper.HelperCreeper;
import hats.client.render.helper.HelperEnderman;
import hats.client.render.helper.HelperGhast;
import hats.client.render.helper.HelperGiantZombie;
import hats.client.render.helper.HelperHorse;
import hats.client.render.helper.HelperOcelot;
import hats.client.render.helper.HelperPig;
import hats.client.render.helper.HelperPlayer;
import hats.client.render.helper.HelperSheep;
import hats.client.render.helper.HelperSkeleton;
import hats.client.render.helper.HelperSlime;
import hats.client.render.helper.HelperSpider;
import hats.client.render.helper.HelperSquid;
import hats.client.render.helper.HelperVillager;
import hats.client.render.helper.HelperWither;
import hats.client.render.helper.HelperWolf;
import hats.client.render.helper.HelperZombie;
import hats.common.Hats;
import hats.common.thread.ThreadReadHats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
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
	public static HashMap<Class, RenderOnEntityHelper> renderHelpers = new HashMap<Class, RenderOnEntityHelper>();
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
		
		CommonProxy.renderHelpers.put(EntityBlaze.class			, new HelperBlaze());
		CommonProxy.renderHelpers.put(EntityChicken.class		, new HelperChicken());
		CommonProxy.renderHelpers.put(EntityCow.class			, new HelperCow());
		CommonProxy.renderHelpers.put(EntityCreeper.class		, new HelperCreeper());
		CommonProxy.renderHelpers.put(EntityEnderman.class		, new HelperEnderman());
		CommonProxy.renderHelpers.put(EntityGhast.class			, new HelperGhast());
		CommonProxy.renderHelpers.put(EntityGiantZombie.class	, new HelperGiantZombie());
		CommonProxy.renderHelpers.put(EntityHorse.class			, new HelperHorse());
		CommonProxy.renderHelpers.put(EntityOcelot.class		, new HelperOcelot());
		CommonProxy.renderHelpers.put(EntityPig.class			, new HelperPig());
		CommonProxy.renderHelpers.put(EntityPlayer.class		, new HelperPlayer());
		CommonProxy.renderHelpers.put(EntitySheep.class			, new HelperSheep());
		CommonProxy.renderHelpers.put(EntitySkeleton.class		, new HelperSkeleton());
		CommonProxy.renderHelpers.put(EntitySlime.class			, new HelperSlime());
		CommonProxy.renderHelpers.put(EntitySpider.class		, new HelperSpider());
		CommonProxy.renderHelpers.put(EntitySquid.class			, new HelperSquid());
		CommonProxy.renderHelpers.put(EntityVillager.class		, new HelperVillager());
		CommonProxy.renderHelpers.put(EntityWolf.class			, new HelperWolf());
		CommonProxy.renderHelpers.put(EntityZombie.class		, new HelperZombie());
		CommonProxy.renderHelpers.put(EntityWither.class		, new HelperWither());
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
    		
    		for(Map.Entry<String, TimeActiveInfo> e : tickHandlerServer.playerActivity.entrySet())
    		{
    			TimeActiveInfo info = e.getValue();
    			saveData.setInteger(e.getKey() + "_activityLevels", info.levels);
    			saveData.setInteger(e.getKey() + "_activityTimeleft", info.timeLeft);
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
