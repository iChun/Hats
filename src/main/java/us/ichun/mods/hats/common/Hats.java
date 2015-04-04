package us.ichun.mods.hats.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.ichun.mods.hats.common.core.*;
import us.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.iChunUtil;

import java.io.File;

@Mod(modid = "Hats", name = "Hats",
        version = Hats.version,
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".3.0,)",
        acceptableRemoteVersions = "[" + iChunUtil.versionMC +".0.0," + iChunUtil.versionMC + ".1.0)"
)
public class Hats
{
    public static final String version = iChunUtil.versionMC +".0.1";

    private static Logger logger = LogManager.getLogger("Hats");

    public static PacketChannel channel;

    public static Config config;

    public static HatInfo favouriteHatInfo = new HatInfo();

    @Instance("Hats")
    public static Hats instance;

    @SidedProxy(clientSide = "us.ichun.mods.hats.client.core.ClientProxy", serverSide = "us.ichun.mods.hats.common.core.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/hats");

        if(!HatHandler.hatsFolder.exists())
        {
            HatHandler.hatsFolder.mkdirs();
        }

        config = (Config)ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Hats", iChunUtil.versionOfMC, version, false));
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

    @Mod.EventHandler
    public void serverStarting(FMLServerAboutToStartEvent event)
    {
        SessionState.serverHasMod = 1;
        SessionState.currentKing = "";

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
