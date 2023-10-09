package net.remmintan.gobi;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WallsEverySecond extends WallsSelection{

    @Override
    protected List<BlockPos> getSelection(int upDelta, ArrayList<Pair<BlockPos, BlockPos>> cornerPairs) {
        final List<List<BlockPos>> parts = cornerPairs
                .stream()
                .map(p -> BlockPos.iterate(p.getFirst(), p.getSecond().up(upDelta)))
                .map(WallsSelection::iterableToList)
                .map(it -> it.collect(Collectors.toList()))
                .toList();

        List<BlockPos> chessWall = new ArrayList<>();
        for(List<BlockPos> part:parts) {
            for(int i = 0; i < part.size(); i++) {
                if(i % 2 == 0) {
                    chessWall.add(part.get(i));
                }
            }
        }

        return chessWall;
    }

    @Override
    public void update(BlockPos pickedBlock, int upDelta) {
        super.update(pickedBlock, 0);
    }

    @Override
    public boolean selectBlock(World level,
                               Item item,
                               BlockPos pickedBlock,
                               int upDelta,
                               ClickType click,
                               ClientPlayNetworkHandler connection,
                               HitResult hitResult) {
        super.upDelta = 0;
        return super.selectBlock(level, item, pickedBlock, 0, click, connection, hitResult);
    }

    @Override
    protected SelectionType getSelectionType() {
        return SelectionType.WALLS_EVERY_SECOND;
    }
}
