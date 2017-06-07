package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.core.SessionState;

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
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, currentKing);
        ByteBufUtils.writeUTF8String(buffer, hatsList);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        SessionState.currentKing = ByteBufUtils.readUTF8String(buffer);

        hatsList = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(!hatsList.isEmpty())
        {
            HatHandler.populateHatsList(hatsList);
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }
}
