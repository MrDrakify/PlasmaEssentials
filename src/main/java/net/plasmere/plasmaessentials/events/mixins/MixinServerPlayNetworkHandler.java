package net.plasmere.plasmaessentials.events.mixins;

import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.util.PlayerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At(value = "HEAD"), method = "onDisconnected", cancellable = true)
    private void onDisconnect(Text text, CallbackInfo ci) {
//        LOGGER.info("{} lost connection: {}", this.player.getName().getString(), reason.getString());
//        this.server.forcePlayerSampleUpdate();
//        this.server.getPlayerManager().broadcastChatMessage((new TranslatableText("multiplayer.player.left", new Object[]{this.player.getDisplayName()})).formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID);
//        this.player.onDisconnect();
//        this.server.getPlayerManager().remove(this.player);
//        this.player.getTextStream().onDisconnect();
//        if (this.isHost()) {
//            LOGGER.info("Stopping singleplayer server as player logged out");
//            this.server.stop(false);
//        }

        if (PlasmaEssentials.getInstance().getEvents().onDisconnect(player, false)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onGameMessage", cancellable = true,
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V"
            )
    )
    private void onChat(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci){
        if (PlasmaEssentials.getInstance().getEvents().onChat(PlayerUtils.getOrCreate(this.player.getUuid()), chatMessageC2SPacket, true)) {
            ci.cancel();
        }
    }
}
