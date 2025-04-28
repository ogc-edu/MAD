package com.example.quizfragments.ui.fragments.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.quizfragments.R;
import com.example.quizfragments.data.api.AI;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Flashcard;
import com.example.quizfragments.data.db.entities.FlashcardDeck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FlashcardGeneratorFragment extends Fragment {
    private EditText topicEditText;
    private NumberPicker numFlashcardsPicker;
    private Button generateButton;
    private ProgressBar progressBar;
    private TextView feedback;

    private static final int MIN_FLASHCARDS = 1;
    private static final int MAX_FLASHCARDS = 10;
    private static final int DEFAULT_FLASHCARDS = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_generator, container, false);

        topicEditText = view.findViewById(R.id.topic_edit_text);
        numFlashcardsPicker = view.findViewById(R.id.num_flashcards_picker);
        generateButton = view.findViewById(R.id.generate_button);
        progressBar = view.findViewById(R.id.progress_bar);
        feedback = view.findViewById(R.id.ai_feedback);

        setupNumberPicker();

        generateButton.setOnClickListener(v -> validateAndGenerateFlashcards());

        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void setupNumberPicker() {
        // Set the min and max values for the number picker
        numFlashcardsPicker.setMinValue(MIN_FLASHCARDS);
        numFlashcardsPicker.setMaxValue(MAX_FLASHCARDS);

        numFlashcardsPicker.setValue(DEFAULT_FLASHCARDS);

        numFlashcardsPicker.setWrapSelectorWheel(false);
    }

    private void validateAndGenerateFlashcards() {
        String topic = topicEditText.getText().toString().trim();
        int numFlashcards = numFlashcardsPicker.getValue();

        if (topic.isEmpty()) {
            topicEditText.setError("Please enter a flashcard topic");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        generateButton.setText("Generating...");

        generateFlashcardsWithAI(topic, numFlashcards);
    }

    private void generateFlashcardsWithAI(String topic, int numFlashcards) {
        progressBar.setVisibility(View.VISIBLE);
        feedback.setText("");
        feedback.setVisibility(View.GONE);

        String prompt = "Generate " + numFlashcards + " flashcards about the topic " + topic + "\n" +
                "Each flashcard must be in JSON format with the following structure:\n" +
                "The first JSON object in JSON array gives the title and a short description for the flashcard deck, and must be in the format\n" +
                "{\n" +
                "  \"title\": \"string\",\n" +
                "  \"description\": \"string\",\n" +
                "  \"flashcard_count\": " + numFlashcards + "\n" +
                "}\n" +
                "Then, for each flashcard:\n" +
                "{\n" +
                "  \"question\": \"string\",\n" +
                "  \"answer\": \"string\"\n" +
                "}\n" +
                "Return the entire output as a JSON array.\n" +
                "Do not include any extra text or explanation outside the JSON array.\n";

        AI.modelCall(prompt, new AI.ResponseCallback() {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            public void onResponse(String result) {
                result = result.replace("```json", "")
                        .replace("```", "")
                        .trim();
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject deckObject = jsonArray.getJSONObject(0);

                    // Create component objects then store in db using dao
                    FlashcardDeck deck = new FlashcardDeck(deckObject.getString("title"), deckObject.getString("description"));

                    // Use a single thread for the entire database operation
                    new Thread(() -> {
                        try {
                            long deckId = db.flashcardDeckDao().insert(deck);

                            for (int i = 1; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                // Create flashcard
                                Flashcard flashcard = new Flashcard(
                                        obj.getString("question"),
                                        obj.getString("answer"),
                                        (int) deckId
                                );
                                flashcard.setPosition(i - 1);
                                db.flashcardDao().insert(flashcard);
                            }

                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                generateButton.setText("Generate Flashcards");
                                feedback.setText("Flashcards generated successfully!");
                                feedback.setTextColor(requireContext().getColor(R.color.aiSuccess));
                                feedback.setVisibility(View.VISIBLE);

                                navigateToFlashcardStudy((int) deckId);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                generateButton.setEnabled(true);
                                generateButton.setText("Generate Flashcards");
                                feedback.setText("Error occurred, please try again");
                                feedback.setTextColor(requireContext().getColor(R.color.aiFailed));
                                feedback.setVisibility(View.VISIBLE);
                            });
                        }
                    }).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        generateButton.setEnabled(true);
                        generateButton.setText("Generate Flashcards");
                        feedback.setText("AI response failed, please try again later");
                        feedback.setTextColor(requireContext().getColor(R.color.aiFailed));
                        feedback.setVisibility(View.VISIBLE);
                    });
                }
            }

            public void onError(String result) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    generateButton.setEnabled(true);
                    generateButton.setText("Generate Flashcards");
                    feedback.setText("AI connection failed, please try again later");
                    feedback.setTextColor(requireContext().getColor(R.color.aiFailed));
                    feedback.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void navigateToFlashcardStudy(int deckId) {
        FlashcardStudyFragment studyFragment = FlashcardStudyFragment.newInstance(deckId);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, studyFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}