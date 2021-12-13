package org.minefortress.ai;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class FortressUseOnContext extends ItemUsageContext {

    public FortressUseOnContext(World p_43713_, PlayerEntity p_43714_, Hand p_43715_, ItemStack p_43716_, BlockHitResult p_43717_) {
        super(p_43713_, p_43714_, p_43715_, p_43716_, p_43717_);
    }
}
