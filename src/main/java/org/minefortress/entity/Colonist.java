package org.minefortress.entity;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.minefortress.IMinefortressEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IAreaBasedTaskControl;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.ai.controls.AreaBasedTaskControl;
import org.minefortress.entity.ai.controls.DigControl;
import org.minefortress.entity.ai.controls.PlaceControl;
import org.minefortress.entity.ai.controls.TaskControl;
import org.minefortress.entity.ai.goal.*;
import org.minefortress.registries.FortressEntities;

import java.util.Optional;

public class Colonist extends NamedPawnEntity implements IMinefortressEntity, IWorkerPawn {

    public static final float FAST_MOVEMENT_SPEED = 0.15f;
    public static final float SLOW_MOVEMENT_SPEED = 0.05f;

    private static final TrackedData<String> CURRENT_TASK_DESCRIPTION = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> PROFESSION_ID = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> HAS_TASK = DataTracker.registerData(Colonist.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final String DEFAULT_PROFESSION_ID = "colonist";
    public static final String RESERVE_PROFESSION_ID = "reserve";

    public static final float WORK_REACH_DISTANCE = 3f;

    private final DigControl digControl;
    private final PlaceControl placeControl;
    private final ITaskControl taskControl;
    private final IAreaBasedTaskControl areaBasedTaskControl;
    private final MovementHelper movementHelper;
    private final IBaritone baritone;

    private boolean allowToPlaceBlockFromFarAway = false;

    public Colonist(EntityType<? extends Colonist> entityType, World world) {
        super(entityType, world, true);

        if(world instanceof ServerWorld) {
            digControl = new DigControl(this, (ServerWorld) world);
            placeControl = new PlaceControl(this);
            taskControl = new TaskControl(this);
            areaBasedTaskControl = new AreaBasedTaskControl(this);
            baritone = BaritoneAPI.getProvider().getBaritone(this);
            movementHelper = new MovementHelper(this);
        } else {
            digControl = null;
            placeControl = null;
            taskControl = null;
            areaBasedTaskControl = null;
            baritone = null;
            movementHelper = null;
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CURRENT_TASK_DESCRIPTION, "");
        this.dataTracker.startTracking(PROFESSION_ID, DEFAULT_PROFESSION_ID);
        this.dataTracker.startTracking(HAS_TASK, false);
    }

    public IBaritone getBaritone() {
        return baritone;
    }

    public ServerWorld getServerWorld() {
        return (ServerWorld) this.getWorld();
    }

    @Override
    public String getClothingId() {
        return getProfessionId();
    }

    public MovementHelper getMovementHelper() {
        return movementHelper;
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
            float f1 = switch (Optional.ofNullable(this.getStatusEffect(StatusEffects.MINING_FATIGUE)).map(StatusEffectInstance::getAmplifier).orElse(3)) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= f1;
        }

        if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            f /= 5.0F;
        }

        if (!this.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ColonistMeleeAttackGoal(this));
        this.goalSelector.add(3, new FleeEntityGoal<>(this, CreeperEntity.class, 5, 1.5D, 2.1D));
        this.goalSelector.add(4, new FollowFortressAttackTargetGoal(this, Colonist.FAST_MOVEMENT_SPEED, Colonist.WORK_REACH_DISTANCE));
        this.goalSelector.add(5, new DailyProfessionTasksGoal(this));
        this.goalSelector.add(6, new ColonistExecuteTaskGoal(this));
        this.goalSelector.add(6, new PawnExecuteAreaBasedTaskGoal(this));
        this.goalSelector.add(8, new WanderAroundTheFortressGoal(this));
        this.goalSelector.add(8, new SleepOnTheBedGoal(this));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new FortressRevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new FortressColonistActiveTargetGoal(this));
        this.targetSelector.add(3, new BuildingAttackerTargetGoal(this));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15d)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0d)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.0)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2.0)
                .add(EntityAttributes.GENERIC_LUCK);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if(this.taskControl.hasTask()) {
            this.taskControl.fail();
        }
        this.areaBasedTaskControl.reset();
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        tickProfessionCheck();
        final var target = this.getTarget();
        if(target != null && !target.isAlive()) {
            this.setTarget(null);
        }
    }

    private void tickProfessionCheck() {
        final String professionId = this.dataTracker.get(PROFESSION_ID);
        if(DEFAULT_PROFESSION_ID.equals(professionId) || RESERVE_PROFESSION_ID.equals(professionId)) {
            ServerModUtils.getManagersProvider(this)
                    .map(IServerManagersProvider::getProfessionsManager)
                    .flatMap(it -> it.getProfessionsWithAvailablePlaces(RESERVE_PROFESSION_ID.equals(professionId)))
                    .ifPresent(this::setProfession);
        }
    }

    @Override
    public void tick() {
        super.tick();

        boolean taskControlHasTask = this.taskControl != null && (taskControl.hasTask() || taskControl.isDoingEverydayTasks());
        boolean areaTaskControlHasTask = this.areaBasedTaskControl != null && areaBasedTaskControl.hasTask();
        this.setHasTask(taskControlHasTask || areaTaskControlHasTask);


        if((isHalfInWall() || isEyesInTheWall()) && !this.isSleeping())
            this.getJumpControl().setActive();

        tickAllControls();
    }

    private void tickAllControls() {
        if(getEatControl().map(IEatControl::isEating).orElse(false)) return;
        if(getDigControl() != null) getDigControl().tick();
        if(getPlaceControl() != null) getPlaceControl().tick();
        if(getMovementHelper() != null) getMovementHelper().tick();
        if (getAreaBasedTaskControl() != null) getAreaBasedTaskControl().tick();
        if (getTaskControl() != null) getTaskControl().tick();
    }

    private boolean isHalfInWall() {
        if (this.noClip) {
            return false;
        } else {
            Vec3d eyePosition = this.getEyePos();
            Vec3d legsPos = new Vec3d(eyePosition.x, eyePosition.y - 1, eyePosition.z);
            Box aabb = Box.of(legsPos, getWidth(), 1.0E-6D, getWidth());
            return this.getWorld()
                    .getBlockCollisions(this, aabb).iterator().hasNext();
        }
    }

    public boolean isWallAboveTheHead() {
        if (this.noClip) {
            return false;
        } else {
            Box legsBox = Box.of(this.getPos(), getWidth()/1.4, 0.5, getWidth()/1.4);
            Box aboveTheHeadBox = legsBox.offset(0, 2.5, 0);
            return this.getWorld().getBlockCollisions(this, aboveTheHeadBox).iterator().hasNext();
        }
    }

    public boolean isEyesInTheWall() {
        if (this.noClip) {
            return false;
        } else {
            Box aabb = Box.of(this.getEyePos(), getWidth(), 1.0E-6D, getWidth());
            return this.getWorld()
                    .getBlockCollisions(this, aabb).iterator().hasNext();
        }
    }

    public DigControl getDigControl() {
        return digControl;
    }

    public PlaceControl getPlaceControl() {
        return placeControl;
    }

    public void resetControls() {
        digControl.reset();
        placeControl.reset();
        movementHelper.reset();
    }

    private BlockPos goal;

    public void setGoal(ITaskBlockInfo taskBlockInfo) {
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
        lookAt(this.goal);
    }

    public void lookAt(BlockPos pos) {
        if(pos == null) return;
        getLookControl().lookAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.hasTask();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        final String professionId = this.getProfessionId();
        if(!DEFAULT_PROFESSION_ID.equals(professionId)) {
            nbt.putString("professionId", professionId);
        }
    }

    public String getProfessionId() {
        return this.dataTracker.get(PROFESSION_ID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if(nbt.contains("professionId")) {
            final String professionId = nbt.getString("professionId");
            this.setProfession(professionId);
        }
    }

    public boolean isAllowToPlaceBlockFromFarAway() {
        return allowToPlaceBlockFromFarAway;
    }

    public void setAllowToPlaceBlockFromFarAway(boolean allowToPlaceBlockFromFarAway) {
        this.allowToPlaceBlockFromFarAway = allowToPlaceBlockFromFarAway;
    }

    public void setCurrentTaskDesc(String currentTaskDesc) {
        this.dataTracker.set(CURRENT_TASK_DESCRIPTION, currentTaskDesc);
    }

    public String getCurrentTaskDesc() {
        return this.dataTracker.get(CURRENT_TASK_DESCRIPTION);
    }

    public void reserve() {
        if(this.getProfessionId().equals(DEFAULT_PROFESSION_ID)) {
            this.setProfession(RESERVE_PROFESSION_ID);
        } else {
            throw new IllegalStateException("Colonist cannot be reserved because it is already assigned to a profession");
        }
    }

    public void setProfession(String professionId) {
        final var type = ServerModUtils.getManagersProvider(this)
                .map(IServerManagersProvider::getProfessionsManager)
                .map(it -> it.getEntityTypeForProfession(professionId))
                .orElse(null);
        final var fmOpt = ServerModUtils.getFortressManager(this);
        if (type == FortressEntities.COLONIST_ENTITY_TYPE) {
            this.dataTracker.set(PROFESSION_ID, professionId);
        } else if (type == FortressEntities.WARRIOR_PAWN_ENTITY_TYPE || type == FortressEntities.ARCHER_PAWN_ENTITY_TYPE) {
            fmOpt.ifPresent(it -> it.replaceColonistWithTypedPawn(this, professionId, type));
        }
        fmOpt.ifPresent(IServerFortressManager::scheduleSync);
    }

    @Override
    public ITaskControl getTaskControl() {
        return taskControl;
    }

    @Override
    public IAreaBasedTaskControl getAreaBasedTaskControl() {
        return areaBasedTaskControl;
    }

    private void setHasTask(boolean hasTask) {
        this.dataTracker.set(HAS_TASK, hasTask);
    }

    public boolean hasTask() {
        return this.dataTracker.get(HAS_TASK);
    }



}
