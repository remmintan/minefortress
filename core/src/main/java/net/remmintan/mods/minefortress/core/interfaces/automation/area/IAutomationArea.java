package net.remmintan.mods.minefortress.core.interfaces.automation.area;

import net.minecraft.world.World;

import java.time.LocalDateTime;
import java.util.Iterator;

public interface IAutomationArea {

    Iterator<IAutomationBlockInfo> iterator(World world);
    void update();
    LocalDateTime getUpdated();

}
