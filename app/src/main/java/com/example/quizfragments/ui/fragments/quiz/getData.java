package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.api.AI;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Option;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Quiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class getData extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.getdata_layout, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button call_ai = view.findViewById(R.id.call_ai_btn);
        TextView response_container = view.findViewById(R.id.ai_container);
        AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
        Button checkDB = view.findViewById(R.id.storeDB);
        checkDB.setOnClickListener(v -> {
            QuizFragment quizFragment = new QuizFragment();

            // Begin a transaction to replace or add the fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, quizFragment) // Replace the container with the new fragment
                    .addToBackStack(null) // Optional: Adds this transaction to the back stack, so you can navigate back
                    .commit(); // Commit the transaction
        });
        call_ai.setOnClickListener(v -> {
            String prompt = "Generate 11 multiple-choice quiz questions about Biology 1.\n" +
                    "Each question must be in JSON format with the following structure:\n" +
                    "The first JSON object in JSON array gives the title and description for quiz, and must be in the format" +
                    "\n" +
                    "  \"title\": \"string\",\n" +
                    "  \"description\": \"string\",\n" +
                    "{\n" +
                    "  \"question\": \"string\",\n" +
                    "  \"options\": [\"string\", \"string\", \"string\", \"string\"],\n" +
                    "  \"answer\": \"int\",\n" +
                    "  \"explanation\": \"string\"\n" +
                    "}\n" +
                    "Return the entire output as a JSON array with 5 such objects.\n" +
                    "Do not include any extra text or explanation outside the JSON array.\n" +
                    "The answer is an int from 0 to 3 corresponding to the correct option index.";
            response_container.setText("Calling AI...");
            Quiz quiz = new Quiz();
            Question questions[] = new Question[10];
            Option options[][] = new Option[10][4];
            AI.modelCall(prompt, new AI.ResponseCallback() {
                @Override
                public void onResponse(String result) {
                    result = result.replaceAll("(?s)```(\\w+)?\\n", "").replaceAll("```", "");      //remove ``` from response
                    try {
                        JSONArray jsonArray = new JSONArray(result);        //turn string response into array of JSON
                        JSONObject quizObject = jsonArray.getJSONObject(0); //first object in array is title & description

                        //create component objects then store in db using dao
                        Quiz quiz = new Quiz();
                        quiz.title = quizObject.getString("title");
                        quiz.description = quizObject.getString("description");

                        // Use a single thread for the entire database operation
                        new Thread(() -> {
                            try {
                                long quizId = db.quizDao().insert(quiz);    //get id after inserting for fk constraints

                                for (int i = 1; i < jsonArray.length(); i++) {      //start from 1 because 0 is title & desc for quiz
                                    JSONObject obj = jsonArray.getJSONObject(i);

                                    //question, 10 questions in 1 response
                                    Question question = new Question();
                                    question.quizId = (int) quizId;  // Use the actual quizId from the database
                                    question.questionText = obj.getString("question");
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
                                    Toast.makeText(getContext(), "Quiz stored successfully!", Toast.LENGTH_SHORT).show();
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                getActivity().runOnUiThread(() -> {
                                    response_container.setText("Error processing JSON: " + e.getMessage());
                                });
                            }
                        }).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        response_container.setText("Invalid JSON response");
                    }
                    response_container.setText(result);
                }
                @Override
                public void onError(String error) {
                    response_container.setText("Error: " + error);
                }
            });

        });
    }
}
