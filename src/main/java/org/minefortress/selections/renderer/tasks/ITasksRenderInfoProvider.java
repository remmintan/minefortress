package org.minefortress.selections.renderer.tasks;

public interface ITasksRenderInfoProvider {

    default boolean shouldRender() {
        return true;
    }

}
