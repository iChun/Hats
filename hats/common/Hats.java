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

import java.io.File;
import java.util.logging.Level;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopped;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "Hats", name = "Hats",
			version = "1.0.0"
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			connectionHandler = ConnectionHandler.class,
			tinyPacketHandler = MapPacketHandler.class,
			clientPacketHandlerSpec = @SidedPacketHandler(channels = { "Hats" }, packetHandler = PacketHandlerClient.class),
			serverPacketHandlerSpec = @SidedPacketHandler(channels = { "Hats" }, packetHandler = PacketHandlerServer.class)
				)
public class Hats 
{
	//Texture editing time
	public static final String version = "1.1.0";
	
	//Global Options
	public static int safeLoad = 1;
	public static int allowSendingOfHats = 1;
	public static int allowReceivingOfHats = 1;
	
	//Server Options
	public static int playerHatsMode = 1;
	public static String defaultHat = "Top Hat";
	
	//Client Options
	public static int renderInFirstPerson = 0;
	public static int enableInServersWithoutMod = 1;
	public static int shouldOtherPlayersHaveHats = 1;
	public static int randomHat = 1;
	public static String favouriteHat = "Top Hat";
	public static String favouriteHatColourizer = "#ffffff";
	public static int guiKeyBind = Keyboard.KEY_H;
	
	public static HatInfo favouriteHatInfo = new HatInfo();
	
	public static File configFile;
	public static boolean firstConfigLoad = true;
	
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
		playerHatsMode = addCommentAndReturnInt(config, "serverOptions", "playerHatsMode", "Player Hats Mode:\n1 = Free Mode, All players are free to choose what hat to wear.\n2 = NOT AVAILABLE YET! Quest Mode, hats are rewarded by achieving certain tasks. NOT AVAILABLE YET!\n3 = Command Giver Mode, what hat you wear is chosen by people who can use commands.", playerHatsMode);
		defaultHat = addCommentAndReturnString(config, "serverOptions", "defaultHat", "All players are given this hat by default, even in Quest Mode.\nLeave blank for no hat.", defaultHat).toLowerCase();
		
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
			
		}
		
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
	
	@PreInit
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
		
	}
	
	@Init
	public void load(FMLInitializationEvent event)
	{
		proxy.initMod();
		proxy.initTickHandlers();
		
		proxy.initRenderersAndTextures();
		
		GameRegistry.registerPlayerTracker(new ConnectionHandler());
		
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event)
	{
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

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.initCommands(event.getServer());
	}
	
	@ServerStarted
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@ServerStopped
	public void serverStopped(FMLServerStoppedEvent event)
	{
		proxy.playerAvailableHats.clear();
		proxy.playerWornHats.clear();
		proxy.saveData = null;
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
