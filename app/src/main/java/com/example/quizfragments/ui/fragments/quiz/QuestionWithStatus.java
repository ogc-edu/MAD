// QuestionWithStatus.java
package com.example.quizfragments.ui.fragments.quiz;

public class QuestionWithStatus {
    private int questionId;
    private String questionText;
    private int attempted; // 0: not attempted, 1: correct, 2: incorrect
    private String correctAnswer;
    private String userAnswer;

    public QuestionWithStatus(int questionId, String questionText, int attempted,
                              String correctAnswer, String userAnswer) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.attempted = attempted;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
    }

    public int getQuestionId() {
        return questionId;
    }       //quizQuestionDetail page will use this to query

    public String getQuestionText() {
        return questionText;
    }

    public int getAttempted() {
        return attempted;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }
}