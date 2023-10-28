package org.minefortress.interfaces;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.panama.renderer.BlueprintRenderer;
import net.remmintan.panama.renderer.CampfireRenderer;
import net.remmintan.panama.renderer.SelectionRenderer;
import net.remmintan.panama.renderer.TasksRenderer;
import org.minefortress.renderer.gui.hud.FortressHud;

import java.util.Map;

public interface IFortressMinecraftClient extends IHoveredBlockProvider {

    void open_HireScreen(MinecraftClient client, String screenName1, Map<String, IHireInfo> professions1);
    FortressHud get_FortressHud();
    BlueprintRenderer get_BlueprintRenderer();
    CampfireRenderer get_CampfireRenderer();
    SelectionRenderer get_SelectionRenderer();
    TasksRenderer get_TasksRenderer();
    boolean is_FortressGamemode();

}
