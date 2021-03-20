package me.ichun.mods.hats.common.item;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.render.ItemRenderHatLauncher;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketEntityHatEntityDetails;
import me.ichun.mods.hats.common.packet.PacketRehatify;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class ItemHatLauncher extends Item
        implements DualHandedItem
{
    public static final String STACK_HAT_PART_TAG = "hats_hatPart";

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
        ItemStack is = player.getHeldItem(hand);
        if(DualHandedItem.getUsableDualHandedItem(player) == is)
        {
            //this is the part of the item we are using
            HatsSavedData.HatPart part = null;

            if(!player.isSneaking()) //sneaking forces open the UI
            {
                part = HatHandler.getHatPart(is).createCopy();

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
                        part = HatHandler.getRandomHat(player);
                    }
                }
                else if(!HatHandler.playerHasHat(player, part)) //TODO Hat inventory isn't updating properly when you fire in survival. Darko got 1 hat, fired it with launcher, picked it up, hat inventory showed -1 count
                {
                    part = null;
                }
            }

            if(part != null && !part.name.isEmpty() && part.count > 0) //TODO if the inventory runs out swap to next item
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

                    hat.setLocationAndAngles(pX, player.getPosY() + player.getEyeHeight() - ((hat.hatDims[1] - hat.hatDims[0]) / 32F) / 1.8F, pZ, player.rotationYaw, player.rotationPitch);

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, is);
                    if(k > 0)
                    {
                        hat.setKnockbackStrength(k);
                    }

                    float momentum = 0.5F;

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, is);
                    if (j > 0) {
                        momentum += (double) j * 0.25F;
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, is) > 0) {
                        hat.setFire(100);
                    }

                    hat.setThrowableHeading((-MathHelper.sin(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI)),
                            (-MathHelper.sin(player.rotationPitch / 180.0F * (float)Math.PI)),
                            (MathHelper.cos(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI))
                            , momentum, 0.4F);

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
                boolean flag = false;
                RayTraceResult entityLook = EntityHelper.getEntityLook(player, 4D);
                if(entityLook.getType() == RayTraceResult.Type.ENTITY)
                {
                    EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult)entityLook;
                    Entity entity = entityRayTraceResult.getEntity();
                    if(entity instanceof LivingEntity)
                    {
                        LivingEntity target = (LivingEntity)entity;
                        HatsSavedData.HatPart entPart = HatHandler.getHatPart(target);
                        if(!entPart.name.isEmpty() && !(target instanceof PlayerEntity && !Hats.configServer.hatLauncherReplacesPlayerHat))
                        {
                            if(!player.world.isRemote)
                            {
                                EntityHelper.playSound(player, Hats.Sounds.TUBE.get(), SoundCategory.PLAYERS, 1.0F, 0.9F + (player.getRNG().nextFloat() * 2F - 1F) * 0.075F);
                                EntityHat hat = new EntityHat(Hats.EntityTypes.HAT.get(), player.world).setHatPart(entPart.createCopy()).setLastInteracted(target);

                                hat.setLocationAndAngles(target.getPosX(), target.getPosY() + target.getEyeHeight() - ((hat.hatDims[1] - hat.hatDims[0]) / 32F) / 1.8F, target.getPosZ(), target.rotationYaw, target.rotationPitch);

                                hat.setMotion(new Vector3d(target.getRNG().nextGaussian() * 0.2F, 0.2F + target.getRNG().nextFloat() * 0.2F, target.getRNG().nextGaussian() * 0.2F));

                                player.world.addEntity(hat);

                                entPart.copy(new HatsSavedData.HatPart());

                                HashMap<Integer, HatsSavedData.HatPart> entIdToHat = new HashMap<>();
                                entIdToHat.put(target.getEntityId(), entPart);
                                Hats.channel.sendTo(new PacketEntityHatDetails(entIdToHat), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target));

                                Hats.channel.sendTo(new PacketRehatify(target.getEntityId()), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target));

                                Hats.channel.sendTo(new PacketEntityHatEntityDetails(hat.getEntityId(), hat.hatPart.write(new CompoundNBT())), PacketDistributor.TRACKING_ENTITY.with(() -> hat));
                            }

                            player.swing(hand, true);
                            flag = true;
                        }
                    }
                }

                if(!flag)
                {
                    if(!world.isRemote)
                    {
                        EntityHelper.playSound(player, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 0.9F + (player.getRNG().nextFloat() * 2F - 1F) * 0.075F);
                    }
                    else
                    {
                        openHatsGui(player, is);
                    }
                }
            }
        }
        return new ActionResult(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void openHatsGui(@Nonnull PlayerEntity player, @Nonnull ItemStack is)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == player)
        {
            mc.displayGuiScreen(new WorkspaceHats(mc.currentScreen, true, player, is));
        }
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

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        HatsSavedData.HatPart defPart = new HatsSavedData.HatPart(":random");
        HatsSavedData.HatPart.CapProvider capProvider = new HatsSavedData.HatPart.CapProvider(defPart);
        if(nbt != null)
        {
            capProvider.deserializeNBT(nbt);
        }
        defPart.isShowing = true;
        stack.getOrCreateTag().put(STACK_HAT_PART_TAG, defPart.write(new CompoundNBT()));
        return capProvider;
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
