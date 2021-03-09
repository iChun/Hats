package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.layer.LayerHat;
import me.ichun.mods.hats.client.model.ModelRendererDragonHook;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketRequestEntityHatDetails;
import me.ichun.mods.hats.common.world.HatsSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.LivingEntity;
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
            if(Minecraft.getInstance().world != null)
            {
                connectionAge++;

                if(!requestedHats.isEmpty())
                {
                    Hats.channel.sendToServer(new PacketRequestEntityHatDetails(requestedHats.toArray(new Integer[0])));

                    requestedHats.clear(); //we don't clear on world unload. No harm to request, server won't find the entity.
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            renderCount = 0;
        }
    }

    public void openHatsMenu()
    {
        Minecraft.getInstance().displayGuiScreen(new WorkspaceHats(Minecraft.getInstance().currentScreen));
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
