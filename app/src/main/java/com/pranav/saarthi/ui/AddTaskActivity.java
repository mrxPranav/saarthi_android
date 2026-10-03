package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseProvider;
import com.pranav.saarthi.data.NotesRepository;
import com.pranav.saarthi.model.Task;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskText;
    private NotesRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Task");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DatabaseProvider.getRepository(this);

        etTaskName = findViewById(R.id.etTaskName);
        etTaskText = findViewById(R.id.etTaskText);
        MaterialButton btnSaveTask = findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String text = etTaskText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(0, name, text, "", "Pending", "", null);

        new Thread(() -> {
            long id = repository.addTask(task);
            runOnUiThread(() -> {
                if (id != -1) {
                    Toast.makeText(AddTaskActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddTaskActivity.this, "Error saving task", Toast.LENGTH_LONG).show();
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
