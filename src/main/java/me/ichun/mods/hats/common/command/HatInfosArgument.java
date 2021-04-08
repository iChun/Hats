package me.ichun.mods.hats.common.command;

import com.google.common.base.Splitter;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HatInfosArgument implements ArgumentType<ArrayList<HatInfo>>
{
    private static final List<String> EXAMPLES_SINGLE = Arrays.asList("Sombrero", "\"Pistontop:Extended Piston\"", "\"Top Hat\"");
    private static final List<String> EXAMPLES_MULTI = Arrays.asList("Sombrero", "\"Pistontop:Extended Piston\"", "\"Top Hat:Suspicious Hat:Open Top Hat:Pyro's Top Hat\"");
    private static final Splitter ON_COLON_SINGLE = Splitter.on(":").trimResults().omitEmptyStrings().limit(2);
    private static final Splitter ON_COLON_MULTI = Splitter.on(":").trimResults().omitEmptyStrings();

    private boolean multi;

    private HatInfosArgument(boolean multi)
    {
        this.multi = multi;
    }

    public static HatInfosArgument single()
    {
        return new HatInfosArgument(false);
    }

    public static HatInfosArgument multi()
    {
        return new HatInfosArgument(true);
    }

    public static <S> ArrayList<HatInfo> getHatInfos(CommandContext<S> context, String name) {
        return context.getArgument(name, ArrayList.class);
    }

    @Override
    public ArrayList<HatInfo> parse(StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readString();
        ArrayList<HatInfo> infos = new ArrayList<>();

        List<String> names = (multi ? ON_COLON_MULTI : ON_COLON_SINGLE).splitToList(name);

        if(names.isEmpty())
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }

        HatInfo hatInfo = HatResourceHandler.HATS.get(names.get(0));
        if(hatInfo == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }

        infos.add(hatInfo);

        for(int i = 1; i < names.size(); i++)
        {
            HatInfo accInfo = hatInfo.getInfoFor(names.get(i));
            if(accInfo == null)
            {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }

            infos.add(accInfo);
        }

        return infos;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        ArrayList<String> names = new ArrayList<>();
        for(HatInfo hat : HatResourceHandler.HATS.values())
        {
            names.add(hat.name);
            for(HatInfo accessory : hat.accessories)
            {
                accessory.addNameWithOrigin(names, hat.name);
            }
        }

        for(int i = names.size() - 1; i >= 0; i--)
        {
            if(names.get(i).contains(" ") || names.get(i).contains(":"))
            {
                names.set(i, "\"" + names.get(i) + "\"");
            }
        }
        return ISuggestionProvider.suggest(names, builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return multi ? EXAMPLES_MULTI :EXAMPLES_SINGLE;
    }
}
