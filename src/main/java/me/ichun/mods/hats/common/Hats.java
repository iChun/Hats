package me.ichun.mods.hats.common;

import me.ichun.mods.hats.client.config.ConfigClient;
import me.ichun.mods.hats.client.core.EventHandlerClient;
import me.ichun.mods.hats.common.config.ConfigCommon;
import me.ichun.mods.hats.common.config.ConfigServer;
import me.ichun.mods.hats.common.core.EventHandlerServer;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.thread.ThreadReadHats;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.network.PacketChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hats.MOD_ID)
public class Hats
{
    public static final String MOD_NAME = "Hats";
    public static final String MOD_ID = "hats";
    public static final String PROTOCOL = "1"; //Network protocol

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigCommon configCommon;
    public static ConfigClient configClient;
    public static ConfigServer configServer;

    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static PacketChannel channel;

    private static ThreadReadHats threadReadHats;

    public Hats()
    {
        if(!HatResourceHandler.init())
        {
            return;
        }
        configCommon = new ConfigCommon().init();
        configServer = new ConfigServer().init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::finishLoading);

        MinecraftForge.EVENT_BUS.register(eventHandlerServer = new EventHandlerServer());

        //no packets yet.
        //TODO make it server optional.
//        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL);


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();

            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());

            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
        });

        threadReadHats = new ThreadReadHats();
        threadReadHats.start();
    }

    private void finishLoading(FMLLoadCompleteEvent event)
    {
        //Thanks ichttt
        if(threadReadHats.latch.getCount() > 0)
        {
            Hats.LOGGER.info("Waiting for file reader thread to finish");
            try
            {
                threadReadHats.latch.await();
            }
            catch(InterruptedException e)
            {
                Hats.LOGGER.error("Got interrupted while waiting for FileReaderThread to finish");
                e.printStackTrace();
            }
        }
        threadReadHats = null; //enjoy this thread, GC.


        HeadHandler.init(); //initialise our head trackers

        if(FMLEnvironment.dist.isClient())
        {
            eventHandlerClient.addLayers(); //Let's add the layers
        }
    }
}
