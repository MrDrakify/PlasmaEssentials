package net.plasmere.plasmaessentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.api.commands.ComplexCommand;
import net.plasmere.plasmaessentials.api.util.MessagingUtils;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.CommandSource.suggestMatching;

public class FlyCommand extends ComplexCommand {
    public FlyCommand() {
        super(ConfigUtils.cFlyBase, ConfigUtils.cFlyPerm, ConfigUtils.cFlyPermO, ConfigUtils.cFlyAliases);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal(this.base)
                .requires(Permissions.require(this.permission, false))
                .executes(context -> execute(context, context.getSource().getPlayer()))
                .then(argument("target", EntityArgumentType.player())
                        .suggests((context, builder) -> suggestMatching(context.getSource().getMinecraftServer().getPlayerNames(), builder))
                        .requires(Permissions.require(this.otherPermission, false))
                        .executes(context -> execute(context, getPlayer(context, "target")))
                );

        commandNode = dispatcher.register(literalArgumentBuilder);

        for (String alias : this.aliases) {
            dispatcher.register(CommandManager.literal(alias).redirect(commandNode));
        }
    }

    private int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
        Player self = getSourcePlayer(context);
        Player target = getOnlinePlayer(player);

        if (! self.equals(target) && PlayerUtils.hasPermission(this.otherPermission, self.player.getCommandSource())) {
            target.player.getAbilities().allowFlying = ! target.player.getAbilities().allowFlying;
            target.player.getAbilities().flying = ! target.player.getAbilities().flying;
            target.player.sendAbilitiesUpdate();

            MessagingUtils.sendUserMessage(target.player.getCommandSource(), MessageConfUtils.cFlySelf
                    .replace("%toggle%", target.player.getAbilities().allowFlying ? MessageConfUtils.togOn : MessageConfUtils.togOff)
            );

            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.cFlyOthers
                    .replace("%toggle%", target.player.getAbilities().allowFlying ? MessageConfUtils.togOn : MessageConfUtils.togOff)
                    .replace("%player%", PlayerUtils.getOffOnDisplay(target))
            );

            return SUCCESS;
        } else if (self.equals(target) && PlayerUtils.hasPermission(this.permission, self.player.getCommandSource())) {
            target.player.getAbilities().allowFlying = ! target.player.getAbilities().allowFlying;
            target.player.getAbilities().flying = ! target.player.getAbilities().flying;
            target.player.sendAbilitiesUpdate();

            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.cFlySelf
                    .replace("%toggle%", target.player.getAbilities().allowFlying ? MessageConfUtils.togOn : MessageConfUtils.togOff)
            );

            return SUCCESS;
        } else {
            MessagingUtils.sendUserMessage(context.getSource(), MessageConfUtils.noPerm);

            return FAILED;
        }
    }
}
