package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.client.entity.EntityDummy;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.layer.LayerHat;
import me.ichun.mods.hats.client.model.ModelRendererDragonHook;
import me.ichun.mods.hats.client.toast.Toast;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.hats.common.packet.PacketRequestEntityHatDetails;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.client.tracker.entity.EntityTracker;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
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
    public float guiX, guiY, guiYaw, guiPitch, guiDist;
    public boolean forceRenderWhenInvisible;

    private boolean shownSyncToast;

    @SubscribeEvent
    public void onClientConnection(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        serverHasMod = false;
        connectionAge = 0;

        resetSyncToast();
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

                openedHatsInventory = mc.currentScreen instanceof WorkspaceHats;
            }
            else if(openMenuAnimation > 0)
            {
                openMenuAnimation--;

                if(mc.currentScreen instanceof IngameMenuScreen) //we double escaped. RESET ASAP
                {
                    openMenuAnimation = 0;
                }

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
                float yawCam = (float)((-160F + guiYaw) * progSin);
                float pitchCam = (float)((-(oriRend.rotationPitch + oriRend.rotationPitch) + guiPitch) * progSin);
                if(mc.gameSettings.getPointOfView().func_243193_b()) //func_243193_b == isReversed
                {
                    yawCam += (float)(180F * progSin);
                }

                //yaw and pitchFromPlayer - distance calculation from player
                float yawFromPlayer = oriRend.rotationYaw + yawCam;
                if(!mc.gameSettings.getPointOfView().func_243193_b()) //func_243193_b == isReversed
                {
                    yawFromPlayer += 180F;
                }
                float pitchFromPlayer = -(oriRend.rotationPitch + pitchCam);
                Vector3d revLookVec = EntityHelper.getVectorForRotation(pitchFromPlayer, yawFromPlayer);
                double dist = (1.4D + guiDist) * progPowHalf;
                if(!mc.gameSettings.getPointOfView().func_243192_a()) //func_243192_a == isFirstPerson
                {
                    dist -= (4.0D * progSin); //minus the third person dist. 4.0D from ActiveRenderInfo
                }

                double upOff = (0.25D + guiY);

                Vector3d sideVec = EntityHelper.getVectorForRotation(oriRend.rotationPitch, yawFromPlayer - 90);
                double sideOff = ((((1.4D + guiDist) * progPowHalf) * 0.57D) + guiX) * (mc.getMainWindow().getWidth() / (float)mc.getMainWindow().getHeight()) / (16F/9F);

                double offX = (sideOff * sideVec.getX() * progSq);
                double offY = (upOff * progSin) + (sideOff * sideVec.getY() * progSq);
                double offZ = (sideOff * sideVec.getZ() * progSq);

                Vector3d rendEyePos = oriRend.getEyePosition(event.renderTickTime);
                RayTraceResult camPoint = EntityHelper.rayTrace(oriRend.getEntityWorld(), rendEyePos, rendEyePos.add(revLookVec.mul(dist, dist, dist)).add(offX, offY, offZ), oriRend, false, RayTraceContext.BlockMode.COLLIDER, b -> true, RayTraceContext.FluidMode.NONE, e -> true);

                if(camPoint.getType() != RayTraceResult.Type.MISS)
                {
                    Vector3d difference = camPoint.getHitVec().subtract(rendEyePos).normalize().mul(0.2D, 0.2D, 0.2D);

                    renderViewEntity.forceSetPosition(camPoint.getHitVec().getX() - difference.getX(), (camPoint.getHitVec().getY() - oriRend.getEyeHeight()) - difference.getY(), camPoint.getHitVec().getZ() - difference.getZ());
                }
                else
                {
                    renderViewEntity.forceSetPosition(camPoint.getHitVec().getX(), (camPoint.getHitVec().getY() - oriRend.getEyeHeight()), camPoint.getHitVec().getZ());
                }

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

    public void openHatsMenu()
    {
        if(Hats.configServer.enabledGuiStyle <= 0) //disable opening.
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.world != null && mc.player != null)
        {
            if(!mc.player.isAlive() || mc.player.isSleeping())
            {
                return; // do not open.
            }

            boolean fallback = Hats.configClient.forceGuiFallback
                    || Hats.configServer.enabledGuiStyle == 1
                    || !(mc.player.getPose() == Pose.STANDING || mc.player.getPose() == Pose.CROUCHING)
                    || mc.player != mc.renderViewEntity
                    || mc.player.getBrightness() <= 0.15F //TODO does this cause issues in the END dimension??
                    || mc.player.isInvisible()
                    ;
            if(!fallback)
            {
                float yawFromPlayer = mc.renderViewEntity.rotationYaw - 160F;
                if(!mc.gameSettings.getPointOfView().func_243193_b()) //func_243193_b == isReversed
                {
                    yawFromPlayer += 180F;
                }
                float pitchFromPlayer = 0F;
                Vector3d revLookVec = EntityHelper.getVectorForRotation(pitchFromPlayer, yawFromPlayer);
                double dist = 1.4D;
                if(!mc.gameSettings.getPointOfView().func_243192_a()) //func_243192_a == isFirstPerson
                {
                    dist -= (4.0D); //minus the third person dist. 4.0D from ActiveRenderInfo
                }

                Vector3d upHeadVec = EntityHelper.getVectorForRotation(mc.renderViewEntity.rotationPitch - 90F, mc.renderViewEntity.rotationYaw);
                double upOff = 0.25D;

                Vector3d sideVec = EntityHelper.getVectorForRotation(mc.renderViewEntity.rotationPitch, yawFromPlayer - 90);
                double sideOff = 0.75D * (mc.getMainWindow().getWidth() / (float)mc.getMainWindow().getHeight()) / (16F / 9F);

                double offX = (upOff * upHeadVec.getX()) + (sideOff * sideVec.getX());
                double offY = (upOff * upHeadVec.getY()) + (sideOff * sideVec.getY());
                double offZ = (upOff * upHeadVec.getZ()) + (sideOff * sideVec.getZ());

                RayTraceResult camPoint = EntityHelper.rayTrace(mc.renderViewEntity.getEntityWorld(), mc.renderViewEntity.getEyePosition(1F), mc.renderViewEntity.getEyePosition(1F).add(revLookVec.mul(dist, dist, dist)).add(offX, offY, offZ), mc.renderViewEntity, false, RayTraceContext.BlockMode.COLLIDER, b -> true, RayTraceContext.FluidMode.NONE, e -> true);

                fallback = !camPoint.getType().equals(RayTraceResult.Type.MISS);
            }

            mc.displayGuiScreen(new WorkspaceHats(mc.currentScreen, fallback, mc.player, null));

            if(!fallback)
            {
                guiX = guiY = guiYaw = guiPitch = guiDist = 0;

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

        boolean added = false;
        for(int i = hatsInventory.hatParts.size() - 1; i >= 0; i--)
        {
            HatsSavedData.HatPart part = hatsInventory.hatParts.get(i);
            if(part.name.equals(hatPart.name)) //same part
            {
                //yeet the old, yoink the new
                hatsInventory.hatParts.remove(i);
                hatsInventory.hatParts.add(i, hatPart);
                added = true;
                break;
            }
        }
        if(!added)
        {
            hatsInventory.hatParts.add(hatPart);
        }

        Collections.sort(hatsInventory.hatParts);
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

    public void nudgeHand(PlayerEntity player)
    {
        Minecraft mc = Minecraft.getInstance();
        if(player == mc.player)
        {
            ItemStack is = DualHandedItem.getUsableDualHandedItem(player);
            if(is.getItem() instanceof ItemHatLauncher)
            {
                mc.player.renderArmPitch -= 200F;
                if(!mc.gameSettings.getPointOfView().func_243192_a())
                {
                    mc.player.swing(mc.player.getPrimaryHand() == DualHandedItem.getHandSide(mc.player, is) ? Hand.MAIN_HAND : Hand.OFF_HAND, true);
                }
            }
        }
    }

    public void showSyncToast()
    {
        if(!shownSyncToast)
        {
            shownSyncToast = true;

            Minecraft.getInstance().getToastGui().add(new Toast(new TranslationTextComponent("hats.toast.sync.title"), new TranslationTextComponent("hats.toast.sync.subtitle"), 2));
        }
    }

    public void resetSyncToast()
    {
        shownSyncToast = false;
    }
}
