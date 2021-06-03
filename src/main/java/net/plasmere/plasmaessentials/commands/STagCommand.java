package net.plasmere.plasmaessentials.commands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.gunpowder.api.builders.Command;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.api.commands.ComplexCommand;
import net.plasmere.plasmaessentials.api.util.MessagingUtils;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class STagCommand extends ComplexCommand {
    public STagCommand() {
        super(ConfigUtils.cSTagBase, ConfigUtils.cSTagPerm, ConfigUtils.cSTagPermO, ConfigUtils.cSTagAliases);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal(this.base)
                .then(argument("target", StringArgumentType.word())
                        .suggests((c, b) -> suggestMatching(getPlayers(c.getSource()), b))
                        .then(argument("arg", StringArgumentType.word())
                                .suggests((c, b) -> suggestMatching(Arrays.asList("add", "list", "remove"), b))
                                .then(argument("tag", StringArgumentType.word())
                                        .requires(s -> {
                                            try {
                                                return PlayerUtils.hasPermission(this.otherPermission, s.getPlayer());
                                            } catch (CommandSyntaxException e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        })
                                        .executes(
                                                context -> execute(
                                                        context,
                                                        getPlayer(context, "target"),
                                                        context.getArgument("arg", String.class),
                                                        context.getArgument("arg", String.class).equals("list") ? "" : context.getArgument("tag", String.class)
                                                )
                                        )
                                )
                        )
                )
                .then(argument("arg", StringArgumentType.word())
                        .suggests((c, b) -> suggestMatching(Arrays.asList("add", "list", "remove"), b))
                        .then(argument("tag", StringArgumentType.word())
                                .requires(s -> {
                                    try {
                                        return PlayerUtils.hasPermission(this.permission, s.getPlayer());
                                    } catch (CommandSyntaxException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                })
                                .executes(
                                        context -> execute(
                                                context,
                                                context.getSource().getPlayer(),
                                                context.getArgument("arg", String.class),
                                                context.getArgument("arg", String.class).equals("list") ? "" : context.getArgument("tag", String.class)
                                        )
                                )
                        )
                );

        commandNode = dispatcher.register(literalArgumentBuilder);

        for (String alias : this.aliases) {
            dispatcher.register(CommandManager.literal(alias).redirect(commandNode));
        }
    }

    private int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, String arg, String tag){
        Player self = getSourcePlayer(context);
        Player target = getOnlinePlayer(context.getArgument("target", StringArgumentType.class).toString());

        if (! target.equals(self) && ! PlayerUtils.hasPermission(this.otherPermission, context.getSource())) {
            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.noPerm, false);
            return FAILED;
        }

        if (target.equals(self) && ! PlayerUtils.hasPermission(this.permission, context.getSource())) {
            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.noPerm, false);
            return FAILED;
        }

        switch (arg) {
            case "add" -> {
                PlayerUtils.addTag(target, context.getSource(), tag);
                return SUCCESS;
            }
            case "remove" -> {
                PlayerUtils.remTag(target, context.getSource(), tag);
                return SUCCESS;
            }
            case "list" -> {
                PlayerUtils.listTags(target, context.getSource());
                return SUCCESS;
            }
            default -> {
                MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.error);
                return FAILED;
            }
        }
    }
}
