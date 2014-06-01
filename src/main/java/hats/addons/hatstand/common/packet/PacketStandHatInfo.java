package hats.addons.hatstand.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import hats.common.core.HatHandler;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;

public class PacketStandHatInfo extends AbstractPacket
{
    public int x;
    public int y;
    public int z;
    public String hatName;
    public int r;
    public int g;
    public int b;
    public int headType;
    public boolean hasBase;
    public boolean hasStand;

    public PacketStandHatInfo(){}

    public PacketStandHatInfo(int xCoord, int yCoord, int zCoord, String hatName, int colourR, int colourG, int colourB, int head, boolean base, boolean standPost)
    {
        this.x = xCoord;
        this.y = yCoord;
        this.z = zCoord;
        this.hatName = hatName;
        this.r = colourR;
        this.g = colourG;
        this.b = colourB;
        this.headType = head;
        this.hasBase = base;
        this.hasStand = standPost;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        ByteBufUtils.writeUTF8String(buffer, hatName);
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
        buffer.writeInt(headType);
        buffer.writeBoolean(hasBase);
        buffer.writeBoolean(hasStand);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();

        hatName = ByteBufUtils.readUTF8String(buffer);

        r = buffer.readInt();
        g = buffer.readInt();
        b = buffer.readInt();

        headType = buffer.readInt();
        hasBase = buffer.readBoolean();
        hasStand = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        TileEntity te = DimensionManager.getWorld(player.dimension).getTileEntity(x, y, z);

        if(te instanceof TileEntityHatStand)
        {
            TileEntityHatStand stand = (TileEntityHatStand)te;

            stand.hatName = hatName;
            stand.colourR = r;
            stand.colourG = g;
            stand.colourB = b;

            stand.head = headType;
            stand.hasBase = hasBase;
            stand.hasStand = hasStand;

            if(stand.head == 4)
            {
                stand.headName = player.getCommandSenderName();
            }
            else
            {
                stand.headName = "";
            }

            if(!HatHandler.hasHat(hatName))
            {
                HatHandler.requestHat(hatName, player);
            }

            DimensionManager.getWorld(player.dimension).markBlockForUpdate(x, y, z);
        }
    }
}
