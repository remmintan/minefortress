package org.minefortress.registries;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class FortressKeybindings {

    public static final KeyBinding switchSelectionKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minefortress.switch_selection",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_R,
            "category.minefortress.general"
    ));

    public static final KeyBinding cancelTaskKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minefortress.cancel_task",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_Z,
            "category.minefortress.general"
    ));

}
