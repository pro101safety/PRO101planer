package by.instruction.planer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SizActivity extends AppCompatActivity {

    private static final String SIZ_URL = "https://otb.by/spec/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siz);
        setTitle(R.string.nav_siz);

        View card = findViewById(R.id.card_siz);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SIZ_URL));
            startActivity(intent);
        });
    }
}

