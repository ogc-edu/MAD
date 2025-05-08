package com.example.quizfragments.ui.fragments.notes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.text.Html;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Note;
import com.example.quizfragments.data.api.AI;

public class NoteDetailFragment extends Fragment {

    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String ARG_NOTE_ID = "note_id";

    private int folderId;
    private int noteId;
    private boolean isEditMode;
    private Note currentNote;

    private EditText titleEditText;
    private EditText contentEditText;
    private Button saveButton;
    private Button summarizeButton;
    private TextView formatStatusText;
    private TextView lastEditedText;
    private TextView categoryTitleText;
    private ImageButton backButton;
    private AppDatabase db;

    private ToggleButton toggleBold;
    private ToggleButton toggleItalic;
    private ToggleButton toggleUnderline;
    private ToggleButton toggleBullet;
    private ToggleButton toggleChecklist;

    // Format state flags
    private boolean isBoldActive = false;
    private boolean isItalicActive = false;
    private boolean isUnderlineActive = false;
    private boolean isBulletActive = false;
    private boolean isChecklistActive = false;

    // Current format tags
    private StringBuilder currentFormatStart = new StringBuilder();
    private StringBuilder currentFormatEnd = new StringBuilder();

    public static NoteDetailFragment newInstance(int folderId, int noteId) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FOLDER_ID, folderId);
        args.putInt(ARG_NOTE_ID, noteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            folderId = getArguments().getInt(ARG_FOLDER_ID);
            noteId = getArguments().getInt(ARG_NOTE_ID);
            isEditMode = noteId > 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        // Initialize views
        titleEditText = view.findViewById(R.id.note_title_edit);
        contentEditText = view.findViewById(R.id.note_content_edit);

        saveButton = view.findViewById(R.id.btn_save_note);
        summarizeButton = view.findViewById(R.id.btn_summarize);
        formatStatusText = view.findViewById(R.id.format_status_text);
        lastEditedText = view.findViewById(R.id.last_edited_text);
        categoryTitleText = view.findViewById(R.id.note_category_title);
        backButton = view.findViewById(R.id.btn_back);
        setupTextSelectionActionMode();

        // Initialize formatting toggle buttons
        toggleBold = view.findViewById(R.id.toggle_bold);
        toggleItalic = view.findViewById(R.id.toggle_italic);
        toggleUnderline = view.findViewById(R.id.toggle_underline);
        toggleBullet = view.findViewById(R.id.toggle_bullet);
        toggleChecklist = view.findViewById(R.id.toggle_checklist);

        // Setup back button
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Make sure toggle buttons are not checked initially
        toggleBold.setChecked(false);
        toggleItalic.setChecked(false);
        toggleUnderline.setChecked(false);
        toggleBullet.setChecked(false);
        toggleChecklist.setChecked(false);
        isChecklistActive = false;

        // Set current date and time for last edited
        updateLastEditedTime();

        // Set folder name as category title
        setCategoryTitle();

        // Set up toggle button listeners
        setupFormatToggleButtons();

        // Show a toast to indicate formatting is available
        Toast.makeText(requireContext(), "Toggle format buttons to activate formatting modes. Press Enter to start a new line.", Toast.LENGTH_LONG).show();

        // If in edit mode, load the note
        if (isEditMode) {
            loadNote();
        }

        // Setup save button click listener
        saveButton.setOnClickListener(v -> saveNote());

        // Setup summarize button click listener
        summarizeButton.setOnClickListener(v -> summarizeNote());

        // Setup key listener for direct key events
        contentEditText.setOnKeyListener((v, keyCode, event) -> {
            // Check if Enter key was pressed
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                // Handle Enter key press with active formatting
                if (isBulletActive || isChecklistActive || isBoldActive || isItalicActive || isUnderlineActive) {
                    handleEnterKeyWithFormatting();
                    return true; // We handled the event
                }
            }
            return false; // Let the default handler process other keys
        });

        // Setup click listener for toggling checkboxes
        contentEditText.setOnClickListener(v -> {
            // Check if we're in checklist mode or if there are checkboxes in the text
            if (isChecklistActive || contentEditText.getText().toString().contains("☐") ||
                    contentEditText.getText().toString().contains("☑")) {
                toggleCheckboxAtCursor();
            }
        });
    }

    //Summarizes the current note content using AI
    private void summarizeNote() {
        String noteContent = contentEditText.getText().toString().trim();
        String noteTitle = titleEditText.getText().toString().trim();

        if (noteContent.isEmpty()) {
            Toast.makeText(requireContext(), "Note is empty. Nothing to summarize.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Summarizing Note")
                .setMessage("Processing your note content...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Create a prompt for the AI
        String prompt = "Summarize the following note clearly and concisely:\n\n" + noteContent + "\n\n" +
                "Your response must follow this exact format:\n\n" +
                "Summary:\n" +
                "Write a short paragraph (1–2 sentences) offering a different viewpoint or angle of the topic." +
                "*******************************\n\n" +
                "Key Ideas:\n" +
                "- Number each section (subtitle) as follows:\n" +
                "    1. Similarities\n" +
                "       - Cell membrane\n" +
                "       - Cytoplasm\n" +
                "       - Nucleus\n" +
                "- Do not add colons or bold text after the subtitle.\n" +
                "- After the subtitle, list the key points with bullet points using '-'.\n" +
                "- Ensure the bullet points are simple and easy to understand.\n" +
                "- List between 2 to 5 bullet points per section.\n\n" +
                "*******************************\n\n" +
                "Important formatting rules:\n" +
                "- Start with 'Summary:' followed by the paragraph.\n" +
                "- Insert this after the summary paragraph to have separation of content *******************************\n\n" +
                "- Do NOT bold anything.\n" +
                "- Do NOT add colons ':' after bullet points or subtitles.\n" +
                "- Maintain exactly one empty line between sections.\n" +
                "- Use simple, clear, and accessible language.";


        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();
                showSummaryResultDialog(result);
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();

                // Show error message
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to summarize note: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    //Shows the summary result in a dialog with options
    private void showSummaryResultDialog(String summaryResult) {
        // Inflate the custom layout
        View customView = getLayoutInflater().inflate(R.layout.dialog_summary_result, null);
        TextView summaryTextView = customView.findViewById(R.id.summary_text);
        TextView summarizeAgainOption = customView.findViewById(R.id.btn_copy);
        TextView insertOption = customView.findViewById(R.id.btn_insert);
        TextView closeOption = customView.findViewById(R.id.btn_close);

        summarizeAgainOption.setText("Summarize Again");

        // Set the summary text
        summaryTextView.setText(summaryResult);

        // Create the dialog with no buttons (we'll use our custom text options)
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Note Summary")
                .setView(customView)
                .setCancelable(false) // Prevent dismissal except via our options
                .create();

        // Set the dialog to adjust to screen size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();

        // Setup option click listeners
        summarizeAgainOption.setOnClickListener(v -> {
            dialog.dismiss();

            // Call summarizeNote() again for a different summary
            summarizeNoteWithAlternativePrompt();
        });

        insertOption.setOnClickListener(v -> {
            Editable editable = contentEditText.getText();
            String formattedSummary = "\n\n--- AI Summary ---\n" + summaryResult + "\n-------------------\n";
            editable.append(formattedSummary);
            Toast.makeText(requireContext(), "Summary added to note", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        closeOption.setOnClickListener(v -> dialog.dismiss());
    }

    // New method to summarize note with a slightly different prompt for variety
    private void summarizeNoteWithAlternativePrompt() {
        String noteContent = contentEditText.getText().toString().trim();
        String noteTitle = titleEditText.getText().toString().trim();

        if (noteContent.isEmpty()) {
            Toast.makeText(requireContext(), "Note is empty. Nothing to summarize.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Summarizing Note")
                .setMessage("Creating an alternative summary...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Alternative prompt for a different perspective
        String prompt = "Please provide a different perspective summary for the following " +
                (noteTitle.isEmpty() ? "note" : "note titled '" + noteTitle + "'") + ":\n\n" + noteContent + "\n\n" +
                "Your response must follow this exact format:\n\n" +
                "Summary:\n" +
                "Write a short paragraph (1–2 sentences) offering a different viewpoint or angle of the topic.\n\n" +
                "*******************************\n\n" +
                "Key Ideas:\n" +
                "- Number each section (subtitle) as follows:\n" +
                "    1. Similarities\n" +
                "       - Cell membrane\n" +
                "       - Cytoplasm\n" +
                "       - Nucleus\n" +
                "- Do not add colons or bold text after the subtitle.\n" +
                "- After the subtitle, list the key points with bullet points using '-'.\n" +
                "- Ensure the bullet points are simple and easy to understand.\n" +
                "- List between 2 to 5 bullet points per section.\n\n" +
                "*******************************\n\n" +
                "Important formatting rules:\n" +
                "- Start with 'Summary:' followed by the paragraph.\n" +
                "- Insert this after the summary paragraph to have separation of content *******************************\n\n" +
                "- Do NOT bold anything.\n" +
                "- Do NOT add colons ':' after bullet points or subtitles.\n" +
                "- Maintain exactly one empty line between sections.\n" +
                "- Use simple, clear, and accessible language.";

        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();
                showSummaryResultDialog(result);  // Show new summary
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();

                // Show error message
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to create alternative summary: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void setupTextSelectionActionMode() {
        contentEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Add our custom options to the context menu

                // Add "Ask AI" option
                MenuItem askAiItem = menu.add(Menu.NONE, R.id.menu_ask_ai, 1, getString(R.string.menu_ask_ai));
                askAiItem.setIcon(R.drawable.ic_ai);
                askAiItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                // Add "Create Quiz" option
                MenuItem createQuizItem = menu.add(Menu.NONE, R.id.menu_create_quiz, 2, getString(R.string.menu_create_quiz));
                createQuizItem.setIcon(android.R.drawable.ic_menu_manage);
                createQuizItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                // Add "Create Flashcard" option
                MenuItem createFlashcardItem = menu.add(Menu.NONE, R.id.menu_create_flashcard, 3, getString(R.string.menu_create_flashcard));
                createFlashcardItem.setIcon(android.R.drawable.ic_menu_info_details);
                createFlashcardItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int start = contentEditText.getSelectionStart();
                int end = contentEditText.getSelectionEnd();

                if (start != end) {
                    String selectedText = contentEditText.getText().toString().substring(start, end);

                    // Check which option was selected
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_ask_ai) {
                        askAI(selectedText);
                        mode.finish(); // Close the action mode
                        return true;
                    } else if (itemId == R.id.menu_create_quiz) {
                        createQuizFromSelectedText(selectedText);
                        mode.finish(); // Close the action mode
                        return true;
                    } else if (itemId == R.id.menu_create_flashcard) {
                        createFlashcardFromSelectedText(selectedText);
                        mode.finish(); // Close the action mode
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Not needed
            }
        });
    }

    /**
     * Creates a quiz from the selected text in the note
     * @param selectedText The text selected by the user
     */
    private void createQuizFromSelectedText(String selectedText) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Creating Quiz")
                .setMessage("Generating quiz questions from your selection...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Create a prompt for the AI to generate quiz questions
        String prompt = "Create a quiz based on this text: \"" + selectedText + "\"\n\n" +
                "IMPORTANT: Your response must be ONLY a valid JSON object with no additional text, explanations, or markdown formatting.\n\n" +
                "Format the response as a JSON object with the following structure:\n" +
                "{\n" +
                "  \"title\": \"Quiz title based on the content\",\n" +
                "  \"description\": \"Brief description of what this quiz covers\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"Question text\",\n" +
                "      \"options\": [\n" +
                "        {\"text\": \"Option 1\", \"isCorrect\": false},\n" +
                "        {\"text\": \"Option 2\", \"isCorrect\": true},\n" +
                "        {\"text\": \"Option 3\", \"isCorrect\": false},\n" +
                "        {\"text\": \"Option 4\", \"isCorrect\": false}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Rules:\n" +
                "1. Create 3-5 multiple-choice questions based on the content\n" +
                "2. Each question must have exactly 4 options with only one correct answer\n" +
                "3. Make sure the questions test understanding, not just memorization\n" +
                "4. The response must be ONLY the JSON object - no explanations, no markdown formatting\n" +
                "5. Do not include any text before or after the JSON structure\n" +
                "6. Make sure all JSON keys and values are properly quoted with double quotes\n" +
                "7. Ensure the JSON is properly formatted and can be parsed by a standard JSON parser";

        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();

                try {
                    // Clean the result string to ensure it's valid JSON
                    String cleanedResult = cleanJsonString(result);

                    // Try to parse the JSON response
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(cleanedResult);
                    String quizTitle = jsonResponse.getString("title");
                    String quizDescription = jsonResponse.getString("description");
                    org.json.JSONArray questionsArray = jsonResponse.getJSONArray("questions");

                    // Create a new quiz in the database
                    createQuizInDatabase(quizTitle, quizDescription, questionsArray);

                } catch (org.json.JSONException e) {
                    // Show error if JSON parsing fails
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setMessage("Failed to parse quiz data. Please try again with a different selection.\n\nError details: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                    e.printStackTrace();

                    // Log the response for debugging
                    Log.e("NoteDetailFragment", "Failed to parse JSON: " + result);

                    // Try to create a simple quiz as fallback
                    tryCreateSimpleQuiz(selectedText);
                }
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();

                // Show error message
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to generate quiz: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    /**
     * Creates a quiz in the database from the AI-generated data
     */
    private void createQuizInDatabase(String title, String description, org.json.JSONArray questionsArray) {
        new Thread(() -> {
            try {
                // Create a new Quiz entity
                com.example.quizfragments.data.db.entities.Quiz newQuiz = new com.example.quizfragments.data.db.entities.Quiz();
                newQuiz.title = title;
                newQuiz.description = description;
                newQuiz.questionCount = questionsArray.length();

                // Insert the quiz and get its ID
                long quizId = db.quizDao().insert(newQuiz);

                // Add questions and options
                for (int i = 0; i < questionsArray.length(); i++) {
                    org.json.JSONObject questionObj = questionsArray.getJSONObject(i);
                    String questionText = questionObj.getString("question");

                    // Create a Question entity
                    com.example.quizfragments.data.db.entities.Question question = new com.example.quizfragments.data.db.entities.Question();
                    question.quizId = (int) quizId;
                    question.questionText = questionText;
                    question.questionNumber = i + 1;
                    question.attempted = 0; // Not attempted yet

                    // Insert the question and get its ID
                    long questionId = db.questionDao().insert(question);

                    // Add options for this question
                    org.json.JSONArray optionsArray = questionObj.getJSONArray("options");
                    for (int j = 0; j < optionsArray.length(); j++) {
                        org.json.JSONObject optionObj = optionsArray.getJSONObject(j);
                        String optionText = optionObj.getString("text");
                        boolean isCorrect = optionObj.getBoolean("isCorrect");

                        // Create an Option entity
                        com.example.quizfragments.data.db.entities.Option option = new com.example.quizfragments.data.db.entities.Option();
                        option.questionId = (int) questionId;
                        option.optionText = optionText;
                        option.isCorrect = isCorrect;

                        // Insert the option
                        db.optionDao().insert(option);
                    }
                }

                // Show success message and navigate to the quiz
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Quiz created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to the quiz questions fragment
                        com.example.quizfragments.ui.fragments.quiz.QuizQuestionsFragment questionsFragment =
                                com.example.quizfragments.ui.fragments.quiz.QuizQuestionsFragment.newInstance((int) quizId, title);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, questionsFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error creating quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    /**
     * Creates flashcards from the selected text in the note
     * @param selectedText The text selected by the user
     */
    private void createFlashcardFromSelectedText(String selectedText) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Creating Flashcards")
                .setMessage("Generating flashcards from your selection...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Create a prompt for the AI to generate flashcards
        String prompt = "Create a set of flashcards based on this text: \"" + selectedText + "\"\n\n" +
                "IMPORTANT: Your response must be ONLY a valid JSON array with no additional text, explanations, or markdown formatting.\n\n" +
                "Format the response as a JSON array with the following structure:\n" +
                "[\n" +
                "  {\n" +
                "    \"title\": \"Flashcard deck title based on the content\",\n" +
                "    \"description\": \"Brief description of what these flashcards cover\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"Front side of flashcard 1\",\n" +
                "    \"answer\": \"Back side of flashcard 1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"Front side of flashcard 2\",\n" +
                "    \"answer\": \"Back side of flashcard 2\"\n" +
                "  }\n" +
                "]\n\n" +
                "Rules:\n" +
                "1. Create 5-10 flashcards based on the content\n" +
                "2. The first object in the array must contain the title and description\n" +
                "3. Each subsequent object represents one flashcard with question and answer\n" +
                "4. Questions should be concise and focused on key concepts\n" +
                "5. Answers should be clear and informative but not too long\n" +
                "6. The response must be ONLY the JSON array - no explanations, no markdown formatting\n" +
                "7. Do not include any text before or after the JSON structure\n" +
                "8. Make sure all JSON keys and values are properly quoted with double quotes\n" +
                "9. Ensure the JSON is properly formatted and can be parsed by a standard JSON parser";

        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();

                try {
                    // Clean the result string to ensure it's valid JSON
                    String cleanedResult = cleanJsonString(result);

                    // Try to parse the JSON response
                    org.json.JSONArray jsonArray = new org.json.JSONArray(cleanedResult);

                    // The first object contains the deck info
                    org.json.JSONObject deckInfo = jsonArray.getJSONObject(0);
                    String deckTitle = deckInfo.getString("title");
                    String deckDescription = deckInfo.getString("description");

                    // Create a new flashcard deck in the database
                    createFlashcardDeckInDatabase(deckTitle, deckDescription, jsonArray);

                } catch (org.json.JSONException e) {
                    // Show error if JSON parsing fails
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setMessage("Failed to parse flashcard data. Please try again with a different selection.\n\nError details: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                    e.printStackTrace();

                    // Log the response for debugging
                    Log.e("NoteDetailFragment", "Failed to parse JSON: " + result);

                    // Try to create simple flashcards as fallback
                    tryCreateSimpleFlashcards(selectedText);
                }
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();

                // Show error message
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to generate flashcards: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    /**
     * Creates a flashcard deck in the database from the AI-generated data
     */
    private void createFlashcardDeckInDatabase(String title, String description, org.json.JSONArray jsonArray) {
        new Thread(() -> {
            try {
                // Create a new FlashcardDeck entity
                com.example.quizfragments.data.db.entities.FlashcardDeck deck =
                        new com.example.quizfragments.data.db.entities.FlashcardDeck(title, description);

                // Insert the deck and get its ID
                long deckId = db.flashcardDeckDao().insert(deck);

                // Add flashcards (starting from index 1, as index 0 contains the deck info)
                for (int i = 1; i < jsonArray.length(); i++) {
                    org.json.JSONObject cardObj = jsonArray.getJSONObject(i);
                    String question = cardObj.getString("question");
                    String answer = cardObj.getString("answer");

                    // Create a Flashcard entity
                    com.example.quizfragments.data.db.entities.Flashcard flashcard =
                            new com.example.quizfragments.data.db.entities.Flashcard(question, answer, (int) deckId);
                    flashcard.setPosition(i - 1);

                    // Insert the flashcard
                    db.flashcardDao().insert(flashcard);
                }

                // Show success message and navigate to the flashcard study fragment
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Flashcards created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to the flashcard study fragment
                        com.example.quizfragments.ui.fragments.flashcard.FlashcardStudyFragment studyFragment =
                                com.example.quizfragments.ui.fragments.flashcard.FlashcardStudyFragment.newInstance((int) deckId);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, studyFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error creating flashcards: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void askAI(String selectedText) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Asking AI")
                .setMessage("Processing your request...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Create a prompt for the AI
        String prompt = "Analyze this text and provide insights: \"" + selectedText + "\"\n" +
                "Instructions:\n" +
                "1. First, provide a short and simple explanation paragraph for the topic.\n" +
                "2. Then, break down the important details into bullet points.\n" +
                "3. Format:\n" +
                "- Show the title normally (no bold, just plain text).\n" +
                "- List bullet points under each title.\n" +
                "- No extra empty lines between bullet points.\n" +
                "- Add ONE empty line between different titles to separate them.\n" +
                "- Keep everything easy to read and clean.\n\n" +
                "Example:\n" +
                "Cell Membrane\n" +
                "- Controls entry and exit of substances\n" +
                "- Made of lipid bilayer\n" +
                "- Contains embedded proteins\n\n" +
                "Nucleus\n" +
                "- Controls cell activities\n" +
                "- Contains DNA\n" +
                "- Surrounded by nuclear envelope";


        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();

                // Show the AI response in a dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("AI Response")
                        .setMessage(result)
                        .setPositiveButton("Close", null)
                        .show();
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();

                // Show error message
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to get AI response: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void loadNote() {
        new Thread(() -> {
            try {
                currentNote = db.noteDao().getNoteById(noteId);
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded() && currentNote != null) {
                            titleEditText.setText(currentNote.getTitle());

                            // Set content
                            String content = currentNote.getContent();
                            contentEditText.setText(content);

                            // Reset toggle buttons
                            toggleBold.setChecked(false);
                            toggleItalic.setChecked(false);
                            toggleUnderline.setChecked(false);
                            toggleBullet.setChecked(false);

                            Toast.makeText(requireContext(), "Note loaded - toggle format buttons to activate formatting modes", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void setupFormatToggleButtons() {
        // Set up toggle button listeners for format mode switching
        toggleBold.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBoldActive = isChecked;
            updateFormatState();
            String message = isChecked ? "Bold mode ON" : "Bold mode OFF";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // Show format status when active
            formatStatusText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        toggleItalic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isItalicActive = isChecked;
            updateFormatState();
            String message = isChecked ? "Italic mode ON" : "Italic mode OFF";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // Show format status when active
            formatStatusText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        toggleUnderline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isUnderlineActive = isChecked;
            updateFormatState();
            String message = isChecked ? "Underline mode ON" : "Underline mode OFF";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // Show format status when active
            formatStatusText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        toggleBullet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBulletActive = isChecked;
            String message = isChecked ? "Bullet mode ON" : "Bullet mode OFF";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // If bullet mode is activated, add a bullet to the current line if needed
            if (isChecked) {
                addBulletToCurrentLine();
            } else {
                // If bullet mode is deactivated, remove bullet from the current line if it exists
                removeBulletFromCurrentLine();
            }

            // Update the format status indicator
            updateFormatStatusText();

            // Show format status when active
            formatStatusText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Checklist button functionality
        toggleChecklist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isChecklistActive = isChecked;
            String message = isChecked ? "Checklist mode ON" : "Checklist mode OFF";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // If checklist mode is activated, add a checkbox to the current line if needed
            if (isChecked) {
                addCheckboxToCurrentLine();
            }

            // Update the format status indicator
            updateFormatStatusText();

            // Show format status when active
            formatStatusText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Add text change listener to apply formatting as user types
        contentEditText.addTextChangedListener(new TextWatcher() {
            private int cursorPosition = 0;
            private boolean isChanging = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isChanging) {
                    cursorPosition = contentEditText.getSelectionStart();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanging) return;

                isChanging = true;

                try {
                    // Apply formatting to newly typed text if any format is active
                    if (isBoldActive || isItalicActive || isUnderlineActive) {
                        int currentPosition = contentEditText.getSelectionStart();

                        // Safety check for valid position
                        if (currentPosition < 0) currentPosition = 0;
                        if (currentPosition > s.length()) currentPosition = s.length();

                        // Safety check for cursor position
                        if (cursorPosition < 0) cursorPosition = 0;
                        if (cursorPosition > s.length()) cursorPosition = s.length();

                        // Only apply formatting if text was added (not deleted)
                        if (currentPosition > cursorPosition) {
                            applyFormatToNewText(cursorPosition, currentPosition);
                        }
                    }

                    // We don't need to handle new lines here anymore
                    // The handleEnterKeyWithFormatting method takes care of it
                    // This avoids duplicate handling of Enter key presses
                } catch (Exception e) {
                    // Log the error but don't crash
                    e.printStackTrace();
                }

                isChanging = false;
            }
        });
    }

    /**
     * Handles Enter key press when formatting is active.
     * This method ensures that the new line inherits the formatting from the previous line.
     */
    private void handleEnterKeyWithFormatting() {
        try {
            // Get current position and text
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();
            String text = editable.toString();

            // Check if the current line is empty (except for bullet/checkbox)
            boolean isCurrentLineEmpty = false;
            if (position > 0) {
                int lineStart = text.lastIndexOf('\n', position - 1) + 1;
                if (lineStart < 0) lineStart = 0;

                String currentLine = text.substring(lineStart, position);
                // Check if line is empty or only contains bullet/checkbox
                isCurrentLineEmpty = currentLine.isEmpty() ||
                        currentLine.equals("• ") ||
                        currentLine.equals("☐ ") ||
                        currentLine.equals("☑ ");

                // If the current line is empty and has a bullet/checkbox, remove it and return
                if (isCurrentLineEmpty && (currentLine.equals("• ") ||
                        currentLine.equals("☐ ") ||
                        currentLine.equals("☑ "))) {
                    // Remove the bullet/checkbox
                    editable.delete(lineStart, lineStart + 2);
                    // Insert a newline
                    editable.insert(lineStart, "\n");
                    // Set cursor position
                    contentEditText.setSelection(lineStart + 1);
                    return;
                }
            }

            // Insert a newline character
            editable.insert(position, "\n");

            // Update position after inserting newline
            position++;
            contentEditText.setSelection(position);

            // If text formatting is active, prepare for the new line
            if (isBoldActive || isItalicActive || isUnderlineActive) {
                // Update format state
                updateFormatState();

                // Apply formatting placeholder at the new position
                if (currentFormatStart.length() > 0) {
                    applyFormattingPlaceholder();
                }
            }

            // If bullet mode is active and the current line wasn't empty, add a bullet to the new line
            if (isBulletActive && !isCurrentLineEmpty) {
                addBulletToCurrentLine();
            }

            // If checklist mode is active and the current line wasn't empty, add a checkbox to the new line
            if (isChecklistActive && !isCurrentLineEmpty) {
                addCheckboxToCurrentLine();
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
        }
    }

    private void updateFormatState() {
        // Update the current format tags based on active formats
        currentFormatStart.setLength(0);
        currentFormatEnd.setLength(0);

        if (isBoldActive) {
            currentFormatStart.append("<b>");
            currentFormatEnd.insert(0, "</b>");
        }

        if (isItalicActive) {
            currentFormatStart.append("<i>");
            currentFormatEnd.insert(0, "</i>");
        }

        if (isUnderlineActive) {
            currentFormatStart.append("<u>");
            currentFormatEnd.insert(0, "</u>");
        }

        // Update the format status indicator
        updateFormatStatusText();
    }

    private void updateFormatStatusText() {
        StringBuilder status = new StringBuilder("Active formats: ");
        boolean hasActiveFormats = false;

        if (isBoldActive) {
            status.append("Bold");
            hasActiveFormats = true;
        }

        if (isItalicActive) {
            if (hasActiveFormats) status.append(", ");
            status.append("Italic");
            hasActiveFormats = true;
        }

        if (isUnderlineActive) {
            if (hasActiveFormats) status.append(", ");
            status.append("Underline");
            hasActiveFormats = true;
        }

        if (isBulletActive) {
            if (hasActiveFormats) status.append(", ");
            status.append("Bullet");
            hasActiveFormats = true;
        }

        if (isChecklistActive) {
            if (hasActiveFormats) status.append(", ");
            status.append("Checklist");
            hasActiveFormats = true;
        }

        if (!hasActiveFormats) {
            status.append("None");
        }

        formatStatusText.setText(status.toString());
    }

    private void applyFormatToNewText(int start, int end) {
        if (currentFormatStart.length() == 0) return; // No active formatting

        try {
            Editable editable = contentEditText.getText();

            // Safety check for valid indices
            int textLength = editable.length();
            if (start < 0) start = 0;
            if (end > textLength) end = textLength;
            if (start > end) return; // Invalid selection

            String newText = editable.toString().substring(start, end);
            String formattedText = currentFormatStart.toString() + newText + currentFormatEnd.toString();

            // Replace the new text with formatted version
            editable.replace(start, end, Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT));

            // Restore cursor position safely
            int newPosition = start + newText.length();
            if (newPosition <= editable.length()) {
                contentEditText.setSelection(newPosition);
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error applying formatting", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Applies a placeholder character with formatting at the current position.
     * This is used to ensure that when the user presses Enter, the new line
     * inherits the formatting from the previous line.
     */
    private void applyFormattingPlaceholder() {
        if (currentFormatStart.length() == 0) return; // No active formatting

        try {
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();

            // Safety check for valid position
            if (position < 0 || position > editable.length()) {
                position = Math.max(0, Math.min(position, editable.length()));
            }

            // Create a zero-width space with formatting
            String formattedSpace = currentFormatStart.toString() + "\u200B" + currentFormatEnd.toString();

            // Insert the formatted space at the current position
            editable.insert(position, Html.fromHtml(formattedSpace, Html.FROM_HTML_MODE_COMPACT));

            // Restore cursor position
            contentEditText.setSelection(position + 1);
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
        }
    }

    private void addBulletToCurrentLine() {
        try {
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();
            String text = editable.toString();

            // Safety check for valid position
            if (position < 0 || position > text.length()) {
                position = Math.max(0, Math.min(position, text.length()));
            }

            // Find the start of the current line
            int lineStart = position > 0 ? text.lastIndexOf('\n', position - 1) + 1 : 0;
            if (lineStart < 0) lineStart = 0;

            // Check if the line already has a bullet
            int prefixEnd = Math.min(lineStart + 2, text.length());
            String linePrefix = text.substring(lineStart, prefixEnd);
            if (!linePrefix.equals("• ")) {
                // Add bullet
                editable.insert(lineStart, "• ");

                // Adjust cursor position safely
                int newPosition = position + 2;
                if (newPosition <= editable.length()) {
                    contentEditText.setSelection(newPosition);
                }
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error adding bullet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Removes the bullet from the current line if it exists
     */
    private void removeBulletFromCurrentLine() {
        try {
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();
            String text = editable.toString();

            // Safety check for valid position
            if (position < 0 || position > text.length()) {
                position = Math.max(0, Math.min(position, text.length()));
            }

            // Find the start of the current line
            int lineStart = position > 0 ? text.lastIndexOf('\n', position - 1) + 1 : 0;
            if (lineStart < 0) lineStart = 0;

            // Check if the line has a bullet
            if (text.length() >= lineStart + 2) {
                String linePrefix = text.substring(lineStart, lineStart + 2);
                if (linePrefix.equals("• ")) {
                    // Remove bullet
                    editable.delete(lineStart, lineStart + 2);

                    // Adjust cursor position safely
                    int newPosition = Math.max(0, position - 2);
                    if (newPosition <= editable.length()) {
                        contentEditText.setSelection(newPosition);
                    }

                    Toast.makeText(requireContext(), "Bullet removed", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error removing bullet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLastEditedTime() {
        // Get current time
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());

        // Update the last edited text
        lastEditedText.setText("Last edited: Today, " + currentTime);
    }

    /**
     * Sets the category title based on the folder name
     */
    private void setCategoryTitle() {
        // In a real app, you would fetch the folder name from the database
        // For now, we'll use a placeholder or the folder ID
        new Thread(() -> {
            try {
                // Try to get folder name from database
                String folderName = "Notes";
                try {
                    folderName = db.folderDao().getFolderById(folderId).getTitle();
                } catch (Exception e) {
                    // If folder not found, use default name
                    folderName = "Notes";
                }

                final String finalFolderName = folderName;
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        categoryTitleText.setText(finalFolderName);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Toggles the checkbox state at the current cursor position
     */
    private void toggleCheckboxAtCursor() {
        try {
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();
            String text = editable.toString();

            // Safety check for valid position
            if (position < 0 || position > text.length()) {
                position = Math.max(0, Math.min(position, text.length()));
            }

            // Find the start of the current line
            int lineStart = position > 0 ? text.lastIndexOf('\n', position - 1) + 1 : 0;
            if (lineStart < 0) lineStart = 0;

            // Define checkbox characters
            String checkboxUnchecked = "☐ "; // Unicode for unchecked box
            String checkboxChecked = "☑ "; // Unicode for checked box

            // Check if the line starts with a checkbox
            if (text.length() >= lineStart + 2) {
                String linePrefix = text.substring(lineStart, lineStart + 2);

                if (linePrefix.equals(checkboxUnchecked.substring(0, 2))) {
                    // Toggle from unchecked to checked
                    editable.replace(lineStart, lineStart + 2, checkboxChecked.substring(0, 2));
                    Toast.makeText(requireContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
                } else if (linePrefix.equals(checkboxChecked.substring(0, 2))) {
                    // Toggle from checked to unchecked
                    editable.replace(lineStart, lineStart + 2, checkboxUnchecked.substring(0, 2));
                    Toast.makeText(requireContext(), "Task marked as incomplete", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
        }
    }

    /**
     * Adds a checkbox to the current line
     */
    private void addCheckboxToCurrentLine() {
        try {
            int position = contentEditText.getSelectionStart();
            Editable editable = contentEditText.getText();
            String text = editable.toString();

            // Safety check for valid position
            if (position < 0 || position > text.length()) {
                position = Math.max(0, Math.min(position, text.length()));
            }

            // Find the start of the current line
            int lineStart = position > 0 ? text.lastIndexOf('\n', position - 1) + 1 : 0;
            if (lineStart < 0) lineStart = 0;

            // Check if the line already has a checkbox
            String checkboxUnchecked = "☐ "; // Unicode for unchecked box
            String checkboxChecked = "☑ "; // Unicode for checked box

            int prefixEnd = Math.min(lineStart + 2, text.length());
            String linePrefix = text.substring(lineStart, prefixEnd);

            if (!linePrefix.equals(checkboxUnchecked) && !linePrefix.equals(checkboxChecked)) {
                // Add unchecked checkbox
                editable.insert(lineStart, checkboxUnchecked);

                // Adjust cursor position safely
                int newPosition = position + 2;
                if (newPosition <= editable.length()) {
                    contentEditText.setSelection(newPosition);
                }
            }
        } catch (Exception e) {
            // Log the error but don't crash
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error adding checkbox", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a simple quiz as a fallback when AI JSON parsing fails
     * @param selectedText The text selected by the user
     */
    private void tryCreateSimpleQuiz(String selectedText) {
        // Show a dialog to confirm creating a simple quiz
        new AlertDialog.Builder(requireContext())
                .setTitle("Create Simple Quiz")
                .setMessage("Would you like to create a simple quiz based on your selection? " +
                        "This will create a basic quiz with true/false questions.")
                .setPositiveButton("Create", (dialog, which) -> {
                    // Create a simple quiz with the selected text
                    createSimpleQuiz(selectedText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Creates a simple quiz with true/false questions
     * @param selectedText The text selected by the user
     */
    private void createSimpleQuiz(String selectedText) {
        new Thread(() -> {
            try {
                // Create a title from the first few words of the selected text
                String title = "Quiz on " + (selectedText.length() > 30 ?
                        selectedText.substring(0, 30) + "..." : selectedText);

                // Create a new Quiz entity
                com.example.quizfragments.data.db.entities.Quiz newQuiz = new com.example.quizfragments.data.db.entities.Quiz();
                newQuiz.title = title;
                newQuiz.description = "Quiz generated from selected text";
                newQuiz.questionCount = 3; // Create 3 simple questions

                // Insert the quiz and get its ID
                long quizId = db.quizDao().insert(newQuiz);

                // Create 3 simple true/false questions
                String[] sentences = selectedText.split("\\. ");
                int numQuestions = Math.min(3, sentences.length);

                for (int i = 0; i < numQuestions; i++) {
                    // Create a Question entity
                    com.example.quizfragments.data.db.entities.Question question = new com.example.quizfragments.data.db.entities.Question();
                    question.quizId = (int) quizId;
                    question.questionText = "Is this statement true? \"" + sentences[i] + "\"";
                    question.questionNumber = i + 1;
                    question.attempted = 0; // Not attempted yet

                    // Insert the question and get its ID
                    long questionId = db.questionDao().insert(question);

                    // Create true and false options
                    com.example.quizfragments.data.db.entities.Option trueOption = new com.example.quizfragments.data.db.entities.Option();
                    trueOption.questionId = (int) questionId;
                    trueOption.optionText = "True";
                    trueOption.isCorrect = true; // Assume true for simplicity

                    com.example.quizfragments.data.db.entities.Option falseOption = new com.example.quizfragments.data.db.entities.Option();
                    falseOption.questionId = (int) questionId;
                    falseOption.optionText = "False";
                    falseOption.isCorrect = false;

                    // Insert the options
                    db.optionDao().insert(trueOption);
                    db.optionDao().insert(falseOption);
                }

                // Show success message and navigate to the quiz
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Simple quiz created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to the quiz questions fragment
                        com.example.quizfragments.ui.fragments.quiz.QuizQuestionsFragment questionsFragment =
                                com.example.quizfragments.ui.fragments.quiz.QuizQuestionsFragment.newInstance((int) quizId, title);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, questionsFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error creating simple quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    /**
     * Creates simple flashcards as a fallback when AI JSON parsing fails
     * @param selectedText The text selected by the user
     */
    private void tryCreateSimpleFlashcards(String selectedText) {
        // Show a dialog to confirm creating simple flashcards
        new AlertDialog.Builder(requireContext())
                .setTitle("Create Simple Flashcards")
                .setMessage("Would you like to create simple flashcards based on your selection? " +
                        "This will create basic flashcards from sentences in your text.")
                .setPositiveButton("Create", (dialog, which) -> {
                    // Create simple flashcards with the selected text
                    createSimpleFlashcards(selectedText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Creates simple flashcards from sentences in the selected text
     * @param selectedText The text selected by the user
     */
    private void createSimpleFlashcards(String selectedText) {
        new Thread(() -> {
            try {
                // Create a title from the first few words of the selected text
                String title = "Flashcards on " + (selectedText.length() > 30 ?
                        selectedText.substring(0, 30) + "..." : selectedText);

                // Create a new FlashcardDeck entity
                com.example.quizfragments.data.db.entities.FlashcardDeck deck =
                        new com.example.quizfragments.data.db.entities.FlashcardDeck(title, "Flashcards generated from selected text");

                // Insert the deck and get its ID
                long deckId = db.flashcardDeckDao().insert(deck);

                // Split the text into sentences
                String[] sentences = selectedText.split("\\. ");
                int numFlashcards = Math.min(5, sentences.length);

                // Create flashcards from sentences
                for (int i = 0; i < numFlashcards; i++) {
                    String sentence = sentences[i].trim();
                    if (sentence.isEmpty()) continue;

                    // Create a question by removing a key word
                    String[] words = sentence.split("\\s+");
                    if (words.length < 3) continue;

                    // Find a word to remove (not the first or last, preferably a noun or verb)
                    int wordToRemove = words.length / 2;
                    String removedWord = words[wordToRemove];
                    words[wordToRemove] = "________";

                    // Create the question with the blank
                    StringBuilder questionBuilder = new StringBuilder();
                    for (String word : words) {
                        questionBuilder.append(word).append(" ");
                    }
                    String question = questionBuilder.toString().trim();

                    // Create a Flashcard entity
                    com.example.quizfragments.data.db.entities.Flashcard flashcard =
                            new com.example.quizfragments.data.db.entities.Flashcard(question, removedWord, (int) deckId);
                    flashcard.setPosition(i);

                    // Insert the flashcard
                    db.flashcardDao().insert(flashcard);
                }

                // Show success message and navigate to the flashcard study fragment
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Simple flashcards created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to the flashcard study fragment
                        com.example.quizfragments.ui.fragments.flashcard.FlashcardStudyFragment studyFragment =
                                com.example.quizfragments.ui.fragments.flashcard.FlashcardStudyFragment.newInstance((int) deckId);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, studyFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error creating simple flashcards: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    /**
     * Cleans a string to ensure it's valid JSON
     * This method helps handle common issues with AI-generated JSON
     * @param jsonString The raw JSON string from the AI
     * @return A cleaned JSON string
     */
    private String cleanJsonString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return "{}"; // Return empty JSON object if input is null or empty
        }

        // Find the first occurrence of '{' or '[' (start of JSON)
        int jsonStart = -1;
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            if (c == '{' || c == '[') {
                jsonStart = i;
                break;
            }
        }

        // Find the last occurrence of '}' or ']' (end of JSON)
        int jsonEnd = -1;
        for (int i = jsonString.length() - 1; i >= 0; i--) {
            char c = jsonString.charAt(i);
            if (c == '}' || c == ']') {
                jsonEnd = i + 1; // +1 to include the closing bracket
                break;
            }
        }

        // If we found valid JSON start and end markers, extract the JSON part
        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            return jsonString.substring(jsonStart, jsonEnd);
        }

        // If we couldn't find valid JSON markers, return the original string
        return jsonString;
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            return;
        }

        // Update last edited time
        updateLastEditedTime();

        // Show a toast to indicate we're saving the note with formatting
        Toast.makeText(requireContext(), "Saving note with formatting...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                if (isEditMode) {
                    // Update existing note
                    currentNote.setTitle(title);
                    currentNote.setContent(content);
                    currentNote.setUpdatedAt(System.currentTimeMillis());
                    db.noteDao().update(currentNote);
                } else {
                    // Create new note
                    Note newNote = new Note(folderId, title, content);
                    db.noteDao().insert(newNote);
                }

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error saving note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }
}