package com.pranav.saarthi.api;

import com.pranav.saarthi.model.AiNoteResponse;
import com.pranav.saarthi.model.AiNotesListResponse;
import com.pranav.saarthi.model.HealthCheck;
import com.pranav.saarthi.model.Note;
import com.pranav.saarthi.model.Task;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Paths for {id} routes have no trailing slash. The server answers a slash
 * mismatch with a 307 redirect, which OkHttp does not follow for PUT/DELETE.
 */
public interface NotesApi {

    @GET("notes/")
    Call<List<Note>> getNotes(@Query("skip") int skip, @Query("limit") int limit);

    @POST("notes/")
    Call<Note> addNote(@Body Note note);

    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Path("id") int id);

    @POST("ai/generate-title/{note_id}")
    Call<AiNoteResponse> generateTitle(@Path("note_id") int noteId);

    @POST("ai/generate-titles/all")
    Call<AiNotesListResponse> generateTitlesAll();

    @POST("ai/generate-titles/today")
    Call<AiNotesListResponse> generateTitlesToday();

    @GET("health/")
    Call<Void> getHealth();

    @GET("health/checks")
    Call<List<HealthCheck>> getHealthChecks(
            @Query("limit") int limit,
            @Query("skip") int skip);

    // Task endpoints
    @GET("tasks/")
    Call<List<Task>> getTasks(@Query("skip") int skip, @Query("limit") int limit);

    @POST("tasks/")
    Call<Task> addTask(@Body Task task);

    @PUT("tasks/{id}")
    Call<Task> updateTask(@Path("id") int id, @Body Task task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") int id);
}
