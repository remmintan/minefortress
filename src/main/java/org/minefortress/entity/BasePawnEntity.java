package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.dtos.PawnSkin;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IPawnSkinnable;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.FortressSlimeEntity;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class BasePawnEntity extends HungryEntity implements IFortressAwareEntity, IPawnSkinnable {

    public static final String FORTRESS_CENTER_BLOCK_KEY = "fortress_center_block";
    public static final String PAWN_SKIN_NBT_KEY = "pawn_skin";
    private static final String BODY_TEXTURE_ID_NBT_KEY = "body_texture_id";

    private static final TrackedData<Optional<BlockPos>> FORTRESS_CENTER = DataTracker.registerData(BasePawnEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Integer> BODY_TEXTURE_ID = DataTracker.registerData(BasePawnEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<PawnSkin> PAWN_SKIN = DataTracker.registerData(BasePawnEntity.class, MineFortressMod.PAWN_SKIN_TRACKED_DATA_HANDLER);

    protected BasePawnEntity(EntityType<? extends BasePawnEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world, enableHunger);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FORTRESS_CENTER, Optional.empty());
        this.dataTracker.startTracking(BODY_TEXTURE_ID, new Random().nextInt(4));
        this.dataTracker.startTracking(PAWN_SKIN, PawnSkin.VILLAGER);
    }

    @NotNull
    public PawnSkin getPawnSkin() {
        return this.dataTracker.get(PAWN_SKIN);
    }

    public void setPawnSkin(@NotNull PawnSkin skin) {
        this.dataTracker.set(PAWN_SKIN, skin);
    }

    public int getBodyTextureId() {
        return this.dataTracker.get(BODY_TEXTURE_ID);
    }

    public String getClothingId() {
        if(this instanceof IProfessional prof) {
            return prof.getProfessionId();
        }
        return "colonist";
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt == null) throw new IllegalStateException("Entity nbt cannot be null");
        final var posLong = entityNbt.getLong(FORTRESS_CENTER_BLOCK_KEY);
        final var fortressCenter = BlockPos.fromLong(posLong);
        this.setFortressCenter(fortressCenter);

        final var pawnSkingString = entityNbt.getString(PAWN_SKIN_NBT_KEY);
        final var pawnSkin = PawnSkin.valueOf(pawnSkingString);
        this.setPawnSkin(pawnSkin);

        addThisPawnToFortress();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    private void addThisPawnToFortress() {
        ServerModUtils.getFortressManager(this).ifPresent(it -> it.addPawn(this));
    }

    private void setFortressCenter(BlockPos fortressCenter) {
        this.dataTracker.set(FORTRESS_CENTER, Optional.ofNullable(fortressCenter));
    }

    @Override
    @Nullable
    public BlockPos getFortressPos() {
        return this.dataTracker.get(FORTRESS_CENTER).orElse(null);
    }

    @Override
    public final @Nullable PlayerEntity getPlayer() {
        final var server = this.getServer();
        final var fortressPos = this.getFortressPos();
        if (fortressPos == null) return null;
        return ServerExtensionsKt.getFortressOwner(server, fortressPos);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.dataTracker.get(FORTRESS_CENTER).ifPresent(it -> nbt.putLong(FORTRESS_CENTER_BLOCK_KEY, it.asLong()));
        nbt.putInt(BODY_TEXTURE_ID_NBT_KEY, this.getBodyTextureId());
        nbt.putString(PAWN_SKIN_NBT_KEY, String.valueOf(this.getPawnSkin()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(FORTRESS_CENTER_BLOCK_KEY)) {
            final var pos = BlockPos.fromLong(nbt.getLong(FORTRESS_CENTER_BLOCK_KEY));
            this.setFortressCenter(pos);
            addThisPawnToFortress(); // FIXME adding pawn every time when reading?
        }
        if(nbt.contains(BODY_TEXTURE_ID_NBT_KEY)) {
            final var bodyTexId = nbt.getInt(BODY_TEXTURE_ID_NBT_KEY);
            this.dataTracker.set(BODY_TEXTURE_ID, bodyTexId);
        }
        if (nbt.contains(PAWN_SKIN_NBT_KEY)) {
            final var skin = PawnSkin.valueOf(nbt.getString(PAWN_SKIN_NBT_KEY));
            this.dataTracker.set(PAWN_SKIN, skin);
        }
    }

    @Override
    public final boolean isInvulnerableTo(DamageSource damageSource) {
        if(damageSource.isOf(DamageTypes.FALL)) return true;
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void tickMovement() {
        super.tickHandSwing();
        super.tickMovement();

        Box boundingBox = this.getBoundingBox();
        List<SlimeEntity> touchingSlimes = getWorld().getEntitiesByClass(SlimeEntity.class, boundingBox, slimeEntity -> true);
        touchingSlimes.forEach(s -> ((FortressSlimeEntity)s).touch_Pawn(this));
    }

    @NotNull
    @Override
    public MinecraftServer getServer() {
        final var server = super.getServer();
        if (server == null) throw new IllegalStateException("Entity is not attached to a server");
        return server;
    }

    public int getAttackCooldown() {
        return 15;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15d)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 2.0d)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK);
    }
}
