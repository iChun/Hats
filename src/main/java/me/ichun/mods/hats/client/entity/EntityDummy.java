package me.ichun.mods.hats.client.entity;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.client.tracker.entity.EntityTracker;
import me.ichun.mods.ichunutil.client.tracker.render.RenderTracker;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
//Most of this "stolen" from iChunUtil ClientEntityTracker
public class EntityDummy extends EntityTracker// iChunUtil's client entity tracker
{
    public EntityDummy(EntityType<?> entityTypeIn, World worldIn, @Nonnull Entity parent)
    {
        super(entityTypeIn, worldIn);
        setParent(parent);
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn)
    {
        //noinspection ConstantConditions
        if(parent == null) //we're still constructing, parent is still null.
        {
            return Minecraft.getInstance().renderViewEntity != null ? Minecraft.getInstance().renderViewEntity.getEyeHeight(poseIn) : Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getEyeHeight(poseIn) : super.getEyeHeight(poseIn, sizeIn);
        }
        else
        {
            return parent.getEyeHeight(poseIn);
        }
    }


    public static EntityDummy create(World world, Entity parent)
    {
        return new EntityDummy(EntityTypes.DUMMY, world, parent);
    }

    public static void init(IEventBus bus)
    {
        bus.addGenericListener(EntityType.class, EntityTypes::onEntityTypeRegistry);
        bus.addListener(EntityDummy::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTypes.DUMMY, new RenderTracker.RenderFactory());
    }

    private static class EntityTypes
    {
        public static EntityType<EntityTracker> DUMMY;
        public static void onEntityTypeRegistry(final RegistryEvent.Register<EntityType<?>> entityTypeRegistryEvent) //we're doing it this way because it's a client-side entity and we don't want to sync registry values
        {
            DUMMY = EntityType.Builder.create(EntityTracker::new, EntityClassification.MISC)
                    .size(0.1F, 0.1F)
                    .disableSerialization()
                    .disableSummoning()
                    .immuneToFire()
                    .build("an entity from " + Hats.MOD_NAME + ". Ignore this.");
            DUMMY.setRegistryName(Hats.MOD_ID, "client_entity_dummy");
        }
    }
}
