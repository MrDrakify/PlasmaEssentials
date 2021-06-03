package net.plasmere.plasmaessentials;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.luckperms.api.LuckPerms;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.plasmere.plasmaessentials.api.holders.LPHolder;
import net.plasmere.plasmaessentials.commands.*;
import net.plasmere.plasmaessentials.config.ConfigHandler;
import net.plasmere.plasmaessentials.created.runnables.OneSecondTimer;
import net.plasmere.plasmaessentials.events.Events;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlasmaEssentials implements ModInitializer {
    private static PlasmaEssentials singletonInst;
    private ConfigHandler configHandler;
    private Logger logger;
    private MinecraftServer server;
    private File plDir;
    private Events events;
    private ModContainer container;

    public ScheduledExecutorService secondScheduled = Executors.newSingleThreadScheduledExecutor();

    public static LPHolder lpHolder;

    public static PlasmaEssentials getInstance() {
        return singletonInst;
    }

    public ConfigHandler getConfigHandler(){
        return configHandler;
    }

    public Logger getLogger(){ return logger; }

    public MinecraftServer getServer() { return server; }

    public LuckPerms getApi(){ return lpHolder.getApi(); }

    public InputStream getResourceAsStream(final String path){
        return PlasmaEssentials.class.getClassLoader().getResourceAsStream(path);
    }

    public File getDataFolder(){
        File dataFoler = new File(System.getProperty("user.dir"), File.separator + "mods" + File.separator + "PlasmaEssentials" + File.separator);
        if (! dataFoler.exists()) if (! dataFoler.mkdir()) this.getLogger().warn("Cannot make the data folder...");
        return dataFoler;
    }

    public File getPlDir(){
        if (! plDir.exists()) if (! plDir.mkdir()) this.getLogger().warn("Cannot make the Players folder...");
        return this.plDir;
    }

    public Events getEvents() { return events; }

    public void initRunnables(){
        secondScheduled.scheduleAtFixedRate(new OneSecondTimer(), 0, 1, TimeUnit.SECONDS);
    }

    public void initCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        FlyCommand fly = new FlyCommand();
        fly.register(dispatcher);
        GlowCommand glow = new GlowCommand();
        glow.register(dispatcher);
        StatsCommand stats = new StatsCommand();
        stats.register(dispatcher);
        STagCommand stag = new STagCommand();
        stag.register(dispatcher);
        NickCommand nick = new NickCommand();
        nick.register(dispatcher);
    }

    @Override
    public void onInitialize() {
        singletonInst = this;
        this.logger = LogManager.getLogger("PlasmaEssentials");
        this.container = FabricLoader.INSTANCE.getModContainer("plasmaessentials").get();

        lpHolder = new LPHolder();

        this.configHandler = new ConfigHandler();

        this.plDir = new File(getDataFolder() + File.separator + "players" + File.separator);
        this.events = new Events();

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            this.server = server;
            initRunnables();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            secondScheduled.shutdown();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                initCommands(dispatcher)
        );
    }
}
