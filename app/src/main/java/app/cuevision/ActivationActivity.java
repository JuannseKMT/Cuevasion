package app.cuevision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ActivationActivity extends AppCompatActivity {

    private EditText input;
    private TextView status;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_activation);
        prefs = new Prefs(this);

        input = findViewById(R.id.keyInput);
        status = findViewById(R.id.status);

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
        }

        String saved = prefs.key();
        if (!saved.isEmpty()) input.setText(saved);

        findViewById(R.id.activateBtn).setOnClickListener(v -> check(true));
        findViewById(R.id.launchBtn).setOnClickListener(v -> check(false));
    }

    private void check(boolean save) {
        String raw = input.getText().toString();
        int r = License.validate(raw);

        if (r == License.VALID) {
            if (save) prefs.setKey(License.normalize(raw));
            status.setText("ACTIVA - " + License.remainingDays(raw) + " dias");
            status.setTextColor(Color.parseColor("#4ADE80"));
            go();
        } else {
            status.setText(r == License.EXPIRED ? "Clave vencida" : "Clave invalida");
            status.setTextColor(Color.parseColor("#E02A2A"));
        }
    }

    private void go() {
        if (Settings.canDrawOverlays(this)) {
            startForegroundService(new Intent(this, OverlayService.class));
            finish();
        } else {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this) && License.validate(prefs.key()) == License.VALID
                && !isFinishing()) {
            startForegroundService(new Intent(this, OverlayService.class));
            finish();
        }
    }
}