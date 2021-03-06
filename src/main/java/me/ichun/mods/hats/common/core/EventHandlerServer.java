package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.common.hats.HatHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if(!event.getWorld().isRemote && event.getEntity() instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity)event.getEntity();
            if(!HatHandler.hasBeenRandomlyAllocated(living))
            {
                if(HatHandler.canWearHat(living) && living.getRNG().nextDouble() < HatHandler.getHatChance(living))
                {
                    HatHandler.assignHat(living);
                }
                else
                {
                    HatHandler.denyHat(living);
                }
            }
        }
    }
}
