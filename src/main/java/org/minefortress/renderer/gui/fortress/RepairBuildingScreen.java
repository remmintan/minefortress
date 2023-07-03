package org.minefortress.renderer.gui.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.client.ClientResourceManager;
import org.minefortress.network.c2s.C2SRepairBuilding;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.WindowScreen;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.minefortress.utils.BlockInfoUtils.convertBlockStatesMapItemsMap;

public class RepairBuildingScreen extends WindowScreen {

    private final UUID buildingId;
    private final Map<BlockPos, BlockState> blocksToRepair;

    private final Map<Item, Long> requiredItems;
    private Map<Item, Boolean> hasEnoughResources;

    private final ClientResourceManager clientResourceManager;

    private ButtonWidget confirmationButton;

    public RepairBuildingScreen(UUID buildingId, Map<BlockPos, BlockState> blocksToRepair, ClientResourceManager clientResourceManager) {
        super(Text.of("Repair Building"));
        this.buildingId = buildingId;
        this.blocksToRepair = blocksToRepair;
        this.clientResourceManager = clientResourceManager;

        requiredItems = convertBlockStatesMapItemsMap(blocksToRepair);

        for (Item item : Collections.unmodifiableSet(requiredItems.keySet())) {
            final var defaultStack = item.getDefaultStack();
            if (defaultStack.isIn(ItemTags.DOORS) || defaultStack.isIn(ItemTags.BEDS)) {
                final var numberOfRequiredItems = requiredItems.get(item);
                requiredItems.put(item, numberOfRequiredItems / 2);
            }
        }

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

        confirmationButton = new ButtonWidget(
                getScreenCenterX() - 10 - 100,
                getScreenBottomY() - 30,
                100,
                20,
                Text.of("Repair"),
                button -> {
                    final var packet = new C2SRepairBuilding(buildingId);
                    FortressClientNetworkHelper.send(C2SRepairBuilding.CHANNEL, packet);
                }
        );
        this.addDrawableChild(confirmationButton);

        this.addDrawableChild(
                new ButtonWidget(
                        getScreenCenterX() + 10,
                        getScreenBottomY() - 30,
                        100,
                        20,
                        Text.of("Cancel"),
                        button -> this.closeScreen()
                )
        );
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        final var itemRenderer =  this.itemRenderer;
        final var textRenderer = this.textRenderer;

        int x = getScreenLeftX() + 10;
        int y = getScreenTopY() + 30;

        for (Item item : requiredItems.keySet()) {
            itemRenderer.renderInGuiWithOverrides(item.getDefaultStack(), x, y);
            final var itemsAmount = requiredItems.get(item);
            final var text = "x" + itemsAmount;
            final var color =  hasEnoughResources.get(item) ? 0xFFFFFF : 0xb81d13;
            textRenderer.draw(matrices, text, x + 16 + 2, y + 6, color);

            final var columnWidth = 16 + textRenderer.getWidth(text);
            x += columnWidth + 5;
            if (x + columnWidth > getScreenRightX() - 10) {
                x = getScreenLeftX() + 10;
                y += 20;
            }
        }

    }
}
