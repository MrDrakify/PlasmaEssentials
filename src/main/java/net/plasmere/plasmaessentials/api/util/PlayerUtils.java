package net.plasmere.plasmaessentials.api.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.conf.Configuration;
import net.plasmere.plasmaessentials.config.ConfigHandler;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerUtils {
    private static final Configuration mess = PlasmaEssentials.getInstance().getConfigHandler().getMess();
    public static LuckPerms api = PlasmaEssentials.getInstance().getApi();

    private static final List<Player> stats = new ArrayList<>();
    private static final Configuration message = ConfigHandler.getInstance().getMess();

    public static List<Player> getStats() {
        return stats;
    }

    public static boolean hasPermission(String perm, ServerCommandSource source){
        return Permissions.check(source, perm);
    }

    public static boolean hasPermission(String perm, int minOpLevel, ServerCommandSource source) {
        return Permissions.check(source, perm, minOpLevel);
    }

    public static boolean hasPermission(String perm, ServerPlayerEntity source){
        return Permissions.check(source, perm);
    }

    public static boolean hasPermission(String perm, int minOpLevel, ServerPlayerEntity source){
        return Permissions.check(source, perm, minOpLevel);
    }

    public static List<String> getAllUsersByUUID(){
        List<String> it = new ArrayList<>();
        File[] files = PlasmaEssentials.getInstance().getPlDir().listFiles();

        if (files == null) return it;
        if (files.length <= 0) return it;

        for (File file : files) {
            String name = file.getName();

            if (! name.endsWith(".properties")) continue;

            name = name.replace(".properties", "");
            it.add(name);
        }

        return it;
    }

    public static CompletableFuture<Suggestions> allUUIDs(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getAllUsersByUUID(), builder);
    }

    public static CompletableFuture<Suggestions> onlineUsers(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(PlasmaEssentials.getInstance().getServer().getPlayerNames(), builder);
    }

    public static ServerPlayerEntity getServerPlayerEntity(String latestName){
        for (ServerPlayerEntity p : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()){
            if (p.getName().asString().equals(latestName)) return p;
        }

        return null;
    }

    public static boolean hasStat(String latestName){
        return getStat(latestName) != null;
    }

    public static Player getOffOnStat(String name){
        Player p = getStat(name);

        if (p == null) {
            if (exists(name)) {
                p = new Player(name);
                addStat(p);
                return p;
            } else {
                return null;
            }
        }

        return p;
    }

    public static Player getOrCreate(UUID uuid){
        Player player = getStat(uuid);

        if (player == null) {
            if (exists(UUIDFetcher.getName(uuid.toString()))) {
                player = new Player(uuid);
            } else {
                ServerPlayerEntity playerEntity = UUIDFetcher.getSPlayer(uuid);
                if (playerEntity == null) return null;

                player = new Player(playerEntity);
            }
            addStat(player);
        }

        return player;
    }

    public static Player getOrCreate(ServerPlayerEntity playerEntity){
        Player player = getStat(playerEntity);

        if (player == null) {
            if (exists(playerEntity.getUuid())) {
                player = new Player(playerEntity, false);
            } else {
                player = new Player(playerEntity);
            }
            addStat(player);
        }

        return player;
    }

    public static boolean isNameEqual(Player player, String name){
        if (player.latestName == null) return false;

        return player.latestName.equals(name);
    }

    public static Player getStat(ServerPlayerEntity player) {
        try {
            for (Player stat : stats) {
                if (isNameEqual(stat, player.getName().asString())) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String forStats(List<Player> players){
        StringBuilder builder = new StringBuilder("[");

        int i = 1;
        for (Player p : players){
            if (i != players.size()) {
                builder.append(p.toString()).append(", ");
            } else {
                builder.append(p.toString()).append("]");
            }

            i++;
        }

        return builder.toString();
    }

    public static Player getStat(String name) {
        try {
            for (Player stat : stats) {
                if (isNameEqual(stat, name)) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Player getStat(UUID uuid) {
        try {
            for (Player stat : stats) {
                if (stat.uuid.equals(uuid)) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Player> transposeList(List<ServerPlayerEntity> players){
        List<Player> ps = new ArrayList<>();
        for (ServerPlayerEntity player : players){
            ps.add(UUIDFetcher.getPlayer(player));
        }

        return ps;
    }

    public static String getOffOnDisplay(Player player){
        if (player == null) {
            return "&c&lNULL";
        }

        if (player.online) {
            return MessageConfUtils.online.replace("%player%", player.displayName);
        } else {
            return MessageConfUtils.offline.replace("%player%", player.displayName);
        }
    }

    public static String getOffOnReg(Player player){
        if (player == null) {
            return "&c&lNULL";
        }

        if (player.online) {
            return MessageConfUtils.online.replace("%player%", player.latestName);
        } else {
            return MessageConfUtils.offline.replace("%player%", player.latestName);
        }
    }



    public static boolean exists(String username){
        File file = new File(PlasmaEssentials.getInstance().getPlDir(), UUIDFetcher.getCachedUUID(username) + ".properties");

        return file.exists();
    }

    public static boolean exists(UUID uuid){
        File file = new File(PlasmaEssentials.getInstance().getPlDir(), uuid.toString() + ".properties");

        return file.exists();
    }

    public static boolean isStats(Player stat){
        return stats.contains(stat);
    }

    public static void reloadStats(Player stat) {
        stats.remove(getStat(stat.latestName));
        stats.add(stat);
    }

    public static void createStat(ServerPlayerEntity player) {
        try {
            Player stat = new Player(player);

            addStat(stat);

            if (ConfigUtils.tellNew) {
                MessagingUtils.sendStatUserMessage(stat, player.getCommandSource(), create, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGroup(Player player){
        AbstractTeam team = player.player.getScoreboardTeam();

        if (team == null) return;

        User user = api.getUserManager().getUser(player.uuid);

        if (user == null) return;

        Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

        if (group == null) {
            user.setPrimaryGroup(team.getName());
            return;
        }

        if (! group.getName().equals(team.getName().toLowerCase(Locale.ROOT))) {
            user.setPrimaryGroup(team.getName());
        }
    }

    public static void updateDisplayName(Player player){
        if (! ConfigUtils.updateNames) return;
        if (! PlasmaEssentials.lpHolder.enabled) return;

        User user = PlasmaEssentials.lpHolder.api.getUserManager().getUser(player.latestName);
        if (user == null) return;

        Group group = PlasmaEssentials.lpHolder.api.getGroupManager().getGroup(user.getPrimaryGroup());
        if (group == null) return;

        String prefix = "";
        String suffix = "";

        TreeMap<Integer, String> preWeight = new TreeMap<>();
        TreeMap<Integer, String> sufWeight = new TreeMap<>();

        for (PrefixNode node : group.getNodes(NodeType.PREFIX)) {
            preWeight.put(node.getPriority(), node.getMetaValue());
        }

        for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
            preWeight.put(node.getPriority(), node.getMetaValue());
        }

        for (SuffixNode node : group.getNodes(NodeType.SUFFIX)) {
            sufWeight.put(node.getPriority(), node.getMetaValue());
        }

        for (SuffixNode node : user.getNodes(NodeType.SUFFIX)) {
            sufWeight.put(node.getPriority(), node.getMetaValue());
        }

        prefix = preWeight.get(getCeilingInt(preWeight.keySet()));
        suffix = sufWeight.get(getCeilingInt(sufWeight.keySet()));

        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        player.updateKey("display-name", StringUtils.codedString(prefix + player.latestName + suffix));
    }

    public static int getCeilingInt(Set<Integer> ints){
        int value = 0;

        for (Integer i : ints) {
            if (i >= value) value = i;
        }

        return value;
    }

    public static void info(Player of, ServerCommandSource to){
        if (! hasPermission(ConfigUtils.cStatsPerm, to)) {
            MessagingUtils.sendUserMessage(to, noPerm, false);
        }

        MessagingUtils.sendStatUserMessage(of, to, info, false);
    }

    public static void remTag(Player of, ServerCommandSource to, String tag){
        if (! hasPermission(ConfigUtils.cSTagPerm, to)) {
            MessagingUtils.sendUserMessage(to, noPerm, false);
            return;
        }

        of.tryRemTag(tag);

        MessagingUtils.sendUserMessage(to, tagRem
                .replace("%player%", getOffOnDisplay(of))
                .replace("%tag%", tag)
                , false
        );
    }

    public static void addTag(Player of, ServerCommandSource to, String tag){
        if (! hasPermission(ConfigUtils.cSTagPerm, to)) {
            MessagingUtils.sendUserMessage(to, noPerm, false);
            return;
        }

        of.tryAddNewTag(tag);

        MessagingUtils.sendUserMessage(to, tagAdd
                        .replace("%player%", getOffOnDisplay(of))
                        .replace("%tag%", tag)
                , false
        );
    }

    public static void listTags(Player of, ServerCommandSource to){
        if (! hasPermission(ConfigUtils.cSTagPerm, to)) {
            MessagingUtils.sendUserMessage(to, noPerm, false);
            return;
        }

        MessagingUtils.sendUserMessage(to, tagListMain
                        .replace("%player%", getOffOnDisplay(of))
                        .replace("%tags%", compileTagList(of))
                , false
        );
    }

    public static String compileTagList(Player of) {
        StringBuilder stringBuilder = new StringBuilder();

        int i = 1;
        for (String tag : of.tagList){
            if (i < of.tagList.size()) {
                stringBuilder.append(tagListNotLast
                        .replace("%player%", getOffOnDisplay(of))
                        .replace("%tag%", tag)
                );
            } else {
                stringBuilder.append(tagListLast
                        .replace("%player%", getOffOnDisplay(of))
                        .replace("%tag%", tag)
                );
            }
            i++;
        }

        return stringBuilder.toString();
    }

    public static void setNick(Player stat, ServerCommandSource source, String newNick){
        stat.updateKey("display-name", StringUtils.codedString(newNick));

        if (stat.latestName.equals(source.getName())) {
            MessagingUtils.sendStatUserMessage(stat, source, MessageConfUtils.cNickSelf.replace("%nick%", stat.displayName), false);
        } else {
            MessagingUtils.sendStatUserMessage(stat, source, MessageConfUtils.cNickOthers
                    .replace("%player%", stat.latestName)
                    .replace("%nick%", stat.displayName), false);
        }
    }

    public static void addStat(Player stat){
        stats.add(stat);
    }

    public static void removeStat(Player stat){
        try {
            stat.saveInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stats.remove(stat);
    }

    public static String noStat = mess.getString("stats.no-stat");
    public static String noPerm = mess.getString("stats.no-permission");
    public static String create = mess.getString("stats.create");
    public static String info = mess.getString("stats.info");
    // Tags.
    public static final String tagRem = message.getString("commands.btag.remove");
    public static final String tagAdd = message.getString("commands.btag.add");
    public static final String tagListMain = message.getString("commands.btag.list.main");
    public static final String tagListLast = message.getString("commands.btag.list.tags.last");
    public static final String tagListNotLast = message.getString("commands.btag.list.tags.not-last");
}
