package com.quizzybee.ui;

import com.quizzybee.auth.UserService;
import com.quizzybee.model.Question;
import com.quizzybee.model.QuizAttempt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame for displaying and taking quizzes
 */
public class QuizTakingFrame extends JFrame {
    
    private QuizAttempt quizAttempt;
    private UserService userService;
    
    public QuizTakingFrame(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
        this.userService = UserService.getInstance();
        
        initUI();
    }
    
    private void initUI() {
        setTitle("QuizzyBee - Take Quiz");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create a panel to display the quiz questions
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Add quiz title
        JLabel titleLabel = new JLabel(quizAttempt.getQuiz().getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        quizPanel.add(titleLabel);
        quizPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Add a scrollable panel for questions
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        
        // Add questions to the panel
        List<Question> questions = quizAttempt.getQuiz().getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            int questionIndex = i;
            
            // Create a panel for this question
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                    new EmptyBorder(10, 0, 10, 0)
            ));
            questionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Question text
            JLabel questionLabel = new JLabel((i + 1) + ". " + question.getQuestionText());
            questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            questionPanel.add(questionLabel);
            questionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            
            // Radio buttons for options
            ButtonGroup optionsGroup = new ButtonGroup();
            List<JRadioButton> optionButtons = new ArrayList<>();
            
            for (int j = 0; j < question.getOptions().size(); j++) {
                String option = question.getOptions().get(j);
                JRadioButton optionButton = new JRadioButton(option);
                optionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                final int optionIndex = j;
                optionButton.addActionListener(e -> {
                    // Save the selected answer
                    quizAttempt.addAnswer(questionIndex, optionIndex);
                });
                
                optionsGroup.add(optionButton);
                optionButtons.add(optionButton);
                questionPanel.add(optionButton);
                questionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            
            questionsPanel.add(questionPanel);
            questionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        quizPanel.add(scrollPane);
        
        // Add submit button
        JButton submitButton = new JButton("Submit Quiz");
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.addActionListener(e -> {
            // Calculate score
            quizAttempt.calculateScore();
            quizAttempt.setCompleted(true);
            
            // Update user data
            userService.updateUser(userService.getCurrentUser());
            
            // Show results
            JOptionPane.showMessageDialog(this,
                    "Quiz completed!\nYour score: " + quizAttempt.getScoreAsString() +
                            "\nCorrect answers: " + quizAttempt.getCorrectAnswersCount() + 
                            " out of " + questions.size(),
                    "Quiz Results",
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Close quiz frame
            dispose();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);
        quizPanel.add(buttonPanel);
        
        add(quizPanel);
    }
} 