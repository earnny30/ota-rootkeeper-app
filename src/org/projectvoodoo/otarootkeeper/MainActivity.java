
package org.projectvoodoo.otarootkeeper;

import org.projectvoodoo.otarootkeeper.R.id;
import org.projectvoodoo.otarootkeeper.backend.Device;
import org.projectvoodoo.otarootkeeper.backend.Device.FileSystem;
import org.projectvoodoo.otarootkeeper.backend.Utils;
import org.projectvoodoo.otarootkeeper.ui.StatusRow;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "Voodoo OTA RootKeeper MainActivity";

    private Device mDevice;

    private StatusRow mSuperuserRow;
    private StatusRow mRootedRow;
    private StatusRow mRootGrantedRow;
    private StatusRow mFsSupportedRow;
    private StatusRow mSuProtectedRow;
    private Button mBackupButton;
    private Button mRestoreButton;
    private Button mDeleteBackupButton;
    private Button mUnrootButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Starting app");
        mDevice = new Device(this);

        setContentView(R.layout.main);

        mSuperuserRow = (StatusRow) findViewById(id.superuser_app_installed);
        mRootedRow = (StatusRow) findViewById(id.rooted);
        mRootGrantedRow = (StatusRow) findViewById(id.root_granted);
        mFsSupportedRow = (StatusRow) findViewById(id.fs_supported);
        mSuProtectedRow = (StatusRow) findViewById(id.su_protected);
        mBackupButton = (Button) findViewById(id.button_backup_root);
        mBackupButton.setOnClickListener(this);
        mRestoreButton = (Button) findViewById(id.button_restore_root);
        mRestoreButton.setOnClickListener(this);
        mDeleteBackupButton = (Button) findViewById(id.button_delete_backup);
        mDeleteBackupButton.setOnClickListener(this);
        mUnrootButton = (Button) findViewById(id.button_unroot);
        mUnrootButton.setOnClickListener(this);

        showStatus();
    }

    private void showStatus() {
        if (mDevice.isSuperuserAppInstalled)
            mSuperuserRow.setAvailable(true);
        else
            mSuperuserRow.setAvailable(false, "market://details?id=com.noshufou.android.su");

        mRootedRow.setAvailable(mDevice.isRooted);

        mRootGrantedRow.setAvailable(Utils.canGainSu(this));

        if (mDevice.mFileSystem == FileSystem.EXTFS)
            mFsSupportedRow.setAvailable(true);
        else
            mFsSupportedRow.setAvailable(false);

        mSuProtectedRow.setAvailable(mDevice.isSuProtected);

        mUnrootButton.setVisibility(View.GONE);

        if (mDevice.isRooted && !mDevice.isSuProtected) {
            if (mDevice.mFileSystem == FileSystem.EXTFS)
                mBackupButton.setText(R.string.protect_root);
            else
                mBackupButton.setText(R.string.backup_root);

            mRestoreButton.setVisibility(View.GONE);
            mDeleteBackupButton.setVisibility(View.GONE);

        } else if (mDevice.isRooted && mDevice.isSuProtected) {
            mBackupButton.setVisibility(View.GONE);
            mRestoreButton.setVisibility(View.GONE);
            mDeleteBackupButton.setVisibility(View.VISIBLE);
            mUnrootButton.setVisibility(View.VISIBLE);

        } else if (!mDevice.isRooted && mDevice.isSuProtected) {
            mBackupButton.setVisibility(View.GONE);
            mRestoreButton.setVisibility(View.VISIBLE);
            mDeleteBackupButton.setVisibility(View.GONE);
        } else {
            mBackupButton.setVisibility(View.GONE);
            mRestoreButton.setVisibility(View.GONE);
            mDeleteBackupButton.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case id.refresh:
                mDevice.analyzeSu();
                showStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button_backup_root:
                mDevice.mSuOps.backup();
                break;

            case R.id.button_restore_root:
                mDevice.mSuOps.restore();
                break;

            case R.id.button_delete_backup:
                mDevice.mSuOps.deleteBackup();
                break;

            case R.id.button_unroot:
                mDevice.mSuOps.unroot();
                break;

            default:
                break;
        }

        mDevice.analyzeSu();
        showStatus();
    }
}
