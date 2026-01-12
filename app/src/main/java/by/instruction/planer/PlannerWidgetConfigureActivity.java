package by.instruction.planer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class PlannerWidgetConfigureActivity extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configure);

        // Get widget ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If invalid ID, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        Button addButton = findViewById(R.id.widget_configure_add_button);
        addButton.setOnClickListener(v -> {
            // Create the widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            PlannerWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

            // Return success
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();

            Toast.makeText(this, "Виджет добавлен на рабочий стол", Toast.LENGTH_SHORT).show();
        });

        Button cancelButton = findViewById(R.id.widget_configure_cancel_button);
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}