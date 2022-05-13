package org.minefortress.entity;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.ColonistNavigation;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.ai.controls.*;
import org.minefortress.entity.ai.goal.*;
import org.minefortress.entity.colonist.ColonistHungerManager;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.interfaces.FortressSlimeEntity;
import org.minefortress.professions.ServerProfessionManager;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class Colonist extends PassiveEntity {

    private static final TrackedData<String> CURRENT_TASK_DECRIPTION = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> CURRENT_FOOD_LEVEL = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PROFESSION_ID = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> HAS_TASK = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> GUY_TYPE = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.INTEGER);
    private static final String DEFAULT_PROFESSION_ID = "colonist";

    public static final float WORK_REACH_DISTANCE = 4f;

    private final DigControl digControl;
    private final PlaceControl placeControl;
    private final ScaffoldsControl scaffoldsControl;
    private final TaskControl taskControl;
    private final MovementHelper movementHelper;
    private final MLGControl mlgControl;

    private UUID masterPlayerId;
    private BlockPos fortressCenter;

    private boolean allowToPlaceBlockFromFarAway = false;
    private final ColonistHungerManager hungerManager = new ColonistHungerManager();
    private final Queue<Consumer<FortressServerPlayerEntity>> masterPlayerActionQueue = new ArrayDeque<>();

    public Colonist(EntityType<? extends Colonist> entityType, World world) {
        super(entityType, world);

        if(world instanceof ServerWorld) {
            digControl = new DigControl(this, (ServerWorld) world);
            placeControl = new PlaceControl(this);
            scaffoldsControl = new ScaffoldsControl(this);
            mlgControl = new MLGControl(this);
            taskControl = new TaskControl(this);
            movementHelper = new MovementHelper((ColonistNavigation) this.getNavigation(), this);
        } else {
            digControl = null;
            placeControl = null;
            scaffoldsControl = null;
            mlgControl = null;
            taskControl = null;
            movementHelper = null;
        }

        this.dataTracker.startTracking(CURRENT_TASK_DECRIPTION, "");
        this.dataTracker.startTracking(CURRENT_FOOD_LEVEL, HungerConstants.FULL_FOOD_LEVEL);
        this.dataTracker.startTracking(PROFESSION_ID, DEFAULT_PROFESSION_ID);
        this.dataTracker.startTracking(HAS_TASK, false);
        this.dataTracker.startTracking(GUY_TYPE, world.random.nextInt(4));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt == null) throw new IllegalStateException("Entity nbt cannot be null");
        this.masterPlayerId = entityNbt.getUuid("fortressUUID");
        int centerX = entityNbt.getInt("centerX");
        int centerY = entityNbt.getInt("centerY");
        int centerZ = entityNbt.getInt("centerZ");
        this.fortressCenter = new BlockPos(centerX, centerY, centerZ);

        this.doActionOnMasterPlayer(player -> player.getFortressServerManager().addColonist(this));
        this.doActionOnMasterPlayer(this::setCustomNameIfNeeded);


        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public MovementHelper getMovementHelper() {
        return movementHelper;
    }

    private void setCustomNameIfNeeded(FortressServerPlayerEntity player) {
        if(!this.hasCustomName()) {
            this.setCustomName(new LiteralText(player.getFortressServerManager().getNameGenerator().generateRandomName()));
        }
    }

    public void doActionOnMasterPlayer(Consumer<FortressServerPlayerEntity> playerConsumer) {
        final MinecraftServer server = getServer();
        final Optional<FortressServerPlayerEntity> masterPlayer = getMasterPlayer(server);
        if(masterPlayer.isPresent()) {
            playerConsumer.accept(masterPlayer.get());
        } else {
            masterPlayerActionQueue.add(playerConsumer);
        }
    }

    public Optional<FortressServerManager> getFortressManager() {
        final Optional<FortressServerPlayerEntity> masterPlayer = getMasterPlayer(getServer());
        return masterPlayer.map(FortressServerPlayerEntity::getFortressServerManager);
    }

    @NotNull
    private Optional<FortressServerPlayerEntity> getMasterPlayer(MinecraftServer server) {
        if(server == null || masterPlayerId == null) return Optional.empty();
        return server
                .getPlayerManager()
                .getPlayerList()
                .stream()
                .filter(p -> p instanceof FortressServerPlayerEntity)
                .map(p -> (FortressServerPlayerEntity) p)
                .filter(p -> p.getFortressUuid().equals(masterPlayerId))
                .findFirst();
    }

    public Optional<FortressServerManager> getFortressServerManager() {
        return getMasterPlayer(this.getServer()).map(FortressServerPlayerEntity::getFortressServerManager);
    }

    public BlockPos getFortressCenter() {
        return fortressCenter;
    }

    @Override
    protected float getBaseMovementSpeedMultiplier() {
        return this.taskControl.hasTask() ? 0.98f :  super.getBaseMovementSpeedMultiplier();
    }

    public void putItemInHand(Item item) {
        ItemStack stackInHand = getStackInHand(Hand.MAIN_HAND);
        if(item == null) {
            if(stackInHand != ItemStack.EMPTY)
                setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            Item itemInHand = stackInHand.getItem();
            if(item.equals(itemInHand)) return;
            setStackInHand(Hand.MAIN_HAND, new ItemStack(item));
        }
    }

    @Override
    public void setAttacking(boolean aggressive) {
        super.setAttacking(aggressive);

        if(aggressive) {
            this.putItemInHand(Items.IRON_SWORD);
        } else {
            this.putItemInHand(null);
        }
    }

    public float getDestroySpeed(BlockState state) {
        float f = getStackInHand(Hand.MAIN_HAND).getMiningSpeedMultiplier(state);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiency(this);
            ItemStack itemstack = this.getMainHandStack();
            if (i > 0 && !itemstack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(this)) {
            f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
        }

        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float f1;
            switch(this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    f1 = 0.3F;
                    break;
                case 1:
                    f1 = 0.09F;
                    break;
                case 2:
                    f1 = 0.0027F;
                    break;
                case 3:
                default:
                    f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            f /= 5.0F;
        }

        if (!this.onGround) {
            f /= 5.0F;
        }

        return f;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2F)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64f)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 100f)
                .add(EntityAttributes.GENERIC_LUCK, 1024f);
    }

    @Override
    protected EntityNavigation createNavigation(World p_21480_) {
        return new ColonistNavigation(this, p_21480_);
    }

    @Override
    public boolean isInvulnerable() {
        if(this.getFortressManager().map(AbstractFortressManager::isCreative).orElse(false))
            return true;
        else
            return super.isInvulnerable();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if(this.getFortressManager().map(AbstractFortressManager::isCreative).orElse(false)) {
            if (damageSource.isOutOfWorld()) return false;
            return true;
        } else {
            return super.isInvulnerableTo(damageSource);
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new LongDoorInteractGoal(this, true));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.add(5, new DailyProfessionTasksGoal(this));
        this.goalSelector.add(6, new ColonistExecuteTaskGoal(this));
        this.goalSelector.add(7, new WanderAroundTheFortressGoal(this));
        this.goalSelector.add(7, new SleepOnTheBedGoal(this));
        this.goalSelector.add(8, new ReturnToFireGoal(this));
        this.goalSelector.add(9, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void tickMovement() {
        super.tickHandSwing();
        super.tickMovement();

        Box boundingBox = getBoundingBox();
        List<SlimeEntity> touchingSlimes = world.getEntitiesByClass(SlimeEntity.class, boundingBox, slimeEntity -> true);
        touchingSlimes.forEach(s -> ((FortressSlimeEntity)s).touchColonist(this));
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
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if(this.taskControl.hasTask()) {
            this.taskControl.fail();
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        tickProfessionCheck();
        if(!this.masterPlayerActionQueue.isEmpty()) {
            final Optional<FortressServerPlayerEntity> masterPlayer = getMasterPlayer(this.getServer());
            if(masterPlayer.isPresent()) {
                final FortressServerPlayerEntity fortressServerPlayerEntity = masterPlayer.get();
                masterPlayerActionQueue.forEach(action -> action.accept(fortressServerPlayerEntity));
                masterPlayerActionQueue.clear();
            }
        }

        this.hungerManager.update(this);

        if(this.getCurrentFoodLevel() != this.hungerManager.getFoodLevel()) {
            this.updateCurrentFoodLevel();
        }
    }

    private void tickProfessionCheck() {
        final String professionId = this.dataTracker.get(PROFESSION_ID);
        if(DEFAULT_PROFESSION_ID.equals(professionId)) {
            this.doActionOnMasterPlayer(player -> {
                final ServerProfessionManager manager = player.getFortressServerManager().getServerProfessionManager();
                manager.getProfessionsWithAvailablePlaces().ifPresent(p -> {
                    this.dataTracker.set(PROFESSION_ID, p);
                });
            });
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(this.taskControl != null) {
            this.setHasTask(this.dataTracker.get(HAS_TASK));
        }
        if(fallDistance > getSafeFallDistance()) {
            BlockPos currentHitPos = getBlockPos().down().down();
            if(!world.getBlockState(currentHitPos).isAir() && getMlgControl() != null) {
                getMlgControl().needAction();
            }
        }

        if((isHalfInWall() || isEyesInTheWall()) && !this.isSleeping()) {
            this.getJumpControl().setActive();
            if(getScaffoldsControl() != null && world.getBlockState(getBlockPos().down()).isAir())
                getScaffoldsControl().needAction();
        }

        if(this.isTouchingWater() || this.isInLava()) {
            if(!this.hasTask())
                getJumpControl().setActive();
            if(getMlgControl() != null) {
                getMlgControl().clearResults();
            }
        }

        tickAllControls();
    }

    private void tickAllControls() {
        if(getDigControl() != null) getDigControl().tick();
        if(getPlaceControl() != null) getPlaceControl().tick();
        if(getMlgControl() != null) getMlgControl().tick();
        if(getScaffoldsControl() != null) getScaffoldsControl().tick();
    }

    private boolean isHalfInWall() {
        if (this.noClip) {
            return false;
        } else {
            Vec3d eyePosition = this.getEyePos();
            Vec3d legsPos = new Vec3d(eyePosition.x, eyePosition.y - 1, eyePosition.z);
            Box aabb = Box.of(legsPos, getWidth(), 1.0E-6D, getWidth());
            BiPredicate<BlockState, BlockPos> collide = (p_20129_, p_20130_) -> !p_20129_.isAir();
            return this.world
                    .getBlockCollisions(this, aabb, collide)
                    .findAny().isPresent();
        }
    }

    public boolean isWallAboveTheHead() {
        if (this.noClip) {
            return false;
        } else {
            Box legsBox = Box.of(this.getPos(), getWidth()/1.4, 0.5, getWidth()/1.4);
            Box aboveTheHeadBox = legsBox.offset(0, 2.5, 0);
            return this.world.getBlockCollisions(this, aboveTheHeadBox).count() >0;
        }
    }

    public boolean isEyesInTheWall() {
        if (this.noClip) {
            return false;
        } else {
            Box aabb = Box.of(this.getEyePos(), (double)getWidth(), 1.0E-6D, (double)getWidth());
            return this.world
                    .getBlockCollisions(this, aabb, (p_20129_, p_20130_) -> !p_20129_.isAir())
                    .findAny()
                    .isPresent();
        }
    }

    public DigControl getDigControl() {
        return digControl;
    }

    public PlaceControl getPlaceControl() {
        return placeControl;
    }

    public ScaffoldsControl getScaffoldsControl() {
        return scaffoldsControl;
    }

    public MLGControl getMlgControl() {
        return mlgControl;
    }

    public void resetControls() {
        digControl.reset();
        placeControl.reset();
        scaffoldsControl.clearResults();
    }

    public boolean diggingOrPlacing() {
        return (placeControl != null && placeControl.isWorking()) || (digControl != null &&  digControl.isWorking());
    }

    private BlockPos goal;

    public void setGoal(TaskBlockInfo taskBlockInfo) {
        this.goal = taskBlockInfo.getPos();
        Item placingItem = taskBlockInfo.getPlacingItem();
        if(placingItem != null) {
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(placingItem));
            this.placeControl.set(taskBlockInfo);
        } else {
            this.digControl.set(taskBlockInfo);
        }
    }

    public void lookAtGoal() {
        getLookControl().lookAt(this.goal.getX(), this.goal.getY(), this.goal.getZ());
    }

    public void lookAt(BlockPos pos) {
        getLookControl().lookAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.hasTask();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putUuid("playerId", this.masterPlayerId);
        if(this.fortressCenter != null) {
            nbt.putInt("fortressCenterX", this.fortressCenter.getX());
            nbt.putInt("fortressCenterY", this.fortressCenter.getY());
            nbt.putInt("fortressCenterZ", this.fortressCenter.getZ());
        }

        final NbtCompound hunger = new NbtCompound();
        this.hungerManager.writeNbt(hunger);
        nbt.put("hunger", hunger);

        final String professionId = this.getProfessionId();
        if(!DEFAULT_PROFESSION_ID.equals(professionId)) {
            nbt.putString("professionId", professionId);
        }

        final var guyType = this.getGuyType();
        nbt.putInt("guyType", guyType);
    }

    public String getProfessionId() {
        return this.dataTracker.get(PROFESSION_ID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if(nbt == null) return;
        this.masterPlayerId = nbt.getUuid("playerId");
        if(nbt.contains("fortressCenterX")) {
            this.fortressCenter = new BlockPos(nbt.getInt("fortressCenterX"), nbt.getInt("fortressCenterY"), nbt.getInt("fortressCenterZ"));
        }
        if(nbt.contains("hunger")) {
            this.hungerManager.readNbt(nbt.getCompound("hunger"));
        }
        if(masterPlayerId != null) {
            doActionOnMasterPlayer(this::setCustomNameIfNeeded);
            doActionOnMasterPlayer(masterPlayer -> {
               masterPlayer.getFortressServerManager().addColonist(this);
            });
        }

        if(nbt.contains("professionId")) {
            final String professionId = nbt.getString("professionId");
            this.setProfession(professionId);
        }

        if (nbt.contains("guyType")) {
            this.dataTracker.set(GUY_TYPE, nbt.getInt("guyType"));
        }
    }

    public boolean isAllowToPlaceBlockFromFarAway() {
        return allowToPlaceBlockFromFarAway;
    }

    public void setAllowToPlaceBlockFromFarAway(boolean allowToPlaceBlockFromFarAway) {
        this.allowToPlaceBlockFromFarAway = allowToPlaceBlockFromFarAway;
    }

    public void setCurrentTaskDesc(String currentTaskDesc) {
        this.dataTracker.set(CURRENT_TASK_DECRIPTION, currentTaskDesc);
    }

    public String getCurrentTaskDesc() {
        return this.dataTracker.get(CURRENT_TASK_DECRIPTION);
    }

    public void updateCurrentFoodLevel() {
        this.dataTracker.set(CURRENT_FOOD_LEVEL, this.hungerManager.getFoodLevel());
    }

    public int getCurrentFoodLevel() {
        return this.dataTracker.get(CURRENT_FOOD_LEVEL);
    }

    public void setProfession(String professionId) {
        this.dataTracker.set(PROFESSION_ID, professionId);
    }

    public int getGuyType() {
        return this.dataTracker.get(GUY_TYPE);
    }

    public void resetProfession() {
        this.dataTracker.set(PROFESSION_ID, DEFAULT_PROFESSION_ID);
    }

    public TaskControl getTaskControl() {
        return taskControl;
    }

    private void setHasTask(boolean hasTask) {
        this.dataTracker.set(HAS_TASK, hasTask);
    }

    private boolean hasTask() {
        return this.dataTracker.get(HAS_TASK);
    }

}
