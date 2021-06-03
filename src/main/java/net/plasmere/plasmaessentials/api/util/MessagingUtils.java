package net.plasmere.plasmaessentials.api.util;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.created.players.Player;

import java.util.Objects;

public class MessagingUtils {
    public static void sendAll(String message, boolean actionBar){
        for (ServerPlayerEntity p : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()){
            p.sendMessage(StringUtils.codedCHText(message), actionBar);
        }
    }

    public static void sendAllIncluding(ServerPlayerEntity playerEntity, String message, boolean actionBar){
        for (ServerPlayerEntity p : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()){
            p.sendMessage(StringUtils.codedCHText(message), actionBar);
        }

        playerEntity.sendMessage(StringUtils.codedCHText(message), actionBar);
    }

    public static void sendStatUserMessage(Player of, ServerCommandSource to, String msg, boolean actionBar){
        if (of == null) return;

        ServerPlayerEntity player = null;
        boolean isPlayer = true;

        try {
            player = to.getPlayer();
        } catch (Exception e) {
            isPlayer = false;
        }

        if (isPlayer && player != null) {
            player.sendMessage(StringUtils.codedText(msg
                    .replace("%sender%", PlayerUtils.getOffOnDisplay(of))
                    .replace("%player%", PlayerUtils.getOffOnReg(of))
                    .replace("%xp%", Integer.toString(of.xp))
                    .replace("%level%", Integer.toString(of.lvl))
                    .replace("%xpneeded%", Integer.toString(of.getNeededXp()))
                    .replace("%xplevel%", Integer.toString(of.xpUntilNextLevel()))
                    .replace("%playtime%", StringUtils.truncate(Double.toString(of.getPlayHours()), 3))
            ), actionBar);
        } else {
            PlasmaEssentials.getInstance().getLogger().info(StringUtils.codedText(msg
                    .replace("%sender%", PlayerUtils.getOffOnDisplay(of))
                    .replace("%player%", PlayerUtils.getOffOnReg(of))
                    .replace("%xp%", Integer.toString(of.xp))
                    .replace("%level%", Integer.toString(of.lvl))
                    .replace("%xpneeded%", Integer.toString(of.getNeededXp()))
                    .replace("%xplevel%", Integer.toString(of.xpUntilNextLevel()))
                    .replace("%playtime%", StringUtils.truncate(Double.toString(of.getPlayHours()), 3))
            ));
        }
    }

    public static void sendUserMessage(ServerCommandSource sender, String msg, boolean actionBar){
        ServerPlayerEntity player = null;
        boolean isPlayer = true;

        try {
            player = sender.getPlayer();
        } catch (Exception e) {
            isPlayer = false;
        }

        if (isPlayer && player != null) {
            player.sendMessage(StringUtils.codedText(msg
                    .replace("%sender%", PlayerUtils.getOffOnDisplay((UUIDFetcher.getPlayer(player))))
            ), actionBar);
        } else {
            PlasmaEssentials.getInstance().getLogger().info(StringUtils.codedText(msg
                    .replace("%sender%", sender.getName())
            ));
        }
    }

    public static void sendUserMessage(ServerCommandSource sender, String msg){
        ServerPlayerEntity player = null;
        boolean isPlayer = true;

        try {
            player = sender.getPlayer();
        } catch (Exception e) {
            isPlayer = false;
        }

        if (isPlayer && player != null) {
            player.sendMessage(StringUtils.codedText(msg
                    .replace("%sender%", PlayerUtils.getOffOnDisplay((UUIDFetcher.getPlayer(player))))
            ), false);
        } else {
            PlasmaEssentials.getInstance().getLogger().info(StringUtils.codedText(msg
                    .replace("%sender%", sender.getName())
            ));
        }
    }
}
