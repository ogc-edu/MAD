package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.*;
import com.example.quizfragments.data.db.entities.Quiz;

public class QuizFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_dashboard, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        Button storeDB = view.findViewById(R.id.storeBtn);
        EditText textToStore = view.findViewById(R.id.textinput);
        Button getDB = view.findViewById(R.id.dbBtn);
        TextView showDB = view.findViewById(R.id.dbInfo);

        AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        storeDB.setOnClickListener(v ->{
            String title = textToStore.getText().toString().trim();
            if(!title.isEmpty()){
                Quiz quiz = new Quiz();
                quiz.title = title;
                quiz.description = "new quiz sample";

                new Thread(() -> {
                    db.quizDao().insert(quiz);
                }).start();
            }else{
                textToStore.setError("Enter a title!");
            }
        });

    }
}
