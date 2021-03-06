package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.common.hats.HatHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event)
    {
        HatHandler.serverStarting();
    }
}
