package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.client.entity.EntityDummy;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.layer.LayerHat;
import me.ichun.mods.hats.client.model.ModelRendererDragonHook;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketRequestEntityHatDetails;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.client.tracker.entity.EntityTracker;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class EventHandlerClient
{
    public boolean serverHasMod;
    public int connectionAge;
    public int renderCount;
    public ArrayList<Integer> requestedHats = new ArrayList<>();

    public HatsSavedData.PlayerHatData hatsInventory;
    public EntityTracker renderViewEntity; //original entity is in the EntityTracker
    public boolean openedHatsInventory;
    public int openMenuAnimation;
    public boolean lastHideGui;
    public float originalPitch;

    @SubscribeEvent
    public void onClientConnection(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        serverHasMod = false;
        connectionAge = 0;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        serverHasMod = false;
        requestedHats.clear();
        hatsInventory = null;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getInstance();
            if(mc.world != null)
            {
                connectionAge++;

                if(!requestedHats.isEmpty())
                {
                    Hats.channel.sendToServer(new PacketRequestEntityHatDetails(requestedHats.toArray(new Integer[0])));

                    requestedHats.clear(); //we don't clear on world unload. No harm to request, server won't find the entity.
                }
            }

            //GUI animation code
            if(openedHatsInventory)
            {
                if(openMenuAnimation < Hats.configClient.guiAnimationTime)
                {
                    openMenuAnimation++;
                }

                openedHatsInventory = Minecraft.getInstance().currentScreen instanceof WorkspaceHats;
            }
            else if(openMenuAnimation > 0)
            {
                openMenuAnimation--;

                if(openMenuAnimation <= 0)
                {
                    renderViewEntity = null;

                    mc.gameSettings.hideGUI = lastHideGui;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.START)
        {
            renderCount = 0; //reset the hat render count

            if(renderViewEntity != null)
            {
                mc.renderViewEntity = renderViewEntity;

                Entity oriRend = renderViewEntity.parent;

                double progLinear = MathHelper.clamp((openMenuAnimation + (openedHatsInventory ? event.renderTickTime : -event.renderTickTime)) / Hats.configClient.guiAnimationTime, 0F, 1F);
                double progPowHalf = Math.pow(progLinear, 1/2D);
                double progSq = Math.pow(progLinear, 2D);
                double progSin = Math.sin(Math.toRadians(progLinear * 90D));

                oriRend.prevRotationPitch = oriRend.rotationPitch = (float)(originalPitch + (-originalPitch * progSin)); //set the pitch to 0 because we're hacky like that.

                //yaw and pitchCam - change from player's rotations
                float yawCam = (float)(-160F * progSin);
                float pitchCam = (float)(-(oriRend.rotationPitch + oriRend.rotationPitch) * progSin);
                if(mc.gameSettings.getPointOfView().func_243193_b()) //func_243193_b == isReversed
                {
                    yawCam += (float)(180F * progSin);;
                }

                //yaw and pitchFromPlayer - distance calculation from player
                float yawFromPlayer = oriRend.rotationYaw + yawCam;
                if(!mc.gameSettings.getPointOfView().func_243193_b()) //func_243193_b == isReversed
                {
                    yawFromPlayer += 180F;
                }
                float pitchFromPlayer = -(oriRend.rotationPitch + pitchCam);
                Vector3d revLookVec = EntityHelper.getVectorForRotation(pitchFromPlayer, yawFromPlayer);
                double dist = 1.4D * progPowHalf;
                if(!mc.gameSettings.getPointOfView().func_243192_a()) //func_243192_a == isFirstPerson
                {
                    dist -= (4.0D * progSin); //minus the third person dist. 4.0D from ActiveRenderInfo
                }

                RayTraceResult camPoint = EntityHelper.rayTrace(oriRend.getEntityWorld(), oriRend.getEyePosition(event.renderTickTime), oriRend.getEyePosition(event.renderTickTime).add(revLookVec.mul(dist, dist, dist)), oriRend, false, RayTraceContext.BlockMode.COLLIDER, b -> true, RayTraceContext.FluidMode.NONE, e -> true);

                Vector3d upHeadVec = EntityHelper.getVectorForRotation(oriRend.rotationPitch - 90F, oriRend.rotationYaw);
                double upOff = 0.25D;

                Vector3d sideVec = EntityHelper.getVectorForRotation(oriRend.rotationPitch, yawFromPlayer - 90);
                double sideOff = 0.75D * (mc.getMainWindow().getWidth() / (float)mc.getMainWindow().getHeight()) / (16F/9F);

                double offX = (upOff * upHeadVec.getX() * progSin) + (sideOff * sideVec.getX() * progSq);
                double offY = (upOff * upHeadVec.getY() * progSin) + (sideOff * sideVec.getY() * progSq);
                double offZ = (upOff * upHeadVec.getZ() * progSin) + (sideOff * sideVec.getZ() * progSq);

                renderViewEntity.forceSetPosition(camPoint.getHitVec().getX() + offX, (camPoint.getHitVec().getY() - oriRend.getEyeHeight()) + offY, camPoint.getHitVec().getZ() + offZ);
                renderViewEntity.prevRotationYaw = renderViewEntity.rotationYaw = oriRend.rotationYaw + yawCam;
                renderViewEntity.prevRotationPitch = renderViewEntity.rotationPitch = oriRend.rotationPitch + pitchCam;
            }
        }
        else
        {
            if(renderViewEntity != null)
            {
                renderViewEntity.parent.prevRotationPitch = renderViewEntity.parent.rotationPitch = originalPitch;
                mc.renderViewEntity = renderViewEntity.parent;
            }
        }
    }

    public void openHatsMenu() //TODO fallback (including in swimming or sleeping pose)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.world != null && mc.renderViewEntity != null)
        {
            mc.displayGuiScreen(new WorkspaceHats(mc.currentScreen));

            openedHatsInventory = true;
            openMenuAnimation = 0;

            lastHideGui = mc.gameSettings.hideGUI;
            mc.gameSettings.hideGUI = true;

            originalPitch = mc.renderViewEntity.rotationPitch;

            renderViewEntity = EntityDummy.create(mc.world, mc.renderViewEntity);
            renderViewEntity.setParent(mc.renderViewEntity);
            renderViewEntity.setEntityId(ClientEntityTracker.getNextEntId());
            renderViewEntity.setLocationAndAngles(mc.renderViewEntity.getPosX(), mc.renderViewEntity.getPosY(), mc.renderViewEntity.getPosZ() - 2, mc.renderViewEntity.rotationYaw, mc.renderViewEntity.rotationPitch);
            renderViewEntity.prevRotationPitch = renderViewEntity.rotationPitch;
            renderViewEntity.prevRotationYaw = renderViewEntity.rotationYaw;
        }
    }

    public void closeHatsMenu()
    {
        //restore things
        Minecraft mc = Minecraft.getInstance();
    }

    public void requestHatDetails(LivingEntity ent)
    {
        if(serverHasMod)
        {
            requestedHats.add(ent.getEntityId());
        }
    }

    public void updateHatInventory(HatsSavedData.HatPart hatPart)
    {
        if(hatsInventory == null)
        {
            Hats.LOGGER.error("We're updating our hats inventory without an inventory!");
            Thread.dumpStack();
            return;
        }

        for(int i = hatsInventory.hatParts.size() - 1; i >= 0; i--)
        {
            HatsSavedData.HatPart part = hatsInventory.hatParts.get(i);
            if(part.name.equals(hatPart.name)) //same part
            {
                //yeet the old, yoink the new
                hatsInventory.hatParts.remove(i);
                hatsInventory.hatParts.add(i, hatPart);
                break;
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addLayers() //Thanks Googly Eyes?
    {
        //Add our sneaky sneaky layers
        LayerHat layerHat = new LayerHat();

        HashSet<LivingRenderer> addedRenderers = new HashSet<>();

        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        Map<String, PlayerRenderer> skinMap = renderManager.getSkinMap();
        for(Map.Entry<String, PlayerRenderer> e : skinMap.entrySet())
        {
            e.getValue().addLayer(layerHat);
            addedRenderers.add(e.getValue());
        }
        renderManager.renderers.forEach((entityType, entityRenderer) -> {
            if(addedRenderers.contains(entityRenderer))
            {
                return;
            }

            if(entityRenderer instanceof LivingRenderer)
            {
                LivingRenderer renderer = (LivingRenderer)entityRenderer;
                renderer.addLayer(layerHat);
            }
            else if(entityRenderer instanceof EnderDragonRenderer)
            {
                EnderDragonRenderer dragonRenderer = (EnderDragonRenderer)entityRenderer;
                dragonRenderer.model.head.addChild(new ModelRendererDragonHook(dragonRenderer.model));
            }
        });
    }
}
