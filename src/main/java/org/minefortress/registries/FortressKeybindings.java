package org.minefortress.registries;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class FortressKeybindings {

    public static final KeyBinding switchSelectionKeybinding;

    public static final KeyBinding cancelTaskKeybinding;

    public static final KeyBinding releaseCameraKeybinding;

    public static final KeyBinding moveSelectionUpKeybinding;
    public static final KeyBinding moveSelectionDownKeybinding;

    static {
        switchSelectionKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minefortress.switch_selection",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_R,
                "category.minefortress.general"
        ));
        cancelTaskKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minefortress.cancel_task",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_Z,
                "category.minefortress.general"
        ));
        releaseCameraKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minefortress.release_camera",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_LEFT_ALT,
                "category.minefortress.general"
        ));
        moveSelectionUpKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minefortress.move_selection_up",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_I,
                "category.minefortress.general"
        ));
        moveSelectionDownKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minefotress.move_selection_down",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_U,
                "category.minefortress.general"
        ));
    }
    public static void init() {
        // do nothing
    }


    public static String getBoundKeyName(KeyBinding keyBinding) {
        return KeyBindingHelper.getBoundKeyOf(keyBinding).getLocalizedText().getString();
    }


}
