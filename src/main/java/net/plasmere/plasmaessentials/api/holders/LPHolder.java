package net.plasmere.plasmaessentials.api.holders;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.plasmere.plasmaessentials.PlasmaEssentials;

public class LPHolder {
    public LuckPerms api;
    public boolean enabled;

    public LPHolder(){
        enabled = isPresent();
    }

    public boolean isPresent(){
        try {
            api = LuckPermsProvider.get();
            return true;
        } catch (Exception e) {
            PlasmaEssentials.getInstance().getLogger().error("LuckPerms not loaded... Disabling LuckPerms support...");
        }
        return false;
    }

    public LuckPerms getApi(){
        if (api != null) return api;

        if (isPresent()) {
            api = LuckPermsProvider.get();

            return api;
        }

        return null;
    }
}
