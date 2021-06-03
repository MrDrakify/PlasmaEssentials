package net.plasmere.plasmaessentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.plasmere.plasmaessentials.created.players.Player;

import java.util.List;

public interface IComplexCommand {
    int SUCCESS = 1;
    int AWAIT = 0;
    int FAILED = -1;

    LiteralArgumentBuilder<ServerCommandSource> literal(String base);
    <T>RequiredArgumentBuilder<ServerCommandSource, T> argument(String string, ArgumentType<T> argumentType);
    Player getPlayerObj(String name);

    String getBase();

    List<String> getAliases();

    default public void register(CommandDispatcher<ServerCommandSource> dispatcher){
    }
}
