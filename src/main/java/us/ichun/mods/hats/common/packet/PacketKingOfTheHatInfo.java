package us.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.hats.common.core.SessionState;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

public class PacketKingOfTheHatInfo extends AbstractPacket
{
    public String currentKing;
    public String hatsList;

    public PacketKingOfTheHatInfo(){}

    public PacketKingOfTheHatInfo(String name, String hats)
    {
        currentKing = name;
        hatsList = hats;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, currentKing);
        ByteBufUtils.writeUTF8String(buffer, hatsList);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        SessionState.currentKing = ByteBufUtils.readUTF8String(buffer);

        hatsList = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(!hatsList.isEmpty())
        {
            HatHandler.populateHatsList(hatsList);
        }
    }
}
