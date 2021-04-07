package me.ichun.mods.hats.common.hats.advancement;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.hats.advancement.criterion.CodeDefinedTrigger;
import me.ichun.mods.hats.common.hats.advancement.criterion.ValueAtOrAboveTrigger;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.data.AdvancementGen;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.criterion.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        //TODO rewards
        Advancement root = Advancement.Builder.builder().withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.root.title"), new TranslationTextComponent("hats.advancement.root.description"), new ResourceLocation("textures/block/basalt_side.png"), FrameType.TASK, false, false, false).withCriterion("killed_something", KilledTrigger.Instance.playerKilledEntity()).register(consumer, "hats:hats/root");
        Advancement hatsUnlocked1 = Advancement.Builder.builder().withParent(root).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatsUnlocked1.title"), new TranslationTextComponent("hats.advancement.hatsUnlocked1.description"), null, FrameType.TASK, false, false, false).withCriterion("hats_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HATS_UNLOCKED.getId(), 1)).register(consumer, "hats:hats/hats_unlocked_1");
        Advancement hatsUnlocked10 = Advancement.Builder.builder().withParent(hatsUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatsUnlocked10.title"), new TranslationTextComponent("hats.advancement.hatsUnlocked10.description"), null, FrameType.TASK, false, false, false).withCriterion("hats_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HATS_UNLOCKED.getId(), 10)).register(consumer, "hats:hats/hats_unlocked_10");
        Advancement hatsUnlocked25 = Advancement.Builder.builder().withParent(hatsUnlocked10).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatsUnlocked25.title"), new TranslationTextComponent("hats.advancement.hatsUnlocked25.description"), null, FrameType.TASK, false, false, false).withCriterion("hats_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HATS_UNLOCKED.getId(), 25)).register(consumer, "hats:hats/hats_unlocked_25");
        Advancement hatsUnlocked50 = Advancement.Builder.builder().withParent(hatsUnlocked25).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatsUnlocked50.title"), new TranslationTextComponent("hats.advancement.hatsUnlocked50.description"), null, FrameType.TASK, false, false, false).withCriterion("hats_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HATS_UNLOCKED.getId(), 50)).register(consumer, "hats:hats/hats_unlocked_50");
        Advancement variants50 = Advancement.Builder.builder().withParent(hatsUnlocked50).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.variants50.title"), new TranslationTextComponent("hats.advancement.variants50.description"), null, FrameType.TASK, false, false, false).withCriterion("variants", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.VARIANTS.getId(), 50)).register(consumer, "hats:hats/variants_50");
        Advancement variants75 = Advancement.Builder.builder().withParent(variants50).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.variants75.title"), new TranslationTextComponent("hats.advancement.variants75.description"), null, FrameType.TASK, false, false, false).withCriterion("variants", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.VARIANTS.getId(), 75)).register(consumer, "hats:hats/variants_75");
        Advancement variants100 = Advancement.Builder.builder().withParent(variants75).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.variants100.title"), new TranslationTextComponent("hats.advancement.variants100.description"), null, FrameType.TASK, false, false, false).withCriterion("variants", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.VARIANTS.getId(), 100)).register(consumer, "hats:hats/variants_100");
        Advancement accUnlocked1 = Advancement.Builder.builder().withParent(hatsUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.accUnlocked1.title"), new TranslationTextComponent("hats.advancement.accUnlocked1.description"), null, FrameType.TASK, false, false, false).withCriterion("acc_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.ACC_UNLOCKED.getId(), 1)).register(consumer, "hats:hats/acc_unlocked_1");
        Advancement accUnlocked10 = Advancement.Builder.builder().withParent(accUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.accUnlocked10.title"), new TranslationTextComponent("hats.advancement.accUnlocked10.description"), null, FrameType.TASK, false, false, false).withCriterion("acc_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.ACC_UNLOCKED.getId(), 10)).register(consumer, "hats:hats/acc_unlocked_10");
        Advancement accUnlocked25 = Advancement.Builder.builder().withParent(accUnlocked10).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.accUnlocked25.title"), new TranslationTextComponent("hats.advancement.accUnlocked25.description"), null, FrameType.TASK, false, false, false).withCriterion("acc_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.ACC_UNLOCKED.getId(), 25)).register(consumer, "hats:hats/acc_unlocked_25");
        Advancement accUnlocked50 = Advancement.Builder.builder().withParent(accUnlocked25).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.accUnlocked50.title"), new TranslationTextComponent("hats.advancement.accUnlocked50.description"), null, FrameType.TASK, false, false, false).withCriterion("acc_unlocked", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.ACC_UNLOCKED.getId(), 50)).register(consumer, "hats:hats/acc_unlocked_50");
        Advancement hatCount10 = Advancement.Builder.builder().withParent(hatsUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatCount10.title"), new TranslationTextComponent("hats.advancement.hatCount10.description"), null, FrameType.TASK, false, false, false).withCriterion("hat_count", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HAT_COUNT.getId(), 10)).register(consumer, "hats:hats/hat_count_10");
        Advancement hatCount25 = Advancement.Builder.builder().withParent(hatCount10).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatCount25.title"), new TranslationTextComponent("hats.advancement.hatCount25.description"), null, FrameType.TASK, false, false, false).withCriterion("hat_count", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HAT_COUNT.getId(), 25)).register(consumer, "hats:hats/hat_count_25");
        Advancement hatCount50 = Advancement.Builder.builder().withParent(hatCount25).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatCount50.title"), new TranslationTextComponent("hats.advancement.hatCount50.description"), null, FrameType.TASK, false, false, false).withCriterion("hat_count", ValueAtOrAboveTrigger.Instance.count(CriteriaTriggers.HAT_COUNT.getId(), 50)).register(consumer, "hats:hats/hat_count_50");

        //TODO the recipe reward
        Advancement killBossWithHat = Advancement.Builder.builder().withParent(hatsUnlocked10).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.killBossWithHat.title"), new TranslationTextComponent("hats.advancement.killBossWithHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.KILL_BOSS_WITH_HAT.getId())).register(consumer, "hats:hats/kill_boss_with_hat");
        Advancement craftHatLauncher = Advancement.Builder.builder().withParent(killBossWithHat).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.craftHatLauncher.title"), new TranslationTextComponent("hats.advancement.craftHatLauncher.description"), null, FrameType.TASK, false, false, false).withCriterion("get_launcher", InventoryChangeTrigger.Instance.forItems(Hats.Items.HAT_LAUNCHER.get())).register(consumer, "hats:hats/craft_hat_launcher");
        Advancement enchantHatLauncher = Advancement.Builder.builder().withParent(craftHatLauncher).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.enchantHatLauncher.title"), new TranslationTextComponent("hats.advancement.enchantHatLauncher.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", new EnchantedItemTrigger.Instance(EntityPredicate.AndPredicate.ANY_AND, ItemPredicate.Builder.create().item(Hats.Items.HAT_LAUNCHER.get()).build(), MinMaxBounds.IntBound.UNBOUNDED)).register(consumer, "hats:hats/enchant_hat_launcher");
        Advancement changeMobHat = Advancement.Builder.builder().withParent(craftHatLauncher).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.changeMobHat.title"), new TranslationTextComponent("hats.advancement.changeMobHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.CHANGE_MOB_HAT.getId())).register(consumer, "hats:hats/change_mob_hat");
        Advancement rogueHat = Advancement.Builder.builder().withParent(changeMobHat).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.rogueHat.title"), new TranslationTextComponent("hats.advancement.rogueHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.ROGUE_HAT.getId())).register(consumer, "hats:hats/rogue_hat");
        Advancement wearHat = Advancement.Builder.builder().withParent(hatsUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.wearHat.title"), new TranslationTextComponent("hats.advancement.wearHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.WEAR_HAT.getId())).register(consumer, "hats:hats/wear_hat");
        Advancement wearHatWithAccessory = Advancement.Builder.builder().withParent(wearHat).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.wearHatWithAccessory.title"), new TranslationTextComponent("hats.advancement.wearHatWithAccessory.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.WEAR_HAT_WITH_ACCESSORY.getId())).register(consumer, "hats:hats/wear_hat_with_accessory");
        Advancement colouriseHat = Advancement.Builder.builder().withParent(wearHat).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.colouriseHat.title"), new TranslationTextComponent("hats.advancement.colouriseHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.COLOURISE_HAT.getId())).register(consumer, "hats:hats/colourise_hat");
        Advancement hatWithThreeOrMoreAccessories = Advancement.Builder.builder().withParent(accUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.hatWithThreeOrMoreAccessories.title"), new TranslationTextComponent("hats.advancement.hatWithThreeOrMoreAccessories.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.HAT_WITH_THREE_OR_MORE_ACCESSORIES.getId())).register(consumer, "hats:hats/hat_with_three_or_more_accessories");
        Advancement accessoryInAccessory = Advancement.Builder.builder().withParent(accUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.accessoryInAccessory.title"), new TranslationTextComponent("hats.advancement.accessoryInAccessory.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.ACCESSORY_IN_ACCESSORY.getId())).register(consumer, "hats:hats/accessory_in_accessory");
        Advancement nonVanillaHat = Advancement.Builder.builder().withParent(hatsUnlocked1).withDisplay(getItemWithDamage(1), new TranslationTextComponent("hats.advancement.nonVanillaHat.title"), new TranslationTextComponent("hats.advancement.nonVanillaHat.description"), null, FrameType.TASK, false, false, false).withCriterion("code_trigger", CodeDefinedTrigger.Instance.create(CriteriaTriggers.NON_VANILLA_HAT.getId())).register(consumer, "hats:hats/non_vanilla_hat");
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

    public static void triggerHatCount(ServerPlayerEntity player, int count)
    {
        CriteriaTriggers.HAT_COUNT.test(player, count);
    }

    public static void checkHatsAndAccessoriesUnlocked(ServerPlayerEntity player)
    {
        if(HatHandler.useInventory(player))
        {
            ArrayList<HatsSavedData.HatPart> playerInventory = HatHandler.getPlayerInventory(player);
            int hatCount = playerInventory.size();
            int accessoriesCount = 0;
            for(HatsSavedData.HatPart hatPart : playerInventory)
            {
                accessoriesCount += hatPart.accessoriesUnlocked();
            }

            CriteriaTriggers.HATS_UNLOCKED.test(player, hatCount);
            CriteriaTriggers.ACC_UNLOCKED.test(player, accessoriesCount);

            List<HatsSavedData.HatPart> hatPartSource = new ArrayList<>(HatResourceHandler.HAT_PARTS);
            hatPartSource = hatPartSource.stream().filter(hatPart -> {
                HatInfo info = HatResourceHandler.getInfo(hatPart);
                return info == null || info.contributorUUID == null;
            }).collect(Collectors.toList());

            int variantPercent = (int)Math.floor(hatCount / (float)hatPartSource.size() * 100);
            CriteriaTriggers.VARIANTS.test(player, variantPercent);
        }
    }

    public static class CriteriaTriggers
    {
        public static final ValueAtOrAboveTrigger HATS_UNLOCKED = register(new ValueAtOrAboveTrigger(new ResourceLocation("hats", "hats_unlocked")));
        public static final ValueAtOrAboveTrigger ACC_UNLOCKED = register(new ValueAtOrAboveTrigger(new ResourceLocation("hats", "acc_unlocked")));
        public static final ValueAtOrAboveTrigger VARIANTS = register(new ValueAtOrAboveTrigger(new ResourceLocation("hats", "variants")));
        public static final ValueAtOrAboveTrigger HAT_COUNT = register(new ValueAtOrAboveTrigger(new ResourceLocation("hats", "hat_count")));
        public static final CodeDefinedTrigger KILL_BOSS_WITH_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "kill_boss_with_hat")));
        public static final CodeDefinedTrigger CHANGE_MOB_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "change_mob_hat")));
        public static final CodeDefinedTrigger ROGUE_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "rogue_hat")));
        public static final CodeDefinedTrigger WEAR_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "wear_hat")));
        public static final CodeDefinedTrigger WEAR_HAT_WITH_ACCESSORY = register(new CodeDefinedTrigger(new ResourceLocation("hats", "wear_hat_with_accessory")));
        public static final CodeDefinedTrigger COLOURISE_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "colourise_hat")));
        public static final CodeDefinedTrigger HAT_WITH_THREE_OR_MORE_ACCESSORIES = register(new CodeDefinedTrigger(new ResourceLocation("hats", "hat_with_three_or_more_accessories")));
        public static final CodeDefinedTrigger ACCESSORY_IN_ACCESSORY = register(new CodeDefinedTrigger(new ResourceLocation("hats", "accessory_in_accessory")));
        public static final CodeDefinedTrigger NON_VANILLA_HAT = register(new CodeDefinedTrigger(new ResourceLocation("hats", "non_vanilla_hat")));

        private static <T extends ICriterionTrigger<?>> T register(T criterion) {
            return net.minecraft.advancements.CriteriaTriggers.register(criterion);
        }

        public static void init(){} //do nothing. This is just so we can classload and register all the Triggers.
    }
}
