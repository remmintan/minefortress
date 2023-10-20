package org.minefortress.renderer.gui.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.resources.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.networking.c2s.C2SRepairBuilding;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.utils.ModUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.minefortress.utils.BlockInfoUtils.convertBlockStatesMapItemsMap;



public class RepairBuildingScreen extends WindowScreen {

    private final UUID buildingId;

    private final Map<Item, Long> requiredItems;
    private Map<Item, Boolean> hasEnoughResources;

    private final IClientResourceManager clientResourceManager;
    private final Set<BlockPos> blocksToRepair;

    private ButtonWidget confirmationButton;

    public RepairBuildingScreen(UUID buildingId, Map<BlockPos, BlockState> blocksToRepair, IClientResourceManager clientResourceManager) {
        super(Text.of("Repair Building"));
        this.buildingId = buildingId;
        this.clientResourceManager = clientResourceManager;
        this.blocksToRepair = blocksToRepair.keySet();

        requiredItems = convertBlockStatesMapItemsMap(blocksToRepair);

        recalculateResources();
    }

    @Override
    public void tick() {
        super.tick();
        recalculateResources();
        confirmationButton.active = !hasEnoughResources.containsValue(false);
    }

    private void recalculateResources() {
        hasEnoughResources = requiredItems.entrySet()
                .stream()
                .map(it -> new ItemInfo(it.getKey(), it.getValue().intValue()))
                .collect((Collectors.toMap(ItemInfo::item, it -> clientResourceManager.hasItems(Collections.singletonList(it)))));
    }

    @Override
    protected void init() {
        super.init();

        confirmationButton = ButtonWidget
            .builder(
                Text.of("Repair"),
                button -> {
                    final var taskId = UUID.randomUUID();

                    ModUtils.getClientTasksHolder().ifPresent(it -> it.addTask(taskId, blocksToRepair));

                    final var packet = new C2SRepairBuilding(taskId, buildingId);
                    FortressClientNetworkHelper.send(C2SRepairBuilding.CHANNEL, packet);
                    Optional.ofNullable(this.client).ifPresent(it -> it.setScreen(null));
                }
            )
            .dimensions(
                    getScreenCenterX() - 10 - 100,
                    getScreenBottomY() - 30,
                    100,
                    20
            )
            .build();
        this.addDrawableChild(confirmationButton);

        final var cancel = ButtonWidget.builder(
                Text.of("Cancel"),
                button -> this.closeScreen()
        )
                .dimensions(
                        getScreenCenterX() + 10,
                        getScreenBottomY() - 30,
                        100,
                        20
                ).build();
        this.addDrawableChild(cancel);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        final var textRenderer = this.textRenderer;

        int x = getScreenLeftX() + 10;
        int y = getScreenTopY() + 30;

        for (Item item : requiredItems.keySet()) {
            drawContext.drawItem(item.getDefaultStack(), x, y);
            final var itemsAmount = requiredItems.get(item);
            final var text = "x" + itemsAmount;
            final var color =  hasEnoughResources.get(item) ? 0xFFFFFF : 0xb81d13;
            drawContext.drawText(textRenderer, text, x + 16 + 2, y + 6, color, false);

            final var columnWidth = 16 + textRenderer.getWidth(text);
            x += columnWidth + 5;
            if (x + columnWidth > getScreenRightX() - 10) {
                x = getScreenLeftX() + 10;
                y += 20;
            }
        }

    }
}
