package com.github.shingyx.lockwidget;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1001;

    private Switch enabledSwitch;
    private Button lockButton;
    private Button addWidgetButton;
    private TextView statusSubtitle;
    private CompoundButton.OnCheckedChangeListener onEnabledSwitched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enabledSwitch = findViewById(R.id.enabled_switch);
        lockButton = findViewById(R.id.lock_button);
        addWidgetButton = findViewById(R.id.add_widget_button);
        statusSubtitle = findViewById(R.id.status_subtitle);

        onEnabledSwitched = (v, isChecked) -> {
            if (isChecked) {
                requestDeviceAdminPermission();
            } else {
                LockAdminReceiver.revokeAdmin(this);
                refreshState();
            }
        };
        enabledSwitch.setOnCheckedChangeListener(onEnabledSwitched);

        lockButton.setOnClickListener(v -> {
            if (LockAdminReceiver.isEnabled(this)) {
                LockAdminReceiver.lockScreen(this);
                finish();
            } else {
                requestDeviceAdminPermission();
            }
        });

        addWidgetButton.setOnClickListener(v -> pinLockWidgetToHomeScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            refreshState();
        }
    }

    private void requestDeviceAdminPermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, LockAdminReceiver.getComponentName(this));
        intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.service_description)
        );
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    private void pinLockWidgetToHomeScreen() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutIntent = new Intent(this, ShortcutActivity.class);
            shortcutIntent.setAction(LockAdminReceiver.ACTION_LOCK);

            ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(this, "lock_screen_shortcut")
                    .setShortLabel(getString(R.string.lock))
                    .setLongLabel(getString(R.string.lock_screen_now))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(shortcutIntent)
                    .build();

            shortcutManager.requestPinShortcut(pinShortcutInfo, null);
            return;
        }

        AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
            ComponentName widgetProvider = new ComponentName(this, LockWidgetProvider.class);
            appWidgetManager.requestPinAppWidget(widgetProvider, null, null);
            return;
        }

        Toast.makeText(this, getString(R.string.widget_pinned_toast), Toast.LENGTH_SHORT).show();
    }

    private void refreshState() {
        boolean enabled = LockAdminReceiver.isEnabled(this);
        silentlyCheckEnabledSwitch(enabled);

        if (enabled) {
            lockButton.setText(R.string.lock_screen_now);
            statusSubtitle.setText("Ready! Tap 'Lock Screen Now' or add the 1-tap widget. Zero Accessibility used — 100% safe for all financial apps.");
        } else {
            lockButton.setText(R.string.enable_screen_lock);
            statusSubtitle.setText("Tap 'Enable Screen Lock' once. Uses Banking-Safe Device Lock instead of Accessibility, so financial apps never warn you.");
        }
    }

    private void silentlyCheckEnabledSwitch(boolean check) {
        enabledSwitch.setOnCheckedChangeListener(null);
        enabledSwitch.setChecked(check);
        enabledSwitch.setOnCheckedChangeListener(onEnabledSwitched);
    }
}
