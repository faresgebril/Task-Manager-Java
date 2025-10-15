package main.java;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class TodoGUI extends JFrame {
    private transient TaskManager taskManager;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JTextField taskInputField;
    private JComboBox<String> priorityDropdown;
    private JTextField dueDateField;
    private JLabel progressLabel;

    public TodoGUI() {
        taskManager = new TaskManager();
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);

        setTitle("To-Do List");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        taskInputField = new JTextField(20);
        priorityDropdown = new JComboBox<>(new String[] { "High", "Medium", "Low" });
        dueDateField = new JTextField("YYYY-MM-DD", 10);
        JButton addButton = new JButton("Add");
        topPanel.add(new JLabel("Task:"));
        topPanel.add(taskInputField);
        topPanel.add(priorityDropdown);
        topPanel.add(dueDateField);
        topPanel.add(addButton);

        JScrollPane scrollPane = new JScrollPane(taskList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton removeButton = new JButton("Remove");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton sortButton = new JButton("Sort");
        JButton filterButton = new JButton("Filter");
        JButton markCompleteButton = new JButton("Mark as Completed");
        progressLabel = new JLabel("Progress: 0% completed");
        bottomPanel.add(removeButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(loadButton);
        bottomPanel.add(sortButton);
        bottomPanel.add(filterButton);
        bottomPanel.add(markCompleteButton);
        bottomPanel.add(progressLabel);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        saveButton.addActionListener(e -> saveTasks());
        loadButton.addActionListener(e -> loadTasks());
        sortButton.addActionListener(e -> sortTasks());
        filterButton.addActionListener(e -> filterTasks());
        markCompleteButton.addActionListener(e -> markTaskAsCompleted());

        startNotificationService();

        setVisible(true);
    }

    private void addTask() {
        String description = taskInputField.getText();
        String priority = (String) priorityDropdown.getSelectedItem();
        LocalDate dueDate;

        try {
            dueDate = LocalDate.parse(dueDateField.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Task task = new Task(description, priority, dueDate);
        taskManager.addTask(task);
        listModel.addElement(task);
        taskInputField.setText("");
        dueDateField.setText("");
        updateProgress();
    }

    private void removeTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            taskManager.removeTask(selectedTask);
            listModel.removeElement(selectedTask);
            updateProgress();
        }
    }

    private void markTaskAsCompleted() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            if (selectedTask.isCompleted()) {
                selectedTask.markIncomplete();
            } else {
                selectedTask.markComplete();
            }
            taskList.repaint();
            updateProgress();
        } else {
            JOptionPane.showMessageDialog(this, "No task selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTasks() {
        try {
            taskManager.saveToFile("tasks.dat");
            JOptionPane.showMessageDialog(this, "Tasks saved successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadTasks() {
        try {
            taskManager.loadFromFile("tasks.dat");
            listModel.clear();
            taskManager.getTasks().forEach(listModel::addElement);
            JOptionPane.showMessageDialog(this, "Tasks loaded successfully!");
            updateProgress();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void sortTasks() {
        String[] options = { "By Priority", "By Due Date" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Sort tasks by:",
                "Sort Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            taskManager.getTasks().sort((task1, task2) -> {
                int priority1 = getPriorityValue(task1.getPriority());
                int priority2 = getPriorityValue(task2.getPriority());
                return Integer.compare(priority1, priority2);
            });
        } else if (choice == 1) {
            taskManager.getTasks().sort((task1, task2) -> task1.getDueDate().compareTo(task2.getDueDate()));
        }

        refreshTaskList(taskManager.getTasks());
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 1;
            case "Medium":
                return 2;
            case "Low":
                return 3;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private void filterTasks() {
        String[] options = { "High", "Medium", "Low" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Filter tasks by priority:",
                "Filter Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice >= 0) {
            String selectedPriority = options[choice];
            List<Task> filteredTasks = taskManager.getTasks().stream()
                    .filter(task -> selectedPriority.equalsIgnoreCase(task.getPriority()))
                    .collect(Collectors.toList());
            refreshTaskList(filteredTasks);
        }
    }

    private void refreshTaskList(List<Task> tasks) {
        listModel.clear();
        tasks.forEach(listModel::addElement);
    }

    private void updateProgress() {
        long totalTasks = taskManager.getTasks().size();
        long completedTasks = taskManager.getTasks().stream()
                .filter(Task::isCompleted)
                .count();

        if (totalTasks == 0) {
            progressLabel.setText("Progress: 0% completed");
        } else {
            int progress = (int) ((completedTasks * 100) / totalTasks);
            progressLabel.setText("Progress: " + progress + "% completed");
        }
    }

    private void startNotificationService() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();
                List<Task> upcomingTasks = taskManager.getTasks().stream()
                        .filter(task -> !task.isCompleted() && !task.getDueDate().isAfter(today.plusDays(3)))
                        .collect(Collectors.toList());

                if (!upcomingTasks.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        StringBuilder message = new StringBuilder("Upcoming Tasks:\n");
                        for (Task task : upcomingTasks) {
                            message.append(task.toString()).append("\n");
                        }
                        JOptionPane.showMessageDialog(TodoGUI.this, message.toString(),
                                "Task Reminder", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            }
        }, 0, 24 * 60 * 60 * 1000);
    }

    public static void main(String[] args) {
        new TodoGUI();
    }
}
