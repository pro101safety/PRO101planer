package by.instruction.planer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(R.string.menu_about);
        android.widget.TextView versionView = findViewById(R.id.version_value);
        if (versionView != null) {
            versionView.setText(BuildConfig.VERSION_NAME);
        }
    }
}

