package hats.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.relauncher.Side;
import hats.client.core.ClientProxy;
import hats.common.core.CommonProxy;
import hats.common.core.EventHandler;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import ichun.common.core.config.Config;
import ichun.common.core.config.ConfigHandler;
import ichun.common.core.config.IConfigUser;
import ichun.common.iChunUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.EnumMap;

@Mod(modid = "Hats", name = "Hats",
			version = Hats.version,
            dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".0.0,)"
                )
public class Hats
    implements IConfigUser
{
	public static final String version = iChunUtil.versionMC +".0.0";

    private static Logger logger = LogManager.getLogger("Hats");

    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    public static Config config;

	public static HatInfo favouriteHatInfo = new HatInfo();
	
	public static boolean hasMorphMod = false;
	
	@Instance("Hats")
	public static Hats instance;
	
	@SidedProxy(clientSide = "hats.client.core.ClientProxy", serverSide = "hats.common.core.CommonProxy")
	public static CommonProxy proxy;

    @Override
    public boolean onConfigChange(Config cfg, Property prop)
    {
        return true;
    }

    //TODO implement something like Hats+ support. Download JSON files with information for classes.
	@Mod.EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/hats");
		
		if(!HatHandler.hatsFolder.exists())
		{
			HatHandler.hatsFolder.mkdirs();
		}

        boolean isClient = proxy instanceof ClientProxy;

        config = ConfigHandler.createConfig(event.getSuggestedConfigurationFile(), "hats", "Hats", logger, instance);

        config.setCurrentCategory("globalOptions", "Global Options", "These settings affect both servers and clients that loads the mod.");
        config.createIntBoolProperty("safeLoad", "Safe Load", "Enable safe load?\nSafe load forces the mod to reject Techne Model Files that have files other than xml and png files.", true, false, true);
        config.createIntBoolProperty("allowSendingOfHats", "Allow Sending Of Hats", "Enable sending of model files to the server/client?", true, false, true);
        config.createIntBoolProperty("allowReceivingOfHats", "Allow Receiving Of Hats", "Enable receiving of model files to the server/client?", true, false, true);

        config.setCurrentCategory("serverOptions", "Server Options", "These settings affect only the server that loads the mod.");
        config.createIntProperty("playerHatsMode", "Player Hats Mode", "Player Hats Mode:\n1 = Free Mode, All players are free to choose what hat to wear.\n2 = Locked mode, all players must wear the same hat, defined in the config.\n3 = Command Giver Mode, what hat you wear is chosen by people who can use commands.\n4 = Hat Hunting Mode, see a mob with a hat, kill it to unlock\n5 = King of the Hat Mode, only one shall wear a hat. The king has to defend their spot or lose the crown!\n6 = Time Active Mode, players unlock more hats the more time they are active on the server.", true, true, 4, 1, 6);
        config.createIntBoolProperty("firstJoinMessage", "First Join Message", "Send a \"First join\" message to the player when they connect to a server for the first time?", true, false, true);
        config.createStringProperty("lockedHat", "Locked Hat", "What hat do players wear in Locked mode (see playerHatsMode 2).\nIf you want different players to wear different hats, use command giver mode.", true, true, "Straw Hat");
        config.createIntProperty("startTime", "Start Time", "For playerHatsMode 6:\nTime required to be active on the server to unlock the first hat.(In ticks)", false, false, 6000, 10, Integer.MAX_VALUE);
        config.createIntProperty("timeIncrement", "Time Increment", "For playerHatsMode 6:\nAmount of extra time required to get the next level hat.\nDefault is 125 (1.25%).\nFor 200% time put 20000", false, false, 125, 0, Integer.MAX_VALUE);
        config.createIntBoolProperty("resetPlayerHatsOnDeath", "Reset Player Hats On Death", "Should player hats be reset when they die?\nOnly in unlockable hats modes", true, false, false);

        if(isClient)
        {
            config.setCurrentCategory("clientOnly", "Client Only", "These settings affect only the client that loads the mod.");
            config.createIntBoolProperty("renderInFirstPerson", "Render In First Person", "Should your hat render in first person?", true, false, false);
            config.createIntBoolProperty("enableInServersWithoutMod", "Enable In Servers Without Mod", "Enable hats in servers without the mod?", true, false, true);
            config.createIntBoolProperty("shouldOtherPlayersHaveHats", "Should Other Players Have Hats", "Do other players have hats? Only when enabled on servers without the mod.", true, false, true);
            config.createIntProperty("randomHat", "Random Hat", "Should each player have a random hat?\nThey randomly change from time to time.\n0 = No\n1 = Yes\n2 = Yes, but not the player!\nOnly when enabled on servers without the mod", true, false, 2, 0, 2);
            String favHat = config.createStringProperty("favouriteHat", "Favourite Hat", "What hat do you want to use on servers without the mod? Only when randomHat = 0", true, false, "Top Hat");
            int clr = config.createColourProperty("favouriteHatColourizer", "Favourite Hat Colouriser", "Do you want to apply a colourizer to your favourite hat?\nIf no, leave as #ffffff\n(Google \"hex color codes\" if you don\'t understand)\nFormat: #<colour index>\nEg: #ffffff for white", true, false, 0xffffff);
            favouriteHatInfo = new HatInfo(favHat.toLowerCase(), clr >> 16 & 255, clr >> 8 & 255, clr & 255);

            config.createKeybindProperty("guiKeyBind", "Open Hats Gui", "Key bind to open the Hat Selection GUI?", Keyboard.KEY_H, false, false, false, false, 0, true);
            config.createStringProperty("personalizeEnabled", "Personalize Categories", "This config is for your GUI personalization.\nPlease don't change this if you don't know what you're doing.", true, false, "1 2 3 4 5 6 7 8 9");
            config.createIntProperty("maxHatRenders", "Max Hat Renders", "Max number of hats to render in one tick", true, false, 300, 0, 5000);
            config.createIntBoolProperty("showContributorHatsInGui", "Show Contributor Hats In Gui", "Show Contributor Hats in the GUI?", true, false, true);

            config.createIntBoolProperty("renderHats", "Render Hats", "Render Hats?", true, true, true);
        }

        config.setCurrentCategory("randoMobOptions", "RandoMob Options", "These settings affect either the client on randoMob settings or Mob Hunting Mode.");
        config.createIntProperty("randomMobHat", "Random Mob Hat", "Do mobs have a random chance of having a hat?\n0 = Disabled (0%)\n100 = All mobs (100%)\n(Client)This follows the randomHat setting, meaning if randomHat is 0, all mobs will wear the favouriteHat setting", true, false, config.getInt("playerHatsMode") != 4 && isClient ? 0 : 10, 0, 100);
        config.createIntProperty("useRandomContributorHats", "Use Random Contributor Hats", "Allow the use of contributor hats when getting a random hat?\n0 - 100%", true, false, 10, 0, 100);

        config.createIntBoolProperty("hatBlaze"     , "Blaze Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatChicken"   , "Chicken Hats"    , "", true, false, true);
        config.createIntBoolProperty("hatCow"       , "Cow Hats"        , "", true, false, true);
        config.createIntBoolProperty("hatCreeper"   , "Creeper Hats"    , "", true, false, true);
        config.createIntBoolProperty("hatEnderman"  , "Enderman Hats"   , "", true, false, true);
        config.createIntBoolProperty("hatGhast"     , "Ghast Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatHorse"     , "Horse Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatOcelot"    , "Ocelot Hats"     , "", true, false, true);
        config.createIntBoolProperty("hatPig"       , "Pig Hats"        , "", true, false, true);
        config.createIntBoolProperty("hatSheep"     , "Sheep Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatSkeleton"  , "Skeleton Hats"   , "", true, false, true);
        config.createIntBoolProperty("hatSlime"     , "Slime Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatSpider"    , "Spider Hats"     , "", true, false, true);
        config.createIntBoolProperty("hatSquid"     , "Squid Hats"      , "", true, false, true);
        config.createIntBoolProperty("hatVillager"  , "Villager Hats"   , "", true, false, true);
        config.createIntBoolProperty("hatWither"    , "Wither Hats"     , "", true, false, true);
        config.createIntBoolProperty("hatWolf"      , "Wolf Hats"       , "", true, false, true);
        config.createIntBoolProperty("hatZombie"    , "Zombie Hats"     , "", true, false, true);

        //		handleConfig();
	}
	
	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.initMod();
		proxy.initTickHandlers();
		
		proxy.initRenderersAndTextures();

        EventHandler eventHandler = new EventHandler();

        FMLCommonHandler.instance().bus().register(eventHandler);
		MinecraftForge.EVENT_BUS.register(eventHandler);
	}
    //TODO redo the read-hats thread to pull off servers instead of being included in the zip, which causes issues with updates sometimes.
	
	@Mod.EventHandler
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

	@Mod.EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event)
	{
        Hats.config.resetSession();
        Hats.config.updateSession("serverHasMod", 1);
        Hats.config.updateSession("currentKing", "");

//		SessionState.serverHasMod = true;
//		SessionState.serverHatMode = playerHatsMode;
//		SessionState.serverHat = lockedHat;

		proxy.initCommands(event.getServer());
	}
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event)
	{
		proxy.tickHandlerServer.mobHats.clear();
		proxy.tickHandlerServer.playerHats.clear();
		proxy.tickHandlerServer.playerActivity.clear();
		proxy.tickHandlerServer.playerTradeRequests.clear();
		proxy.tickHandlerServer.activeTrades.clear();
		proxy.playerWornHats.clear();
	}

    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }

    public static void console(String s)
    {
    	console(s, false);
    }
}
