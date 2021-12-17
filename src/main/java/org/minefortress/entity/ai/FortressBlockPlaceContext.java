package org.minefortress.entity.ai;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class FortressBlockPlaceContext extends ItemPlacementContext {

    private final Direction horizontalDirection;

    public FortressBlockPlaceContext(PlayerEntity p_43631_, Hand p_43632_, ItemStack p_43633_, BlockHitResult p_43634_, Direction horizontalDirection) {
        super(p_43631_, p_43632_, p_43633_, p_43634_);
        this.horizontalDirection = horizontalDirection;
    }

    @Override
    public Direction getPlayerLookDirection() {
        return this.horizontalDirection;
    }
}
