package by.instruction.planer;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

public class TemplateTasksActivity extends AppCompatActivity implements TemplateSectionAdapter.Listener {

    private TemplateTaskStorage storage;
    private TemplateSectionAdapter adapter;

    private static final String SECTION_PZ = "templates_pz";
    private static final String SECTION_INSTR = "templates_instr";
    private static final String SECTION_STAJ = "templates_staj";
    private static final String SECTION_OBUCH = "templates_obuch";
    private static final String SECTION_INSTR_DOC = "templates_instr_doc";
    private static final String SECTION_SUOT = "templates_suot";
    private static final String SECTION_MED = "templates_med";
    private static final String SECTION_CONTROL = "templates_control";
    private static final String SECTION_SIZ = "templates_siz";
    private static final String SECTION_PLAN_OT = "templates_plan_ot";
    private static final String SECTION_ORDERS = "templates_orders";
    private static final String SECTION_ATTEST = "templates_attest";
    private static final String SECTION_REPORTS = "templates_reports";
    private static final String SECTION_BUILDINGS = "templates_buildings";
    private static final String SECTION_MILK = "templates_milk";
    private static final String SECTION_FIRE = "templates_fire";
    private static final String SECTION_INDUSTRIAL = "templates_industrial";
    private static final String SECTION_ECOLOGY = "templates_ecology";
    private static final String SECTION_ELECTRO = "templates_electro";
    private static final String SECTION_RADIATION = "templates_radiation";
    private static final String SECTION_CIVIL = "templates_civil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_tasks);
        setTitle(R.string.templates_section_label);

        storage = new TemplateTaskStorage(this);
        adapter = new TemplateSectionAdapter(this);

        RecyclerView list = findViewById(R.id.template_sections_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        List<TemplateSectionAdapter.TemplateSection> sections = new ArrayList<>();
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_PZ,
                getString(R.string.templates_title_pz),
                storage.load(SECTION_PZ, seedPz())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_INSTR,
                getString(R.string.templates_title_instr),
                storage.load(SECTION_INSTR, seedInstr())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_STAJ,
                getString(R.string.templates_title_staj),
                storage.load(SECTION_STAJ, seedStaj())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_OBUCH,
                getString(R.string.templates_title_obuchenie),
                storage.load(SECTION_OBUCH, seedObuch())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_INSTR_DOC,
                getString(R.string.templates_title_instr_docs),
                storage.load(SECTION_INSTR_DOC, seedInstrDocs())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_SUOT,
                getString(R.string.templates_title_suot),
                storage.load(SECTION_SUOT, seedSuot())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_MED,
                getString(R.string.templates_title_med),
                storage.load(SECTION_MED, seedMed())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_CONTROL,
                getString(R.string.templates_title_control),
                storage.load(SECTION_CONTROL, seedControl())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_SIZ,
                getString(R.string.templates_title_siz),
                storage.load(SECTION_SIZ, seedSiz())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_PLAN_OT,
                getString(R.string.templates_title_plan_ot),
                storage.load(SECTION_PLAN_OT, seedPlanOt())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_ORDERS,
                getString(R.string.templates_title_orders),
                storage.load(SECTION_ORDERS, seedOrders())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_ATTEST,
                getString(R.string.templates_title_attest),
                storage.load(SECTION_ATTEST, seedAttest())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_REPORTS,
                getString(R.string.templates_title_reports),
                storage.load(SECTION_REPORTS, seedReports())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_BUILDINGS,
                getString(R.string.templates_title_buildings),
                storage.load(SECTION_BUILDINGS, seedBuildings())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_MILK,
                getString(R.string.templates_title_milk),
                storage.load(SECTION_MILK, seedMilk())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_FIRE,
                getString(R.string.templates_title_fire),
                storage.load(SECTION_FIRE, seedFire())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_INDUSTRIAL,
                getString(R.string.templates_title_industrial),
                storage.load(SECTION_INDUSTRIAL, seedIndustrial())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_ECOLOGY,
                getString(R.string.templates_title_ecology),
                storage.load(SECTION_ECOLOGY, seedEcology())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_ELECTRO,
                getString(R.string.templates_title_electro),
                storage.load(SECTION_ELECTRO, seedElectro())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_RADIATION,
                getString(R.string.templates_title_radiation),
                storage.load(SECTION_RADIATION, seedRadiation())
        ));
        sections.add(new TemplateSectionAdapter.TemplateSection(
                SECTION_CIVIL,
                getString(R.string.templates_title_civil),
                storage.load(SECTION_CIVIL, seedCivil())
        ));
        adapter.setData(sections);
    }

    private List<TemplateTaskStorage.TaskItem> seedPz() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_pz_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_pz_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_pz_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_pz_4), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedInstr() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instr_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instr_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instr_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instr_4), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instr_5), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedStaj() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_staj_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedObuch() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_4), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_5), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_6), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_7), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_obuch_8), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedInstrDocs() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instrdoc_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_instrdoc_2), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedSuot() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_suot_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_suot_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_suot_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_suot_4), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedMed() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_med_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_med_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_med_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_med_4), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedControl() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_control_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_control_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_control_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_control_4), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedSiz() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_siz_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_siz_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_siz_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_siz_4), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_siz_5), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedPlanOt() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_planot_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_planot_2), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedOrders() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_orders_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_orders_2), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_orders_3), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_orders_4), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedAttest() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_attest_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedReports() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_reports_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_reports_2), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedBuildings() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_buildings_1), true, null, null, null));
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_buildings_2), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedMilk() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_milk_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedFire() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_fire_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedIndustrial() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_industrial_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedEcology() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_ecology_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedElectro() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_electro_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedRadiation() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_radiation_1), true, null, null, null));
        return list;
    }

    private List<TemplateTaskStorage.TaskItem> seedCivil() {
        List<TemplateTaskStorage.TaskItem> list = new ArrayList<>();
        list.add(new TemplateTaskStorage.TaskItem(id(), getString(R.string.templates_civil_1), true, null, null, null));
        return list;
    }

    private String id() {
        return storage.newId();
    }

    @Override
    public void onAddClick(TemplateSectionAdapter.TemplateSection section) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_template_task, null, false);
        EditText input = view.findViewById(R.id.template_task_input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.templates_add)
                .setView(view)
                .setPositiveButton(R.string.quick_task_save, (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, R.string.reminder_need_text, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addTask(section, text);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addTask(TemplateSectionAdapter.TemplateSection section, String text) {
        List<TemplateTaskStorage.TaskItem> current = new ArrayList<>(section.tasks);
        current.add(new TemplateTaskStorage.TaskItem(id(), text, false, null, null, null));
        storage.save(section.key, current);
        loadData();
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReminderClick(TemplateSectionAdapter.TemplateSection section, TemplateTaskStorage.TaskItem task) {
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
                        showRepeatDialog(section, task, selected, offset);
                    })
                    .setNegativeButton(R.string.reminder_remove, (d, w) -> {
                        updateReminder(section, task, null, null, null);
                        cancelReminder(task);
                        loadData();
                        Toast.makeText(this, R.string.reminder_removed, Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    @Override
    public void onDeleteClick(TemplateSectionAdapter.TemplateSection section, TemplateTaskStorage.TaskItem task) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.templates_remove_confirm)
                .setPositiveButton(R.string.folder_delete, (d, w) -> {
                    List<TemplateTaskStorage.TaskItem> current = new ArrayList<>(section.tasks);
                    current.removeIf(t -> t.id.equals(task.id));
                    cancelReminder(task);
                    storage.save(section.key, current);
                    loadData();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showRepeatDialog(TemplateSectionAdapter.TemplateSection section, TemplateTaskStorage.TaskItem task, Calendar selected, int offsetMinutes) {
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
                    updateReminder(section, task, selected.getTimeInMillis(), offsetMinutes, recurrence);
                    scheduleReminder(task, selected, offsetMinutes, recurrence);
                    loadData();
                    Toast.makeText(this, R.string.reminder_set_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateReminder(TemplateSectionAdapter.TemplateSection section, TemplateTaskStorage.TaskItem task, Long atMillis, Integer offset, Integer recurrence) {
        List<TemplateTaskStorage.TaskItem> current = new ArrayList<>(section.tasks);
        List<TemplateTaskStorage.TaskItem> updated = new ArrayList<>();
        for (TemplateTaskStorage.TaskItem t : current) {
            if (t.id.equals(task.id)) {
                updated.add(t.withReminder(atMillis, offset, recurrence));
            } else {
                updated.add(t);
            }
        }
        storage.save(section.key, updated);
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

    private void scheduleReminder(TemplateTaskStorage.TaskItem task, Calendar selected, int offsetMinutes, int recurrence) {
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
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, getString(R.string.templates_section_label));
        intent.putExtra(ReminderReceiver.EXTRA_MESSAGE, task.text);
        intent.putExtra(ReminderReceiver.EXTRA_RECURRENCE, recurrence);
        intent.putExtra(ReminderReceiver.EXTRA_TRIGGER_AT, baseTime.getTimeInMillis());
        intent.putExtra(ReminderReceiver.EXTRA_OFFSET, offsetMinutes);
        intent.putExtra(ReminderReceiver.EXTRA_SKIP_SAVE, true);
        intent.putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, buildRequestCode(task.id));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(task.id),
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

    private void cancelReminder(TemplateTaskStorage.TaskItem task) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                buildRequestCode(task.id),
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
}

