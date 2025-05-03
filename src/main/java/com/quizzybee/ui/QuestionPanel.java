package com.quizzybee.ui;

import com.quizzybee.model.Question;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying a single question with options and explanation
 */
public class QuestionPanel extends JPanel {
    
    private Question question;
    private List<JRadioButton> optionButtons;
    private JButton checkButton;
    private JTextArea explanationArea;
    private JLabel resultLabel;
    
    public QuestionPanel(int questionNumber, Question question) {
        this.question = question;
        this.optionButtons = new ArrayList<>();
        
        setLayout(new BorderLayout(0, 10));
        setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)));
        
        // Create the question header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel numberLabel = new JLabel("Question " + questionNumber);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(numberLabel, BorderLayout.NORTH);
        
        // Question text
        JTextArea questionTextArea = new JTextArea(question.getQuestionText());
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setLineWrap(true);
        questionTextArea.setEditable(false);
        questionTextArea.setBackground(getBackground());
        questionTextArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        headerPanel.add(questionTextArea, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        ButtonGroup buttonGroup = new ButtonGroup();
        
        // Add each option as a radio button
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            char optionLetter = (char) ('A' + i);
            JRadioButton optionButton = new JRadioButton(optionLetter + ") " + options.get(i));
            optionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            buttonGroup.add(optionButton);
            optionsPanel.add(optionButton);
            optionsPanel.add(Box.createVerticalStrut(5));
            optionButtons.add(optionButton);
        }
        
        add(optionsPanel, BorderLayout.CENTER);
        
        // Create bottom panel for check button and explanation
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        
        // Result label
        resultLabel = new JLabel(" ");
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultLabel.setVisible(false);
        
        // Check answer button
        checkButton = new JButton("Check Answer");
        checkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkButton.addActionListener(e -> checkAnswer());
        
        // Explanation area
        explanationArea = new JTextArea(3, 20);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setLineWrap(true);
        explanationArea.setEditable(false);
        explanationArea.setBorder(BorderFactory.createTitledBorder("Explanation"));
        explanationArea.setVisible(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(checkButton);
        
        bottomPanel.add(buttonPanel);
        bottomPanel.add(resultLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(explanationArea);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void checkAnswer() {
        // Find which option was selected
        int selectedIndex = -1;
        for (int i = 0; i < optionButtons.size(); i++) {
            if (optionButtons.get(i).isSelected()) {
                selectedIndex = i;
                break;
            }
        }
        
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an answer option",
                    "No Answer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if the answer is correct
        boolean isCorrect = (selectedIndex == question.getCorrectOptionIndex());
        resultLabel.setText(isCorrect ? "Correct!" : "Incorrect. The correct answer is: " + 
                (char)('A' + question.getCorrectOptionIndex()));
        resultLabel.setForeground(isCorrect ? new Color(0, 128, 0) : Color.RED);
        resultLabel.setVisible(true);
        
        // Show explanation
        explanationArea.setText(question.getExplanation());
        explanationArea.setVisible(true);
        
        // Disable the option buttons and check button
        optionButtons.forEach(button -> button.setEnabled(false));
        checkButton.setEnabled(false);
    }
} 