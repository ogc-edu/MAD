package com.example.quizfragments;

import android.os.Handler;
import android.os.Looper;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class AI{
    private static final String API_KEY = "AIzaSyD3EW6Y58ai3PLtU9GUDlQJCps4n-Yrgp0";
    private static final String MODEL_ID = "gemini-1.5-flash";

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ResponseCallback {
        void onResponse(String result);
        void onError(String error);
    }

    public static void modelCall(String prompt, ResponseCallback callback) {
        GenerativeModel gm = new GenerativeModel(MODEL_ID, API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content input = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> future = model.generateContent(input);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse response) {
                mainHandler.post(() -> callback.onResponse(response.getText()));
            }

            @Override
            public void onFailure(Throwable t) {
                mainHandler.post(() -> callback.onError(t.getMessage()));
            }
        }, MoreExecutors.directExecutor());
    }


}
