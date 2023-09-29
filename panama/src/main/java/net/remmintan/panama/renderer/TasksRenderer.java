package net.remmintan.panama.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.remmintan.panama.model.BuiltModel;
import org.minefortress.selections.renderer.tasks.ITasksModelBuilderInfoProvider;
import org.minefortress.selections.renderer.tasks.ITasksRenderInfoProvider;
import net.remmintan.panama.model.builder.TasksModelBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TasksRenderer extends AbstractCustomRenderer {

    private final TasksModelBuilder modelBuilder;
    private final Supplier<ITasksRenderInfoProvider> tasksHolderSupplier;

    public TasksRenderer(
         MinecraftClient client,
         BufferBuilder bufferBuilder,
         Supplier<ITasksRenderInfoProvider> tasksHolderSupplier,
         Supplier<ITasksModelBuilderInfoProvider> tasksModelBuilderInfoProviderSupplier
    ) {
        super(client);
        modelBuilder = new TasksModelBuilder(bufferBuilder, tasksModelBuilderInfoProviderSupplier);
        this.tasksHolderSupplier = tasksHolderSupplier;
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(modelBuilder.getBuiltTasks());
    }

    @Override
    protected boolean shouldRender() {
        final ITasksRenderInfoProvider tasksHolder = getTasksHolder();
        return !client.options.hudHidden && tasksHolder.shouldRender();
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

    private ITasksRenderInfoProvider getTasksHolder() {
        return tasksHolderSupplier.get();
    }
}
