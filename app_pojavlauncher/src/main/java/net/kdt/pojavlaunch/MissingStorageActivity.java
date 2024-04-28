package net.kdt.pojavlaunch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.widget.Toast;

public class MissingStorageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_test_no_sdcard);

        // Check if external storage is available
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        boolean externalStorageAvailable = false;
        for (StorageVolume storageVolume : storageManager.getStorageVolumes()) {
            if (storageVolume.isRemovable()) {
                externalStorageAvailable = true;
                break;
            }
        }

        // Show a message if external storage is not available
        if (!externalStorageAvailable) {
            Toast.makeText(this, "External storage is not available. Please insert an SD card.", Toast.LENGTH_LONG).show();
        }
    }
}
