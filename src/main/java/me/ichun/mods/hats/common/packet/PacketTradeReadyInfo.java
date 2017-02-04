package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import me.ichun.mods.hats.client.gui.GuiTradeWindow;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;

public class PacketTradeReadyInfo extends AbstractPacket
{
    public String trader1String;
    public boolean trader1Ready;
    public String trader2String;
    public boolean trader2Ready;

    public PacketTradeReadyInfo(){}

    public PacketTradeReadyInfo(String t1UUID, boolean t1Ready, String t2UUID, boolean t2Ready)
    {
        this.trader1String = t1UUID;
        this.trader1Ready = t1Ready;
        this.trader2String = t2UUID;
        this.trader2Ready = t2Ready;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, trader1String);
        buffer.writeBoolean(trader1Ready);
        ByteBufUtils.writeUTF8String(buffer, trader2String);
        buffer.writeBoolean(trader2Ready);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        trader1String = ByteBufUtils.readUTF8String(buffer);
        trader1Ready = buffer.readBoolean();
        trader2String = ByteBufUtils.readUTF8String(buffer);
        trader2Ready = buffer.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            handleClient(side, player);
        }
        return null;
    }

    @Override
    public Side receivingSide() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
        {
            GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;

            if(trader1String.equals(Minecraft.getMinecraft().player.getName()))
            {
                if(trade.selfReady && !trader1Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                trade.selfReady = trader1Ready;
                trade.theirReady = trader2Ready;
            }
            else
            {
                if(trade.selfReady && !trader2Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
