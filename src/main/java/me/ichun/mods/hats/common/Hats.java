package me.ichun.mods.hats.common;

import me.ichun.mods.hats.client.core.EventHandlerClient;
import me.ichun.mods.hats.common.core.*;
import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import java.io.File;

@Mod(modid = Hats.MOD_ID, name = Hats.MOD_NAME,
        version = Hats.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR +".4.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR +".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class Hats
{
    public static final String MOD_NAME = "Hats";
    public static final String MOD_ID = "hats";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    public static PacketChannel channel;

    public static Config config;

    public static HatInfo favouriteHatInfo = new HatInfo();

    @Instance(MOD_ID)
    public static Hats instance;

    @SidedProxy(clientSide = "me.ichun.mods.hats.client.core.ProxyClient", serverSide = "me.ichun.mods.hats.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/mods/hats");
        if(!HatHandler.hatsFolder.exists())
        {
            HatHandler.hatsFolder.mkdirs();
        }

        config = ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        proxy.preInitMod();

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
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
        eventHandlerServer.mobHats.clear();
        eventHandlerServer.playerHats.clear();
        eventHandlerServer.playerActivity.clear();
        eventHandlerServer.playerTradeRequests.clear();
        eventHandlerServer.activeTrades.clear();
        proxy.playerWornHats.clear();
    }

    public static void console(String s, boolean warning)
    {
        StringBuilder sb = new StringBuilder();
        if(warning)
        {
            LOGGER.warn(sb.append("[").append(VERSION).append("] ").append(s).toString());
        }
        else
        {
            LOGGER.info(sb.append("[").append(VERSION).append("] ").append(s).toString());
        }
    }

    public static void console(String s)
    {
        console(s, false);
    }
}
