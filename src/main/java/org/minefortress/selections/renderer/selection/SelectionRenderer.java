package org.minefortress.selections.renderer.selection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.math.Vec3f;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.FortressRenderLayer;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.SelectionManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SelectionRenderer extends AbstractCustomRenderer {

    private static final Vec3f WRONG_PLACEMENT_COLOR = new Vec3f(1.0F, 0.5F, 0.5F);
    private static final Vec3f CORRECT_PLACEMENT_COLOR = new Vec3f(1F, 1.0F, 1F);

    private final SelectionManager selectionManager;
    private final SelectionModelBuilder selectionModelBuilder;

    public SelectionRenderer(MinecraftClient client, Map<RenderLayer, BufferBuilder> selectionBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        super(client);
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
        this.selectionManager = fortressClient.getSelectionManager();
        this.selectionModelBuilder = new SelectionModelBuilder(selectionBufferBuilderStorage, blockBufferBuilderStorage, selectionManager);
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(selectionModelBuilder.getBuiltSelection());
    }

    @Override
    protected boolean shouldRender() {
        return !client.options.hudHidden && selectionManager.isSelecting();
    }

    @Override
    public void prepareForRender() {
        if(shouldRender() && selectionManager.isNeedsUpdate()) {
            selectionModelBuilder.build();
            selectionManager.setNeedsUpdate(false);
        }
    }

    @Override
    public void close() {
        selectionModelBuilder.close();
    }

    @Override
    protected List<RenderLayer> getRenderLayers() {
        return Arrays.asList(
                RenderLayer.getLines(),
                RenderLayer.getSolid(),
                RenderLayer.getCutoutMipped(),
                RenderLayer.getCutout(),
                FortressRenderLayer.getLinesNoDepth()
        );
    }

    @Override
    protected Vec3f getColorModulator() {
        return selectionManager.isInCorrectState() ? CORRECT_PLACEMENT_COLOR : WRONG_PLACEMENT_COLOR;
    }
}
