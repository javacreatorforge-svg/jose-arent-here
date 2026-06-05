package com.redstonedev.josearenthere.event;

import com.redstonedev.josearenthere.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class ForgeEvents {
    private static final Random RNG = new Random();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) return;
        tickCounter++;
        if (tickCounter % 100 != 0) return; // ~5s
        for (ServerLevel level : event.getServer().getAllLevels()) trySpawn(level);
    }

    private boolean joseExists(ServerLevel level) {
        return !level.getEntities(ModEntities.JOSE.get(), e -> e.isAlive()).isEmpty();
    }

    private void trySpawn(ServerLevel level) {
        List<? extends ServerPlayer> players = level.players();
        if (players.isEmpty() || joseExists(level)) return;

        // Only at night - unless it's thundering, then daytime is allowed too.
        boolean night = !level.isDay();
        boolean allowed = night || level.isThundering();
        if (!allowed) return;

        for (ServerPlayer player : players) {
            if (RNG.nextInt(260) != 0) continue;
            BlockPos pos = pickSpawnPos(level, player);
            if (pos == null) continue;
            Mob mob = ModEntities.JOSE.get().create(level);
            if (mob == null) return;
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            level.addFreshEntity(mob);
            // Spawn message - dark red but readable.
            player.sendSystemMessage(Component.literal("Something is lurking........")
                    .withStyle(ChatFormatting.DARK_RED));
            return;
        }
    }

    private BlockPos pickSpawnPos(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = origin.getX() + (RNG.nextInt(32) - 16);
            int z = origin.getZ() + (RNG.nextInt(32) - 16);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos c = new BlockPos(x, y, z);
            boolean floor = !level.getBlockState(c.below()).getCollisionShape(level, c.below()).isEmpty();
            if (floor && level.getBlockState(c).getCollisionShape(level, c).isEmpty()
                    && level.getBlockState(c.above()).getCollisionShape(level, c.above()).isEmpty()) {
                double d = c.distSqr(origin);
                if (d > 64 && d < 1600) return c; // 8-40 blocks away
            }
        }
        return null;
    }
}
