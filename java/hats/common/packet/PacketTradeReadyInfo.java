package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.client.gui.GuiTradeWindow;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class PacketTradeReadyInfo extends AbstractPacket
{
    public String trader1Name;
    public boolean trader1Ready;
    public String trader2Name;
    public boolean trader2Ready;

    public PacketTradeReadyInfo(){}

    public PacketTradeReadyInfo(String t1Name, boolean t1Ready, String t2Name, boolean t2Ready)
    {
        this.trader1Name = t1Name;
        this.trader1Ready = t1Ready;
        this.trader2Name = t2Name;
        this.trader2Ready = t2Ready;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, trader1Name);
        buffer.writeBoolean(trader1Ready);
        ByteBufUtils.writeUTF8String(buffer, trader2Name);
        buffer.writeBoolean(trader2Ready);
    }

    //TODO as usual, test if it breaks cause of the Minecraft ref.
    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        trader1Name = ByteBufUtils.readUTF8String(buffer);
        trader1Ready = buffer.readBoolean();
        trader2Name = ByteBufUtils.readUTF8String(buffer);
        trader2Ready = buffer.readBoolean();

        if(side.isClient() && Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
        {
            GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;

            if(trader1Name.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getCommandSenderName()))
            {
                if(trade.selfReady && !trader1Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
                }
                trade.selfReady = trader1Ready;
                trade.theirReady = trader2Ready;
            }
            else
            {
                if(trade.selfReady && !trader2Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
                }
                trade.selfReady = trader2Ready;
                trade.theirReady = trader1Ready;
            }
            if(!trade.theirReady)
            {
                trade.pointOfNoReturn = false;
                trade.clickedMakeTrade = false;
            }
        }

    }
}
