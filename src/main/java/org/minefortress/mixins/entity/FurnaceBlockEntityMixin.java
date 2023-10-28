package org.minefortress.mixins.entity;


import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(FurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin extends AbstractFurnaceBlockEntity {

    protected FurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, pos, state, recipeType);
    }

    @Override
    public List<Recipe<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        if(world == null) throw new IllegalStateException("World is null");
        final var isFortress = world.getServer() instanceof IFortressServer;
        if(isFortress)
            return Collections.emptyList();
        else
            return super.getRecipesUsedAndDropExperience(world, pos);
    }
}
