package org.minefortress.tasks;

import org.minefortress.tasks.interfaces.Task;

import java.util.*;

public class TaskManager {

    private final Deque<Task> tasks = new ArrayDeque<>();
    private final Set<UUID> cancelledTasks = new HashSet<>();

    public void addTask(Task task) {
        task.prepareTask();
        if(task.hasAvailableParts()) {
            tasks.add(task);
        }
    }

    public boolean hasTask() {
        return !tasks.isEmpty();
    }

    public Task getTask() {
        return tasks.element();
    }

    public boolean nextTaskIdIsNotIn(Set<UUID> set) {
        if (tasks.peek() != null) {
            return !set.contains(tasks.peek().getId());
        }

        return true;
    }

    public void removeTask() {
        tasks.remove();
    }

    public void returnTaskPart(TaskPart taskPart) {
        Task task = taskPart.getTask();
        task.returnPart(taskPart.getStartAndEnd());

        if(!tasks.contains(task)) {
            tasks.addFirst(task);
        }
    }

    public void cancelTask(UUID id) {
        cancelledTasks.add(id);
        tasks.removeIf(task -> task.getId().equals(id));
    }

    public boolean isCancelled(UUID id) {
        return cancelledTasks.contains(id);
    }

}
