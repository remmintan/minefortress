package org.minefortress.entity.ai.controls;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.event.GameEvent;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.utils.BuildingHelper;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.ReplaceTaskBlockInfo;
import org.slf4j.LoggerFactory;

import static org.minefortress.entity.colonist.FortressHungerManager.ACTIVE_EXHAUSTION;

public class DigControl extends PositionedActionControl {

    private final Colonist pawn;
    private final ServerWorld level;

    private float destroyProgress = 0.0f;
    private int destroyTicks = 0;

    public DigControl(Colonist pawn, ServerWorld level) {
        this.pawn = pawn;
        this.level = level;
    }

    public static void addDropToTheResourceManager(ServerWorld w, BlockPos pos, IFortressAwareEntity c) {
        if (ServerExtensionsKt.isSurvivalFortress(c.getServer())) {
            ServerModUtils.getManagersProvider(c).ifPresent(it -> {
                final var blockState = w.getBlockState(pos);
                final var blockEntity = w.getBlockEntity(pos);
                // FIXME: consider the tool and the entity
                final var drop = Block.getDroppedStacks(blockState, w, pos, blockEntity);
                if (!it.getResourceHelper().putItemsToSuitableContainer(drop)) {
                    // FIXME: chests are full!
                    LoggerFactory.getLogger(ServerModUtils.class).error("THE ITEMS ARE NOT SAVED, ALL CHESTS FULL");
                }
            });
        }
    }

    @Override
    public void tick() {
        if(isDone()) return;
        if (!super.canReachTheGoal(pawn) || !pawn.getNavigation().isIdle()) return;

        if (taskBlockInfo instanceof ReplaceTaskBlockInfo replaceTaskBlockInfo) {
            final var canRemove = BuildingHelper.canRemoveBlock(level, goal);
            if (!canRemove) {
                reset();
                return;
            }

            // already placed correct state
            final var expectedBlock = replaceTaskBlockInfo.getState().getBlock();
            final var alreadyCorrectBlockPlaced = level.getBlockState(goal).isOf(expectedBlock);
            if (alreadyCorrectBlockPlaced) {
                reset();
                return;
            }
        }

        if(act()) {
            reset();
        }
    }

    private boolean act() {
        putProperItemInHand();

        pawn.addHunger(ACTIVE_EXHAUSTION);
        if(destroyProgress >= 1.0f){
            this.destroyProgress = 0f;
            addDropToTheResourceManager(level, goal, pawn);
            level.breakBlock(this.goal, false, this.pawn);
            level.emitGameEvent(this.pawn, GameEvent.BLOCK_DESTROY, goal);
            return true;
        } else {
            this.destroyProgress += this.getDestroyProgress(level.getBlockState(goal), pawn, level, goal) / pawn.getHungerMultiplier();
            this.pawn.lookAtGoal();
            if (++destroyTicks % (4 * pawn.getHungerMultiplier()) == 0) {
                this.pawn.swingHand(Hand.MAIN_HAND);
            }
            return false;
        }
    }

    private void putProperItemInHand() {
        final var creative = ServerExtensionsKt.isCreativeFortress(pawn.getServer());

        final BlockState blockState = level.getBlockState(goal);
        Item item = null;
        final String professionId = pawn.getProfessionId();
        if(blockState.isIn(BlockTags.PICKAXE_MINEABLE)) {
            if(creative) {
                item = Items.DIAMOND_PICKAXE;
            } else {
                item = switch (professionId) {
                    case "miner1" -> Items.STONE_PICKAXE;
                    case "miner2" -> Items.IRON_PICKAXE;
                    case "miner3" -> Items.DIAMOND_PICKAXE;
                    default -> Items.WOODEN_PICKAXE;
                };
            }

        } else if (blockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
            if(creative) {
                item = Items.DIAMOND_SHOVEL;
            } else {
                item = switch (professionId) {
                    case "miner1" -> Items.STONE_SHOVEL;
                    case "miner2" -> Items.IRON_SHOVEL;
                    case "miner3" -> Items.DIAMOND_SHOVEL;
                    default -> Items.WOODEN_SHOVEL;
                };
            }
        } else if (blockState.isIn(BlockTags.AXE_MINEABLE)) {
            if(creative) {
                item = Items.DIAMOND_AXE;
            } else {
                item = switch (professionId) {
                    case "miner1", "lumberjack1" -> Items.STONE_AXE;
                    case "miner2", "lumberjack2" -> Items.IRON_AXE;
                    case "miner3", "lumberjack3" -> Items.DIAMOND_AXE;
                    default -> Items.WOODEN_AXE;
                };
            }
        } else if (blockState.isIn(BlockTags.HOE_MINEABLE)) {
            if(creative) {
                item = Items.DIAMOND_HOE;
            } else {
                if("farmer".equals(professionId)) {
                    item = Items.IRON_HOE;
                } else {
                    item = Items.WOODEN_HOE;
                }
            }
        }

        pawn.putItemInHand(item);
    }

    private float getDestroyProgress(BlockState state, Colonist pawn, StructureWorldAccess world, BlockPos pos) {
        final boolean creative = ServerExtensionsKt.isCreativeFortress(pawn.getServer());
        if(creative) return 1.0f;

        float f = state.getHardness(world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            return pawn.getDestroySpeed(state) / f / 100f;
        }
    }

    @Override
    public void reset() {
        super.reset();
        destroyProgress = 0.0f;
        destroyTicks = 0;
    }
}
