package org.minefortress.selections.renderer.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import org.minefortress.selections.ClientSelection;
import org.minefortress.tasks.ClientTasksHolder;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TasksModelBuilder {

    private final BufferBuilder bufferBuilder;
    private final Supplier<ClientTasksHolder> tasksHolderSupplier;

    private BuiltTasks builtTasks;

    public TasksModelBuilder(BufferBuilder bufferBuilder, Supplier<ClientTasksHolder> tasksHolderSupplier) {
        this.bufferBuilder = bufferBuilder;
        this.tasksHolderSupplier = tasksHolderSupplier;
    }

    public void build() {
        final ClientTasksHolder tasksHolder = getTasksHolder();

        if(!tasksHolder.isNeedRebuild()) return;
        tasksHolder.setNeedRebuild(false);

        if(this.builtTasks != null)
            builtTasks.close();

        final Set<ClientSelection> allBuildTasks = tasksHolder.getAllBuildTasks();
        final Set<ClientSelection> allRemoveTasks = tasksHolder.getAllRemoveTasks();
        builtTasks = new BuiltTasks(allBuildTasks, allRemoveTasks);
        builtTasks.build(bufferBuilder);
    }

    public BuiltTasks getBuiltTasks() {
        return builtTasks;
    }

    public void close() {
        if(builtTasks != null)
            builtTasks.close();
    }

    private ClientTasksHolder getTasksHolder() {
        return tasksHolderSupplier.get();
    }
}
