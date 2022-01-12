package org.minefortress.entity;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.ColonistNavigation;
import org.minefortress.entity.ai.controls.DigControl;
import org.minefortress.entity.ai.controls.MLGControl;
import org.minefortress.entity.ai.controls.PlaceControl;
import org.minefortress.entity.ai.controls.ScaffoldsControl;
import org.minefortress.entity.ai.goal.ColonistExecuteTaskGoal;
import org.minefortress.interfaces.FortressSlimeEntity;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.List;
import java.util.function.BiPredicate;

public class Colonist extends PassiveEntity {

    public static final float WORK_REACH_DISTANCE = 3f;

    private final DigControl digControl;
    private final PlaceControl placeControl;
    private final ScaffoldsControl scaffoldsControl;
    private final MLGControl mlgControl;

    private ColonistExecuteTaskGoal executeTaskGoal;

    public Colonist(EntityType<? extends Colonist> entityType, World world) {
        super(entityType, world);

        if(world instanceof ServerWorld) {
            digControl = new DigControl(this, (ServerWorld) world);
            placeControl = new PlaceControl(this);
            scaffoldsControl = new ScaffoldsControl(this);
            mlgControl = new MLGControl(this);
        } else {
            digControl = null;
            placeControl = null;
            scaffoldsControl = null;
            mlgControl = null;
        }
    }


    @Override
    protected float getBaseMovementSpeedMultiplier() {
        return this.isHasTask() ? 0.98f :  super.getBaseMovementSpeedMultiplier();
    }

    private boolean hasTask = false;

    public boolean isHasTask() {
        return hasTask;
    }

    public void setHasTask(boolean hasTask) {
        this.hasTask = hasTask;
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
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 3f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 100f)
                .add(EntityAttributes.GENERIC_LUCK, 1024f);
    }



    @Override
    protected EntityNavigation createNavigation(World p_21480_) {
        return new ColonistNavigation(this, p_21480_);
    }


    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new LongDoorInteractGoal(this, true));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.5, true));
        executeTaskGoal  = new ColonistExecuteTaskGoal(this);
        this.goalSelector.add(6, executeTaskGoal);
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(8, new LookAroundGoal(this));

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
    public void remove(RemovalReason p_146876_) {
        super.remove(p_146876_);
        if(this.executeTaskGoal != null) {
            this.executeTaskGoal.returnTask();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(fallDistance > getSafeFallDistance()) {
            BlockPos currentHitPos = getBlockPos().down().down();
            if(!world.getBlockState(currentHitPos).isAir() && getMlgControl() != null) {
                getMlgControl().needAction();
            }
        }

        if(isHalfInWall() || isEyesInTheWall()) {
            this.getJumpControl().setActive();
            if(getScaffoldsControl() != null && world.getBlockState(getBlockPos().down()).isAir())
                getScaffoldsControl().needAction();
        }

        if(this.isTouchingWater() || this.isInLava()) {
            if(!this.isHasTask())
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

    @Override
    public boolean isPushedByFluids() {
        return !this.isHasTask();
    }
}
