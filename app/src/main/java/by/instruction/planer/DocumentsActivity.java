package by.instruction.planer;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.AlarmManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public class DocumentsActivity extends AppCompatActivity implements DocumentAdapter.Listener {

    public static final String EXTRA_FOLDER_ID = "extra_folder_id";
    public static final String EXTRA_FOLDER_TITLE = "extra_folder_title";

    private DocumentStorage storage;
    private DocumentStorage.Data data;
    private DocumentAdapter adapter;
    private String currentFolderId;
    private String currentFolderTitle;

    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        setTitle(R.string.documents_title);

        currentFolderId = getIntent().getStringExtra(EXTRA_FOLDER_ID);
        currentFolderTitle = getIntent().getStringExtra(EXTRA_FOLDER_TITLE);
        if (currentFolderTitle != null) {
            setTitle(currentFolderTitle);
        }

        storage = new DocumentStorage(this);
        data = storage.load();

        adapter = new DocumentAdapter(this);
        RecyclerView list = findViewById(R.id.documents_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.add_folder_fab).setOnClickListener(v -> showAddFolderDialog());
        findViewById(R.id.add_file_fab).setOnClickListener(v -> pickFile());

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                addFile(uri);
            }
        });

        refresh();
    }

    private void refresh() {
        data = storage.load();
        List<DocumentStorage.FolderItem> folders = new ArrayList<>();
        List<DocumentStorage.FileItem> files = new ArrayList<>();
        for (DocumentStorage.FolderItem f : data.folders) {
            if (equalsNullable(f.parentId, currentFolderId)) {
                folders.add(f);
            }
        }
        for (DocumentStorage.FileItem f : data.files) {
            if (equalsNullable(f.folderId, currentFolderId)) {
                files.add(f);
            }
        }
        adapter.setData(folders, files);
    }

    private boolean equalsNullable(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private void showAddFolderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_folder, null, false);
        EditText nameInput = dialogView.findViewById(R.id.folder_name_input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.documents_add_folder)
                .setView(dialogView)
                .setPositiveButton(R.string.folder_add_action, (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.folder_empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<DocumentStorage.FolderItem> folders = new ArrayList<>(data.folders);
                    folders.add(new DocumentStorage.FolderItem(storage.newId(), currentFolderId, name));
                    storage.save(new DocumentStorage.Data(folders, data.files));
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void pickFile() {
        String[] types = getString(R.string.documents_file_types).split(",");
        filePickerLauncher.launch(types);
    }

    private void addFile(Uri uri) {
        String name = queryDisplayName(uri);
        if (name == null || name.isEmpty()) {
            name = uri.getLastPathSegment();
        }
        String mime = getContentResolver().getType(uri);
        List<DocumentStorage.FileItem> files = new ArrayList<>(data.files);
        files.add(new DocumentStorage.FileItem(
                storage.newId(),
                currentFolderId,
                name,
                uri.toString(),
                mime,
                null,
                null,
                null
        ));
        storage.save(new DocumentStorage.Data(data.folders, files));
        refresh();
    }

    private String queryDisplayName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void onFolderClick(DocumentStorage.FolderItem folder) {
        Intent intent = new Intent(this, DocumentsActivity.class);
        intent.putExtra(EXTRA_FOLDER_ID, folder.id);
        intent.putExtra(EXTRA_FOLDER_TITLE, folder.name);
        startActivity(intent);
    }

    @Override
    public void onFolderLongClick(DocumentStorage.FolderItem folder) {
        String[] actions = {
                getString(R.string.folder_rename),
                getString(R.string.folder_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(folder.name)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showRenameFolderDialog(folder);
                    } else if (which == 1) {
                        deleteFolder(folder);
                    }
                })
                .show();
    }

    private void showRenameFolderDialog(DocumentStorage.FolderItem folder) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_folder, null, false);
        EditText nameInput = dialogView.findViewById(R.id.folder_name_input);
        nameInput.setText(folder.name);
        new AlertDialog.Builder(this)
                .setTitle(R.string.folder_rename)
                .setView(dialogView)
                .setPositiveButton(R.string.folder_rename, (d, w) -> {
                    String newName = nameInput.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, R.string.folder_empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<DocumentStorage.FolderItem> folders = new ArrayList<>();
                    for (DocumentStorage.FolderItem f : data.folders) {
                        if (f.id.equals(folder.id)) {
                            folders.add(new DocumentStorage.FolderItem(f.id, f.parentId, newName));
                        } else {
                            folders.add(f);
                        }
                    }
                    storage.save(new DocumentStorage.Data(folders, data.files));
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteFolder(DocumentStorage.FolderItem folder) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.folder_delete)
                .setPositiveButton(R.string.folder_delete, (d, w) -> {
                    List<String> toRemove = collectDescendants(folder.id);
                    toRemove.add(folder.id);

                    List<DocumentStorage.FolderItem> folders = new ArrayList<>();
                    for (DocumentStorage.FolderItem f : data.folders) {
                        if (!toRemove.contains(f.id)) {
                            folders.add(f);
                        }
                    }

                    List<DocumentStorage.FileItem> files = new ArrayList<>();
                    for (DocumentStorage.FileItem fi : data.files) {
                        if (fi.folderId != null && toRemove.contains(fi.folderId)) continue;
                        files.add(fi);
                    }

                    storage.save(new DocumentStorage.Data(folders, files));
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private List<String> collectDescendants(String folderId) {
        List<String> ids = new ArrayList<>();
        for (DocumentStorage.FolderItem f : data.folders) {
            if (folderId.equals(f.parentId)) {
                ids.add(f.id);
                ids.addAll(collectDescendants(f.id));
            }
        }
        return ids;
    }

    @Override
    public void onFileClick(DocumentStorage.FileItem file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(file.uri), file.mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.reminder_in_past, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderClick(DocumentStorage.FileItem file) {
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
                        showRepeatDialog(file, selected, offset);
                    })
                    .setNegativeButton(R.string.reminder_remove, (d, w) -> {
                        updateReminder(file, null, null, null);
                        cancelReminder(file);
                        refresh();
                        Toast.makeText(this, R.string.reminder_removed, Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void showRepeatDialog(DocumentStorage.FileItem file, Calendar selected, int offsetMinutes) {
        String[] repeatOptions = new String[]{
                getString(R.string.reminder_repeat_none),
                getString(R.string.reminder_repeat_hour),
                getString(R.string.reminder_repeat_day),
                getString(R.string.reminder_repeat_week),
                getString(R.string.reminder_repeat_month),
                getString(R.string.reminder_repeat_quarter),
                getString(R.string.reminder_repeat_half_year),
                getString(R.string.reminder_repeat_year)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.reminder_repeat_title)
                .setSingleChoiceItems(repeatOptions, 0, null)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    AlertDialog dlg = (AlertDialog) d;
                    int checked = dlg.getListView().getCheckedItemPosition();
                    int recurrence = mapRecurrence(checked);
                    updateReminder(file, selected.getTimeInMillis(), offsetMinutes, recurrence);
                    scheduleReminder(file, selected, offsetMinutes, recurrence);
                    refresh();
                    Toast.makeText(this, R.string.reminder_set_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateReminder(DocumentStorage.FileItem file, Long atMillis, Integer offsetMinutes, Integer recurrence) {
        List<DocumentStorage.FileItem> files = new ArrayList<>();
        for (DocumentStorage.FileItem f : data.files) {
            if (f.id.equals(file.id)) {
                files.add(f.withReminder(atMillis, offsetMinutes, recurrence));
            } else {
                files.add(f);
            }
        }
        storage.save(new DocumentStorage.Data(data.folders, files));
        data = storage.load();
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
                return ReminderConfig.RECURRENCE_HALF_YEAR;
            case 7:
                return ReminderConfig.RECURRENCE_YEAR;
            default:
                return ReminderConfig.RECURRENCE_NONE;
        }
    }

    private void pickDateTime(Calendar base, Consumer<Calendar> onPicked) {
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

    private void scheduleReminder(DocumentStorage.FileItem file, Calendar selected, int offsetMinutes, int recurrence) {
        Calendar baseTime = (Calendar) selected.clone();
        Calendar reminderTime = (Calendar) baseTime.clone();
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
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, getString(R.string.documents_title));
        intent.putExtra(ReminderReceiver.EXTRA_MESSAGE, file.name);
        intent.putExtra(ReminderReceiver.EXTRA_RECURRENCE, recurrence);
        intent.putExtra(ReminderReceiver.EXTRA_TRIGGER_AT, baseTime.getTimeInMillis());
        intent.putExtra(ReminderReceiver.EXTRA_OFFSET, offsetMinutes);
        intent.putExtra(ReminderReceiver.EXTRA_SKIP_SAVE, true);
        intent.putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, buildRequestCode(file.id));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(file.id),
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

    private void cancelReminder(DocumentStorage.FileItem file) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(file.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private int buildRequestCode(String id) {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public void onDeleteFile(DocumentStorage.FileItem file) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.documents_file_delete_confirm)
                .setPositiveButton(R.string.folder_delete, (d, w) -> {
                    cancelReminder(file);
                    List<DocumentStorage.FileItem> files = new ArrayList<>();
                    for (DocumentStorage.FileItem f : data.files) {
                        if (!f.id.equals(file.id)) {
                            files.add(f);
                        }
                    }
                    storage.save(new DocumentStorage.Data(data.folders, files));
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}

