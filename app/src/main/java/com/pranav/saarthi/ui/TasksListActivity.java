package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseProvider;
import com.pranav.saarthi.data.NotesRepository;
import com.pranav.saarthi.model.Task;
import com.pranav.saarthi.ui.adapter.TasksAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TasksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TasksAdapter adapter;
    private NotesRepository repository;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tasks List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        repository = DatabaseProvider.getRepository(this);

        loadTasks();
    }

    private void loadTasks() {
        new Thread(() -> {
            taskList = repository.getAllTasks();
            runOnUiThread(() -> {
                if (taskList.isEmpty()) {
                    Toast.makeText(this, "No tasks found.", Toast.LENGTH_SHORT).show();
                }
                adapter = new TasksAdapter(taskList, new TasksAdapter.OnTaskActionListener() {
                    @Override
                    public void onEdit(int position) {
                        Task task = taskList.get(position);
                        Intent intent = new Intent(TasksListActivity.this, EditTaskActivity.class);
                        intent.putExtra("task", task);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(int position) {
                        Task task = taskList.get(position);
                        new androidx.appcompat.app.AlertDialog.Builder(TasksListActivity.this)
                                .setTitle("Delete Task")
                                .setMessage("Are you sure you want to delete this task?")
                                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task.getId()))
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void deleteTask(int id) {
        new Thread(() -> {
            repository.deleteTaskFromRepo(id);
            runOnUiThread(() -> {
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                loadTasks();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
