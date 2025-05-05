package org.minefortress.entity.ai.professions;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.remmintan.gobi.helpers.TreeData;
import net.remmintan.gobi.helpers.TreeFinder;
import net.remmintan.gobi.helpers.TreeRemover;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;

import java.util.Collections;
import java.util.Optional;

public class LumberjackDailyTask extends AbstractAutomationAreaTask {

    private IAutomationBlockInfo goal;
    private TreeData tree;

    @Override
    protected ProfessionType getProfessionType() {
        return ProfessionType.LUMBERJACK;
    }

    @Override
    protected String getTaskDesc() {
        return "Harvesting trees";
    }

    @Override
    public void tick(Colonist colonist) {
        if(area == null) return;
        final var movementHelper = colonist.getMovementHelper();

        if(goal == null) {
            if(!iterator.hasNext()) return;
            goal = iterator.next();
        }

        if (goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if (movementHelper.hasReachedWorkGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone()){
            final var actionType = goal.info();
            if(actionType == AutomationActionType.CHOP_TREE) {
                doChopTree(colonist);
            } else if(actionType == AutomationActionType.PLANT_SAPLING) {
                final var pos = goal.pos();
                final var world = colonist.getWorld();
                final var blockState = world.getBlockState(pos);
                if(blockState.isIn(BlockTags.SAPLINGS)) {
                    this.goal = null;
                    colonist.getMovementHelper().reset();
                } else {
                    getSapling(colonist)
                            .ifPresent(it -> colonist.setGoal(new BlockStateTaskBlockInfo(it, pos)));
                }
            } else {
                throw new IllegalStateException("Unknown action type: " + actionType);
            }
        }


        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up().west();
            colonist.resetControls();
            colonist.teleport(workGoal.getX() + 0.5, workGoal.getY(), workGoal.getZ() + 0.5);
        }
    }

    private void doChopTree(Colonist colonist) {
        final var pos = goal.pos();
        final var world = colonist.getWorld();
        final var blockState = world.getBlockState(pos);
        if(blockState.isIn(BlockTags.AXE_MINEABLE)) {
            this.tree = new TreeFinder(world).findTree(pos);
            colonist.setGoal(new DigTaskBlockInfo(pos));
        } else {
            if (tree == null) {
                this.goal = null;
                colonist.getMovementHelper().reset();
            } else {
                ServerModUtils.getManagersProvider(colonist)
                        .ifPresent(it ->
                                new TreeRemover((ServerWorld) world, null, colonist).removeTheTree(tree));

                tree = null;
            }
        }
    }

    private Optional<BlockItem> getSapling(Colonist colonist) {
        if (ServerExtensionsKt.isCreativeFortress(colonist.getServer())) {
            return Optional.of((BlockItem) Items.OAK_SAPLING);
        } else {
            return ServerModUtils.getManagersProvider(colonist)
                    .map(IServerManagersProvider::getResourceManager)
                    .flatMap(rm -> rm
                            .getAllItems()
                            .stream()
                            .filter(it -> !it.isEmpty() && it.isIn(ItemTags.SAPLINGS))
                            .findFirst()
                            .map(it -> (BlockItem) it.getItem())
                            .map(it -> {
                                rm.removeItems(Collections.singletonList(new ItemInfo(it, 1)));
                                return it;
                            })
                    );
        }
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (iterator.hasNext() || this.goal != null);
    }

    @Override
    public void stop(Colonist colonist) {
        super.stop(colonist);
        this.goal = null;
    }
}
