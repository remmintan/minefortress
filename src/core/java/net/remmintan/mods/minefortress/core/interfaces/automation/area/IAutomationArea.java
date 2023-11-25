package net.remmintan.mods.minefortress.core.interfaces.automation.area;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

public interface IAutomationArea {
    UUID getId();
    Iterator<IAutomationBlockInfo> iterator(World world);
    void update();
    LocalDateTime getUpdated();
    default boolean isEmpty(World world) {
        return !iterator(world).hasNext();
    }
    default void sendFinishMessage(ServerPlayerEntity entity) {}

}
