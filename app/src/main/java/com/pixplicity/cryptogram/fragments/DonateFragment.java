package com.pixplicity.cryptogram.fragments;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;

import butterknife.OnClick;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DonateFragment extends BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate, container, false);
    }

    @OnClick(R.id.bt_bitcoin)
    protected void onClickBitcoin() {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + BuildConfig.BITCOIN_ADDRESS));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.donate_title)
                .setMessage(R.string.donate_message)
                .setPositiveButton(R.string.donate_copy_address, (dialogInterface, i) -> {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", BuildConfig.BITCOIN_ADDRESS));
                        Toast.makeText(context, R.string.donate_copy_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.donate_copy_failure, Toast.LENGTH_SHORT).show();
                        Crashlytics.logException(new IllegalStateException("Failed copying bitcoin address"));
                    }
                })
                .setNegativeButton(R.string.donate_launch_wallet, ((dialog1, which) -> {
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ignore) {
                        String installPackageName = "de.schildbach.wallet";
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + installPackageName)));
                        } catch (ActivityNotFoundException ignore2) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + installPackageName)));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, R.string.donate_launch_failure, Toast.LENGTH_SHORT).show();
                                Crashlytics.logException(new IllegalStateException("Failed launching Google Play", e));
                            }
                        }
                    }
                }))
                .show();

        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) == null) {
            // No intent available to handle action
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.install_bitcoin_wallet);
        }
    }

}
