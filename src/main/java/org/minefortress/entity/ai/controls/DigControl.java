package org.minefortress.entity.ai.controls;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.AbstractFortressManager;

public class DigControl extends PositionedActionControl {

    private final Colonist colonist;
    private final ServerWorld level;

    private float destroyProgress = 0.0f;
    private int destroyTicks = 0;

    public DigControl(Colonist colonist, ServerWorld level) {
        this.colonist = colonist;
        this.level = level;
    }

    @Override
    public void tick() {
        if(isDone()) return;
        if(!super.canReachTheGoal(colonist) || !colonist.getNavigation().isIdle()) return;

        if(act()) {
            reset();
        }
    }

    private boolean act() {
        putProperItemInHand();

        colonist.addExhaustion(0.1f);
        if(destroyProgress >= 1.0f){
            this.destroyProgress = 0f;
            addDropToTheResourceManager(level, goal, colonist);
            level.breakBlock(this.goal, false, this.colonist);
            level.emitGameEvent(this.colonist, GameEvent.BLOCK_DESTROY, goal);
            return true;
        } else {
            this.destroyProgress += this.getDestroyProgress(level.getBlockState(goal), colonist, level, goal);
            this.colonist.lookAtGoal();
            if(++destroyTicks % 4 == 0) {
                this.colonist.swingHand(Hand.MAIN_HAND);
            }
            return false;
        }
    }

    public static void addDropToTheResourceManager(ServerWorld w, BlockPos g, Colonist c) {
        final var blockState = w.getBlockState(g);
        final var blockEntity = blockState instanceof BlockEntityProvider provider ? provider.createBlockEntity(g, blockState) : null;
        final var drop = Block.getDroppedStacks(blockState, w, g, blockEntity);

        c.doActionOnMasterPlayer(p -> {
            final var fortressServerManager = p.getFortressServerManager();
            if(fortressServerManager.isSurvival()) {
                final var serverResourceManager = fortressServerManager.getServerResourceManager();
                for (ItemStack itemStack : drop) {
                    final var item = itemStack.getItem();
                    final var count = itemStack.getCount();
                    serverResourceManager.increaseItemAmount(item, count);
                }
            }
        });
    }

    private void putProperItemInHand() {
        final Boolean creative = colonist.getFortressManager().map(AbstractFortressManager::isCreative).orElse(false);

        final BlockState blockState = level.getBlockState(goal);
        Item item = null;
        final String professionId = colonist.getProfessionId();
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

        colonist.putItemInHand(item);
    }

    private float getDestroyProgress(BlockState p_60466_, Colonist p_60467_, StructureWorldAccess p_60468_, BlockPos p_60469_) {
        final boolean creative = colonist
                .getFortressManager()
                .map(AbstractFortressManager::isCreative)
                .orElse(false);
        if(creative) return 1.0f;

        float f = p_60466_.getHardness(p_60468_, p_60469_);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            return p_60467_.getDestroySpeed(p_60466_) / f / 100f;
        }
    }

    @Override
    public void reset() {
        super.reset();
        destroyProgress = 0.0f;
        destroyTicks = 0;
    }
}
