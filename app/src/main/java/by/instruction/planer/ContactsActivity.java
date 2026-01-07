package by.instruction.planer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setTitle(R.string.menu_contacts);

        CardView developerCard = findViewById(R.id.card_developer);
        TextView developerText = findViewById(R.id.developer_text);
        developerCard.setOnClickListener(v -> openEmail(getString(R.string.contacts_email)));
        developerText.setOnClickListener(v -> openEmail(getString(R.string.contacts_email)));

        CardView servicesCard = findViewById(R.id.card_services);
        servicesCard.setOnClickListener(v -> openLink(getString(R.string.contacts_services_link)));

        CardView tgChannelCard = findViewById(R.id.card_tg_channel);
        tgChannelCard.setOnClickListener(v -> openLink(getString(R.string.contacts_tg_channel_link)));

        CardView tgAdsCard = findViewById(R.id.card_tg_ads);
        tgAdsCard.setOnClickListener(v -> openLink(getString(R.string.contacts_tg_ads_link)));

        CardView azbukaCard = findViewById(R.id.card_azbuka);
        azbukaCard.setOnClickListener(this::openAzbuka);

        CardView paperkaCard = findViewById(R.id.card_paperka);
        paperkaCard.setOnClickListener(this::openPaperka);
    }

    private void openEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(intent, getString(R.string.contacts_email_title)));
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void openAzbuka(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.azbuka_link)));
        startActivity(intent);
    }

    private void openPaperka(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.paperka_link)));
        startActivity(intent);
    }
}

