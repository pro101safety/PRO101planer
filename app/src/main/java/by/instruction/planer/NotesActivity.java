package by.instruction.planer;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.AlarmManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NoteAdapter.Listener {

    public static final String EXTRA_SECTION_KEY = "extra_section_key";
    public static final String EXTRA_FOLDER_ID = "extra_folder_id";
    public static final String EXTRA_FOLDER_TITLE = "extra_folder_title";

    private NoteStorage storage;
    private NoteAdapter adapter;
    private String sectionKey;
    private String folderId;
    private String folderTitle;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        sectionKey = getIntent().getStringExtra(EXTRA_SECTION_KEY);
        folderId = getIntent().getStringExtra(EXTRA_FOLDER_ID);
        folderTitle = getIntent().getStringExtra(EXTRA_FOLDER_TITLE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (folderTitle != null) {
                actionBar.setTitle(folderTitle);
            }
        }

        storage = new NoteStorage(this);
        adapter = new NoteAdapter(this, this);

        RecyclerView list = findViewById(R.id.notes_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.add_note_fab).setOnClickListener(v -> showAddMenu());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                addPhotoNote(uri);
            }
        });

        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddMenu() {
        String[] options = {getString(R.string.note_add_text), getString(R.string.note_add_photo)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.note_add)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddTextDialog();
                    } else if (which == 1) {
                        pickImage();
                    }
                })
                .show();
    }

    private void showAddTextDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_note, null, false);
        EditText input = view.findViewById(R.id.note_text_input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.note_add_text)
                .setView(view)
                .setPositiveButton(R.string.quick_task_save, (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, R.string.reminder_need_text, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveNote(text, null);
                    Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void pickImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void addPhotoNote(Uri uri) {
        saveNote("", uri.toString());
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
    }

    private void saveNote(String text, String imageUri) {
        NoteStorage.NoteItem note = new NoteStorage.NoteItem(
                storage.newId(),
                folderId,
                text,
                imageUri,
                null,
                null,
                null,
                System.currentTimeMillis()
        );
        storage.upsert(sectionKey, note);
        loadData();
    }

    private void loadData() {
        List<NoteStorage.NoteItem> all = storage.load(sectionKey);
        List<NoteStorage.NoteItem> filtered = new ArrayList<>();
        for (NoteStorage.NoteItem note : all) {
            if (folderId != null && !folderId.equals(note.folderId)) continue;
            filtered.add(note);
        }
        filtered.sort(Comparator.comparingLong(n -> -n.createdAtMillis));
        adapter.setData(filtered);
    }

    @Override
    public void onReminderClick(NoteStorage.NoteItem note) {
        showReminderDialog(note);
    }

    @Override
    public void onDeleteClick(NoteStorage.NoteItem note) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.note_delete_confirm)
                .setPositiveButton(R.string.folder_delete, (d, w) -> {
                    cancelNoteReminder(note);
                    storage.delete(sectionKey, note.id);
                    loadData();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showReminderDialog(NoteStorage.NoteItem note) {
        Calendar initial = Calendar.getInstance();
        pickDateTime(initial, selected -> {
            String[] options = new String[]{
                    getString(R.string.reminder_at_time),
                    getString(R.string.reminder_before_15),
                    getString(R.string.reminder_before_30),
                    getString(R.string.reminder_before_60)
            };
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reminder_dialog_title)
                    .setItems(options, (dialog, which) -> {
                        if (which < 0 || which >= ReminderConfig.REMINDER_OFFSETS.length) return;
                        int offset = ReminderConfig.REMINDER_OFFSETS[which];
                        showRepeatDialog(note, selected, offset);
                    })
                    .setNegativeButton(R.string.reminder_remove, (d, w) -> {
                        cancelNoteReminder(note);
                        NoteStorage.NoteItem cleared = note.withReminder(null, null, null);
                        storage.upsert(sectionKey, cleared);
                        loadData();
                        Toast.makeText(this, R.string.reminder_removed, Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void showRepeatDialog(NoteStorage.NoteItem note, Calendar selected, int offsetMinutes) {
        String[] repeatOptions = new String[]{
                getString(R.string.reminder_repeat_none),
                getString(R.string.reminder_repeat_hour),
                getString(R.string.reminder_repeat_day),
                getString(R.string.reminder_repeat_week),
                getString(R.string.reminder_repeat_month),
                getString(R.string.reminder_repeat_quarter),
                getString(R.string.reminder_repeat_year)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.reminder_repeat_title)
                .setSingleChoiceItems(repeatOptions, 0, null)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    AlertDialog dlg = (AlertDialog) d;
                    int checked = dlg.getListView().getCheckedItemPosition();
                    int recurrence = mapRecurrence(checked);
                    NoteStorage.NoteItem updated = note.withReminder(selected.getTimeInMillis(), offsetMinutes, recurrence);
                    storage.upsert(sectionKey, updated);
                    cancelNoteReminder(note);
                    scheduleNoteReminder(updated);
                    loadData();
                    Toast.makeText(this, R.string.reminder_set_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int mapRecurrence(int checked) {
        switch (checked) {
            case 1:
                return ReminderConfig.RECURRENCE_HOUR;
            case 2:
                return ReminderConfig.RECURRENCE_DAY;
            case 3:
                return ReminderConfig.RECURRENCE_WEEK;
            case 4:
                return ReminderConfig.RECURRENCE_MONTH;
            case 5:
                return ReminderConfig.RECURRENCE_QUARTER;
            case 6:
                return ReminderConfig.RECURRENCE_YEAR;
            default:
                return ReminderConfig.RECURRENCE_NONE;
        }
    }

    private void pickDateTime(Calendar base, java.util.function.Consumer<Calendar> onPicked) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = (Calendar) base.clone();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (t, hourOfDay, minute) -> {
                                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selected.set(Calendar.MINUTE, minute);
                                selected.set(Calendar.SECOND, 0);
                                selected.set(Calendar.MILLISECOND, 0);
                                onPicked.accept(selected);
                            },
                            base.get(Calendar.HOUR_OF_DAY),
                            base.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void scheduleNoteReminder(NoteStorage.NoteItem note) {
        if (note.reminderAtMillis == null) return;
        Calendar baseTime = Calendar.getInstance();
        baseTime.setTimeInMillis(note.reminderAtMillis);

        Calendar reminderTime = (Calendar) baseTime.clone();
        int offsetMinutes = note.reminderOffsetMinutes == null ? 0 : note.reminderOffsetMinutes;
        reminderTime.add(Calendar.MINUTE, -offsetMinutes);

        long triggerAt = reminderTime.getTimeInMillis();
        if (triggerAt < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.reminder_in_past, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    Toast.makeText(this, R.string.reminder_exact_alarm_request, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.reminder_exact_alarm_unavailable, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, buildReminderTitle());
        intent.putExtra(ReminderReceiver.EXTRA_MESSAGE, buildReminderMessage(note));
        intent.putExtra(ReminderReceiver.EXTRA_RECURRENCE, note.recurrenceType == null ? ReminderConfig.RECURRENCE_NONE : note.recurrenceType);
        intent.putExtra(ReminderReceiver.EXTRA_TRIGGER_AT, baseTime.getTimeInMillis());
        intent.putExtra(ReminderReceiver.EXTRA_OFFSET, offsetMinutes);
        intent.putExtra(ReminderReceiver.EXTRA_SKIP_SAVE, true);
        intent.putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, buildRequestCode(note.id));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(note.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            boolean useExact = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    useExact = false;
                }
            }
            try {
                if (useExact) {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                }
            } catch (SecurityException e) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        }
    }

    private void cancelNoteReminder(NoteStorage.NoteItem note) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(note.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private int buildRequestCode(String noteId) {
        return noteId == null ? 0 : noteId.hashCode();
    }

    private String buildReminderTitle() {
        if (folderTitle != null) return folderTitle;
        return getString(R.string.app_name);
    }

    private String buildReminderMessage(NoteStorage.NoteItem note) {
        if (note.text != null && !note.text.trim().isEmpty()) return note.text;
        return getString(R.string.note_add_photo);
    }
}

