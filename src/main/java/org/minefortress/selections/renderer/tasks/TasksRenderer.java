package org.minefortress.selections.renderer.tasks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.tasks.ClientTasksHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TasksRenderer extends AbstractCustomRenderer {

    private final TasksModelBuilder modelBuilder;
    private final Supplier<ClientTasksHolder> tasksHolderSupplier;

    public TasksRenderer(MinecraftClient client, BufferBuilder bufferBuilder, Supplier<ClientTasksHolder> tasksHolderSupplier) {
        super(client);
        modelBuilder = new TasksModelBuilder(bufferBuilder, tasksHolderSupplier);
        this.tasksHolderSupplier = tasksHolderSupplier;
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(modelBuilder.getBuiltTasks());
    }

    @Override
    protected boolean shouldRender() {
        final ClientTasksHolder tasksHolder = getTasksHolder();
        return !tasksHolder.isEmpty() && !tasksHolder.isSelectionHidden();
    }

    @Override
    public void prepareForRender() {
        modelBuilder.build();
    }

    @Override
    public void close() {
        modelBuilder.close();
    }

    @Override
    protected List<RenderLayer> getRenderLayers() {
        return Collections.singletonList(RenderLayer.getLines());
    }

    private ClientTasksHolder getTasksHolder() {
        return tasksHolderSupplier.get();
    }
}
