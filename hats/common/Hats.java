package hats.common;

import hats.client.core.ClientProxy;
import hats.client.core.PacketHandlerClient;
import hats.common.core.CommonProxy;
import hats.common.core.ConnectionHandler;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.core.LoggerHelper;
import hats.common.core.MapPacketHandler;
import hats.common.core.PacketHandlerServer;
import hats.common.core.SessionState;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "Hats", name = "Hats",
			version = Hats.version
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			connectionHandler = ConnectionHandler.class,
			tinyPacketHandler = MapPacketHandler.class,
			clientPacketHandlerSpec = @SidedPacketHandler(channels = { "Hats" }, packetHandler = PacketHandlerClient.class),
			serverPacketHandlerSpec = @SidedPacketHandler(channels = { "Hats" }, packetHandler = PacketHandlerServer.class),
			versionBounds = "[2.1.0,2.2.0)"
				)
public class Hats 
{
	public static final String version = "2.1.2";
	
	//Global Options
	public static int safeLoad = 1;
	public static int allowSendingOfHats = 1;
	public static int allowReceivingOfHats = 1;
	
	//Server Options
	public static int playerHatsMode = 4;
	public static int firstJoinMessage = 1;
	public static String lockedHat = "Straw Hat";
	public static int startTime = 6000;
	public static float timeIncrement = 0.0125F;
	
	//Client Options
	public static int renderInFirstPerson = 0;
	public static int enableInServersWithoutMod = 1;
	public static int shouldOtherPlayersHaveHats = 1;
	public static int randomHat = 2;
	public static String favouriteHat = "Top Hat";
	public static String favouriteHatColourizer = "#ffffff";
	public static int guiKeyBind = Keyboard.KEY_H;
	public static String enabled = "1 2 3 4 5 6 7 8 9";
	public static int maxHatRenders = 300;
	public static int allowContributorHats = 1;
	public static int renderHats = 1;
	
	//RandoMob Options
	public static int randomMobHat = 0;
	public static int useRandomContributorHats = 10;
	public static int resetPlayerHatsOnDeath = 0;
	public static int hatZombie = 1;
	public static int hatCreeper = 1;
	public static int hatEnderman = 1;
	public static int hatSkeleton = 1;
	public static int hatVillager = 1;
	public static int hatGhast = 1;
	public static int hatBlaze = 1;
	public static int hatSquid = 1;
	public static int hatPig = 1;
	public static int hatSpider = 1;
	public static int hatSheep = 1;
	public static int hatCow = 1;
	public static int hatChicken = 1;
	public static int hatSlime = 1;
	public static int hatWolf = 1;
	public static int hatOcelot = 1;
	public static int hatHorse = 1;
	public static int hatWither = 1;
	
	public static HatInfo favouriteHatInfo = new HatInfo();
	
	public static File configFile;
	public static boolean firstConfigLoad = true;
	
	public static boolean hasMorphMod = false;
	
	@Instance("Hats")
	public static Hats instance;
	
	@SidedProxy(clientSide = "hats.client.core.ClientProxy", serverSide = "hats.common.core.CommonProxy")
	public static CommonProxy proxy;

	public static void handleConfig()
	{
		boolean isClient = proxy instanceof ClientProxy;
		
		Configuration config = new Configuration(configFile);
		config.load();
		
		config.addCustomCategoryComment("globalOptions", "These settings affect both servers and clients that loads the mod.");
		safeLoad = addCommentAndReturnInt(config, "globalOptions", "safeLoad", "Enable safe load?\nSafe load forces the mod to reject Techne Model Files that have files other than xml and png files.", safeLoad);
		allowSendingOfHats = addCommentAndReturnInt(config, "globalOptions", "allowSendingOfHats", "Enable sending of model files to the server/client?.", allowSendingOfHats);
		allowReceivingOfHats = addCommentAndReturnInt(config, "globalOptions", "allowReceivingOfHats", "Enable receiving of model files from the server/client?", allowReceivingOfHats);
		
		config.addCustomCategoryComment("serverOptions", "These settings affect only the server that loads the mod.");
		playerHatsMode = addCommentAndReturnInt(config, "serverOptions", "playerHatsMode", "Player Hats Mode:\n1 = Free Mode, All players are free to choose what hat to wear.\n2 = Locked mode, all players must wear the same hat, defined in the config.\n3 = Command Giver Mode, what hat you wear is chosen by people who can use commands.\n4 = Hat Hunting Mode, see a mob with a hat, kill it to unlock\n5 = King of the Hat Mode, only one shall wear a hat. The king has to defend their spot or lose the crown!\n6 = Time Active Mode, players unlock more hats the more time they are active on the server.", playerHatsMode);
		firstJoinMessage = addCommentAndReturnInt(config, "serverOptions", "firstJoinMessage", "Send a \"First join\" message to the player when they connect to a server for the first time?", firstJoinMessage);
		lockedHat = addCommentAndReturnString(config, "serverOptions", "lockedHat", "What hat do players wear in Locked mode (see playerHatsMode 2).\nIf you want different players to wear different hats, use command giver mode.", lockedHat).toLowerCase();
		startTime = addCommentAndReturnInt(config, "serverOptions", "startTime", "For playerhatsMode 6:\nTime required to be active on the server to unlock the first hat.(In ticks)", startTime);
		timeIncrement = (float)addCommentAndReturnInt(config, "serverOptions", "timeIncrement", "For playerhatsMode 6:\nAmount of extra time required to get the next level hat.\nDefault is 125 (1.25%).\nFor 200% time put 20000", (int)Math.floor(timeIncrement * 10000F)) / 10000F;
		
		if(isClient)
		{
			config.addCustomCategoryComment("clientOnly", "These settings affect only the client that loads the mod.");
			
			renderInFirstPerson = addCommentAndReturnInt(config, "clientOnly", "renderInFirstPerson", "Should your hat render in first person?", renderInFirstPerson);
			enableInServersWithoutMod = addCommentAndReturnInt(config, "clientOnly", "enableInServersWithoutMod", "Enable hats in servers without the mod?", enableInServersWithoutMod);
			shouldOtherPlayersHaveHats = addCommentAndReturnInt(config, "clientOnly", "shouldOtherPlayersHaveHats", "Do other players have hats? Only when enableInServersWithoutMod = 1", shouldOtherPlayersHaveHats);
			randomHat = addCommentAndReturnInt(config, "clientOnly", "randomHat", "Should each player have a random hat?\nThey randomly change from time to time.\n0 = No\n1 = Yes\n2 = Yes, but not the player!\nOnly when enableInServersWithoutMod = 1", randomHat);
			favouriteHat = addCommentAndReturnString(config, "clientOnly", "favouriteHat", "What hat do you want to use on servers without the mod? Only when randomHat = 0", favouriteHat).toLowerCase();
			favouriteHatColourizer = addCommentAndReturnString(config, "clientOnly", "favouriteHatColourizer", "Do you want to apply a colourizer to your favourite hat?\nIf no, leave as #ffffff\n(Google \"hex color codes\" if you don\'t understand)\nFormat: #<colour index> or 0x<colour index>\nEg: #ffffff or 0xffffff for white", favouriteHatColourizer).toLowerCase();
			
			favouriteHatInfo = getHatInfoFromConfig();
			
			guiKeyBind = addCommentAndReturnInt(config, "clientOnly", "guiKeyBind", "What key code do you want to use to open the Hat Selection GUI?\nMouse binds are posible, starting from -100 and higher.\nFor info on Key codes, check here: http://www.minecraftwiki.net/wiki/Key_codes", guiKeyBind);
			enabled = addCommentAndReturnString(config, "clientOnly", "personalizeEnabled", "DO NOT CHANGE THIS. PERIOD.\nI'M NOT JOKING.", enabled);
			maxHatRenders = Math.max(addCommentAndReturnInt(config, "clientOnly", "maxHatRenders", "Max number of hats to render in one tick", maxHatRenders), 0);
			
			allowContributorHats = addCommentAndReturnInt(config, "clientOnly", "allowContributorHats", "Show Contributor Hats in the GUI?", allowContributorHats);
			
			renderHats = addCommentAndReturnInt(config, "clientOnly", "renderHats", "Render hats?", renderHats);
			
		}
		
		config.addCustomCategoryComment("randoMobOptions", "These settings affect either the client on randoMob settings or Mob Hunting Mode.");
		if(firstConfigLoad)
		{
			randomMobHat = playerHatsMode != 4 && isClient ? 0 : 10;
		}
		randomMobHat = Math.min(100, Math.max(addCommentAndReturnInt(config, "randoMobOptions", "randomMobHat", "Do mobs have a random chance of having a hat?\n0 = Disabled (0%)\n100 = All mobs (100%)\n(Client)This follows the randomHat setting, meaning if randomHat is 0, all mobs will wear the favouriteHat setting", randomMobHat), 0));
		useRandomContributorHats = Math.min(100, Math.max(addCommentAndReturnInt(config, "randoMobOptions", "useRandomContributorHats", "Allow the use of contributor hats when getting a random hat?\n0 - 100%", useRandomContributorHats), 0));
		resetPlayerHatsOnDeath = addCommentAndReturnInt(config, "randoMobOptions", "resetPlayerHatsOnDeath", "Should player hats be reset when they die?\nOnly in unlockable hats modes\n0 = No\n1 = Yes", resetPlayerHatsOnDeath);
		
		hatZombie = addCommentAndReturnInt(config, "randoMobOptions", "hatZombie", "", hatZombie);
		hatCreeper = addCommentAndReturnInt(config, "randoMobOptions", "hatCreeper", "", hatCreeper);
		hatEnderman = addCommentAndReturnInt(config, "randoMobOptions", "hatEnderman", "", hatEnderman);
		hatSkeleton = addCommentAndReturnInt(config, "randoMobOptions", "hatSkeleton", "", hatSkeleton);
		hatVillager = addCommentAndReturnInt(config, "randoMobOptions", "hatVillager", "", hatVillager);
		hatGhast = addCommentAndReturnInt(config, "randoMobOptions", "hatGhast", "", hatGhast);
		hatBlaze = addCommentAndReturnInt(config, "randoMobOptions", "hatBlaze", "", hatBlaze);
		hatSquid = addCommentAndReturnInt(config, "randoMobOptions", "hatSquid", "", hatSquid);
		hatPig = addCommentAndReturnInt(config, "randoMobOptions", "hatPig", "", hatPig);
		hatSpider = addCommentAndReturnInt(config, "randoMobOptions", "hatSpider", "", hatSpider);
		hatSheep = addCommentAndReturnInt(config, "randoMobOptions", "hatSheep", "", hatSheep);
		hatCow = addCommentAndReturnInt(config, "randoMobOptions", "hatCow", "", hatCow);
		hatChicken = addCommentAndReturnInt(config, "randoMobOptions", "hatChicken", "", hatChicken);
		hatSlime = addCommentAndReturnInt(config, "randoMobOptions", "hatSlime", "", hatSlime);
		hatWolf = addCommentAndReturnInt(config, "randoMobOptions", "hatWolf", "", hatWolf);
		hatOcelot = addCommentAndReturnInt(config, "randoMobOptions", "hatOcelot", "", hatOcelot);
		hatHorse = addCommentAndReturnInt(config, "randoMobOptions", "hatHorse", "", hatHorse);
		hatWither = addCommentAndReturnInt(config, "randoMobOptions", "hatWither", "", hatWither);
		
		config.save();
		
		if(firstConfigLoad)
		{
			firstConfigLoad = false;
		}
	}
	
	public static HatInfo getHatInfoFromConfig()
	{
		String index = favouriteHatColourizer;
		if(index.length() < 7)
		{
			Hats.console("Invalid colourizer length!");
			return new HatInfo(favouriteHat);
		}
		
		try
		{
			int i = Integer.decode(favouriteHatColourizer);
			return new HatInfo(favouriteHat, i >> 16 & 255, i >> 8 & 255, i & 255);
		}
		catch(NumberFormatException e)
		{
			Hats.console("Invalid colourizer string!");
			return new HatInfo(favouriteHat);
		}
	}
	
	@EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		LoggerHelper.init();
		
		HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/hats");
		
		if(!HatHandler.hatsFolder.exists())
		{
			HatHandler.hatsFolder.mkdirs();
		}
		
		configFile = event.getSuggestedConfigurationFile();
		
		handleConfig();
		
		HatHandler.obfuscation = true;
        try
        {
            Field[] fields = World.class.getDeclaredFields();
            for(Field f : fields)
            {
            	f.setAccessible(true);
            	if(f.getName().equalsIgnoreCase("loadedEntityList"))
            	{
            		HatHandler.obfuscation = false;
            		break;
            	}
            }
        }
        catch (Exception e)
        {
        }
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.initMod();
		proxy.initTickHandlers();
		
		proxy.initRenderersAndTextures();
		
		GameRegistry.registerPlayerTracker(new ConnectionHandler());
		
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
	        try
	        {
	        	Class clz = Class.forName("morph.common.Morph");
	        	hasMorphMod = true;
	        }
	        catch(Exception e)
	        {
	        }
		}
	}
	
	public static int addCommentAndReturnInt(Configuration config, String cat, String s, String comment, int i) //Taken from iChun Util
	{
		Property prop = config.get(cat, s, i);
		if(!firstConfigLoad)
		{
			prop.set(Integer.toString(i));
		}
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getInt();
	}
	
	public static String addCommentAndReturnString(Configuration config, String cat, String s, String comment, String value)
	{
		Property prop = config.get(cat, s, value);
		if(!firstConfigLoad)
		{
			prop.set(value);
		}
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getString();
	}
	
	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && event.world.provider.dimensionId == 0)
		{
			proxy.loadData((WorldServer)event.world);
		}
	}
	
	@ForgeSubscribe
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && event.world.provider.dimensionId == 0)
		{
			proxy.saveData((WorldServer)event.world);
		}
	}
	
	@ForgeSubscribe
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(SessionState.serverHatMode >= 4)
			{
				if(SessionState.serverHatMode == 4)
				{
					if(!(event.entityLiving instanceof EntityPlayer) && event.source.getEntity() instanceof EntityPlayer && !((EntityPlayer)event.source.getEntity()).capabilities.isCreativeMode)
					{
						proxy.tickHandlerServer.playerKilledEntity(event.entityLiving, (EntityPlayer)event.source.getEntity());
					}
				}
				
				if(event.entityLiving instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer)event.entityLiving;
					EntityPlayer executer = null;
					if(event.source.getEntity() instanceof EntityPlayer)
					{
						executer = (EntityPlayer)event.source.getEntity();
					}
					if(SessionState.serverHatMode == 5)
					{
						//King died
						if(SessionState.currentKing.equalsIgnoreCase(player.username))
						{
							if(executer != null)
							{
								Hats.proxy.tickHandlerServer.updateNewKing(executer.username, null, true);
								Hats.proxy.tickHandlerServer.updateNewKing(executer.username, executer, true);
								FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromTranslationWithSubstitutions("hats.kingOfTheHat.update.playerSlayed", new Object[] { player.username, executer.username }));
							}
							else
							{
								List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
								List<EntityPlayerMP> list = new ArrayList(players);
								list.remove(player);
								if(!list.isEmpty())
								{
									EntityPlayer newKing = list.get(player.worldObj.rand.nextInt(list.size()));
									Hats.proxy.tickHandlerServer.updateNewKing(newKing.username, null, true);
									Hats.proxy.tickHandlerServer.updateNewKing(newKing.username, newKing, true);
									FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromTranslationWithSubstitutions("hats.kingOfTheHat.update.playerDied", new Object[] { player.username, newKing.username }));
								}
							}
						}
						else if(executer != null && SessionState.currentKing.equalsIgnoreCase(executer.username))
						{
							ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(executer.username);
							if(playerHatsList == null)
							{
								playerHatsList = new ArrayList<String>();
								Hats.proxy.tickHandlerServer.playerHats.put(executer.username, playerHatsList);
							}

							ArrayList<String> newHats = HatHandler.getAllHatsAsList();
							
							newHats.removeAll(playerHatsList);

							EntityPlayerMP newKingEnt = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(executer.username);
							
							if(newKingEnt != null && !newHats.isEmpty())
							{
								HatHandler.unlockHat(newKingEnt, newHats.get(newKingEnt.worldObj.rand.nextInt(newHats.size())));
							}
						}
					}	
					
					if(resetPlayerHatsOnDeath == 1)
					{
						proxy.tickHandlerServer.playerDeath((EntityPlayer)event.entityLiving);
					}
				}
			}
		}
		proxy.tickHandlerServer.mobHats.remove(event.entityLiving);
	}

	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event)
	{
		SessionState.serverHasMod = true;
		SessionState.serverHatMode = playerHatsMode;
		SessionState.serverHat = lockedHat;
		proxy.initCommands(event.getServer());
	}
	
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event)
	{
		proxy.tickHandlerServer.mobHats.clear();
		proxy.tickHandlerServer.playerHats.clear();
		proxy.tickHandlerServer.playerActivity.clear();
		proxy.tickHandlerServer.playerTradeRequests.clear();
		proxy.tickHandlerServer.activeTrades.clear();
		proxy.playerWornHats.clear();
		proxy.saveData = null;
	}
	
    public static NBTTagCompound readNBTTagCompound(DataInput par0DataInput) throws IOException
    {
        short short1 = par0DataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            par0DataInput.readFully(abyte);
            return CompressedStreamTools.decompress(abyte);
        }
    }

    public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutput.writeShort((short)abyte.length);
            par1DataOutput.write(abyte);
        }
    }
	
    public static int getNetId()
    {
    	return ((NetworkModHandler)FMLNetworkHandler.instance().findNetworkModHandler(Hats.instance)).getNetworkId();
    }

    
    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	LoggerHelper.log(warning ? Level.WARNING : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }

    public static void console(String s)
    {
    	console(s, false);
    }
}
