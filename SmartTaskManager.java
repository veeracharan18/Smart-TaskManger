import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

class Task {
    private final String name;
    private final int priority;

    public Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "🔹 " + name + " (Priority: " + priority + ")";
    }
}

public class SmartTaskManager extends Application {

    private final List<Task> tasks = new ArrayList<>();
    private final ListView<String> taskListView = new ListView<>();
    private final Label statusLabel = new Label();
    private final TextArea aiChatArea = new TextArea();
    private final TextField aiInputField = new TextField();
    private final Deque<String> chatHistory = new LinkedList<>();

    @Override
    public void start(Stage stage) {
        // --- Task input section ---
        TextField taskNameInput = new TextField();
        taskNameInput.setPromptText("Task name");

        TextField priorityInput = new TextField();
        priorityInput.setPromptText("Priority (1-10)");

        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            String name = taskNameInput.getText().trim();
            String priorityStr = priorityInput.getText().trim();

            if (name.isEmpty() || priorityStr.isEmpty()) {
                statusLabel.setText("Please fill in both fields.");
                return;
            }

            try {
                int priority = Integer.parseInt(priorityStr);
                if (priority < 1 || priority > 10) {
                    statusLabel.setText("Priority must be between 1 and 10.");
                    return;
                }

                tasks.add(new Task(name, priority));
                tasks.sort(Comparator.comparingInt(Task::getPriority).reversed());
                updateTaskListView();

                taskNameInput.clear();
                priorityInput.clear();
                statusLabel.setText("Task added.");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Priority must be a number.");
            }
        });

        taskListView.setOnMouseClicked(e -> {
            int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < tasks.size()) {
                tasks.remove(selectedIndex);
                updateTaskListView();
                statusLabel.setText("Task removed.");
            }
        });

        VBox taskBox = new VBox(10,
                new Label("Add Task"),
                taskNameInput,
                priorityInput,
                addButton,
                new Label("Tasks (click to delete):"),
                taskListView,
                statusLabel
        );
        taskBox.setPadding(new Insets(10));
        taskBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #42a5f5;");
        taskListView.setPrefHeight(180);

        // --- AI Assistant section ---
        aiChatArea.setEditable(false);
        aiChatArea.setWrapText(true);
        aiChatArea.setPrefHeight(180);

        Button sendButton = new Button("Ask");
        sendButton.setOnAction(e -> {
            String userMessage = aiInputField.getText().trim();
            if (!userMessage.isEmpty()) {
                chatHistory.add("You: " + userMessage);
                String response = generateAIResponse(userMessage);
                chatHistory.add("AI: " + response);
                updateAIChatArea();
                aiInputField.clear();
            }
        });

        HBox aiInputBox = new HBox(10, aiInputField, sendButton);
        aiInputBox.setAlignment(Pos.CENTER);

        VBox aiBox = new VBox(10,
                new Label("AI Assistant"),
                aiChatArea,
                aiInputBox
        );
        aiBox.setPadding(new Insets(10));
        aiBox.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ffb74d;");

        // --- Main layout ---
        VBox root = new VBox(20, taskBox, aiBox);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 520, 620);
        stage.setScene(scene);
        stage.setTitle("Smart Task Manager");
        stage.show();
    }

    private void updateTaskListView() {
        taskListView.getItems().clear();
        for (Task task : tasks) {
            taskListView.getItems().add(task.toString());
        }
    }

    private void updateAIChatArea() {
        aiChatArea.clear();
        for (String msg : chatHistory) {
            aiChatArea.appendText(msg + "\n");
        }
    }

    private String generateAIResponse(String input) {
        input = input.toLowerCase();

        if (input.contains("how many")) {
            return "You have " + tasks.size() + " task(s).";
        } else if (input.contains("highest") || input.contains("top priority")) {
            if (!tasks.isEmpty()) {
                Task top = tasks.get(0);
                return "Top task: " + top.getName() + " (Priority: " + top.getPriority() + ")";
            } else {
                return "You have no tasks.";
            }
        } else if (input.contains("delete") || input.contains("remove")) {
            return "Click on a task in the list to delete it.";
        } else if (input.contains("sort")) {
            return "Tasks are already sorted by priority (highest first).";
        } else if (input.contains("hello") || input.contains("hi")) {
            return "Hey there! How can I assist you with your tasks?";
        } else {
            return "Try asking how many tasks you have or what the top priority is.";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
