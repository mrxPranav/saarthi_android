package com.pranav.saarthi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewNoteActivity extends AppCompatActivity {

    private TextView tvNoteTitle;
    private EditText etNoteContent;
    private EditText etPrompt;
    private Button btnAction;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("View Note");
        }

        tvNoteTitle = findViewById(R.id.tvNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        etPrompt = findViewById(R.id.etPrompt);
        btnAction = findViewById(R.id.btnAction);

        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            noteId = intent.getIntExtra("note_id", -1);
            String title = intent.getStringExtra("note_title");
            String content = intent.getStringExtra("note_content");

            tvNoteTitle.setText(title);
            etNoteContent.setText(content);
        }

        btnAction.setOnClickListener(v -> {
            String prompt = etPrompt.getText().toString().trim();
            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Action clicked for prompt: " + prompt, Toast.LENGTH_SHORT).show();
                // TODO: Implement AI action here
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
