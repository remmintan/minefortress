package org.minefortress.entity.ai.professions;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;

import java.util.Collections;

public class CrafterDailyTask extends AbstractStayNearBlockDailyTask {

    private int ticksAfterTableClose = 0;

    @Override
    public boolean canStart(Colonist colonist) {
        return craftingTableMenuOpened(colonist);
    }


    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Crafting");
        super.start(colonist);
    }

    @Override
    public void tick(Colonist colonist) {
        super.tick(colonist);
        if (craftingTableMenuOpened(colonist)) {
            ticksAfterTableClose = 400;
        } else {
            ticksAfterTableClose--;
        }
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return super.shouldContinue(colonist) && ticksAfterTableClose > 0 || craftingTableMenuOpened(colonist);
    }

    @Override
    public void stop(Colonist colonist) {
        super.stop(colonist);
        this.ticksAfterTableClose = 0;
    }

    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
        // Use ServerModUtils to get buildings directly
        final var buildings = ServerModUtils.getManagersProvider(colonist)
                .map(IServerManagersProvider::getBuildingsManager)
                .map(it -> it.getBuildings(ProfessionType.CRAFTSMAN))
                .orElse(Collections.emptyList());

        if (buildings.isEmpty()) {
            return null;
        }

        return buildings.get(0).getCenter();
    }

    // TODO get rid of this
    private boolean craftingTableMenuOpened(Colonist colonsit) {
        return ServerExtensionsKt.fortressOwnerHasScreenOpened(colonsit.getServer(), colonsit.getFortressPos(), FortressCraftingScreenHandler.class);
    }

}
