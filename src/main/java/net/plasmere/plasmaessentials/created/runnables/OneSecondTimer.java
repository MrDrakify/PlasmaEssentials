package net.plasmere.plasmaessentials.created.runnables;

import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.config.ConfigUtils;
import net.plasmere.plasmaessentials.created.players.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class OneSecondTimer implements Runnable {
    public int countdown;
    public int reset;

    public OneSecondTimer() {
        this.countdown = 1;
        this.reset = 1;
    }

    @Override
    public void run() {
        countdown--;
        if (countdown == 0) if (PlasmaEssentials.getInstance().getServer() != null) done();
    }

    public void done(){
        countdown = reset;

        if (PlasmaEssentials.lpHolder.enabled && ConfigUtils.updateNames) {
            for (Player player : PlayerUtils.getStats()) {
                PlayerUtils.updateDisplayName(player);
            }
        }

        try {
            for (ServerPlayerEntity player : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()) {
                Player p = PlayerUtils.getStat(player);

                if (p == null) continue;
                if (! p.online) continue;

                p.addPlaySecond(1);

                p.saveInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            List<Player> players = PlayerUtils.getStats();
            List<Player> toRemove = new ArrayList<>();

            for (Player player : players) {
                if (! player.onlineCheck()) {
                    toRemove.add(player);
                }
            }

            for (Player player : toRemove) {
                player.saveInfo();
                PlayerUtils.removeStat(player);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            for (ServerPlayerEntity playerEntity : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()) {
                boolean found = false;
                for (Player player : PlayerUtils.getStats()) {
                    if (player.latestName.equals(playerEntity.getName().asString())) {
                        found = true;
                        break;
                    }
                }
                if (found) continue;

                PlayerUtils.addStat(new Player(playerEntity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // PlasmaEssentials.getInstance().getLogger().info("1 second passed.");
    }
}

