package net.remmintan.mods.minefortress.core.interfaces.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;

import java.util.List;

public interface ISelection {
    boolean isSelecting();

    boolean needUpdate(BlockPos pickedBlock, int upDelta);

    boolean selectBlock(
            World level,
            Item mainHandItem,
            BlockPos pickedBlock,
            int upDelta,
            ClickType click,
            ClientPlayNetworkHandler clientPacketListener,
            HitResult hitResult
    );

    void update(
            BlockPos pickedBlock,
            int upDelta
    );

    List<BlockPos> getSelection();

    void reset();

    List<Pair<Vec3i, Vec3i>> getSelectionDimensions();

    List<Pair<Vec3d, String>> getSelectionLabelsPosition();
}
