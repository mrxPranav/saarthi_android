package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseProvider;
import com.pranav.saarthi.data.NotesRepository;
import com.pranav.saarthi.model.Task;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskText;
    private NotesRepository repository;
    private Task existingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Task");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        existingTask = (Task) getIntent().getSerializableExtra("task");
        if (existingTask == null) {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = DatabaseProvider.getRepository(this);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText("Edit Task");

        etTaskName = findViewById(R.id.etTaskName);
        etTaskText = findViewById(R.id.etTaskText);
        MaterialButton btnSaveTask = findViewById(R.id.btnSaveTask);
        btnSaveTask.setText("UPDATE TASK");

        etTaskName.setText(existingTask.getName());
        etTaskText.setText(existingTask.getText());

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask();
            }
        });
    }

    private void updateTask() {
        String name = etTaskName.getText().toString().trim();
        String text = etTaskText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            return;
        }

        existingTask.setName(name);
        existingTask.setText(text);

        new Thread(() -> {
            repository.updateTask(existingTask);
            runOnUiThread(() -> {
                Toast.makeText(EditTaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
