package me.ichun.mods.hats.common.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import net.minecraft.command.ISuggestionProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class HatNameArgument implements ArgumentType<HatInfo>
{
    public String lastArg;

    private HatNameArgument(String lastArg)
    {
        this.lastArg = lastArg;
    }

    public static HatNameArgument lastArg(String s)
    {
        return new HatNameArgument(s);
    }

    public static HatNameArgument noArg()
    {
        return new HatNameArgument(null);
    }

    @Override
    public HatInfo parse(StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readString();
        System.out.println(name);
        HatInfo hatInfo = HatResourceHandler.HATS.get(name);
        if(hatInfo == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return hatInfo;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        ArrayList<String> names = new ArrayList<>();
        if(lastArg != null)
        {
            HatInfo info = context.getArgument(lastArg, HatInfo.class);
            for(HatInfo accessory : info.accessories)
            {
                names.add(accessory.name);
            }
            Collections.sort(names);
        }
        else
        {
            names.addAll(HatResourceHandler.HATS.keySet());
        }
        for(int i = names.size() - 1; i >= 0; i--)
        {
            if(names.get(i).contains(" "))
            {
                names.set(i, "\"" + names.get(i) + "\"");
            }
        }
        return ISuggestionProvider.suggest(names, builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return Arrays.asList("\"Top Hat\"", "Sombrero", "Squid");
    }
}
