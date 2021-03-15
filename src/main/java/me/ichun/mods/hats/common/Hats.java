package me.ichun.mods.hats.common;

import me.ichun.mods.hats.client.config.ConfigClient;
import me.ichun.mods.hats.client.core.EventHandlerClient;
import me.ichun.mods.hats.client.entity.EntityDummy;
import me.ichun.mods.hats.client.module.tabula.HatsTabulaPlugin;
import me.ichun.mods.hats.common.config.ConfigCommon;
import me.ichun.mods.hats.common.config.ConfigServer;
import me.ichun.mods.hats.common.core.EventHandlerServer;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.*;
import me.ichun.mods.hats.common.thread.ThreadReadHats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.network.PacketChannel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

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

        bus.addListener(this::onCommonSetup);
        bus.addListener(this::finishLoading);

        MinecraftForge.EVENT_BUS.register(eventHandlerServer = new EventHandlerServer());

        //TODO make it server optional.
        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL,
                PacketPing.class,
                PacketRequestEntityHatDetails.class,
                PacketEntityHatDetails.class,
                PacketRequestHeadInfos.class,
                PacketHeadInfos.class,
                PacketNewHatPart.class,
                PacketUpdateHats.class,
                PacketHatCustomisation.class
        );


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();

            EntityDummy.init(bus); //we use this for the client-based entity for GUI rendering

            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());

            bus.addListener(this::onClientSetup);
            bus.addListener(this::enqueueIMC);

            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
        });

        threadReadHats = new ThreadReadHats();
        threadReadHats.start();
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        //We don't need a proper IStorage / factory: https://github.com/MinecraftForge/MinecraftForge/issues/7622
        CapabilityManager.INSTANCE.register(HatsSavedData.HatPart.class, new Capability.IStorage<HatsSavedData.HatPart>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<HatsSavedData.HatPart> capability, HatsSavedData.HatPart instance, Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<HatsSavedData.HatPart> capability, HatsSavedData.HatPart instance, Direction side, INBT nbt)
            {

            }
        }, () -> null);
    }

    @OnlyIn(Dist.CLIENT)
    private void onClientSetup(FMLClientSetupEvent event)
    {
        new KeyBind(new KeyBinding("hats.key.hatsMenu", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_H), "key.categories.ui"), keyBind -> eventHandlerClient.openHatsMenu(), null);
    }

    @OnlyIn(Dist.CLIENT)
    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        InterModComms.sendTo("tabula", "plugin", HatsTabulaPlugin::new);
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
