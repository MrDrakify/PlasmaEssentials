package net.plasmere.plasmaessentials.created.players;

import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.UUIDFetcher;
import net.plasmere.plasmaessentials.config.ConfigUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Player {
    private TreeMap<String, String> info = new TreeMap<>();
    private final File folder = PlasmaEssentials.getInstance().getPlDir();

    public ServerPlayerEntity player;

    public File file;
    public UUID uuid;
    public int xp;
    public int lvl;
    public int playSeconds;
    public String ips;
    public String names;
    public String latestIP;
    public String latestName;
    public List<String> ipList;
    public List<String> nameList;
    public boolean online;
    public String displayName;
    public String guild;
    public boolean sspy;
    public boolean gspy;
    public boolean pspy;
    public boolean sc;
    public String tags;
    public List<String> tagList;
    public int points;
    public String lastMessengerUUID;
    public String lastToUUID;
    public String lastMessage;
    public String lastToMessage;
    public String ignoreds;
    public List<String> ignoredList;
    public boolean muted;
    public Date mutedTill;
    public String friends;
    public List<String> friendList;
    public String pendingToFriends;
    public List<String> pendingToFriendList;
    public String pendingFromFriends;
    public List<String> pendingFromFriendList;

    public List<String> savedKeys = new ArrayList<>();

    public Player(ServerPlayerEntity player) {
        this.player = player;

        String ipSt = player.getIp().replace("/", "");
        String[] ipSplit = ipSt.split(":");
        ipSt = ipSplit[0];

        this.uuid = player.getUuid();
        this.latestIP = ipSt;
        this.latestName = player.getName().asString();

        this.ips = ipSt;
        this.names = player.getName().asString();
        this.online = true;
        this.displayName = player.getDisplayName().asString();
        this.tagList = ConfigUtils.tagsDefaults;

        construct(player.getUuid(), true);
    }

    public Player(ServerPlayerEntity player, boolean create){
        this.player = player;

        String ipSt = player.getIp().replace("/", "");
        String[] ipSplit = ipSt.split(":");
        ipSt = ipSplit[0];

        this.uuid = player.getUuid();
        this.latestIP = ipSt;
        this.latestName = player.getName().asString();

        this.ips = ipSt;
        this.names = player.getName().asString();
        this.online = true;
        this.displayName = player.getDisplayName().asString();
        this.tagList = ConfigUtils.tagsDefaults;

        construct(player.getUuid(), create);
    }

    public Player(String thing){
        createCheck(thing);
    }

    public Player(UUID uuid) {
        createCheck(uuid.toString());
    }

    public void createCheck(String thing){
        if (thing.contains("-")){
            construct(UUID.fromString(thing), false);
            this.online = onlineCheck();
        } else {
            construct(Objects.requireNonNull(UUIDFetcher.getCachedUUID(thing)), false);
            this.online = onlineCheck();
        }
    }

    public boolean onlineCheck(){
        for (ServerPlayerEntity p : PlasmaEssentials.getInstance().getServer().getPlayerManager().getPlayerList()){
            if (p.getName().equals(this.latestName)) return true;
        }

        return false;
    }

    private void construct(UUID uuid, boolean createNew){
        if (uuid == null) return;

        this.uuid = uuid;

        this.file = new File(folder, uuid + ".properties");

        if (createNew) {
            try {
                this.updateWithNewDefaults();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            getFromConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TreeMap<String, String> getInfo() {
        return info;
    }
    public void remKey(String key){
        info.remove(key);
    }
    public String getFromKey(String key){
        return info.get(key);
    }
    public void updateKey(String key, Object value) {
        info.remove(key);
        addKeyValuePair(key, String.valueOf(value));
        loadVars();
    }
    public File getFile() { return file; }

    public boolean hasProperty(String property) {
        for (String info : getInfoAsPropertyList()) {
            if (info.startsWith(property)) return true;
        }

        return false;
    }

    public TreeSet<String> getInfoAsPropertyList() {
        TreeSet<String> infoList = new TreeSet<>();
        List<String> keys = new ArrayList<>();
        for (String key : info.keySet()){
            if (keys.contains(key)) continue;

            infoList.add(key + "=" + getFromKey(key));
            keys.add(key);
        }

        return infoList;
    }

    public String getFullProperty(String key) throws Exception {
        if (hasProperty(key)) {
            return key + "=" + getFromKey(key);
        } else {
            throw new Exception("No property saved!");
        }
    }

    public void flushInfo(){
        this.info = new TreeMap<>();
    }

    public void addKeyValuePair(String key, String value){
        if (info.containsKey(key)) return;

        info.put(key, value);
    }

    public String stringifyList(List<String> list, String splitter){
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i <= list.size(); i++) {
            if (i < list.size()) {
                stringBuilder.append(list.get(i - 1)).append(splitter);
            } else {
                stringBuilder.append(list.get(i - 1));
            }
        }

        return stringBuilder.toString();
    }

    public void getFromConfigFile() throws IOException {
        if (file.exists()){
            Scanner reader = new Scanner(file);

            List<String> keys = new ArrayList<>();
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                while (data.startsWith("#")) {
                    data = reader.nextLine();
                }
                String[] dataSplit = data.split("=", 2);
                if (keys.contains(dataSplit[0])) continue;
                keys.add(dataSplit[0]);
                addKeyValuePair(tryUpdateFormat(dataSplit[0]), dataSplit[1]);
            }

            reader.close();

            if (needUpdate()) {
                updateWithNewDefaults();
            }

            loadVars();
        }
    }

    public boolean needUpdate() {
        if (info.size() != propertiesDefaults().size()) return true;

        int i = 0;
        for (String p : getInfoAsPropertyList()) {
            if (! p.startsWith(propertiesDefaults().get(i).split("=", 2)[0])) return true;
            i++;
        }

        return false;
    }

    public void updateWithNewDefaults() throws IOException {
        file.delete();

        file.createNewFile();

        FileWriter writer = new FileWriter(file);

        savedKeys = new ArrayList<>();

        for (String p : propertiesDefaults()) {
            String key = p.split("=", 2)[0];
            if (savedKeys.contains(key)) continue;
            savedKeys.add(key);

            String[] propSplit = p.split("=", 2);

            String property = propSplit[0];

            String write = "";
            try {
                write = getFullProperty(property);
            } catch (Exception e) {
                write = p;
            }

            writer.write(tryUpdateFormatRaw(write) + "\n");
        }

        writer.close();

        flushInfo();

        Scanner reader = new Scanner(file);

        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            while (data.startsWith("#")) {
                data = reader.nextLine();
            }
            String[] dataSplit = data.split("=", 2);
            addKeyValuePair(dataSplit[0], dataSplit[1]);
        }

        reader.close();

        loadVars();
    }

    public List<String> propertiesDefaults() {
        List<String> defaults = new ArrayList<>();
        defaults.add("uuid=" + uuid);
        defaults.add("ips=" + ips);
        defaults.add("names=" + names);
        defaults.add("latest-ip=" + latestIP);
        defaults.add("latest-name=" + latestName);
        defaults.add("xp=0");
        defaults.add("lvl=1");
        defaults.add("playtime=0");
        defaults.add("display-name=" + displayName);
        defaults.add("guild=");
        defaults.add("sspy=true");
        defaults.add("gspy=true");
        defaults.add("pspy=true");
        defaults.add("sc=true");
        defaults.add("tags=" + defaultTags());
        defaults.add("points=" + ConfigUtils.pointsDefault);
        defaults.add("last-messenger=");
        defaults.add("last-to=");
        defaults.add("last-message=");
        defaults.add("last-to-message=");
        defaults.add("ignored=");
        defaults.add("muted=false");
        defaults.add("muted-till=");
        defaults.add("friends=");
        defaults.add("pending-to-friends=");
        defaults.add("pending-from-friends=");
        //defaults.add("");
        return defaults;
    }

    public String defaultTags(){
        StringBuilder stringBuilder = new StringBuilder();

        int i = 1;
        for (String tag : ConfigUtils.tagsDefaults) {
            if (tag == null) continue;
            if (tag.equals("")) continue;
            if (i != tag.length()) {
                stringBuilder.append(tag).append(",");
            } else {
                stringBuilder.append(tag);
            }
            i++;
        }

        return stringBuilder.toString();
    }

    public void loadVars(){
        // StreamLine.getInstance().getLogger().info("UUID : " + getFromKey("uuid"));

        this.uuid = UUID.fromString(getFromKey("uuid"));
        this.ips = getFromKey("ips");
        this.names = getFromKey("names");
        this.latestIP = getFromKey("latest-ip");
        this.latestName = getFromKey("latest-name");
        this.ipList = loadIPs();
        this.nameList = loadNames();
        this.xp = Integer.parseInt(getFromKey("xp"));
        this.lvl = Integer.parseInt(getFromKey("lvl"));
        this.playSeconds = Integer.parseInt(getFromKey("playtime"));
        this.displayName = getFromKey("display-name");
        this.guild = getFromKey("guild");
        this.online = onlineCheck();
        this.sspy = Boolean.parseBoolean(getFromKey("sspy"));
        this.gspy = Boolean.parseBoolean(getFromKey("gspy"));
        this.pspy = Boolean.parseBoolean(getFromKey("pspy"));
        this.sc = Boolean.parseBoolean(getFromKey("sc"));
        this.tagList = loadTags();
        this.points = Integer.parseInt(getFromKey("points"));
        this.lastMessengerUUID = getFromKey("last-messenger");
        this.lastToUUID = getFromKey("last-to");
        this.lastMessage = getFromKey("last-message");
        this.lastToMessage = getFromKey("last-to-message");
        this.ignoredList = loadIgnored();
        this.muted = Boolean.parseBoolean(getFromKey("muted"));
        try {
            this.mutedTill = new Date(Long.parseLong(getFromKey("muted-till")));
        } catch (Exception e) {
            this.mutedTill = null;
        }
        this.friendList = loadFriends();
        this.pendingToFriendList = loadPendingToFriends();
        this.pendingFromFriendList = loadPendingFromFriends();
    }

    public TreeMap<String, String> updatableKeys() {
        TreeMap<String, String> thing = new TreeMap<>();

        thing.put("latestip", "latest-ip");
        thing.put("latestname", "latest-name");
        thing.put("displayname", "display-name");
        thing.put("latestversion", "latest-version");

        return thing;
    }

    public String tryUpdateFormat(String from){
        for (String key : updatableKeys().keySet()) {
            if (! from.equals(key)) continue;

            return updatableKeys().get(key);
        }

        return from;
    }

    public String tryUpdateFormatRaw(String from){
        String[] fromSplit = from.split("=", 2);

        return tryUpdateFormat(fromSplit[0]) + "=" + fromSplit[1];
    }

    public void tryAddNewName(String name){
        if (nameList.contains(name)) return;

        this.nameList.add(name);

        this.names = stringifyList(nameList, ",");

        updateKey("names", this.names);
    }

    public void tryAddNewTag(String tag){
        if (tagList.contains(tag)) return;

        this.tagList.add(tag);

        this.tags = stringifyList(tagList, ",");

        updateKey("tags", this.tags);
    }

    public void tryRemTag(String tag){
        if (! tagList.contains(tag)) return;

        this.tagList.remove(tag);

        this.tags = stringifyList(tagList, ",");

        updateKey("tags", this.tags);
    }

    public void tryAddNewIgnored(String uuid){
        if (ignoredList.contains(uuid)) return;

        this.ignoredList.add(uuid);

        this.ignoreds = stringifyList(ignoredList, ",");

        updateKey("ignored", this.ignoreds);
    }

    public void tryRemIgnored(String uuid){
        if (! ignoredList.contains(uuid)) return;

        this.ignoredList.remove(uuid);

        this.ignoreds = stringifyList(ignoredList, ",");

        updateKey("ignored", this.ignoreds);
    }

    public void tryAddNewFriend(String uuid){
        tryRemPendingToFriend(uuid);
        tryRemPendingFromFriend(uuid);

        if (friendList.contains(uuid)) return;

        this.friendList.add(uuid);

        this.friends = stringifyList(friendList, ",");

        updateKey("friends", this.friends);
    }

    public void tryRemFriend(String uuid){
        if (friendList.contains(uuid)) return;

        this.friendList.remove(uuid);

        this.friends = stringifyList(friendList, ",");

        updateKey("friends", this.friends);
    }

    public void tryAddNewPendingToFriend(String uuid){
        if (pendingToFriendList.contains(uuid)) return;

        this.pendingToFriendList.add(uuid);

        this.pendingToFriends = stringifyList(pendingToFriendList, ",");

        updateKey("pending-to-friends", this.pendingToFriends);
    }

    public void tryRemPendingToFriend(String uuid){
        if (pendingToFriendList.contains(uuid)) return;

        this.pendingToFriendList.remove(uuid);

        this.pendingToFriends = stringifyList(pendingToFriendList, ",");

        updateKey("pending-to-friends", this.pendingToFriends);
    }

    public void tryAddNewPendingFromFriend(String uuid){
        if (pendingFromFriendList.contains(uuid)) return;

        this.pendingFromFriendList.add(uuid);

        this.pendingFromFriends = stringifyList(pendingFromFriendList, ",");

        updateKey("pending-from-friends", this.pendingFromFriends);
    }

    public void tryRemPendingFromFriend(String uuid){
        if (pendingFromFriendList.contains(uuid)) return;

        this.pendingFromFriendList.remove(uuid);

        this.pendingFromFriends = stringifyList(pendingFromFriendList, ",");

        updateKey("pending-from-friends", this.pendingFromFriends);
    }

    public void tryAddNewIP(String ip){
        if (ipList.contains(ip)) return;

        this.ipList.add(ip);

        this.ips = stringifyList(ipList, ",");

        updateKey("ips", this.ips);
    }

    public void tryAddNewIP(ServerPlayerEntity player){
        String ipSt = player.getIp().replace("/", "");
        String[] ipSplit = ipSt.split(":");
        ipSt = ipSplit[0];

        tryAddNewIP(ipSt);
    }

    public void addPlaySecond(int amount){
        updateKey("playtime", playSeconds + amount);
    }

    public void setPlaySeconds(int amount){
        updateKey("playtime", amount);
    }

    public double getPlayDays(){
        return playSeconds / (60.0d * 60.0d * 24.0d);
    }

    public double getPlayHours(){
        return playSeconds / (60.0d * 60.0d);
    }

    public double getPlayMinutes(){
        return playSeconds / (60.0d);
    }

    public List<String> loadTags(){
        List<String> thing = new ArrayList<>();

        String search = "tags";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadIPs(){
        List<String> thing = new ArrayList<>();

        String search = "ips";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadNames(){
        List<String> thing = new ArrayList<>();

        String search = "names";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadIgnored(){
        List<String> thing = new ArrayList<>();

        String search = "ignored";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadFriends(){
        List<String> thing = new ArrayList<>();

        String search = "friends";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadPendingToFriends(){
        List<String> thing = new ArrayList<>();

        String search = "pending-to-friends";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public List<String> loadPendingFromFriends(){
        List<String> thing = new ArrayList<>();

        String search = "pending-from-friends";

        try {
            if (getFromKey(search).equals("") || getFromKey(search) == null) return thing;
            if (! getFromKey(search).contains(",")) {
                thing.add(getFromKey(search));
                return thing;
            }

            for (String t : getFromKey(search).split(",")) {
                try {
                    thing.add(t);
                } catch (Exception e) {
                    //continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return thing;
    }

    public void saveInfo() throws IOException {
        file.delete();

        file.createNewFile();

        savedKeys = new ArrayList<>();
        FileWriter writer = new FileWriter(file);
        for (String s : getInfoAsPropertyList()){
            String key = s.split("=")[0];
            if (savedKeys.contains(key)) continue;
            savedKeys.add(key);

            writer.write(tryUpdateFormatRaw(s) + "\n");
        }
        writer.close();

        //StreamLine.getInstance().getLogger().info("Just saved Player info for player: " + PlayerUtils.getOffOnReg(player));
    }

    /*
   Experience required =
   2 × current_level + 7 (for levels 0–15)
   5 × current_level – 38 (for levels 16–30)
   9 × current_level – 158 (for levels 31+)
    */

    public int getNeededXp(){
        int needed = 0;

        needed = 2500 + (2500 * lvl);

        return needed;
    }

    public int xpUntilNextLevel(){
        return getNeededXp() - this.xp;
    }

    public void addXp(int amount){
        int setAmount = this.xp + amount;

        while (setAmount >= getNeededXp()) {
            setAmount -= getNeededXp();
            int setLevel = this.lvl + 1;
            updateKey("lvl", setLevel);
        }

        updateKey("xp", setAmount);
    }

    public void setXp(int amount){
        int setAmount = amount;

        while (setAmount >= getNeededXp()) {
            setAmount -= getNeededXp();
            int setLevel = this.lvl + 1;
            updateKey("lvl", setLevel);
        }

        updateKey("xp", setAmount);
    }

    public void dispose() throws Throwable {
        try {
            this.uuid = null;
        } finally {
            super.finalize();
        }
    }

    public void setSSPY(boolean value) {
        sspy = value;
        updateKey("sspy", value);
    }

    public void toggleSSPY() { setSSPY(! sspy); }

    public void setGSPY(boolean value) {
        gspy = value;
        updateKey("gspy", value);
    }

    public void toggleGSPY() { setGSPY(! gspy); }

    public void setPSPY(boolean value) {
        pspy = value;
        updateKey("pspy", value);
    }

    public void togglePSPY() { setPSPY(! pspy); }

    public void setSC(boolean value) {
        sc = value;
        updateKey("sc", value);
    }

    public void toggleSC() { setSC(! sc); }

    public void setMuted(boolean value) {
        muted = value;
        updateKey("muted", value);
    }

    public void setMutedTill(long value) {
        updateKey("muted-till", value);
    }

    public void removeMutedTill(){
        updateKey("muted-till", "");
    }

    public void updateMute(boolean set, Date newMutedUntil){
        setMuted(set);
        setMutedTill(newMutedUntil.getTime());
    }

    public void toggleMuted() { setMuted(! muted); }

    public void setPoints(int amount) {
        points = amount;
        updateKey("points", amount);
    }

    public void addPoints(int amount) {
        setPoints(points + amount);
    }

    public void remPoints(int amount) {
        setPoints(points - amount);
    }

    public void updateLastMessage(String message){
        updateKey("last-message", message);
    }

    public void updateLastToMessage(String message){
        updateKey("last-to-message", message);
    }

    public void updateLastMessenger(Player messenger){
        updateKey("last-messenger", messenger.uuid);
    }

    public void updateLastTo(Player to){
        updateKey("last-to", to.uuid);
    }

    public String toString(){
        return latestName;
    }
}
