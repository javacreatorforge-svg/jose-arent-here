package com.redstonedev.josearenthere.network;

import com.redstonedev.josearenthere.JoseArentHere;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JoseArentHere.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static int nextId = 0;

    public static void register() {
        CHANNEL.registerMessage(nextId++, JumpscarePacket.class,
                JumpscarePacket::encode, JumpscarePacket::decode, JumpscarePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static class JumpscarePacket {
        public final int ticks;
        public JumpscarePacket(int ticks) { this.ticks = ticks; }
        public static void encode(JumpscarePacket p, FriendlyByteBuf b) { b.writeInt(p.ticks); }
        public static JumpscarePacket decode(FriendlyByteBuf b) { return new JumpscarePacket(b.readInt()); }
        public static void handle(JumpscarePacket p, Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> com.redstonedev.josearenthere.client.overlay.JumpscareState.trigger(p.ticks)));
            ctx.get().setPacketHandled(true);
        }
    }
}
