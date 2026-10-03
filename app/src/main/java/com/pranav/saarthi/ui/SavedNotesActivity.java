package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseProvider;
import com.pranav.saarthi.data.NotesRepository;
import com.pranav.saarthi.model.Note;
import com.pranav.saarthi.ui.adapter.NotesAdapter;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SavedNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private NotesRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Notes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Use the selected repository
        repository = DatabaseProvider.getRepository(this);

        loadNotes();
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes = repository.getAllNotes();
            runOnUiThread(() -> {
                if (notes.isEmpty()) {
                    Toast.makeText(this, "No notes found in current database.", Toast.LENGTH_SHORT).show();
                }
                adapter = new NotesAdapter(notes,
                        position -> { // OnDelete
                            new androidx.appcompat.app.AlertDialog.Builder(SavedNotesActivity.this)
                                    .setTitle("Delete Note")
                                    .setMessage("Are you sure you want to delete this note?")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        Note note = notes.get(position);
                                        deleteNote(note.getId());
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        },
                        position -> { // OnAi
                            Note note = notes.get(position);
                            generateAiTitle(note.getId());
                        },
                        note -> { // OnItemClick
                            android.content.Intent intent = new android.content.Intent(SavedNotesActivity.this,
                                    ViewNoteActivity.class);
                            intent.putExtra("note_id", note.getId());
                            intent.putExtra("note_title", note.getTitle());
                            intent.putExtra("note_content", note.getText());
                            startActivity(intent);
                        });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void deleteNote(int id) {
        new Thread(() -> {
            repository.deleteNote(id);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                loadNotes(); // Refresh list
            });
        }).start();
    }

    private void generateAiTitle(int id) {
        Toast.makeText(this, "Generating AI Title...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            Note updatedNote = repository.generateTitle(id);
            runOnUiThread(() -> {
                if (updatedNote != null) {
                    Toast.makeText(this, "Title generated!", Toast.LENGTH_SHORT).show();
                    loadNotes();
                } else {
                    Toast.makeText(this, "Failed to generate title. This feature might only work with cloud database.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
