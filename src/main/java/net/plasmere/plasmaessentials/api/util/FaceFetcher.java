package net.plasmere.plasmaessentials.api.util;
import net.plasmere.plasmaessentials.created.players.Player;

public class FaceFetcher {
    public static String getFaceAvatarURL(Player player){
        return "https://minotar.net/avatar/" + player.latestName + "/1280.png";
    }

    public static String getFaceAvatarURL(String player){
        return "https://minotar.net/avatar/" + player + "/1280.png";
    }
}
