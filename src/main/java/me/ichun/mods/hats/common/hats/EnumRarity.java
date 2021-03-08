package me.ichun.mods.hats.common.hats;

import net.minecraft.util.text.TextFormatting;

public enum EnumRarity
{
    //Keep in order of increasing rarity
    COMMON(TextFormatting.WHITE),
    UNCOMMON(TextFormatting.GREEN),
    RARE(TextFormatting.BLUE),
    EPIC(TextFormatting.LIGHT_PURPLE),
    LEGENDARY(TextFormatting.GOLD);

    private final TextFormatting colour;

    EnumRarity(TextFormatting colour) {this.colour = colour;}

    public TextFormatting getColour()
    {
        return colour;
    }
}
