package by.instruction.planer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TemplateSectionAdapter extends RecyclerView.Adapter<TemplateSectionAdapter.VH> {

    public interface Listener {
        void onAddClick(TemplateSection section);

        void onReminderClick(TemplateSection section, TemplateTaskStorage.TaskItem task);

        void onDeleteClick(TemplateSection section, TemplateTaskStorage.TaskItem task);
    }

    public static class TemplateSection {
        public final String key;
        public final String title;
        public final List<TemplateTaskStorage.TaskItem> tasks;

        public TemplateSection(String key, String title, List<TemplateTaskStorage.TaskItem> tasks) {
            this.key = key;
            this.title = title;
            this.tasks = tasks;
        }
    }

    private final List<TemplateSection> data = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());

    public TemplateSectionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<TemplateSection> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template_section, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TemplateSection section = data.get(position);
        holder.title.setText(section.title);
        holder.addButton.setOnClickListener(v -> {
            if (listener != null) listener.onAddClick(section);
        });

        holder.tasksContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        for (TemplateTaskStorage.TaskItem task : section.tasks) {
            View row = inflater.inflate(R.layout.item_template_task, holder.tasksContainer, false);
            TextView text = row.findViewById(R.id.template_task_text);
            TextView info = row.findViewById(R.id.template_task_reminder);
            ImageButton reminder = row.findViewById(R.id.template_task_reminder_button);
            ImageButton delete = row.findViewById(R.id.template_task_delete_button);

            text.setText(task.text);
            if (task.reminderAtMillis != null) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(task.reminderAtMillis);
                StringBuilder b = new StringBuilder();
                b.append(dateFormat.format(c.getTime()));
                if (task.recurrenceType != null && task.recurrenceType > 0) {
                    b.append(" • ").append(recurrenceLabel(row, task.recurrenceType));
                }
                info.setText(b.toString());
                info.setVisibility(View.VISIBLE);
            } else {
                info.setVisibility(View.GONE);
            }

            reminder.setOnClickListener(v -> {
                if (listener != null) listener.onReminderClick(section, task);
            });

            if (task.isStatic) {
                delete.setVisibility(View.GONE);
            } else {
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(section, task);
                });
            }

            holder.tasksContainer.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
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

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageButton addButton;
        final LinearLayout tasksContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.template_section_title);
            addButton = itemView.findViewById(R.id.template_section_add_button);
            tasksContainer = itemView.findViewById(R.id.template_section_tasks);
        }
    }
}

