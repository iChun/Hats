package me.ichun.mods.hats.common;

import me.ichun.mods.hats.client.render.RenderHat;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.ichun.mods.hats.common.core.*;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;

import java.io.File;

@Mod(modid = "hats", name = "Hats",
        version = Hats.version,
        dependencies = "required-after:ichunutil@[7.0.0,)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_OF_MC +".0.0)"
)
public class Hats
{
    public static final String version = iChunUtil.VERSION_OF_MC +".0.0";

    private static Logger logger = LogManager.getLogger("Hats");

    public static PacketChannel channel;

    public static Config config;

    public static HatInfo favouriteHatInfo = new HatInfo();

    @Instance("hats")
    public static Hats instance;

    @SidedProxy(clientSide = "me.ichun.mods.hats.client.core.ClientProxy", serverSide = "me.ichun.mods.hats.common.core.CommonProxy", modId = "hats")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/mods/hats");

        if(!HatHandler.hatsFolder.exists())
        {
            HatHandler.hatsFolder.mkdirs();
        }

        config = ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo("Hats", iChunUtil.VERSION_OF_MC, version, false));

    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void preInit(FMLPreInitializationEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityHat.class, RenderHat::new);
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.initMod();
        proxy.initTickHandlers();

        EventHandler eventHandler = new EventHandler();

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
