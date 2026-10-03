package com.pranav.saarthi.api;


import com.pranav.saarthi.model.AiNoteResponse;
import com.pranav.saarthi.model.AiNotesListResponse;
import com.pranav.saarthi.model.HealthCheck;
import com.pranav.saarthi.model.Note;
import com.pranav.saarthi.model.Task;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotesApi {

    @GET("notes/")
    Call<List<Note>> getNotes();

    @POST("notes/")
    Call<Note> addNote(@Body Note note);

    @GET("health/")
    Call<Void> getHealth();

    @GET("health/checks")
    Call<List<HealthCheck>> getHealthChecks(
            @Query("limit") int limit,
            @Query("skip") int skip);

    @retrofit2.http.DELETE("notes/{id}/")
    Call<Void> deleteNote(@retrofit2.http.Path("id") int id);

    @POST("ai/generate-title/{note_id}/")
    Call<AiNoteResponse> generateTitle(@retrofit2.http.Path("note_id") int noteId);

    @POST("ai/generate-titles/all/")
    Call<AiNotesListResponse> generateTitlesAll();

    @POST("ai/generate-titles/today/")
    Call<AiNotesListResponse> generateTitlesToday();

    // Task endpoints
    @GET("tasks/")
    Call<List<Task>> getTasks();

    @POST("tasks/")
    Call<Task> addTask(@Body Task task);

    @retrofit2.http.PUT("tasks/{id}/")
    Call<Task> updateTask(@retrofit2.http.Path("id") int id, @Body Task task);

    @retrofit2.http.DELETE("tasks/{id}/")
    Call<Void> deleteTask(@retrofit2.http.Path("id") int id);
}
