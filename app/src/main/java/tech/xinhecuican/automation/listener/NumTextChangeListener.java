package tech.xinhecuican.automation.listener;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;

public abstract class NumTextChangeListener implements TextWatcher {
    TextInputEditText editText;

    public NumTextChangeListener(TextInputEditText edit){
        editText = edit;
    }

    public abstract void onNumInput(int num);

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String s = String.valueOf(editText.getText());
        if (!s.trim().substring(0).equals(".")
                &&Integer.parseInt(s)<1) {
            editText.setText("1");
            editText.setSelection(1);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        onNumInput(Integer.parseInt(editable.toString()));
    }
}
