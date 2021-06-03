package net.plasmere.plasmaessentials.config;

import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.conf.Configuration;

import java.util.List;


public class ConfigUtils {
    private static Configuration config = ConfigHandler.getConf();

    public static void updateConfig(Configuration conf){
        config = conf;
    }

    public static String version = config.getString("version");

    public static String permMan = config.getString("permission-manager");

    public static boolean features(String key){
        return config.getBoolean("features." + key);
    }

    // Players:
    public static List<String> tagsDefaults = config.getStringList("players.default-tag");
    public static boolean tellNew = config.getBoolean("players.tell-new");
    public static boolean updateNames = config.getBoolean("players.update-names");
    public static int pointsDefault = config.getInt("players.points.default");

    // Commands:
    public static String cStatsBase = config.getString("commands.stats.base");
    public static String cStatsPerm = config.getString("commands.stats.permission");
    public static List<String> cStatsAliases = config.getStringList("commands.stats.aliases");
    public static String cStatsPermO = config.getString("commands.stats.other");

    public static String cSTagBase = config.getString("commands.stag.base");
    public static String cSTagPerm = config.getString("commands.stag.permission");
    public static List<String> cSTagAliases = config.getStringList("commands.stag.aliases");
    public static String cSTagPermO = config.getString("commands.stag.other");

    public static String cFlyBase = config.getString("commands.fly.base");
    public static String cFlyPerm = config.getString("commands.fly.permission");
    public static List<String> cFlyAliases = config.getStringList("commands.fly.aliases");
    public static String cFlyPermO = config.getString("commands.fly.other");

    public static String cGlowBase = config.getString("commands.glow.base");
    public static String cGlowPerm = config.getString("commands.glow.permission");
    public static List<String> cGlowAliases = config.getStringList("commands.glow.aliases");
    public static String cGlowPermO = config.getString("commands.glow.other");

    public static String cNickBase = config.getString("commands.nick.base");
    public static String cNickPerm = config.getString("commands.nick.permission");
    public static List<String> cNickAliases = config.getStringList("commands.nick.aliases");
    public static String cNickPermO = config.getString("commands.nick.other");

    // Chat.
    public static boolean kickIllChars = config.getBoolean("chat.kick-illegal-chars");
}
