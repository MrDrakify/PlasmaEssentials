package net.plasmere.plasmaessentials.config;

import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.conf.Configuration;
import net.plasmere.plasmaessentials.api.conf.ConfigurationProvider;
import net.plasmere.plasmaessentials.api.conf.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ConfigHandler {
    private static Configuration conf;
    private static Configuration oConf;
    private static Configuration mess;
    private static Configuration oMess;
    private final String configVer = "2";
    private final String messagesVer = "1";

    private final PlasmaEssentials inst = PlasmaEssentials.getInstance();
    private final File cfile = new File(inst.getDataFolder() + File.separator + "config.yml");
    private final File mfile = new File(inst.getDataFolder()+ File.separator + "messages.yml");

    private static ConfigHandler singletonInst;

    public static ConfigHandler getInstance() {
        return singletonInst;
    }

    public String getPath(String fileName) {
        if (fileName.endsWith(".yml")) {
            return "saves/" + fileName;
        } else {
            return "saves/" + fileName + ".yml";
        }
    }

    public ConfigHandler(){
        singletonInst = this;

        if (! this.inst.getDataFolder().exists()) {
            if (this.inst.getDataFolder().mkdir()) {
                this.inst.getLogger().info("Made folder: " + this.inst.getDataFolder().getName());
            }
        }

        this.conf = this.loadConf();
        this.mess = this.loadMess();
    }

    public static Configuration getConf() { return conf; }
    public static Configuration getMess() { return mess; }
    public static Configuration getoConf() { return oConf; }
    public static Configuration getoMess() { return oMess; }

    public void reloadConfig(){
        try {
            this.conf = loadConf();

            ConfigUtils.updateConfig(this.conf);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void reloadMessages(){
        try {
            this.mess = loadMess();

            MessageConfUtils.updateMess(this.mess);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Configuration loadConf(){
        if (! this.cfile.exists()){
            try	{
                InputStream in = this.inst.getResourceAsStream(getPath("config.yml"));
                if (this.cfile.isDirectory()) throw new Exception("Config to is Directory!");
                Files.copy(Objects.requireNonNull(in), this.cfile.toPath());
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        try {
            this.conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(inst.getDataFolder(), "config.yml"));

            if (! this.configVer.equals(ConfigUtils.version)){
                this.conf = this.iterateConfigs("oldconfig.yml");

                this.inst.getLogger().error("----------------------------------------------------------");
                this.inst.getLogger().error("YOU NEED TO UPDATE THE VALUES IN YOUR NEW CONFIG FILE AS");
                this.inst.getLogger().error("YOUR OLD ONE WAS OUTDATED. I IMPORTED THE NEW ONE FOR YOU.");
                this.inst.getLogger().error("----------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.inst.getLogger().info("Loaded configuration!");

        return this.conf;
    }

    public Configuration loadMess(){
        if (! this.mfile.exists()){
            try	{
                InputStream in = this.inst.getResourceAsStream(getPath("messages.yml"));
                if (this.mfile.isDirectory()) throw new Exception("Messages to is Directory!");
                Files.copy(Objects.requireNonNull(in), this.mfile.toPath());
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        try {
            this.mess = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(inst.getDataFolder(), "messages.yml"));

            if (! this.messagesVer.equals(MessageConfUtils.version)){
                this.mess = this.iterateMessagesConf("oldmessages.yml");

                this.inst.getLogger().error("----------------------------------------------------------");
                this.inst.getLogger().error("YOU NEED TO UPDATE THE VALUES IN YOUR NEW MESSAGES FILE AS");
                this.inst.getLogger().error("YOUR OLD ONE WAS OUTDATED. I IMPORTED THE NEW ONE FOR YOU.");
                this.inst.getLogger().error("----------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.inst.getLogger().info("Loaded messages!");

        return this.mess;
    }

    private Configuration iterateConfigs(String old) {
        File oldfile = new File(this.inst.getDataFolder(), old);
        if (oldfile.exists()) {
            this.iterateConfigs("new" + old);
        } else {
            try	{
                InputStream in = this.inst.getResourceAsStream(getPath("config.yml"));
                if (this.cfile.isDirectory()) throw new Exception("Config to is Directory!");
                Files.copy(Objects.requireNonNull(in), this.cfile.toPath());
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

            this.oConf = this.conf;

            try {
                this.conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cfile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.conf;
    }

    private Configuration iterateMessagesConf(String old) {
        File oldfile = new File(this.inst.getDataFolder(), old);
        if (oldfile.exists()) {
            this.iterateMessagesConf("new" + old);
        } else {
            try	{
                InputStream in = this.inst.getResourceAsStream(getPath("messages.yml"));
                if (this.mfile.isDirectory()) throw new Exception("Messages to is Directory!");
                Files.copy(Objects.requireNonNull(in), this.mfile.toPath());
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

            this.oMess = this.mess;

            try {
                this.mess = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mfile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.mess;
    }
}
