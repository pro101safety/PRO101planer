package by.instruction.planer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class PlansActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        setTitle(getString(R.string.nav_checks));

        findViewById(R.id.card_plan_selective).setOnClickListener(v ->
                openUrl("https://otb.by/index.php?option=com_acym&ctrl=fronturl&task=click&urlid=60&userid=23802&mailid=873"));

        findViewById(R.id.card_plan_inspection).setOnClickListener(v ->
                openUrl("https://otb.by/index.php?option=com_acym&ctrl=fronturl&task=click&urlid=61&userid=23802&mailid=873"));
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}

