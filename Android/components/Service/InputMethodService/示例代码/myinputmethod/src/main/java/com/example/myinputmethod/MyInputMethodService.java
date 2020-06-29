package com.example.myinputmethod;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public class MyInputMethodService extends InputMethodService {

    public static final String TAG = MyInputMethodService.class.getSimpleName();

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard_global, null);
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        Log.d(TAG, "onCreateInputView editorInfo:" + editorInfo.inputType);
        return view;
    }

    @Override
    public View onCreateCandidatesView() {
        TextView textView = new TextView(getBaseContext());
        textView.setText("fdsfdsf");
        return textView;
    }

    @Override
    public void onUpdateExtractingViews(EditorInfo ei) {
        super.onUpdateExtractingViews(ei);
        Log.d(TAG, "onUpdateExtractingViews editorInfo:" + ei.inputType);
        if (ei.inputType == MainActivity.INPUT_TYPE_LETTER) {
            View view = getLayoutInflater().inflate(R.layout.keyboard_global, null);
            setInputView(view);
        } else if (ei.inputType == MainActivity.INPUT_TYPE_NUM) {
            View view = getLayoutInflater().inflate(R.layout.keyboard_num, null);
            setInputView(view);
        } else {
            View view = getLayoutInflater().inflate(R.layout.keyboard_symbol, null);
            setInputView(view);
        }
    }
}
