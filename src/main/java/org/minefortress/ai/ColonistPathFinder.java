package org.minefortress.ai;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.world.chunk.ChunkCache;
import org.minefortress.BuildingManager;

import java.util.*;

public class ColonistPathFinder extends PathNodeNavigator {

    private final PathNode[] neighbors = new PathNode[32];
    private final int maxVisitedNodes;
    private final NodeMaker nodeMaker;
    private final PathMinHeap openNodes = new PathMinHeap();

    public ColonistPathFinder(NodeMaker evaluator, int maxVisitedNodes) {
        super(evaluator, maxVisitedNodes);
        this.nodeMaker = evaluator;
        this.maxVisitedNodes = maxVisitedNodes;
    }

    @Override
    public Path findPathToAny(
            ChunkCache navigationRegion,
            MobEntity colonist,
            Set<BlockPos> workGoals,
            float followRange,
            int reachDistance,
            float maxVisitedNodesMultiplyer
    ) {
        this.openNodes.clear();
        this.nodeMaker.init(navigationRegion, colonist);
        PathNode startNode = this.nodeMaker.getStart();
        final HashMap<TargetPathNode, BlockPos> targetsMap = new HashMap<>();
        for(BlockPos pos: workGoals) {
            final TargetPathNode target = this.nodeMaker.getNode(pos.getX(), pos.getY(), pos.getZ());
            if(target != null) {
                targetsMap.put(target, pos);
                BlockPos targetPos = target.getBlockPos();
                int colonistY = colonist.getBlockPos().getY();
                if(!BuildingManager.canStayOnBlock(colonist.world, target.getBlockPos())) {
                    nodeMaker.setFakeWalkable(targetPos);
                }
                if(
                        BuildingManager.doesNotHaveCollisions(colonist.world, targetPos.down()) &&
                        BuildingManager.doesNotHaveCollisions(colonist.world, targetPos.down(2))
                ) {
                    BlockPos below = targetPos.down();
                    while (below.getY()>=colonistY) {
                        nodeMaker.putNewFakeBlocking(below);
                        below = below.down();
                    }
                }
            }

        }
        if(targetsMap.isEmpty()) return null;

        Path path = this.findPath(navigationRegion.getProfiler(), startNode, targetsMap, followRange, reachDistance, maxVisitedNodesMultiplyer);
        this.nodeMaker.clear();
        return path;
    }

    private Path findPath(Profiler profiler, PathNode startNode, Map<TargetPathNode, BlockPos> goals, float followRange, int reachDistance, float maxVisitedNodesMultiplier) {
        profiler.push("find_path");
        profiler.markSampleType(SampleType.PATH_FINDING);
        Set<TargetPathNode> targets = goals.keySet();
        // node cost
        startNode.penalizedPathLength= 0.0F;
        // distance to goal
        startNode.distanceToNearestTarget = this.getBestH(startNode, targets);
        startNode.heapWeight = startNode.distanceToNearestTarget;
        this.openNodes.clear();
        this.openNodes.push(startNode);
        int i = 0;
        Set<TargetPathNode> reachedTargets = Sets.newHashSetWithExpectedSize(targets.size());
        int j = (int)((float)this.maxVisitedNodes * maxVisitedNodesMultiplier);

        while(!this.openNodes.isEmpty()) {
            ++i;
            if (i >= j) {
                break;
            }

            PathNode currentPoint = this.openNodes.pop();
            currentPoint.visited = true;

            for(TargetPathNode target : targets) {
                if (currentPoint.getManhattanDistance(target) <= (float)reachDistance) {
                    target.markReached();
                    reachedTargets.add(target);
                }
            }

            if (!reachedTargets.isEmpty()) {
                break;
            }

            if (!(currentPoint.getDistance(startNode) >= followRange)) {
                int k = this.nodeMaker.getSuccessors(this.neighbors, currentPoint);

                for(int l = 0; l < k; ++l) {
                    PathNode node1 = this.neighbors[l];
                    float f = currentPoint.getDistance(node1);
                    node1.pathLength = currentPoint.pathLength + f;
                    float f1 = currentPoint.penalizedPathLength+ f + node1.penalty;
                    if (node1.pathLength < followRange && (!node1.isInHeap() || f1 < node1.penalizedPathLength)) {
                        node1.previous = currentPoint;
                        node1.penalizedPathLength= f1;
                        node1.distanceToNearestTarget = this.getBestH(node1, targets) * 1.5F;
                        if (node1.isInHeap()) {
                            this.openNodes.setNodeWeight(node1, node1.penalizedPathLength+ node1.distanceToNearestTarget);
                        } else {
                            node1.heapWeight = node1.penalizedPathLength+ node1.distanceToNearestTarget;
                            this.openNodes.push(node1);
                        }
                    }
                }
            }
        }

        Optional<Path> optional = !reachedTargets.isEmpty() ?
                reachedTargets.stream().map((target) ->
                    this.reconstructPath(target.getNearestNode(), goals.get(target), true))
                    .min(Comparator.comparingInt(Path::getLength)) :
                targets.stream().map((target) ->
                    this.reconstructPath(target.getNearestNode(), goals.get(target), false))
                    .min(Comparator.comparingDouble(Path::getManhattanDistanceFromTarget)
                    .thenComparingInt(Path::getLength));


        profiler.pop();
        return !optional.isPresent() ? null : optional.get();
    }

    private float getBestH(PathNode node, Set<TargetPathNode> targets) {
        float maxDistance = Float.MAX_VALUE;

        for(TargetPathNode target : targets) {
            float distance = node.getDistance(target);
            target.updateNearestNode(distance, node);
            maxDistance = Math.min(distance, maxDistance);
        }

        return maxDistance;
    }

    private Path reconstructPath(PathNode nearestNode, BlockPos targetPos, boolean reached) {
        List<PathNode> list = Lists.newArrayList();
        PathNode node = nearestNode;

        final Vec3i fakeWalkablePos = nodeMaker.getFakeWalkablePos();
        if(fakeWalkablePos !=null && fakeWalkablePos != Vec3i.ZERO) {
            if(fakeWalkablePos.equals(node.getBlockPos())) {
                final PathNode cameFrom = node.previous;
                if(cameFrom != null) {
                    node = node.copyWithNewPosition(cameFrom.x, node.y, cameFrom.z);
                }
            }
        }

        list.add(0, node);

        while(node.previous != null) {
            node = node.previous;
            list.add(0, node);
        }

        return new Path(list, targetPos, reached);
    }

}
