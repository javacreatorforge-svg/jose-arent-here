package com.redstonedev.josearenthere.entity;

import com.redstonedev.josearenthere.init.ModSounds;
import com.redstonedev.josearenthere.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class JoseEntity extends Monster {

    public static final int DORMANT = 0, STARING = 1, AGGRESSIVE = 2;
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(JoseEntity.class, EntityDataSerializers.INT);

    private int stareTicks = 0;
    private int ambientCooldown;
    private boolean stalking;
    private boolean pendingDespawn = false;
    private int wanderCooldown = 0;

    public JoseEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.maxUpStep = 1.0F;
        this.stalking = this.random.nextFloat() < 0.7F; // mostly stalks
        this.ambientCooldown = 300 + this.random.nextInt(500); // never plays on spawn
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, DORMANT);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        // No random-stroll goal - it was pulling him away from the player mid-chase.
    }

    public int getState() { return this.entityData.get(DATA_STATE); }
    public void setState(int s) { this.entityData.set(DATA_STATE, s); }

    @Override
    public void aiStep() {
        if (!this.level.isClientSide && pendingDespawn) { this.discard(); return; }
        super.aiStep();
        if (this.level.isClientSide) return;

        if (ambientCooldown > 0) ambientCooldown--;
        if (ambientCooldown <= 0) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.AMBIENT.get(this.random.nextInt(ModSounds.AMBIENT.size())).get(),
                    SoundSource.HOSTILE, 0.9F, 1.0F);
            ambientCooldown = 500 + this.random.nextInt(900);
        }

        Player player = nearestVisiblePlayer();
        int state = getState();

        if (state == DORMANT) {
            if (stalking && player != null && this.tickCount % 20 == 0) {
                double d = this.distanceTo(player);
                if (d > 7.0D) this.getNavigation().moveTo(player, 0.8D);
                else this.getNavigation().stop();
            } else if (!stalking) {
                // Wander around (manual, so no goal pulls him off a chase later).
                if (wanderCooldown > 0) wanderCooldown--;
                if (wanderCooldown <= 0 && this.getNavigation().isDone()) {
                    double ang = this.random.nextDouble() * Math.PI * 2;
                    double dist = 6 + this.random.nextInt(6);
                    this.getNavigation().moveTo(this.getX() + Math.cos(ang) * dist, this.getY(),
                            this.getZ() + Math.sin(ang) * dist, 0.8D);
                    wanderCooldown = 60 + this.random.nextInt(80);
                }
            }
            if (player != null && (canSee(player) || isPlayerLookingAt(player))) {
                setState(STARING);
                stareTicks = 60; // 3 seconds
            }
        } else if (state == STARING) {
            if (player == null) { setState(DORMANT); return; }
            this.getNavigation().stop();
            lookAt(player);
            stareTicks--;
            if (stareTicks <= 0) setState(AGGRESSIVE);
        } else { // AGGRESSIVE - relentlessly chase, never back off
            if (player != null) {
                this.setTarget(player);
                if (this.tickCount % 10 == 0) this.getNavigation().moveTo(player, 1.1D);
                breakBlocksAhead(player);
                if (this.tickCount % 40 == 0 && this.distanceTo(player) < 14.0D) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
                }
            }
        }
    }

    private Player nearestVisiblePlayer() {
        Player best = null;
        double bestSq = 48.0D * 48.0D;
        for (int i = 0; i < this.level.players().size(); i++) {
            Player p = this.level.players().get(i);
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bestSq) { bestSq = d; best = p; }
        }
        return best;
    }

    private boolean canSee(Player p) {
        if (this.distanceTo(p) > 28.0D) return false;
        if (!this.hasLineOfSight(p)) return false;
        Vec3 toPlayer = p.position().subtract(this.position()).normalize();
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
        return (toPlayer.x * facing.x + toPlayer.z * facing.z) > 0.2D;
    }

    private boolean isPlayerLookingAt(Player p) {
        if (this.distanceTo(p) > 40.0D) return false;
        if (!p.hasLineOfSight(this)) return false;
        double dx = this.getX() - p.getX();
        double dy = this.getEyeY() - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001D) return false;
        dx /= len; dy /= len; dz /= len;
        Vec3 look = p.getViewVector(1.0F);
        return (look.x * dx + look.y * dy + look.z * dz) > 0.9D;
    }

    private void lookAt(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        this.setYBodyRot(yaw); this.setYHeadRot(yaw); this.setYRot(yaw);
    }

    private void breakBlocksAhead(Player target) {
        Vec3 dir = target.position().subtract(this.position());
        int ox = (int) Math.signum(dir.x);
        int oz = (int) Math.signum(dir.z);
        BlockPos base = this.blockPosition();
        int broken = 0;
        for (int dx = -1; dx <= 1 && broken < 18; dx++)
            for (int dz = -1; dz <= 1 && broken < 18; dz++)
                for (int dy = 0; dy <= 2 && broken < 18; dy++) {
                    BlockPos p = base.offset(dx + ox, dy, dz + oz);
                    if (!this.level.getBlockState(p).isAir()
                            && this.level.getBlockState(p).getDestroySpeed(this.level, p) >= 0) {
                        if (this.level.destroyBlock(p, false)) broken++;
                    }
                }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        boolean ok = super.doHurtTarget(target);
        if (ok && target instanceof Player) {
            if (target instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) target;
                PacketHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                        new PacketHandler.JumpscarePacket(100));
            }
            pendingDespawn = true; // vanish right after the jumpscare
        }
        return ok;
    }

    // Tanky horror: immune to non-netherite melee, projectiles, fire, suffocation.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) return false;
        if (source == DamageSource.IN_WALL || source == DamageSource.DROWN) return false;
        if (source.isProjectile()) return false;
        Entity direct = source.getEntity();
        if (direct instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) direct;
            boolean netherite = attacker.getMainHandItem().getItem() == Items.NETHERITE_SWORD
                    || attacker.getMainHandItem().getItem() == Items.NETHERITE_AXE;
            if (!netherite) return false;
        }
        return super.hurt(source, amount);
    }

    @Override protected float getSoundVolume() { return 0.9F; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        tag.putBoolean("Stalking", stalking);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
        stalking = tag.getBoolean("Stalking");
    }
}
