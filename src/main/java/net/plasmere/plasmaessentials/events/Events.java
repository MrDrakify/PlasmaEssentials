package net.plasmere.plasmaessentials.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.MessagingUtils;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.api.util.UUIDFetcher;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

public class Events {
    public boolean onConnect(ServerPlayerEntity player, boolean cancel){
//        Player stat = PlayerUtils.getOrCreate(player);

//        if (stat == null) {
//            PlasmaEssentials.getInstance().getLogger().warn("Could not instantiate player for " + player.getName().asString());
//            return cancel;
//        }

//        if (stat.uuid == null) {
//            PlasmaEssentials.getInstance().getLogger().warn("Could not instantiate player for " + player.getName().asString());
//            return cancel;
//        }

        PlasmaEssentials.getInstance().getLogger().info(MessageConfUtils.join.replace("%player%", player.getDisplayName().asString()));

//        MessagingUtils.sendAllIncluding(player, MessageConfUtils.join.replace("%player%", PlayerUtils.getOffOnDisplay(stat)), false);
//        PlasmaEssentials.getInstance().getLogger().info(MessageConfUtils.join.replace("%player%", PlayerUtils.getOffOnDisplay(stat)));

        return cancel;
    }

    public boolean onDisconnect(ServerPlayerEntity player, boolean cancel){
        Player stat = PlayerUtils.getOrCreate(player);

        if (stat == null) {
            PlasmaEssentials.getInstance().getLogger().warn("Could not instantiate player for " + player.getName().asString());
            return cancel;
        }

        if (stat.uuid == null) {
            PlasmaEssentials.getInstance().getLogger().warn("Could not instantiate player for " + player.getName().asString());
            return cancel;
        }

        PlasmaEssentials.getInstance().getLogger().info(MessageConfUtils.leave.replace("%player%", PlayerUtils.getOffOnDisplay(stat)));

//        MessagingUtils.sendAllIncluding(player, MessageConfUtils.leave.replace("%player%", PlayerUtils.getOffOnDisplay(stat)), false);
//        PlasmaEssentials.getInstance().getLogger().info(MessageConfUtils.leave.replace("%player%", PlayerUtils.getOffOnDisplay(stat)));

        PlayerUtils.removeStat(stat);

        return cancel;
    }

    public boolean onChat(Player player, ChatMessageC2SPacket chatMessageC2SPacket, boolean cancel) {
        ServerPlayerEntity pe = player.player;
        NetworkThreadUtils.forceMainThread(chatMessageC2SPacket, pe.networkHandler, pe.getServerWorld());

        String string = StringUtils.normalizeSpace(chatMessageC2SPacket.getChatMessage());

        for (int i = 0; i < string.length(); ++i) {
            if (!SharedConstants.isValidChar(string.charAt(i))) {
                if (ConfigUtils.kickIllChars) {
                    pe.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                } else {
                    pe.getCommandSource().sendError(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                }

                return true;
            }
        }
        if (string.startsWith("/")) {
            PlasmaEssentials.getInstance().getServer().getCommandManager().execute(pe.getCommandSource(), string);
        } else {
            MessagingUtils.sendAll(MessageConfUtils.chat.replace("%player%", player.displayName).replace("%message%", string), false);
        }

        return cancel;
    }
}
