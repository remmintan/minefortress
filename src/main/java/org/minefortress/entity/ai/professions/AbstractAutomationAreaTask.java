package org.minefortress.entity.ai.professions;

import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.minefortress.entity.Colonist;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

abstract class AbstractAutomationAreaTask implements ProfessionDailyTask{

    protected IAutomationArea area;
    protected Iterator<IAutomationBlockInfo> iterator;
    private long stopTime = 0L;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && isEnoughTimeSinceLastTimePassed(colonist);
    }

    @Override
    public void start(Colonist colonist) {
        colonist.resetControls();
        colonist.setCurrentTaskDesc(getTaskDesc());
        getArea(colonist).ifPresent(f -> this.area = f);
        initIterator(colonist.getWorld());
    }

    @Override
    public void stop(Colonist colonist) {
        this.stopTime = colonist.getWorld().getTime();
        this.area = null;
        this.iterator = Collections.emptyIterator();
        colonist.resetControls();
    }

    protected abstract ProfessionType getProfessionType();

    protected abstract String getTaskDesc();

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.getWorld().getTime() - stopTime > 100;
    }

    private void initIterator(World world) {
        if(this.area == null) {
            this.iterator = Collections.emptyIterator();
        } else {
            this.area.update();
            this.iterator = this.area.iterator(world);
        }
    }

    private Optional<IAutomationArea> getArea(Colonist colonist) {
        return colonist
                .getServerFortressManager()
                .flatMap(it -> it.getAutomationAreaByProfessionType(getProfessionType(),
                                    colonist.getMasterPlayer().orElse(null)));
    }

}
