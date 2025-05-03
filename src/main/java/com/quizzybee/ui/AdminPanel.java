package com.quizzybee.ui;

import com.quizzybee.auth.UserService;
import com.quizzybee.model.QuizAttempt;
import com.quizzybee.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for managing users and viewing quiz attempts
 */
public class AdminPanel extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JTable usersTable;
    private JTable attemptsTable;
    private DefaultTableModel usersTableModel;
    private DefaultTableModel attemptsTableModel;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton logoutButton;
    private JButton refreshButton;
    
    private UserService userService;
    
    public AdminPanel() {
        userService = UserService.getInstance();
        initUI();
        loadData();
    }
    
    private void initUI() {
        setTitle("QuizzyBee - Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Users tab
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users", usersPanel);
        
        // Quiz Attempts tab
        JPanel attemptsPanel = createAttemptsPanel();
        tabbedPane.addTab("Quiz Attempts", attemptsPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Create table model with column names
        usersTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        usersTableModel.addColumn("Username");
        usersTableModel.addColumn("Full Name");
        usersTableModel.addColumn("Email");
        usersTableModel.addColumn("Role");
        
        // Create table
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setReorderingAllowed(false);
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
        refreshButton = new JButton("Refresh");
        
        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(refreshButton);
        
        // Add action listeners
        addUserButton.addActionListener(e -> showAddUserDialog());
        editUserButton.addActionListener(e -> editSelectedUser());
        deleteUserButton.addActionListener(e -> deleteSelectedUser());
        refreshButton.addActionListener(e -> loadData());
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAttemptsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Create table model with column names
        attemptsTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        attemptsTableModel.addColumn("Student");
        attemptsTableModel.addColumn("Quiz Title");
        attemptsTableModel.addColumn("Date");
        attemptsTableModel.addColumn("Score");
        attemptsTableModel.addColumn("Status");
        
        // Create table
        attemptsTable = new JTable(attemptsTableModel);
        attemptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attemptsTable.getTableHeader().setReorderingAllowed(false);
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(attemptsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewAttemptButton = new JButton("View Details");
        JButton refreshAttemptsButton = new JButton("Refresh");
        
        buttonPanel.add(viewAttemptButton);
        buttonPanel.add(refreshAttemptsButton);
        
        // Add action listeners
        viewAttemptButton.addActionListener(e -> viewSelectedAttempt());
        refreshAttemptsButton.addActionListener(e -> loadData());
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadData() {
        // Clear existing data
        usersTableModel.setRowCount(0);
        attemptsTableModel.setRowCount(0);
        
        // Load users
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            Object[] row = {
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString()
            };
            usersTableModel.addRow(row);
        }
        
        // Load quiz attempts from all students
        List<User> students = userService.getAllStudents();
        for (User student : students) {
            for (QuizAttempt attempt : student.getQuizAttempts()) {
                Object[] row = {
                        student.getFullName(),
                        attempt.getQuiz().getTitle(),
                        attempt.getAttemptDate().toString(),
                        attempt.getScoreAsString(),
                        attempt.isCompleted() ? "Completed" : "In Progress"
                };
                attemptsTableModel.addRow(row);
            }
        }
    }
    
    private void showAddUserDialog() {
        // Create a dialog similar to the registration dialog
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);
        
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);
        
        // Full Name
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(nameField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);
        
        // Role
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Administrator"});
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(roleComboBox, gbc);
        
        // Add button
        JButton addButton = new JButton("Add User");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(addButton, gbc);
        
        addButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String fullName = nameField.getText();
            String email = emailField.getText();
            User.Role role = roleComboBox.getSelectedIndex() == 0 ? User.Role.STUDENT : User.Role.ADMIN;
            
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "All fields are required",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = userService.registerUser(username, password, fullName, email, role);
            
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "User added successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData(); // Refresh the table
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Username already exists",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to edit",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) usersTable.getValueAt(selectedRow, 0);
        User user = userService.getUserByUsername(username);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Selected user not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create edit dialog
        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username (read-only)
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(user.getUsername());
        usernameField.setEditable(false);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);
        
        // Password
        JLabel passwordLabel = new JLabel("New Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);
        
        // Full Name
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField(user.getFullName());
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(nameField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(user.getEmail());
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);
        
        // Role
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Administrator"});
        roleComboBox.setSelectedIndex(user.isAdmin() ? 1 : 0);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(roleComboBox, gbc);
        
        // Update button
        JButton updateButton = new JButton("Update User");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(updateButton, gbc);
        
        updateButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String fullName = nameField.getText();
            String email = emailField.getText();
            User.Role role = roleComboBox.getSelectedIndex() == 0 ? User.Role.STUDENT : User.Role.ADMIN;
            
            if (fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Name and email are required",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update user
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            
            // Only update password if a new one is provided
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            
            userService.updateUser(user);
            JOptionPane.showMessageDialog(dialog,
                    "User updated successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadData(); // Refresh the table
            dialog.dispose();
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) usersTable.getValueAt(selectedRow, 0);
        
        // Don't allow deleting the currently logged-in user
        if (username.equals(userService.getCurrentUser().getUsername())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account while logged in",
                    "Operation Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            boolean success = userService.deleteUser(username);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "User deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete user",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewSelectedAttempt() {
        int selectedRow = attemptsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an attempt to view",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // In a real application, we would retrieve the actual QuizAttempt object
        // and display its details. For now, we'll just show a message.
        JOptionPane.showMessageDialog(this,
                "Viewing details for selected quiz attempt",
                "Quiz Attempt Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        userService.logout();
        
        // Show login screen
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        
        // Close this window
        this.dispose();
    }
} 