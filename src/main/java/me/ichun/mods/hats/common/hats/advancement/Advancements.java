package me.ichun.mods.hats.common.hats.advancement;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.ichunutil.common.data.AdvancementGen;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.HashMap;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Hats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Advancements implements Consumer<Consumer<Advancement>>
{
    public static final HashMap<Integer, ResourceLocation> DAMAGE_TO_TEXTURE_MAP = new HashMap<Integer, ResourceLocation>() {{
        //DO NOT USE 0. 0 DEFINES THE NORMAL HAT LAUNCHER
        put(1, new ResourceLocation("hats", "textures/icon/hat.png"));
    }};

    @Override
    public void accept(Consumer<Advancement> consumer)
    {
        Advancement root = Advancement.Builder.builder().withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.root.title"), new TranslationTextComponent("hats.advancement.root.description"), new ResourceLocation("textures/block/basalt_side.png"), FrameType.TASK, false, false, false).withCriterion("killed_something", KilledTrigger.Instance.playerKilledEntity()).register(consumer, "hats:hats/root");
    }

    private static ItemStack getItemWithDamage(int i)
    {
        ItemStack is = new ItemStack(Hats.Items.HAT_LAUNCHER.get());
        is.setDamage(i);
        is.getTag().remove(ItemHatLauncher.STACK_HAT_PART_TAG); //there's already a tag set by setDamage()
        return is;
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        if(event.includeServer()) {
            gen.addProvider(new AdvancementGen(gen, new Advancements()));
        }
    }

    public static class CriteriaTriggers
    {

        private static <T extends ICriterionTrigger<?>> T register(T criterion) {
            return net.minecraft.advancements.CriteriaTriggers.register(criterion);
        }

        public static void init(){} //do nothing. This is just so we can classload and register all the Triggers.
    }
}
