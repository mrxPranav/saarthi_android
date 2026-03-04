package com.pranav.saarthi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class UpdateTitleActivity extends AppCompatActivity {

    private NotesRepository repository;
    private Button btnGenerateAll, btnGenerateToday;
    private ProgressBar progressBar;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AI Update Titles");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DatabaseProvider.getRepository(this);

        btnGenerateAll = findViewById(R.id.btnGenerateAll);
        btnGenerateToday = findViewById(R.id.btnGenerateToday);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        btnGenerateAll.setOnClickListener(v -> performBatchUpdate(true));
        btnGenerateToday.setOnClickListener(v -> performBatchUpdate(false));
    }

    private void performBatchUpdate(boolean isAll) {
        setLoading(true);
        tvStatus.setText("Generating titles...");

        new Thread(() -> {
            List<Note> updatedNotes;
            if (isAll) {
                updatedNotes = repository.generateTitlesAll();
            } else {
                updatedNotes = repository.generateTitlesToday();
            }

            runOnUiThread(() -> {
                setLoading(false);
                if (updatedNotes != null && !updatedNotes.isEmpty()) {
                    tvStatus.setText("Success! Updated " + updatedNotes.size() + " notes.");
                    Toast.makeText(this, "Titles updated successfully", Toast.LENGTH_SHORT).show();
                } else if (updatedNotes != null) {
                    tvStatus.setText("No notes needed updating.");
                    Toast.makeText(this, "No notes found to update", Toast.LENGTH_SHORT).show();
                } else {
                    tvStatus.setText("Failed to generate titles.");
                    Toast.makeText(this, "Error: AI update failed. Ensure you are using Cloud Database.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void setLoading(boolean loading) {
        btnGenerateAll.setEnabled(!loading);
        btnGenerateToday.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
