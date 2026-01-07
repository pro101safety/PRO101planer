package by.instruction.planer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BackupActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        setTitle(R.string.menu_backup);

        Button exportBtn = findViewById(R.id.backup_export);
        Button importBtn = findViewById(R.id.backup_import);

        exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        writeBackup(uri);
                        Toast.makeText(this, R.string.backup_export_ok, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.backup_export_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        readBackup(uri);
                        Toast.makeText(this, R.string.backup_import_ok, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.backup_import_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        exportBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "planer_backup.json");
            exportLauncher.launch(intent);
        });

        importBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importLauncher.launch(intent);
        });
    }

    private void writeBackup(Uri uri) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("planner", PrefsUtil.readPref(this, PlannerStorage.PREF_NAME, PlannerStorage.KEY_ENTRIES));
        obj.put("folders", PrefsUtil.readPref(this, FolderStorage.PREF_NAME, null));

        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new IOException("No stream");
            os.write(obj.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private void readBackup(Uri uri) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getContentResolver().openInputStream(uri);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        JSONObject obj = new JSONObject(sb.toString());
        String planner = obj.optString("planner", null);
        String folders = obj.optString("folders", null);
        PrefsUtil.writePref(this, PlannerStorage.PREF_NAME, PlannerStorage.KEY_ENTRIES, planner);
        PrefsUtil.writePref(this, FolderStorage.PREF_NAME, null, folders);
    }
}