package net.plasmere.plasmaessentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.api.commands.ComplexCommand;
import net.plasmere.plasmaessentials.api.util.MessagingUtils;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class NickCommand extends ComplexCommand {
    public NickCommand() {
        super(ConfigUtils.cNickBase, ConfigUtils.cNickPerm, ConfigUtils.cNickPermO, ConfigUtils.cNickAliases);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal(this.base)
                .then(argument("target", StringArgumentType.word())
                        .suggests((c, b) -> suggestMatching(getPlayers(c.getSource()), b))
                        .then(argument("nick", StringArgumentType.greedyString())
                                .requires(s -> {
                                    try {
                                        return PlayerUtils.hasPermission(this.otherPermission, s.getPlayer());
                                    } catch (CommandSyntaxException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                })
                                .executes(context -> execute(context, getPlayer(context, "target"), context.getArgument("nick", String.class)))
                        )
                )
                .then(argument("nick", StringArgumentType.greedyString())
                        .requires(s -> {
                            try {
                                return PlayerUtils.hasPermission(this.permission, s.getPlayer());
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .executes(context -> execute(context, context.getSource().getPlayer(), context.getArgument("nick", String.class)))
                );

        commandNode = dispatcher.register(literalArgumentBuilder);

        for (String alias : this.aliases) {
            dispatcher.register(CommandManager.literal(alias).redirect(commandNode));
        }
    }

    private int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, String nick){
        Player self = getSourcePlayer(context);
        Player target = getOnlinePlayer(player);

        if (! target.equals(self) && ! PlayerUtils.hasPermission(this.otherPermission, context.getSource())) {
            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.noPerm, false);
            return FAILED;
        }

        if (target.equals(self) && ! PlayerUtils.hasPermission(this.permission, context.getSource())) {
            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.noPerm, false);
            return FAILED;
        }

        PlayerUtils.setNick(target, context.getSource(), nick);

        return SUCCESS;
    }
}
