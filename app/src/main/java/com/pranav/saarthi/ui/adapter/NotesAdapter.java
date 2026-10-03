package com.pranav.saarthi.ui.adapter;



import com.pranav.saarthi.R;
import com.pranav.saarthi.model.Note;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnAiClickListener {
        void onAiClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    private List<Note> notes;
    private OnDeleteClickListener deleteListener;
    private OnAiClickListener aiListener;
    private OnItemClickListener itemClickListener;

    public NotesAdapter(List<Note> notes, OnDeleteClickListener listener, OnAiClickListener aiListener,
            OnItemClickListener itemClickListener) {
        this.notes = notes;
        this.deleteListener = listener;
        this.aiListener = aiListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view, deleteListener, aiListener, itemClickListener, notes);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteText.setText(note.getText());
        holder.noteDateTime.setText(note.getDateTime());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteText, noteDateTime;
        android.widget.ImageButton btnDelete, btnGenerateTitle;

        public NoteViewHolder(@NonNull View itemView, OnDeleteClickListener listener, OnAiClickListener aiListener,
                OnItemClickListener itemClickListener, List<Note> notes) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteText = itemView.findViewById(R.id.noteText);
            noteDateTime = itemView.findViewById(R.id.noteDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnGenerateTitle = itemView.findViewById(R.id.btnGenerateTitle);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(notes.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });

            btnGenerateTitle.setOnClickListener(v -> {
                if (aiListener != null) {
                    aiListener.onAiClick(getAdapterPosition());
                }
            });
        }
    }
}
