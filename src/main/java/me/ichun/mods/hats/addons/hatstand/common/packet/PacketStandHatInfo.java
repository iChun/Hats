package me.ichun.mods.hats.addons.hatstand.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketStandHatInfo extends AbstractPacket
{
    public BlockPos pos;
    public String hatName;
    public int r;
    public int g;
    public int b;
    public int a;
    public int headType;
    public boolean hasBase;
    public boolean hasStand;

    public PacketStandHatInfo(){}

    public PacketStandHatInfo(BlockPos pos, String hatName, int colourR, int colourG, int colourB, int alpha, int head, boolean base, boolean standPost)
    {
        this.pos = pos;
        this.hatName = hatName;
        this.r = colourR;
        this.g = colourG;
        this.b = colourB;
        this.a = alpha;
        this.headType = head;
        this.hasBase = base;
        this.hasStand = standPost;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buffer, hatName);
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
        buffer.writeInt(a);
        buffer.writeInt(headType);
        buffer.writeBoolean(hasBase);
        buffer.writeBoolean(hasStand);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        pos = BlockPos.fromLong(buffer.readLong());

        hatName = ByteBufUtils.readUTF8String(buffer);

        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();
        a = buffer.readInt();

        headType = buffer.readInt();
        hasBase = buffer.readBoolean();
        hasStand = buffer.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        TileEntity te = player.worldObj.getTileEntity(pos);

        if(te instanceof TileEntityHatStand)
        {
            TileEntityHatStand stand = (TileEntityHatStand)te;

            stand.hatName = hatName;
            stand.colourR = r;
            stand.colourG = g;
            stand.colourB = b;
            stand.alpha = a;

            stand.head = headType;
            stand.hasBase = hasBase;
            stand.hasStand = hasStand;

            if(stand.head == 4)
            {
                stand.gameProfile = player.getGameProfile();
            }
            else
            {
                stand.gameProfile = null;
            }

            if(!HatHandler.hasHat(hatName))
            {
                HatHandler.requestHat(hatName, player);
            }

            IBlockState state = stand.getWorld().getBlockState(pos);
            stand.getWorld().notifyBlockUpdate(pos, state, state, 3);
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
