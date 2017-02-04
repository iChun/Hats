package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.core.HatInfo;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;

public class PacketPlayerHatSelection extends AbstractPacket
{

    public String hatName;
    public int r;
    public int g;
    public int b;
    public int a;

    public PacketPlayerHatSelection(){}

    public PacketPlayerHatSelection(String name, int R, int G, int B, int A)
    {
        this.hatName = name;
        this.r = R;
        this.g = G;
        this.b = B;
        this.a = A;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        //should always be clientside
        ByteBufUtils.writeUTF8String(buffer, hatName);
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
        buffer.writeInt(a);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        //should always be serverside
        hatName = ByteBufUtils.readUTF8String(buffer);
        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();
        a = buffer.readInt();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        Hats.proxy.playerWornHats.put(player.getName(), new HatInfo(hatName, r, g, b, a));

        if(HatHandler.hasHat(hatName))
        {
            NBTTagCompound persistentTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persistentTag.setString("Hats_wornHat", hatName);
            persistentTag.setInteger("Hats_colourR", r);
            persistentTag.setInteger("Hats_colourG", g);
            persistentTag.setInteger("Hats_colourB", b);
            persistentTag.setInteger("Hats_alpha", a);
            player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

            Hats.proxy.sendPlayerListOfWornHats(player, false);
        }
        else
        {
            HatHandler.requestHat(hatName, player);
        }
        return null;
    }

    @Override
    public Side receivingSide() {
        return null;
    }
}
