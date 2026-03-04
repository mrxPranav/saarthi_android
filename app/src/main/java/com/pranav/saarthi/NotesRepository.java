package com.pranav.saarthi;

import java.util.List;

public interface NotesRepository {
    long addNote(String text, String title, String category, String subCategory, String status, String remarks,
            String comment);

    List<Note> getAllNotes();

    void deleteNote(int id);

    Note generateTitle(int noteId);

    List<Note> generateTitlesAll();

    List<Note> generateTitlesToday();

    // Task related methods
    long addTask(Task task);

    List<Task> getAllTasks();

    void updateTask(Task task);

    void deleteTaskFromRepo(int id);
}
