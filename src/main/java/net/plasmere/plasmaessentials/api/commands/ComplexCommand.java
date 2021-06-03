package net.plasmere.plasmaessentials.api.commands;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import net.plasmere.plasmaessentials.api.util.StringUtils;
import net.plasmere.plasmaessentials.api.util.UUIDFetcher;
import net.plasmere.plasmaessentials.config.ConfigHandler;
import net.plasmere.plasmaessentials.created.players.Player;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class ComplexCommand implements IComplexCommand {
    protected static final transient Logger logger = PlasmaEssentials.getInstance().getLogger();
    private static final DynamicCommandExceptionType PROFILE_RESOLVE_EXCEPTION = new DynamicCommandExceptionType((obj) -> StringUtils.newText("Unexpected error while resolving the requested profile\n" + obj));
    public final String base;
    public ConfigHandler configHandler = PlasmaEssentials.getInstance().getConfigHandler();
    protected transient List<String> aliases;
    protected transient LiteralArgumentBuilder<ServerCommandSource> argumentBuilder;
    protected transient LiteralCommandNode<ServerCommandSource> commandNode;
    protected transient Predicate<ServerCommandSource> PERMISSION_CHECK_ROOT;
    protected transient String permission;
    protected transient String otherPermission;
    protected transient int MIN_OP_LEVEL;
    private transient List<String> usageArgs = null;
    private transient String descriptionId = null;

    public ComplexCommand(final String base){
        this.base = base;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public ComplexCommand(final String base, final List<String> aliaseses){
        this.base = base;
        this.aliases = aliaseses;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public ComplexCommand(final String base, final Predicate<ServerCommandSource> predicate) {
        this.base = base;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public ComplexCommand(final String base, final Predicate<ServerCommandSource> predicate, final List<String> aliases) {
        this.base = base;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.aliases = aliases;
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public ComplexCommand(final String base, final String permission) {
        this.base = base;
        this.PERMISSION_CHECK_ROOT = src -> PlayerUtils.hasPermission(permission, src);
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
    }

    public ComplexCommand(final String base, final String permission, final int minOpLevel) {
        this.base = base;
        this.PERMISSION_CHECK_ROOT = src -> PlayerUtils.hasPermission(permission, minOpLevel, src);
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    public ComplexCommand(final String base, final String permission, final List<String> aliases) {
        this.base = base;
        this.aliases = aliases;
        this.PERMISSION_CHECK_ROOT = src -> PlayerUtils.hasPermission(permission, src);
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.otherPermission = null;
    }

    public ComplexCommand(final String base, final String permission, final String otherPermission, final List<String> aliases) {
        this.base = base;
        this.aliases = aliases;
        this.PERMISSION_CHECK_ROOT = src -> PlayerUtils.hasPermission(permission, src);
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.otherPermission = otherPermission;
    }

    public ComplexCommand(final String base, final String permission, final int minOpLevel, final List<String> aliases) {
        this.base = base;
        this.aliases = aliases;
        this.PERMISSION_CHECK_ROOT = src -> PlayerUtils.hasPermission(permission, minOpLevel, src);
        this.argumentBuilder = this.literal(base).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    public Collection<String> getPlayers(ServerCommandSource source){
        Set<String> players = Sets.newLinkedHashSet();
        players.addAll(source.getPlayerNames());
        return players;
    }

    public PlasmaEssentials getEssentials() { return PlasmaEssentials.getInstance(); }

    public LiteralCommandNode<ServerCommandSource> getCommandNode() { return this.commandNode; }

    public LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilder() { return this.argumentBuilder; }

    public Predicate<ServerCommandSource> getRootPermissionPredicate() { return this.PERMISSION_CHECK_ROOT; }

    public final void withUsage(final String identifier, final String... arguments) {
        this.usageArgs = Arrays.asList(arguments.clone());
        this.descriptionId = identifier;
    }

    public final List<String> getUsageArgs() { return this.usageArgs; }

    public final String getDescriptionId() { return this.descriptionId; }

    public final boolean hasUsage() { return this.usageArgs != null || this.descriptionId != null; }

    public boolean hasPermission(final ServerCommandSource src, final String cmdPerm) {
        return PlayerUtils.hasPermission(cmdPerm, src);
    }

    public boolean hasPermission(final ServerCommandSource src, final String cmdPerm, final int minOpLevel) {
        return PlayerUtils.hasPermission(cmdPerm, minOpLevel, src);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> literal(String base) {
        return CommandManager.literal(base);
    }

    @Override
    public <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String string, ArgumentType<T> argumentType) {
        return CommandManager.argument(base, argumentType);
    }

    @Override
    public Player getPlayerObj(String name) {
        return UUIDFetcher.getPlayer(name);
    }

    @Override
    public String getBase() {
        return this.base;
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    public Player getSourcePlayer(final CommandContext<ServerCommandSource> ctx){
        try {
            return UUIDFetcher.getPlayer(ctx.getSource().getPlayer().getUuid());
        } catch (Exception e) {
            return null;
        }
    }

    public Player getOnlinePlayer(final ServerPlayerEntity player) {
        return UUIDFetcher.getPlayer(player.getUuid());
    }

    public Player getOnlinePlayer(final String name) {
        return UUIDFetcher.getPlayer(name);
    }

    public String getUserArgumentInput(final CommandContext<ServerCommandSource> ctx, final String base) {
        return getString(ctx, base);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> getUserArgument(final String base) {
        return this.argument(base, string()).suggests(PlayerUtils::allUUIDs);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> getOnlineUserArgument(final String base) {
        return this.argument(base, string()).suggests(PlayerUtils::onlineUsers);
    }

    public CompletableFuture<GameProfile> resolveAndGetProfileAsync(final CommandContext<ServerCommandSource> ctx, final String base) throws CommandSyntaxException {
        return CompletableFuture.completedFuture(resolveAndGetProfile(ctx, base));
    }

    public GameProfile resolveAndGetProfile(final CommandContext<ServerCommandSource> ctx, final String base) throws CommandSyntaxException {
        try {
            final String input = ctx.getArgument(base, String.class);
            Matcher idMatcher = StringUtils.UUID_PATTERN.matcher(input);
            if (idMatcher.matches()) {
                UUID uuid = UUID.fromString(input);
                Player player = UUIDFetcher.getPlayer(uuid);

                if (player == null) return null;

                if (player.online) {
                    return player.player.getGameProfile();
                }

                try {
                    String name = UUIDFetcher.getName(input);
                    return new GameProfile(uuid, name);
                } catch (Exception e) {
                    throw PROFILE_RESOLVE_EXCEPTION.create(e.getMessage());
                }
            }

            Player player = UUIDFetcher.getPlayer(input);

            if (player == null) return null;

            if (player.online) {
                return player.player.getGameProfile();
            }

            Matcher nameMatcher = StringUtils.USERNAME_PATTERN.matcher(input);
            if (nameMatcher.matches()) {
                try {
                    String id = UUIDFetcher.getCachedUUID(input).toString();
                    if (id == null) {
                        throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
                    }
                    UUID uuid = UUID.fromString(id);
                    return new GameProfile(uuid, id);
                } catch (Exception e) {
                    throw PROFILE_RESOLVE_EXCEPTION.create(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
    }

    public int sendUsage(CommandContext<ServerCommandSource> ctx, String key, Object... objects) {
        // TODO: Add a way to report the usage to player.
        return AWAIT;
    }
}
