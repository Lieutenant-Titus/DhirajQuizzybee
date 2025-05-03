package com.quizzybee;

import com.formdev.flatlaf.FlatLightLaf;
import com.quizzybee.ui.LoginFrame;

import javax.swing.*;

/**
 * Main application class for QuizzyBee
 * A PDF syllabus to MCQ generator application
 */
public class QuizzyBeeApp {
    
    public static void main(String[] args) {
        // Set up the look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF");
        }
        
        // Run the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "Error starting application: " + e.getMessage(),
                        "Application Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
} 