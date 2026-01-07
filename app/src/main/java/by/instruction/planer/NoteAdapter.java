package by.instruction.planer;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

    public interface Listener {
        void onReminderClick(NoteStorage.NoteItem note);

        void onDeleteClick(NoteStorage.NoteItem note);
    }

    private final List<NoteStorage.NoteItem> data = new ArrayList<>();
    private final Context context;
    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());

    public NoteAdapter(Context context, Listener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setData(List<NoteStorage.NoteItem> notes) {
        data.clear();
        if (notes != null) data.addAll(notes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NoteStorage.NoteItem note = data.get(position);
        holder.text.setText(note.text == null ? "" : note.text);

        if (note.imageUri != null) {
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageURI(Uri.parse(note.imageUri));
        } else {
            holder.image.setVisibility(View.GONE);
        }

        if (note.reminderAtMillis != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(note.reminderAtMillis);
            StringBuilder builder = new StringBuilder();
            builder.append(dateFormat.format(c.getTime()));
            if (note.recurrenceType != null && note.recurrenceType > 0) {
                builder.append(" • ").append(recurrenceLabel(note.recurrenceType));
            }
            holder.reminderInfo.setVisibility(View.VISIBLE);
            holder.reminderInfo.setText(builder.toString());
        } else {
            holder.reminderInfo.setVisibility(View.GONE);
        }

        holder.reminderButton.setOnClickListener(v -> {
            if (listener != null) listener.onReminderClick(note);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String recurrenceLabel(int recurrence) {
        if (context == null) return "";
        switch (recurrence) {
            case ReminderConfig.RECURRENCE_HOUR:
                return context.getString(R.string.reminder_repeat_hour);
            case ReminderConfig.RECURRENCE_DAY:
                return context.getString(R.string.reminder_repeat_day);
            case ReminderConfig.RECURRENCE_WEEK:
                return context.getString(R.string.reminder_repeat_week);
            case ReminderConfig.RECURRENCE_MONTH:
                return context.getString(R.string.reminder_repeat_month);
            case ReminderConfig.RECURRENCE_QUARTER:
                return context.getString(R.string.reminder_repeat_quarter);
            case ReminderConfig.RECURRENCE_YEAR:
                return context.getString(R.string.reminder_repeat_year);
            default:
                return "";
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView text;
        final ImageView image;
        final TextView reminderInfo;
        final ImageButton reminderButton;
        final ImageButton deleteButton;

        VH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.note_text);
            image = itemView.findViewById(R.id.note_image);
            reminderInfo = itemView.findViewById(R.id.note_reminder_info);
            reminderButton = itemView.findViewById(R.id.note_reminder_button);
            deleteButton = itemView.findViewById(R.id.note_delete_button);
        }
    }
}

