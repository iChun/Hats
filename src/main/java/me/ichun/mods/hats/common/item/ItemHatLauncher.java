package me.ichun.mods.hats.common.item;

import me.ichun.mods.hats.client.render.ItemRenderHatLauncher;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;


public class ItemHatLauncher extends Item
        implements DualHandedItem
{
    public ItemHatLauncher(Properties properties)
    {
        super(DistExecutor.unsafeRunForDist(() -> () -> attachISTER(properties), () -> () -> properties));
    }

    @OnlyIn(Dist.CLIENT)
    public static Properties attachISTER(Properties properties)
    {
        return properties.setISTER(() -> () -> ItemRenderHatLauncher.INSTANCE);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player)
    {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity)
    {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        //        if(DualHandedItem.getUsableDualHandedItem(player) == player.getHeldItem(hand) && (player.abilities.isCreativeMode || EntityHelper.consumeInventoryItem(player.inventory, Hats.Items.TORCH_ROCKET.get())))
        //        {
        //            if(!world.isRemote)
        //            {
        //                EntityHelper.playSound(player, Torched.Sounds.RPT.get(), SoundCategory.PLAYERS, 1.0F, 0.9F + (player.getRNG().nextFloat() * 2F - 1F) * 0.075F);
        //                player.world.addEntity(new EntityTorchFirework(Torched.EntityTypes.TORCH_FIREWORK.get(), player.world).shot(player));
        //            }
        //            else
        //            {
        //                Torched.eventHandlerClient.nudgeHand(player);
        //            }
        //        }
        return new ActionResult(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || !ItemStack.areItemStacksEqual(oldStack, newStack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return Integer.MAX_VALUE;
    }
}
