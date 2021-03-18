package me.ichun.mods.hats.common.item;

import me.ichun.mods.hats.client.render.ItemRenderHatLauncher;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatEntityDetails;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;


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
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) //TODO enchantability, knockback + power + fire??
    {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack is = player.getHeldItem(hand);
        if(DualHandedItem.getUsableDualHandedItem(player) == is)
        {
            //this is the part of the item we are using
            HatsSavedData.HatPart part = null;

            if(!player.isSneaking()) //sneaking forces open the UI
            {
                part = HatHandler.getHatPart(is);

                if(part.name.equals(":random"))
                {
                    if(world.isRemote)
                    {
                        ArrayList<HatsSavedData.HatPart> source = HatHandler.getHatSource(player).stream().filter(hatPart -> hatPart.count > 0).collect(Collectors.toCollection(ArrayList::new));

                        if(source.isEmpty())
                        {
                            part = null;
                        }
                    }
                    else
                    {
                        part = HatHandler.getRandomHat(player); //TODO test this
                    }
                }
            }

            if(part != null && part.count > 0)
            {
                if(!world.isRemote)
                {
                    EntityHelper.playSound(player, Hats.Sounds.TUBE.get(), SoundCategory.PLAYERS, 1.0F, 0.9F + (player.getRNG().nextFloat() * 2F - 1F) * 0.075F);
                    EntityHat hat = new EntityHat(Hats.EntityTypes.HAT.get(), player.world).setHatPart(part).setLastInteracted(player);

                    double pX = player.getPosX();
                    double pZ = player.getPosZ();
                    Vector3d rot = EntityHelper.getVectorForRotation(0F, player.rotationYaw + 90);
                    switch(DualHandedItem.getHandSide(player, DualHandedItem.getUsableDualHandedItem(player)))
                    {
                        case RIGHT:
                        {
                            pX += rot.x * 0.25F;
                            pZ += rot.z * 0.25F;
                            break;
                        }
                        case LEFT:
                        {
                            pX -= rot.x * 0.25F;
                            pZ -= rot.z * 0.25F;
                            break;
                        }
                    }

                    hat.setLocationAndAngles(pX, player.getPosY() + player.getEyeHeight() - ((hat.hatDims[1] - hat.hatDims[0]) / 16F) / 1.8F, pZ, player.rotationYaw, player.rotationPitch);

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, is);
                    if(k > 0)
                    {
                        hat.setKnockbackStrength(k);
                    }

                    float momentum = 0.5F;

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, is);
                    if (j > 0) {
                        momentum += (double) j * 0.2F;
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, is) > 0) {
                        hat.setFire(100);
                    }

                    hat.setThrowableHeading((-MathHelper.sin(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI)),
                            (-MathHelper.sin(player.rotationPitch / 180.0F * (float)Math.PI)),
                            (MathHelper.cos(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI))
                            , momentum, 0.4F);


                    //TODO power = further distance
                    //TODO fire!
                    player.world.addEntity(hat);

                    Hats.channel.sendTo(new PacketEntityHatEntityDetails(hat.getEntityId(), hat.hatPart.write(new CompoundNBT())), PacketDistributor.TRACKING_ENTITY.with(() -> hat));

                    if(HatHandler.useInventory(player))
                    {
                        HatHandler.removeOneFromInventory((ServerPlayerEntity)player, part);
                    }
                }
                else
                {
                    Hats.eventHandlerClient.nudgeHand(player);
                }
            }
            else
            {
                //TODO open the UI if we still have hats.
            }
        }
        return new ActionResult(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return enchantment == Enchantments.POWER || enchantment == Enchantments.PUNCH || enchantment == Enchantments.FLAME;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getItemEnchantability()
    {
        return 22; //Stolen from gold ItemTier
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book)
    {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(book);
        for(Enchantment enchantment : enchantments.keySet())
        {
            if(!canApplyAtEnchantingTable(stack, enchantment))
            {
                return false;
            }
        }

        return true;
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
