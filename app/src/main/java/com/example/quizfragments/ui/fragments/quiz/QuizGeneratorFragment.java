package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.quizfragments.data.api.AI;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Option;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Quiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizGeneratorFragment extends Fragment {
    private EditText topicEditText;
    private EditText numQuestionsEditText;
    private TextView difficultyTextView;
    private Button generateButton;
    private ProgressBar progressBar;
    private String selectedDifficulty = "Medium"; // Default difficulty
    private TextView feedback;
    private Button navToNewQuiz;
    private long newId;
    private String newTitle;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_generator, container, false);

        // Initialize views
        topicEditText = view.findViewById(R.id.topic_edit_text);
        numQuestionsEditText = view.findViewById(R.id.num_questions_edit_text);
        difficultyTextView = view.findViewById(R.id.difficulty_text_view);
        generateButton = view.findViewById(R.id.generate_button);
        progressBar = view.findViewById(R.id.progress_bar);
        feedback = view.findViewById(R.id.ai_feedback);
        navToNewQuiz = view.findViewById(R.id.newQuiz);
        navToNewQuiz.setVisibility(View.GONE);

        // Set up the difficulty selection with custom popup
        difficultyTextView.setText(selectedDifficulty);
        difficultyTextView.setOnClickListener(v -> showDifficultyPopup());

        // Set up the generate button
        generateButton.setOnClickListener(v -> validateAndGenerateQuiz());

        // Set up the back button
        ImageButton backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.difficulty_text_view) {
            menu.setHeaderTitle("Select Difficulty");
            menu.add(Menu.NONE, 1, Menu.NONE, "Easy");
            menu.add(Menu.NONE, 2, Menu.NONE, "Medium");
            menu.add(Menu.NONE, 3, Menu.NONE, "Hard");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                selectedDifficulty = "Easy";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            case 2:
                selectedDifficulty = "Medium";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            case 3:
                selectedDifficulty = "Hard";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void validateAndGenerateQuiz() {
        String topic = topicEditText.getText().toString().trim();
        String numQuestionsStr = numQuestionsEditText.getText().toString().trim();

        // Validate topic
        if (topic.isEmpty()) {
            topicEditText.setError("Please enter a quiz topic");
            return;
        }

        // Validate number of questions
        int numQuestions;
        try {
            numQuestions = Integer.parseInt(numQuestionsStr);
            if (numQuestions < 5 || numQuestions > 20) {
                numQuestionsEditText.setError("Number of questions must be between 5 and 20");
                return;
            }
        } catch (NumberFormatException e) {
            numQuestionsEditText.setError("Please enter a valid number");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);

        // Generate quiz (placeholder function for your AI implementation)
        generateQuizWithAI(topic, numQuestions + 1, selectedDifficulty);
    }

    private void showDifficultyPopup() {
        // Create a custom popup menu
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_difficulty, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setElevation(10);

        // Set up difficulty options
        TextView easyOption = popupView.findViewById(R.id.option_easy);
        TextView mediumOption = popupView.findViewById(R.id.option_medium);
        TextView hardOption = popupView.findViewById(R.id.option_hard);

        // Set up click listeners
        easyOption.setOnClickListener(v -> {
            selectedDifficulty = "Easy";
            difficultyTextView.setText(selectedDifficulty);
            popupWindow.dismiss();
        });

        mediumOption.setOnClickListener(v -> {
            selectedDifficulty = "Medium";
            difficultyTextView.setText(selectedDifficulty);
            popupWindow.dismiss();
        });

        hardOption.setOnClickListener(v -> {
            selectedDifficulty = "Hard";
            difficultyTextView.setText(selectedDifficulty);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(difficultyTextView, 0, 0);
    }

    private void generateQuizWithAI(String topic, int numQuestions, String difficulty) {
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        feedback.setText("");
        generateButton.setText("Generating...");
        String prompt = "Generate " + numQuestions + " multiple-choice quiz questions about the topic " +  topic + "\n" +
                "The difficulty of question is " + difficulty + "\n" +
                "Each question must be in JSON format with the following structure:\n" +
                "The first JSON object in JSON array gives the title(short one will do), description and question count for quiz, and must be in the format" +
                "\n" +
                "  \"title\": \"string\",\n" +
                "  \"description\": \"string\",\n" +
                "  \"question_count\": \"int\",\n" +
                "{\n" +
                "  \"question\": \"string\",\n" +
                "  \"options\": [\"string\", \"string\", \"string\", \"string\"],\n" +
                "  \"answer\": \"int\",\n" +
                "  \"explanation\": \"string\"\n" +
                "}\n" +
                "Return the entire output as a JSON array.\n" +
                "Do not include any extra text or explanation outside the JSON array.\n" +
                "The answer is an int from 0 to 3 corresponding to the correct option index." +
                "Make sure u have 2 questions excluding the metadata\n";
        AI.modelCall(prompt, new AI.ResponseCallback(){
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            public void onResponse(String result){
                result = result.replace("```json", "")
                        .replace("```", "")
                        .trim();
                try {
                    JSONArray jsonArray = new JSONArray(result);        //turn string response into array of JSON
                    JSONObject quizObject = jsonArray.getJSONObject(0); //first object in array is title & description

                    //create component objects then store in db using dao
                    Quiz quiz = new Quiz();
                    quiz.title = quizObject.getString("title");
                    quiz.description = quizObject.getString("description");
                    quiz.questionCount = jsonArray.length() - 1;;

                    // Use a single thread for the entire database operation
                    new Thread(() -> {
                        try {
                            long quizId = db.quizDao().insert(quiz);    //get id after inserting for fk constraints
                            newId = quizId;
                            newTitle = quiz.title;

                            for (int i = 1; i < jsonArray.length(); i++) {      //start from 1 because 0 is title & desc for quiz
                                JSONObject obj = jsonArray.getJSONObject(i);

                                //question, 10 questions in 1 response
                                Question question = new Question();
                                question.quizId = (int) quizId;  // Use the actual quizId from the database
                                question.questionText = obj.getString("question");
                                question.attempted = 0;
                                question.userAnswer = -1;
                                question.questionNumber = i;
                                long questionId = db.questionDao().insert(question);

                                //options, 4 options for each question, so for loop runs 4 iterations
                                JSONArray optionsArray = obj.getJSONArray("options");
                                int correctAnswerIndex = obj.getInt("answer");
                                for (int y = 0; y < optionsArray.length(); y++) {
                                    Option option = new Option();
                                    option.optionText = optionsArray.getString(y);
                                    option.isCorrect = (y == correctAnswerIndex);
                                    option.questionId = (int) questionId;
                                    db.optionDao().insert(option);
                                }
                            }

                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                feedback.setText("Quiz generated successfully!");
                                feedback.setTextColor(requireContext().getColor(R.color.aiSuccess));
                                feedback.setVisibility(View.VISIBLE);
                                navToNewQuiz.setVisibility(View.VISIBLE);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                feedback.setText("Error occurred, please try again");
                                feedback.setTextColor(requireContext().getColor(R.color.aiFailed));
                                feedback.setVisibility(View.VISIBLE);
                            });
                        }
                    }).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                    feedback.setText("AI response failed, please try again later");
                } finally{
                    generateButton.setText("Generate Quiz");
                }
            }
            public void onError(String result){

            }
        });
        navToNewQuiz.setOnClickListener(v -> {      //after get response only show
            QuizQuestionsFragment questionsFragment = QuizQuestionsFragment.newInstance((int)newId, newTitle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, questionsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}