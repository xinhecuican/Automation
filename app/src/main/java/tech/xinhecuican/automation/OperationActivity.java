package tech.xinhecuican.automation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import tech.xinhecuican.automation.model.Operation;

public class OperationActivity extends AppCompatActivity {
    private Operation operation;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        Intent intent = getIntent();
        operation = (Operation)intent.getSerializableExtra("operation");
        index = intent.getIntExtra("index", 0);
        TextInputEditText textEdit = (TextInputEditText) findViewById(R.id.operation_name_edit);
        textEdit.setText(operation.getName());
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                operation.setName(String.valueOf(editable));
            }
        });
    }
}