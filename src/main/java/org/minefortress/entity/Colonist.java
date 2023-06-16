package org.minefortress.entity;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.minefortress.IFortressColonist;
import baritone.api.minefortress.IMinefortressEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.ai.controls.*;
import org.minefortress.entity.ai.goal.*;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.registries.FortressEntities;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.Optional;

public final class Colonist extends NamedPawnEntity implements IMinefortressEntity, IFortressColonist, IWorkerPawn {

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
    private final ScaffoldsControl scaffoldsControl;
    private final TaskControl taskControl;
    private final MovementHelper movementHelper;
    private final IBaritone baritone;

    private boolean allowToPlaceBlockFromFarAway = false;

    public Colonist(EntityType<? extends Colonist> entityType, World world) {
        super(entityType, world, true);

        if(world instanceof ServerWorld) {
            digControl = new DigControl(this, (ServerWorld) world);
            placeControl = new PlaceControl(this);
            scaffoldsControl = new ScaffoldsControl(this);
            taskControl = new TaskControl(this);
            baritone = BaritoneAPI.getProvider().getBaritone(this);
            movementHelper = new MovementHelper(this);
        } else {
            digControl = null;
            placeControl = null;
            scaffoldsControl = null;
            taskControl = null;
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
        return (ServerWorld) this.world;
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

        if (!this.onGround) {
            f /= 5.0F;
        }

        return f;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this));
        this.goalSelector.add(3, new FleeEntityGoal<>(this, CreeperEntity.class, 5, 1.5D, 2.1D));
        this.goalSelector.add(4, new FollowFortressAttackTargetGoal(this, Colonist.FAST_MOVEMENT_SPEED, Colonist.WORK_REACH_DISTANCE));
        this.goalSelector.add(5, new DailyProfessionTasksGoal(this));
        this.goalSelector.add(6, new ColonistExecuteTaskGoal(this));
        this.goalSelector.add(8, new WanderAroundTheFortressGoal(this));
        this.goalSelector.add(8, new SleepOnTheBedGoal(this));
        this.goalSelector.add(9, new ReturnToFireGoal(this));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new FortressRevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, false));
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
        final var target = this.getTarget();
        if(target != null && !target.isAlive()) {
            this.setTarget(null);
        }
    }

    private void tickProfessionCheck() {
        final String professionId = this.dataTracker.get(PROFESSION_ID);
        if(DEFAULT_PROFESSION_ID.equals(professionId) || RESERVE_PROFESSION_ID.equals(professionId)) {
            getFortressServerManager()
                    .map(FortressServerManager::getServerProfessionManager)
                    .flatMap(it -> it.getProfessionsWithAvailablePlaces(RESERVE_PROFESSION_ID.equals(professionId)))
                    .ifPresent(this::setProfession);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(this.taskControl != null)
            this.setHasTask(this.taskControl.hasTask() || this.taskControl.isDoingEverydayTasks());


        if((isHalfInWall() || isEyesInTheWall()) && !this.isSleeping())
            this.getJumpControl().setActive();

        tickAllControls();
    }

    private void tickAllControls() {
        if(getEatControl().map(EatControl::isEating).orElse(false)) return;
        if(getDigControl() != null) getDigControl().tick();
        if(getPlaceControl() != null) getPlaceControl().tick();
        if(getScaffoldsControl() != null) getScaffoldsControl().tick();
        if(getMovementHelper() != null) getMovementHelper().tick();
    }

    private boolean isHalfInWall() {
        if (this.noClip) {
            return false;
        } else {
            Vec3d eyePosition = this.getEyePos();
            Vec3d legsPos = new Vec3d(eyePosition.x, eyePosition.y - 1, eyePosition.z);
            Box aabb = Box.of(legsPos, getWidth(), 1.0E-6D, getWidth());
            return this.world
                    .getBlockCollisions(this, aabb).iterator().hasNext();
        }
    }

    public boolean isWallAboveTheHead() {
        if (this.noClip) {
            return false;
        } else {
            Box legsBox = Box.of(this.getPos(), getWidth()/1.4, 0.5, getWidth()/1.4);
            Box aboveTheHeadBox = legsBox.offset(0, 2.5, 0);
            return this.world.getBlockCollisions(this, aboveTheHeadBox).iterator().hasNext();
        }
    }

    public boolean isEyesInTheWall() {
        if (this.noClip) {
            return false;
        } else {
            Box aabb = Box.of(this.getEyePos(), getWidth(), 1.0E-6D, getWidth());
            return this.world
                    .getBlockCollisions(this, aabb).iterator().hasNext();
        }
    }

    public DigControl getDigControl() {
        return digControl;
    }

    public PlaceControl getPlaceControl() {
        return placeControl;
    }

    @Override
    public ScaffoldsControl getScaffoldsControl() {
        return scaffoldsControl;
    }

    public void resetControls() {
        digControl.reset();
        placeControl.reset();
        scaffoldsControl.clearResults();
        movementHelper.reset();
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


    public void reserveColonist() {
        if(this.getProfessionId().equals(DEFAULT_PROFESSION_ID)) {
            this.setProfession(RESERVE_PROFESSION_ID);
        } else {
            throw new IllegalStateException("Colonist cannot be reserved because it is already assigned to a profession");
        }
    }
    public void setProfession(String professionId) {
        getFortressServerManager().ifPresent(it -> {
            final var spm = it.getServerProfessionManager();
            final var type = spm.getEntityTypeForProfession(professionId);
            if(type == FortressEntities.COLONIST_ENTITY_TYPE) {
                this.dataTracker.set(PROFESSION_ID, professionId);
            } else if (type == FortressEntities.WARRIOR_PAWN_ENTITY_TYPE || type == FortressEntities.ARCHER_PAWN_ENTITY_TYPE) {
                it.replaceColonistWithTypedPawn(this, professionId, type);
            }
            it.scheduleSync();
        });
    }

    public void resetProfession() {
        this.setProfession(DEFAULT_PROFESSION_ID);
    }

    public TaskControl getTaskControl() {
        return taskControl;
    }

    private void setHasTask(boolean hasTask) {
        this.dataTracker.set(HAS_TASK, hasTask);
    }

    public boolean hasTask() {
        return this.dataTracker.get(HAS_TASK);
    }



}
