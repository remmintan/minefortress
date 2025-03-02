package org.minefortress.interfaces;

import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder;
import net.remmintan.panama.renderer.BlueprintRenderer;
import net.remmintan.panama.renderer.SelectionRenderer;
import net.remmintan.panama.renderer.TasksRenderer;
import org.minefortress.renderer.gui.hud.FortressHud;

public interface IFortressMinecraftClient extends IFortressGamemodeHolder {

    FortressHud get_FortressHud();
    BlueprintRenderer get_BlueprintRenderer();
    SelectionRenderer get_SelectionRenderer();
    TasksRenderer get_TasksRenderer();

}
