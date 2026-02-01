package by.instruction.planer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onFolderClick(DocumentStorage.FolderItem folder);

        void onFileClick(DocumentStorage.FileItem file);

        void onReminderClick(DocumentStorage.FileItem file);

        void onDeleteFile(DocumentStorage.FileItem file);

        void onFolderLongClick(DocumentStorage.FolderItem folder);
    }

    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_FILE = 1;

    private final List<Object> items = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());

    public DocumentAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<DocumentStorage.FolderItem> folders, List<DocumentStorage.FileItem> files) {
        items.clear();
        if (folders != null) items.addAll(folders);
        if (files != null) items.addAll(files);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof DocumentStorage.FolderItem) return TYPE_FOLDER;
        return TYPE_FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FOLDER) {
            View v = inflater.inflate(R.layout.item_document_folder, parent, false);
            return new FolderVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_document_file, parent, false);
            return new FileVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof FolderVH) {
            DocumentStorage.FolderItem folder = (DocumentStorage.FolderItem) item;
            FolderVH vh = (FolderVH) holder;
            vh.title.setText(folder.name);
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFolderClick(folder);
            });
            vh.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onFolderLongClick(folder);
                return true;
            });
        } else if (holder instanceof FileVH) {
            DocumentStorage.FileItem file = (DocumentStorage.FileItem) item;
            FileVH vh = (FileVH) holder;
            vh.title.setText(file.name);
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFileClick(file);
            });

            if (file.reminderAtMillis != null) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(file.reminderAtMillis);
                StringBuilder b = new StringBuilder();
                b.append(dateFormat.format(c.getTime()));
                if (file.recurrenceType != null && file.recurrenceType > 0) {
                    b.append(" • ").append(recurrenceLabel(vh.itemView, file.recurrenceType));
                }
                vh.reminderInfo.setVisibility(View.VISIBLE);
                vh.reminderInfo.setText(b.toString());
            } else {
                vh.reminderInfo.setVisibility(View.GONE);
            }

            vh.reminderButton.setOnClickListener(v -> {
                if (listener != null) listener.onReminderClick(file);
            });

            vh.deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteFile(file);
            });
        }
    }

    private String recurrenceLabel(View view, int recurrence) {
        switch (recurrence) {
            case ReminderConfig.RECURRENCE_HOUR:
                return view.getResources().getString(R.string.reminder_repeat_hour);
            case ReminderConfig.RECURRENCE_DAY:
                return view.getResources().getString(R.string.reminder_repeat_day);
            case ReminderConfig.RECURRENCE_WEEK:
                return view.getResources().getString(R.string.reminder_repeat_week);
            case ReminderConfig.RECURRENCE_MONTH:
                return view.getResources().getString(R.string.reminder_repeat_month);
            case ReminderConfig.RECURRENCE_QUARTER:
                return view.getResources().getString(R.string.reminder_repeat_quarter);
            case ReminderConfig.RECURRENCE_HALF_YEAR:
                return view.getResources().getString(R.string.reminder_repeat_half_year);
            case ReminderConfig.RECURRENCE_YEAR:
                return view.getResources().getString(R.string.reminder_repeat_year);
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FolderVH extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageView icon;

        FolderVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.document_folder_title);
            icon = itemView.findViewById(R.id.document_folder_icon);
        }
    }

    static class FileVH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView reminderInfo;
        final ImageButton reminderButton;
        final ImageButton deleteButton;
        final ImageView icon;

        FileVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.document_file_title);
            reminderInfo = itemView.findViewById(R.id.document_file_reminder);
            reminderButton = itemView.findViewById(R.id.document_file_reminder_button);
            deleteButton = itemView.findViewById(R.id.document_file_delete_button);
            icon = itemView.findViewById(R.id.document_file_icon);
        }
    }
}

