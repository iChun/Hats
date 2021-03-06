package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.command.CommandHats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.advancement.Advancements;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketPing;
import me.ichun.mods.hats.common.packet.PacketRehatify;
import me.ichun.mods.hats.common.packet.PacketUpdateHats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.api.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event)
    {
        Entity entity = event.getObject();
        if(entity instanceof LivingEntity)
        {
            event.addCapability(HatsSavedData.HatPart.CAPABILITY_IDENTIFIER, new HatsSavedData.HatPart.CapProvider(new HatsSavedData.HatPart()));
        }
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if(!event.getWorld().isRemote && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof PlayerEntity)) //don't allocate player hats.
        {
            LivingEntity living = (LivingEntity)event.getEntity();
            if(HatHandler.hasBeenRandomlyAllocated(living))
            {
                HatHandler.checkValidity(living);
            }

            if(!HatHandler.hasBeenRandomlyAllocated(living))
            {
                if(HatHandler.canWearHat(living) && living.getRNG().nextDouble() < HatHandler.getHatChance(living))
                {
                    HatHandler.assignHat(living);
                }
                else
                {
                    HatHandler.assignNoHat(living);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.getEntityLiving().getEntityWorld().isRemote && !event.getEntityLiving().removed && !(event.getEntityLiving() instanceof PlayerEntity))
        {
            if(event.getSource().getTrueSource() instanceof ServerPlayerEntity && !(event.getSource().getTrueSource() instanceof FakePlayer))
            {
                HatsSavedData.HatPart hatPart = HatHandler.getHatPart(event.getEntityLiving());
                if(hatPart.isAHat())
                {
                    HatHandler.addHat((ServerPlayerEntity)event.getSource().getTrueSource(), hatPart);

                    HeadInfo info = HeadHandler.getHelper(event.getEntityLiving().getClass());
                    if(info != null && info.isBoss)
                    {
                        Advancements.CriteriaTriggers.KILL_BOSS_WITH_HAT.trigger((ServerPlayerEntity)event.getSource().getTrueSource());
                    }

                    if(!event.getEntityLiving().getClass().getName().contains("minecraft"))
                    {
                        Advancements.CriteriaTriggers.NON_VANILLA_HAT.trigger((ServerPlayerEntity)event.getSource().getTrueSource());
                    }
                }
            }
            else if(event.getSource().getTrueSource() instanceof LivingEntity && event.getSource().getTrueSource().isAlive() && Hats.configServer.mobHatTakeover)
            {
                LivingEntity killer = (LivingEntity)event.getSource().getTrueSource();
                HatsSavedData.HatPart killerHat = HatHandler.getHatPart(killer);
                if(HatHandler.canWearHat(killer) && !killerHat.isAHat())
                {
                    HatsSavedData.HatPart hatPart = HatHandler.getHatPart(event.getEntityLiving());
                    if(hatPart.isAHat())
                    {
                        killerHat.read(hatPart.write(new CompoundNBT()));

                        HashMap<Integer, HatsSavedData.HatPart> entIdToHat = new HashMap<>();
                        entIdToHat.put(killer.getEntityId(), hatPart);

                        Hats.channel.sendTo(new PacketRehatify(killer.getEntityId()), PacketDistributor.TRACKING_ENTITY.with(() -> killer));
                        Hats.channel.sendTo(new PacketEntityHatDetails(entIdToHat), PacketDistributor.TRACKING_ENTITY.with(() -> killer));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Hats.channel.sendTo(new PacketPing(), (ServerPlayerEntity)event.getPlayer());
        Hats.channel.sendTo(new PacketUpdateHats(HatHandler.getPlayerHatsNBT(event.getPlayer()), true), (ServerPlayerEntity)event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        HatHandler.getHatPart(event.getPlayer()).read(HatHandler.getHatPart(event.getOriginal()).write(new CompoundNBT()));
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(!event.getWorld().isRemote() && ((ServerWorld)event.getWorld()).getDimensionKey().equals(World.OVERWORLD))
        {
            HatHandler.setSaveData(((ServerWorld)event.getWorld()).getSavedData().getOrCreate(HatsSavedData::new, HatsSavedData.ID));
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        CommandHats.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStopped(FMLServerStoppedEvent event)
    {
        HatHandler.setSaveData(null);
    }
}
