package net.plasmere.plasmaessentials.config;

import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.conf.Configuration;

public class MessageConfUtils {
    private static Configuration mess = ConfigHandler.getMess();

    public static void updateMess(Configuration m){
        mess = m;
    }

    public static String version = mess.getString("version");

    public static String join = mess.getString("join");
    public static String leave = mess.getString("leave");

    public static String chat = mess.getString("chat");

    public static String error = mess.getString("error");
    public static String noPlayer = mess.getString("no-player");
    public static String noPerm = mess.getString("no-perm");
    public static String onlyPlayers = mess.getString("only-players");
    public static String needsMore = mess.getString("needs-more");
    public static String needsLess = mess.getString("needs-less");

    // Toggle.
    public static String togOn = mess.getString("toggle.enable");
    public static String togOff = mess.getString("toggle.disable");

    // Players.
    public static String offline = mess.getString("players.offline");
    public static String online = mess.getString("players.online");

    // Commands:
    public static String cFlySelf = mess.getString("fly.self");
    public static String cFlyOthers = mess.getString("fly.others");

    public static String cGlowSelf = mess.getString("glow.self");
    public static String cGlowOthers = mess.getString("glow.others");

    public static String cNickSelf = mess.getString("nick.self");
    public static String cNickOthers = mess.getString("nick.others");
}
