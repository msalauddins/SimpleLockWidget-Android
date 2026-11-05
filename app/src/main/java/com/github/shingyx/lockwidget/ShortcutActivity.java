package com.github.shingyx.lockwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent() != null ? getIntent().getAction() : null;
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            createShortcut();
        } else {
            lockScreen();
        }

        finish();
    }

    @SuppressWarnings("deprecation")
    private void createShortcut() {
        Intent shortcutIntent = new Intent(LockAdminReceiver.ACTION_LOCK, null, this, ShortcutActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.lock));
        resultIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
        );
        setResult(Activity.RESULT_OK, resultIntent);
    }

    private void lockScreen() {
        if (!LockAdminReceiver.lockScreen(this)) {
            Toast.makeText(this, getString(R.string.toast_help), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
