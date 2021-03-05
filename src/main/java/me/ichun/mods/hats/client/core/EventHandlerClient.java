package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.client.layer.LayerHat;
import me.ichun.mods.hats.client.model.ModelRendererDragonHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;

import java.util.HashSet;
import java.util.Map;

public class EventHandlerClient //TODO bosses have an increased rarity hat, fix skyrim hat missing pixel
{

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
