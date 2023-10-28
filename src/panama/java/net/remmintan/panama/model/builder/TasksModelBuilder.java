package net.remmintan.panama.model.builder;

import net.minecraft.client.render.BufferBuilder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksModelBuilderInfoProvider;
import net.remmintan.panama.model.BuiltTasks;

import java.util.Set;
import java.util.function.Supplier;

public class TasksModelBuilder {

    private final BufferBuilder bufferBuilder;
    private final Supplier<ITasksModelBuilderInfoProvider> tasksHolderSupplier;

    private BuiltTasks builtTasks;

    public TasksModelBuilder(BufferBuilder bufferBuilder, Supplier<ITasksModelBuilderInfoProvider> tasksHolderSupplier) {
        this.bufferBuilder = bufferBuilder;
        this.tasksHolderSupplier = tasksHolderSupplier;
    }

    public void build() {
        final ITasksModelBuilderInfoProvider tasksHolder = getTasksHolder();

        if(!tasksHolder.isNeedRebuild()) return;
        tasksHolder.setNeedRebuild(false);

        if(this.builtTasks != null)
            builtTasks.close();

        final Set<IClientTask> allBuildTasks = tasksHolder.getAllSelections();
        builtTasks = new BuiltTasks(allBuildTasks);
        builtTasks.build(bufferBuilder);
    }

    public BuiltTasks getBuiltTasks() {
        return builtTasks;
    }

    public void close() {
        if(builtTasks != null)
            builtTasks.close();
    }

    private ITasksModelBuilderInfoProvider getTasksHolder() {
        return tasksHolderSupplier.get();
    }
}
