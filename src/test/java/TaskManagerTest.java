package test.java;

import main.java.TaskManager;
import main.java.Task;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class TaskManagerTest {

    @Test
    public void testAddTask() {
        TaskManager manager = new TaskManager();
        Task task = new Task("Test Task", "High", LocalDate.now());
        manager.addTask(task);
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    public void testRemoveTask() {
        TaskManager manager = new TaskManager();
        Task task = new Task("Test Task", "High", LocalDate.now());
        manager.addTask(task);
        manager.removeTask(task);
        assertEquals(0, manager.getTasks().size());
    }

    @Test
    public void testSaveAndLoadTasks() throws Exception {
        TaskManager manager = new TaskManager();
        Task task = new Task("Test Task", "High", LocalDate.now());
        manager.addTask(task);
        manager.saveToFile("test_tasks.dat");

        TaskManager loadedManager = new TaskManager();
        loadedManager.loadFromFile("test_tasks.dat");

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals("Test Task", loadedManager.getTasks().get(0).getDescription());
    }
}
