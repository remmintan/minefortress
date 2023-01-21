package org.minefortress.fortress;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.time.LocalDateTime;
import java.util.Iterator;

public interface IAutomationArea {

    Iterator<BlockPos> iterator(World world);
    void update();
    LocalDateTime getUpdated();

}
