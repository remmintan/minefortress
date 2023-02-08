package org.minefortress.fortress;

import net.minecraft.world.World;
import org.minefortress.fortress.automation.AutomationBlockInfo;

import java.time.LocalDateTime;
import java.util.Iterator;

public interface IAutomationArea {

    Iterator<AutomationBlockInfo> iterator(World world);
    void update();
    LocalDateTime getUpdated();

}
