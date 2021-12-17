package org.minefortress.entity.ai;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.*;
import org.minefortress.tasks.BuildingManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class NodeMaker extends LandPathNodeMaker {

    private static final int MAX_SET_UP = 320;

    private final Object2BooleanMap<Box> collisionCache = new Object2BooleanOpenHashMap<>();
    private Vec3i fakeWalkablePos = Vec3i.ZERO;
    private final Set<Vec3i> fakeBlockingForTargets = new HashSet<>();
    private BlockPos workGoal;

    private boolean wallClimbMode = false;

    public void setWallClimbMode(boolean wallClimbMode) {
        this.wallClimbMode = wallClimbMode;
    }

    public void setFakeWalkable(Vec3i fakeWalkable) {
        this.fakeWalkablePos = fakeWalkable;
    }

    public Vec3i getFakeWalkablePos() {
        return fakeWalkablePos;
    }

    public void putNewFakeBlocking(Vec3i pos) {
        fakeBlockingForTargets.add(pos);
    }

    @Override
    public void clear() {
        collisionCache.clear();
        fakeWalkablePos = Vec3i.ZERO;
        fakeBlockingForTargets.clear();
        super.clear();
    }

    @Override
    public TargetPathNode getNode(double p_77550_, double p_77551_, double p_77552_) {
        workGoal = new BlockPos(MathHelper.floor(p_77550_), MathHelper.floor(p_77551_), MathHelper.floor(p_77552_));

        return tryGetWorkGoalAsGoal()
                .or(this::tryGetNearestCorrectGoalAsGoal)
                .or(this::tryGetBottomGoalAsGoal)
                .or(this::tryGetOpenBlockGoal)
                .map(it -> new TargetPathNode(this.getNode(it.getX(), it.getY(), it.getZ())))
                .orElse(null);
    }

    @Override
    public int getSuccessors(PathNode[] neighbors, PathNode currentNode) {
        int i = 0;
        PathNodeType aboveType = this.getNodeType(this.entity, currentNode.x, currentNode.y + 1, currentNode.z);
        PathNodeType currentType = this.getNodeType(this.entity, currentNode.x, currentNode.y, currentNode.z);
        int stepUp = 0;
        if(wallClimbMode) {
            stepUp = MAX_SET_UP;
        } else if (this.entity.getPathfindingPenalty(aboveType) >= 0.0F && currentType != PathNodeType.STICKY_HONEY) {
            stepUp = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
        }

        double currentNodeFloor = this.method_37003(new BlockPos(currentNode.x, currentNode.y, currentNode.z));
        PathNode node = this.findAcceptedNode(currentNode.x, currentNode.y, currentNode.z + 1, stepUp, currentNodeFloor, Direction.SOUTH, currentType);
        if (this.isValidAdjacentSuccessor(node, currentNode)) {
            neighbors[i++] = node;
        }

        PathNode node1 = this.findAcceptedNode(currentNode.x - 1, currentNode.y, currentNode.z, stepUp, currentNodeFloor, Direction.WEST, currentType);
        if (this.isValidAdjacentSuccessor(node1, currentNode)) {
            neighbors[i++] = node1;
        }

        PathNode node2 = this.findAcceptedNode(currentNode.x + 1, currentNode.y, currentNode.z, stepUp, currentNodeFloor, Direction.EAST, currentType);
        if (this.isValidAdjacentSuccessor(node2, currentNode)) {
            neighbors[i++] = node2;
        }

        PathNode node3 = this.findAcceptedNode(currentNode.x, currentNode.y, currentNode.z - 1, stepUp, currentNodeFloor, Direction.NORTH, currentType);
        if (this.isValidAdjacentSuccessor(node3, currentNode)) {
            neighbors[i++] = node3;
        }

        PathNode node4 = this.findAcceptedNode(currentNode.x - 1, currentNode.y, currentNode.z - 1, stepUp, currentNodeFloor, Direction.NORTH, currentType);
        if (this.isValidDiagonalSuccessor(currentNode, node1, node3, node4)) {
            neighbors[i++] = node4;
        }

        PathNode node5 = this.findAcceptedNode(currentNode.x + 1, currentNode.y, currentNode.z - 1, stepUp, currentNodeFloor, Direction.NORTH, currentType);
        if (this.isValidDiagonalSuccessor(currentNode, node2, node3, node5)) {
            neighbors[i++] = node5;
        }

        PathNode node6 = this.findAcceptedNode(currentNode.x - 1, currentNode.y, currentNode.z + 1, stepUp, currentNodeFloor, Direction.SOUTH, currentType);
        if (this.isValidDiagonalSuccessor(currentNode, node1, node, node6)) {
            neighbors[i++] = node6;
        }

        PathNode node7 = this.findAcceptedNode(currentNode.x + 1, currentNode.y, currentNode.z + 1, stepUp, currentNodeFloor, Direction.SOUTH, currentType);
        if (this.isValidDiagonalSuccessor(currentNode, node2, node, node7)) {
            neighbors[i++] = node7;
        }

        return i;
    }

    protected PathNode findAcceptedNode(int x, int y, int z, int stepUp, double floorLevel, Direction moveDirection, PathNodeType currentNodeType) {
        final int stepUpCost = Math.max(MAX_SET_UP - stepUp, 0) * 8;
        PathNode node = null;
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        double d0 = this.method_37003(cursor.set(x, y, z));
        double maxDelta = wallClimbMode ? MAX_SET_UP : 1.125;
        if (d0 - floorLevel > maxDelta) {
            return null;
        } else {
            PathNodeType blockpathtypes = this.getNodeType(this.entity, x, y, z);
            float f = this.entity.getPathfindingPenalty(blockpathtypes);
            double d1 = (double)this.entity.getWidth() / 2.0D;
            if (f >= 0.0F) {
                node = this.getNode(x, y, z);
                node.type = blockpathtypes;
                if(wallClimbMode) {
                    node.penalty = Math.max(node.penalty, f + stepUpCost);
                } else {
                    node.penalty = Math.max(node.penalty, f);
                }

            }

            if (currentNodeType == PathNodeType.FENCE && node != null && node.penalty >= 0.0F && !this.canReachWithoutCollision(node)) {
                node = null;
            }

            // method 37004 - is amphibious
            if ((blockpathtypes != PathNodeType.WALKABLE) && (!this.method_37004() || blockpathtypes != PathNodeType.WATER)) {
                if ((node == null || node.penalty < 0.0F || moveDirection == Direction.UP) && stepUp > 0 && blockpathtypes != PathNodeType.FENCE && blockpathtypes != PathNodeType.UNPASSABLE_RAIL && blockpathtypes != PathNodeType.TRAPDOOR && blockpathtypes != PathNodeType.POWDER_SNOW) {
                    node = this.findAcceptedNode(x, y + 1, z, stepUp - 1, floorLevel, moveDirection, currentNodeType);
                    if (node != null && (node.type == PathNodeType.OPEN || node.type == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0F) {
                        double d2 = (double)(x - moveDirection.getOffsetX()) + 0.5D;
                        double d3 = (double)(z - moveDirection.getOffsetZ()) + 0.5D;
                        Box aabb = new Box(d2 - d1, getFeetY(cachedWorld, cursor.set(d2, (double)(y + 1), d3)) + 0.001D, d3 - d1, d2 + d1, (double)this.entity.getHeight() + getFeetY(this.cachedWorld, cursor.set((double)node.x, (double)node.y, (double)node.z)) - 0.002D, d3 + d1);
                        if (this.hasCollisions(aabb)) {
                            node = null;
                        }
                    }
                }

                if (!this.method_37004() && blockpathtypes == PathNodeType.WATER && !this.canSwim()) {
                    if (this.getNodeType(this.entity, x, y - 1, z) != PathNodeType.WATER) {
                        return node;
                    }

                    while(y > this.entity.world.getBottomY()) {
                        --y;
                        blockpathtypes = this.getNodeType(entity, x, y, z);
                        if (blockpathtypes != PathNodeType.WATER) {
                            return node;
                        }

                        node = this.getNode(x, y, z);
                        node.type = blockpathtypes;
                        node.penalty = Math.max(node.penalty, this.entity.getPathfindingPenalty(blockpathtypes));
                    }
                }

                if (blockpathtypes == PathNodeType.OPEN) {
                    int j = 0;
                    int i = y;

                    while(blockpathtypes == PathNodeType.OPEN) {
                        --y;
                        if (y < this.entity.world.getBottomY()) {
                            PathNode node3 = this.getNode(x, i, z);
                            node3.type = PathNodeType.BLOCKED;
                            node3.penalty = -1.0F;
                            return node3;
                        }

                        if (j++ >= MAX_SET_UP) {
                            PathNode node2 = this.getNode(x, y, z);
                            node2.type = PathNodeType.BLOCKED;
                            node2.penalty = -1.0F;
                            return node2;
                        }

                        blockpathtypes = this.getNodeType(this.entity, x, y, z);
                        f = this.entity.getPathfindingPenalty(blockpathtypes);
                        if (blockpathtypes != PathNodeType.OPEN && f >= 0.0F) {
                            node = this.getNode(x, y, z);
                            node.type = blockpathtypes;
                            if(wallClimbMode) {
                                node.penalty = Math.max(node.penalty, f + stepUpCost);
                            } else {
                                node.penalty = Math.max(node.penalty, f);
                            }
                            break;
                        }

                        if (f < 0.0F) {
                            PathNode node1 = this.getNode(x, y, z);
                            node1.type = PathNodeType.BLOCKED;
                            node1.penalty = -1.0F;
                            return node1;
                        }
                    }
                }

                if (blockpathtypes == PathNodeType.FENCE) {
                    node = this.getNode(x, y, z);
                    node.visited = true;
                    node.type = blockpathtypes;
                    node.penalty = blockpathtypes.getDefaultPenalty();
                }

                return node;
            } else {
                return node;
            }
        }
    }

    private boolean canReachWithoutCollision(PathNode p_77625_) {
        Vec3d vec3 = new Vec3d((double)p_77625_.x - this.entity.getX(), (double)p_77625_.y - this.entity.getY(), (double)p_77625_.z - this.entity.getZ());
        Box aabb = this.entity.getBoundingBox();

        int i = MathHelper.ceil(vec3.length() / aabb.getAverageSideLength());
        vec3 = vec3.multiply((double)(1.0F / (float)i));

        for(int j = 1; j <= i; ++j) {
            aabb = aabb.offset(vec3);
            if (this.hasCollisions(aabb)) {
                return false;
            }
        }

        return true;
    }

    private Optional<BlockPos> tryGetWorkGoalAsGoal() {
        if(correctGoal(workGoal))
            return Optional.of(workGoal);

        return Optional.empty();
    }

    private Optional<BlockPos> tryGetNearestCorrectGoalAsGoal() {
        return BlockPos.findClosest(workGoal, 1, 1, pos ->  this.correctGoal(pos) && this.positionCloserThanWorkGoal(pos));
    }

    private Optional<BlockPos> tryGetBottomGoalAsGoal() {
        return BlockPos.findClosest(
                workGoal, 1, 1,
                pos -> {
                    if (workGoal.getY() < pos.getY()) return false;
                    return correctGoal(pos) && this.positionCloserThanWorkGoal(pos);
                }
        );
    }

    private Optional<BlockPos> tryGetOpenBlockGoal() {
        return BlockPos.findClosest(
                workGoal, 1, 1,
                pos -> workGoal.getY()>pos.getY() && BuildingManager.canGoUpOnBlock(entity.world, pos) && notPlacingInMyHead(pos) && this.positionCloserThanWorkGoal(pos)
        );
    }

    private boolean correctGoal(BlockPos pos) {
        return  workGoal.getY() >= pos.getY() &&
                BuildingManager.canStayOnBlock(entity.world, pos) &&
                notPlacingInMyHead(pos);// don't place blocks on you own head!
    }

    private boolean notPlacingInMyHead(BlockPos blockPos) {
        return !blockPos.up().up().equals(workGoal);
    }

    protected boolean hasCollisions(Box p_77635_) {
        return this.collisionCache.computeIfAbsent(p_77635_, (p_77638_) -> !this.entity.world.isSpaceEmpty(this.entity, p_77635_));
    }

    private boolean positionCloserThanWorkGoal(BlockPos pos) {
        final double distanceToPos = ColonistNavigation.flatDistanceBetween(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), this.entity.getPos());
        final double distanceToWorkGoal = ColonistNavigation.flatDistanceBetween(new Vec3d(workGoal.getX(), workGoal.getY(), workGoal.getZ()), this.entity.getPos());
        return distanceToPos < distanceToWorkGoal;
    }

    @Override
    protected PathNodeType getNodeType(MobEntity p_77568_, int p_77569_, int p_77570_, int p_77571_) {
        final Vec3i pos = new Vec3i(p_77569_, p_77570_, p_77571_);
        if(fakeWalkablePos.equals(pos)) {
            return PathNodeType.WALKABLE;
        }
        if(fakeBlockingForTargets.contains(pos)) {
            return PathNodeType.BLOCKED;
        }
        return super.getNodeType(p_77568_, p_77569_, p_77570_, p_77571_);
    }
}
