package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.screen.world.WorldScreenOptionGrid;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.minefortress.interfaces.FortressWorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CreateWorldScreen.GameTab.class)
public abstract class GameTabMixin {

    @Unique
    private static final Text SHOW_CAMPFIRE = Text.literal("Campfire enabled");
    @Unique
    private static final Text SHOW_CAMPFIRE_INFO = Text.literal("When enabled you place a campfire in the world otherwise just select village center");
    @Unique
    private static final Text BORDER_ENABLED = Text.literal("Border enabled");
    @Unique
    private static final Text BORDER_ENABLED_INFO = Text.literal("When enabled you have expandable border in Survival mode");

    @Inject(method="<init>", at=@At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void init(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridWidget.Adder  adder) {
        final var worldCreator = createWorldScreen.getWorldCreator();
        if(worldCreator instanceof FortressWorldCreator fortressWorldCreator) {
            WorldScreenOptionGrid.Builder builder = WorldScreenOptionGrid.builder(200).marginLeft(1);
            builder.add(SHOW_CAMPFIRE, fortressWorldCreator::is_ShowCampfire, fortressWorldCreator::set_ShowCampfire).toggleable(() -> true).tooltip(SHOW_CAMPFIRE_INFO);
            builder.add(BORDER_ENABLED, fortressWorldCreator::is_BorderEnabled, fortressWorldCreator::set_BorderEnabled).toggleable(() -> true).tooltip(BORDER_ENABLED_INFO);
            final var grid = builder.build(adder::add);
            worldCreator.addListener(it -> grid.refresh());
        }
    }

}
