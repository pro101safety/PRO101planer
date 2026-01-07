package by.instruction.planer;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.annotation.RequiresApi;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import android.graphics.Paint;

import by.instruction.planer.ReminderConfig;
import by.instruction.planer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 5001;
    private static final int[] REMINDER_OFFSETS = ReminderConfig.REMINDER_OFFSETS;

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private PlannerStorage storage;
    private LayoutInflater inflater;
    private GestureDetectorCompat gestureDetector;
    private int dayOffset = 0;

    private final String[] dayTitles = new String[]{
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inflater = LayoutInflater.from(this);
        storage = new PlannerStorage(this);

        setSupportActionBar(binding.toolbar);
        setupDrawer();
        setupMenu();
        setupGestures();
        setupBackHandler();
        createNotificationChannel();
        requestNotificationPermission();
        binding.contentContainer.setNestedScrollingEnabled(false);
        binding.contentContainer.post(this::buildDayScreen);

        binding.todayButton.setOnClickListener(v -> {
            dayOffset = 0;
            buildDayScreen();
        });

        binding.prevButton.setOnClickListener(v -> changeDay(-1));
        binding.nextButton.setOnClickListener(v -> changeDay(1));
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = binding.drawerLayout;
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setupMenu() {
        NavigationView navigationView = binding.navigationView;
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) {
            searchView = new SearchView(this);
            searchItem.setActionView(searchView);
        }
        final SearchView finalSearchView = searchView;
        if (finalSearchView != null) {
            finalSearchView.setQueryHint(getString(R.string.search_hint));
            finalSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    handleSearch(query);
                    finalSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_contacts) {
            startActivity(new Intent(this, ContactsActivity.class));
            return true;
        } else if (id == R.id.action_backup) {
            startActivity(new Intent(this, BackupActivity.class));
            return true;
        } else if (id == R.id.action_how_it_works) {
            startActivity(new Intent(this, HowItWorksActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSearch(String query) {
        java.util.List<PlannerStorage.SearchResult> matches = storage.searchEntries(query);
        if (matches.isEmpty()) {
            Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat displayDate = new SimpleDateFormat("d MMM", Locale.getDefault());
        CharSequence[] items = new CharSequence[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            PlannerStorage.SearchResult r = matches.get(i);
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(r.date));
            } catch (Exception e) {
                c = null;
            }
            String dateStr = c == null ? r.date : displayDate.format(c.getTime());
            items[i] = dateStr + " • " + String.format(Locale.getDefault(), "%02d:00", r.hour) + "\n" + r.text;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.search_results_title)
                .setItems(items, (dialog, which) -> {
                    if (which < 0 || which >= matches.size()) return;
                    PlannerStorage.SearchResult r = matches.get(which);
                    navigateToResult(r);
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void navigateToResult(PlannerStorage.SearchResult result) {
        try {
            Calendar targetDate = Calendar.getInstance();
            targetDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(result.date));

            Calendar today = Calendar.getInstance();
            long diffMs = targetDate.getTimeInMillis() - today.getTimeInMillis();
            int daysDiff = (int) Math.round(diffMs / (1000d * 60 * 60 * 24));
            dayOffset = daysDiff;

            buildDayScreen();

            int targetDayIndex = getDayIndex(targetDate);
            binding.contentContainer.post(() -> scrollToDayHour(targetDayIndex, result.hour));
        } catch (Exception e) {
            Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToDayHour(int dayIndex, int hour) {
        if (binding.dayContainer.getChildCount() == 0) return;
        View card = binding.dayContainer.getChildAt(0);
        if (card == null) return;

        androidx.core.widget.NestedScrollView dayScroll = card.findViewById(R.id.day_scroll);
        LinearLayout hoursContainer = card.findViewById(R.id.hours_container);
        if (hoursContainer == null || dayScroll == null) return;
        if (hour < 0 || hour >= hoursContainer.getChildCount()) return;
        View targetHour = hoursContainer.getChildAt(hour);
        dayScroll.post(() -> dayScroll.smoothScrollTo(0, targetHour.getTop()));
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String message = null;
        if (id == R.id.nav_templates) {
            startActivity(new Intent(this, TemplateTasksActivity.class));
        } else if (id == R.id.nav_documents) {
            startActivity(new Intent(this, DocumentsActivity.class));
        } else if (id == R.id.nav_siz) {
            startActivity(new Intent(this, SizActivity.class));
        } else if (id == R.id.nav_checks) {
            startActivity(new Intent(this, PlansActivity.class));
        }

        binding.drawerLayout.closeDrawers();
        return true;
    }

    private void openFolderSection(String key, String title, String defaults) {
        Intent intent = new Intent(this, FolderActivity.class);
        intent.putExtra(FolderActivity.EXTRA_SECTION_KEY, key);
        intent.putExtra(FolderActivity.EXTRA_SECTION_TITLE, title);
        if (defaults != null) intent.putExtra(FolderActivity.EXTRA_DEFAULT_CHILDREN, defaults);
        startActivity(intent);
    }

    private void setupGestures() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        // swipe left -> next day
                        changeDay(1);
                    } else {
                        // swipe right -> previous day
                        changeDay(-1);
                    }
                    return true;
                }
                return false;
            }
        });

        View.OnTouchListener listener = (v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // allow vertical scroll inside
        };
        binding.contentContainer.setOnTouchListener(listener);
        binding.dayContainer.setOnTouchListener(listener);
        binding.getRoot().setOnTouchListener(listener);

        binding.fabAddTask.setOnClickListener(v -> showQuickAddDialog());
    }

    private void changeDay(int delta) {
        dayOffset += delta;
        buildDayScreen();
    }

    private void buildDayScreen() {
        LinearLayout container = binding.dayContainer;
        container.removeAllViews();

        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_MONTH, dayOffset);

        int dayIndex = getDayIndex(day);

        SimpleDateFormat rangeFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        int year = day.get(Calendar.YEAR);
        binding.weekTitle.setText(rangeFormat.format(day.getTime()) + " " + year);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        binding.dayName.setText(dayNameFormat.format(day.getTime()));

        View card = createDayCard(day, dayIndex);
        container.addView(card);
    }

    private View createDayCard(Calendar day, int dayIndex) {
        View card = inflater.inflate(R.layout.item_day_planner, binding.dayContainer, false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (getResources().getDisplayMetrics().density * 6);
        lp.setMargins(margin, margin, margin, margin);
        card.setLayoutParams(lp);

        TextView title = card.findViewById(R.id.day_title);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        int normalizedIndex = ((dayIndex % 7) + 7) % 7;
        title.setText(dayTitles[normalizedIndex] + " • " + dateFormat.format(day.getTime()));

        LinearLayout hoursContainer = card.findViewById(R.id.hours_container);
        addHourRows(hoursContainer, day, normalizedIndex);

        androidx.core.widget.NestedScrollView dayScroll = card.findViewById(R.id.day_scroll);
        dayScroll.setNestedScrollingEnabled(true);
        dayScroll.post(() -> {
            View target = hoursContainer.getChildAt(7); // 07:00
            if (target != null) {
                dayScroll.scrollTo(0, target.getTop());
            }
        });

        return card;
    }

    private void addHourRows(LinearLayout container, Calendar day, int dayIndex) {
        container.removeAllViews();
        for (int hour = 0; hour < 24; hour++) {
            final int hourOfDay = hour;
            final Calendar dayCopy = (Calendar) day.clone();
            View row = inflater.inflate(R.layout.item_hour_row, container, false);
            TextView hourLabel = row.findViewById(R.id.hour_label);
            EditText hourInput = row.findViewById(R.id.hour_input);
            ImageButton reminderButton = row.findViewById(R.id.reminder_button);

            hourLabel.setText(String.format(Locale.getDefault(), "%02d:00", hourOfDay));

            PlannerEntry stored = storage.getEntry(dayCopy, hourOfDay);
            if (stored != null && !TextUtils.isEmpty(stored.getText())) {
                hourInput.setText(stored.getText());
                reminderButton.setVisibility(View.VISIBLE);
            } else {
                reminderButton.setVisibility(View.GONE);
            }

            hourInput.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString();
                    PlannerEntry latest = storage.getEntry(dayCopy, hourOfDay);
                    Integer offset = latest != null ? latest.getReminderOffsetMinutes() : null;
                    Long reminderAt = latest != null ? latest.getReminderAtMillis() : null;
                    Integer recurrence = latest != null ? latest.getRecurrenceType() : null;
                    Boolean done = latest != null ? latest.isDone() : null;
                    storage.saveEntry(dayCopy, hourOfDay, text, offset, reminderAt, recurrence, done);
                    if (text.trim().isEmpty()) {
                        CheckBox doneBox = row.findViewById(R.id.done_checkbox);
                        doneBox.setChecked(false);
                        applyStrike(hourInput, false);
                        reminderButton.setVisibility(View.GONE);
                        cancelReminder(dayCopy, hourOfDay);
                    } else {
                        reminderButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            CheckBox doneBox = row.findViewById(R.id.done_checkbox);
            boolean isDone = stored != null && stored.isDone();
            doneBox.setChecked(isDone);
            applyStrike(hourInput, isDone);

            doneBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String text = hourInput.getText().toString();
                if (text.trim().isEmpty()) {
                    buttonView.setChecked(false);
                    Toast.makeText(this, R.string.reminder_need_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                PlannerEntry latest = storage.getEntry(dayCopy, hourOfDay);
                Integer offset = latest != null ? latest.getReminderOffsetMinutes() : null;
                Long reminderAt = latest != null ? latest.getReminderAtMillis() : null;
                Integer recurrence = latest != null ? latest.getRecurrenceType() : null;
                storage.saveEntry(dayCopy, hourOfDay, text, offset, reminderAt, recurrence, isChecked);
                applyStrike(hourInput, isChecked);
            });

            reminderButton.setOnClickListener(v -> {
                String currentText = hourInput.getText().toString().trim();
                if (currentText.isEmpty()) {
                    Toast.makeText(this, R.string.reminder_need_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                showReminderDialog(dayCopy, hourOfDay, dayIndex, currentText, hourInput, doneBox, reminderButton);
            });

            container.addView(row);
        }
    }

    private void applyStrike(EditText editText, boolean strike) {
        if (strike) {
            editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            editText.setPaintFlags(editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private int getDayIndex(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday=1
        return (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2; // Monday=0
    }

    private void showReminderDialog(Calendar day, int hour, int dayIndex, String text, EditText hourInput, CheckBox doneBox, ImageButton reminderButton) {
        Calendar initial = (Calendar) day.clone();
        initial.set(Calendar.HOUR_OF_DAY, hour);
        initial.set(Calendar.MINUTE, 0);
        initial.set(Calendar.SECOND, 0);
        initial.set(Calendar.MILLISECOND, 0);
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
                        if (which < 0 || which >= REMINDER_OFFSETS.length) {
                            return;
                        }
                        int offsetMinutes = REMINDER_OFFSETS[which];
                        showRepeatDialog(selected, day, hour, dayIndex, text, offsetMinutes, hourInput, doneBox, reminderButton);
                    })
                    .setNegativeButton(R.string.reminder_remove, (dialog, which) -> {
                        cancelReminder((Calendar) day.clone(), hour);
                        storage.clearReminder(day, hour);
                        Toast.makeText(this, R.string.reminder_removed, Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void showRepeatDialog(Calendar selected, Calendar originalDay, int originalHour, int originalDayIndex, String text,
                                  int offsetMinutes, EditText hourInput, CheckBox doneBox, ImageButton reminderButton) {
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
                    PlannerEntry latest = storage.getEntry(originalDay, originalHour);
                    Boolean done = latest != null ? latest.isDone() : null;
                    int targetHour = selected.get(Calendar.HOUR_OF_DAY);
                    int targetDayIndex = getDayIndex(selected);

                        scheduleReminder((Calendar) selected.clone(), targetDayIndex, offsetMinutes, text, recurrence);
                    storage.saveEntry(selected, targetHour, text, offsetMinutes, selected.getTimeInMillis(), recurrence, done);

                    Calendar original = (Calendar) originalDay.clone();
                    if (original.get(Calendar.YEAR) != selected.get(Calendar.YEAR)
                            || original.get(Calendar.DAY_OF_YEAR) != selected.get(Calendar.DAY_OF_YEAR)
                            || originalHour != targetHour) {
                        storage.saveEntry(original, originalHour, "", null, null, null, false);
                        hourInput.setText("");
                        doneBox.setChecked(false);
                        applyStrike(hourInput, false);
                        reminderButton.setVisibility(View.GONE);
                    }
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

    private void scheduleReminder(Calendar baseTime, int dayIndex, int offsetMinutes, String text, int recurrence) {
        Calendar reminderTime = (Calendar) baseTime.clone();
        reminderTime.add(Calendar.MINUTE, -offsetMinutes);

        long triggerAt = reminderTime.getTimeInMillis();
        if (triggerAt < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.reminder_in_past, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, formatReminderTitle(baseTime, dayIndex));
        intent.putExtra(ReminderReceiver.EXTRA_MESSAGE, text);
        intent.putExtra(ReminderReceiver.EXTRA_RECURRENCE, recurrence);
        intent.putExtra(ReminderReceiver.EXTRA_TRIGGER_AT, baseTime.getTimeInMillis());
        intent.putExtra(ReminderReceiver.EXTRA_OFFSET, offsetMinutes);
        int requestCode = buildRequestCode(baseTime, baseTime.get(Calendar.HOUR_OF_DAY));
        intent.putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
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

    private void cancelReminder(Calendar day, int hour) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        int requestCode = buildRequestCode(day, hour);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private int buildRequestCode(Calendar day, int hour) {
        String key = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(day.getTime()) + "_" + hour;
        return Objects.hash(key);
    }

    private String formatReminderTitle(Calendar day, int dayIndex) {
        return dayTitles[dayIndex] + " " + String.format(Locale.getDefault(), "%02d:%02d", day.get(Calendar.HOUR_OF_DAY), day.get(Calendar.MINUTE));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void promptExactAlarmPermission() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            Toast.makeText(this, R.string.reminder_exact_alarm_request, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.reminder_exact_alarm_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void pickDateTime(Calendar base, java.util.function.Consumer<Calendar> onPicked) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar selected = (Calendar) base.clone();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (TimePicker t, int hourOfDay, int minute) -> {
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                ReminderReceiver.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(getString(R.string.notification_channel_description));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.exit_toast_reminders, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showQuickAddDialog() {
        View dialogView = inflater.inflate(R.layout.dialog_add_quick_task, null, false);
        EditText input = dialogView.findViewById(R.id.quick_task_input);
        Calendar now = Calendar.getInstance();
        new AlertDialog.Builder(this)
                .setTitle(R.string.quick_task_title)
                .setView(dialogView)
                .setPositiveButton(R.string.quick_task_save, (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, R.string.reminder_need_text, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pickDateTime(now, selected -> {
                        showRepeatDialogForQuickTask(selected, text);
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showRepeatDialogForQuickTask(Calendar selected, String text) {
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
                    int targetHour = selected.get(Calendar.HOUR_OF_DAY);
                    int targetDayIndex = getDayIndex(selected);
                    storage.saveEntry(selected, targetHour, text, 0, selected.getTimeInMillis(), recurrence, false);
                    scheduleReminder((Calendar) selected.clone(), targetDayIndex, 0, text, recurrence);
                    if (targetDayIndex == getDayIndex(Calendar.getInstance())) {
                        buildDayScreen();
                    }
                    Toast.makeText(this, R.string.reminder_set_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}

