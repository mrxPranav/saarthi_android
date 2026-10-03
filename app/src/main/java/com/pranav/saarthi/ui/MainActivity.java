package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseProvider;
import com.pranav.saarthi.data.NotesRepository;
import com.pranav.saarthi.model.Note;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NotesRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository based on settings
        repository = DatabaseProvider.getRepository(this);

        EditText textPad = findViewById(R.id.textPad);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textPad.getText().toString().trim();
                if (!text.isEmpty()) {
                    // Database operations should be off the main thread, especially for remote DB
                    new Thread(() -> {
                        long id = repository.addNote(
                                text,
                                "", // Title kept blank as requested
                                "General",
                                "Uncategorized",
                                "Active",
                                "",
                                "");

                        runOnUiThread(() -> {
                            if (id != -1) {
                                Toast.makeText(MainActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                                textPad.setText("");
                            } else {
                                Toast.makeText(MainActivity.this, "Error saving note. Check logs for details.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh repository in case settings changed
        repository = DatabaseProvider.getRepository(this);
    }

}