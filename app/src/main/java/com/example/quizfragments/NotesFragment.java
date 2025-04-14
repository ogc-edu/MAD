package com.example.quizfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class NotesFragment extends Fragment {

    private TextView tv1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv1 = view.findViewById(R.id.id1);

        String prompt = "Generate 10 multiple-choice quiz questions about Biology 1.\n" +
                "Each question must be in JSON format with the following structure:\n" +
                "{\n" +
                "  \"question\": \"string\",\n" +
                "  \"options\": [\"string\", \"string\", \"string\", \"string\"],\n" +
                "  \"answer\": \"int\",\n" +
                "  \"explanation\": \"string\"\n" +
                "}\n" +
                "Return the entire output as a JSON array with 5 such objects.\n" +
                "Do not include any extra text or explanation outside the JSON array.\n" +
                "The answer is an int from 0 to 3 corresponding to the correct option index.";

        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                tv1.setText(result);
            }

            @Override
            public void onError(String error) {
                tv1.setText("Error: " + error);
            }
        });
    }

}
