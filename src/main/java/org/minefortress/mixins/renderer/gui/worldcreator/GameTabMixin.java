package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CreateWorldScreen.GameTab.class)
public abstract class GameTabMixin {

    @Unique
    private static final Text FORTRESS_GAMEMODE = Text.literal("Fortress gamemode");
    @Unique
    private static final Text FORTRESS_GAMEMODE_DESCRIPTION = Text.literal("Survival / Creative fortress gamemode");

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void init(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridWidget.Adder adder) {
        final var worldCreator = createWorldScreen.getWorldCreator();
        if (worldCreator instanceof IFortressGamemodeHolder IFortressGamemodeHolder) {
            CyclingButtonWidget<FortressGamemode> fortressGamemodeButton = adder.add(
                    CyclingButtonWidget
                            .builder((FortressGamemode value) -> switch (value) {
                                case CREATIVE -> Text.literal("Creative Fortress");
                                case SURVIVAL -> Text.literal("Survival Fortress");
                            })
                            .values(FortressGamemode.values())
                            .build(0, 0, 210, 20, FORTRESS_GAMEMODE,
                                    (button, value) -> IFortressGamemodeHolder.set_fortressGamemode(value)
                            )
            );

            // Update button value when worldCreator changes
            createWorldScreen.getWorldCreator().addListener(creator -> {
                if (creator instanceof IFortressGamemodeHolder fCreator) {
                    fortressGamemodeButton.setValue(fCreator.get_fortressGamemode());
                    fortressGamemodeButton.setTooltip(Tooltip.of(FORTRESS_GAMEMODE_DESCRIPTION));
                    fortressGamemodeButton.visible = creator.getGameMode() == WorldCreator.Mode.DEBUG;
                }
            });
        }
    }

}
