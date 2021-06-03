package net.plasmere.plasmaessentials.events.mixins;

import com.google.common.collect.Lists;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.StringUtils;
import net.plasmere.plasmaessentials.api.util.UUIDFetcher;
import net.plasmere.plasmaessentials.config.MessageConfUtils;
import net.plasmere.plasmaessentials.created.players.Player;
import org.apache.http.util.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Final
    @Shadow
    private List<ServerPlayerEntity> players;

    @Final
    @Shadow
    private MinecraftServer server;

    @Inject(at = @At("HEAD"), method = "onPlayerConnect", cancellable = true)
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo ci) {
        if (PlasmaEssentials.getInstance().getEvents().onConnect(playerEntity, false)) {
            ci.cancel();
        }
    }
}
