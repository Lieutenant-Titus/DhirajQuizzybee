package com.quizzybee.model;

import java.util.List;

/**
 * Represents a multiple-choice question
 */
public class Question {
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;

    public Question(String questionText, List<String> options, int correctOptionIndex, String explanation) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getCorrectAnswer() {
        if (correctOptionIndex >= 0 && correctOptionIndex < options.size()) {
            return options.get(correctOptionIndex);
        }
        return "Unknown";
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(questionText).append("\n");
        
        for (int i = 0; i < options.size(); i++) {
            char optionLetter = (char) ('A' + i);
            sb.append(optionLetter).append(") ").append(options.get(i)).append("\n");
        }
        
        char correctLetter = (char) ('A' + correctOptionIndex);
        sb.append("Correct Answer: ").append(correctLetter).append("\n");
        sb.append("Explanation: ").append(explanation).append("\n");
        
        return sb.toString();
    }
} 