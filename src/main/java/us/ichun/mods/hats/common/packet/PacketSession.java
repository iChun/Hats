package us.ichun.mods.hats.common.packet;

import us.ichun.mods.hats.common.core.SessionState;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.hats.common.core.SessionState;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSession extends AbstractPacket
{
    public boolean hasVisited;
    public String serverHat;
    public String currentKing;
    public String playerHats;

    public PacketSession(){}

    public PacketSession(boolean visited, String currentKing, String hats)
    {
        this.hasVisited = visited;
        this.currentKing = currentKing;
        this.playerHats = hats;
    }

    //TODO test the length of super long hats list to see if it won't be too long
    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeBoolean(hasVisited);
        ByteBufUtils.writeUTF8String(buffer, currentKing);
        ByteBufUtils.writeUTF8String(buffer, playerHats);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        SessionState.serverHasMod = 1;
        SessionState.hasVisited = buffer.readBoolean() ? 1 : 0;
        SessionState.currentKing = ByteBufUtils.readUTF8String(buffer);
        SessionState.showJoinMessage = Hats.config.playerHatsMode >= 4 && SessionState.hasVisited == 0 ? 1 : 0;

        String availHats = ByteBufUtils.readUTF8String(buffer); //ignored on Free Mode
        if(Hats.config.playerHatsMode >= 4)
        {
            HatHandler.populateHatsList(availHats);
        }

    }

    @Override
    public void execute(Side side, EntityPlayer player){} //Hacky fix
}
