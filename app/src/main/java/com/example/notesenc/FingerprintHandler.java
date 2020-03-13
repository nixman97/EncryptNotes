package com.example.notesenc;


import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private TextView tv;
    private RecyclerView r;
    private ImageView im;
    private AppCompatActivity context;
    private Menu upMenu;
    private AppCompatActivity act;
    FingerprintHandler(AppCompatActivity context, TextView tv, ImageView im, RecyclerView r) {
        this.tv = tv;
        this.im=im;
        this.context=context;
        this.r=r;
        r.setVisibility(View.INVISIBLE);
        im.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);
        context.findViewById(R.id.usePin).setVisibility(View.VISIBLE);

        context.findViewById(R.id.addBtn).setVisibility(View.INVISIBLE);

    }
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (errorCode!=5) {
            tv.setTextColor(Color.RED);
            im.setImageResource(R.drawable.ic_fingerprint_black_96dp);
            tv.setText(R.string.multiple_fingerprints_wrong);
            Intent add = new Intent(context, PinActivity.class);
            add.putExtra("check", 1); //open for add
            context.startActivityForResult(add, 1);
        }
    }
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
    }
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        tv.setVisibility(View.INVISIBLE);
        im.setVisibility(View.INVISIBLE);
        r.setVisibility(View.VISIBLE);
        context.findViewById(R.id.usePin).setVisibility(View.INVISIBLE);
        context.invalidateOptionsMenu();
        context.findViewById(R.id.addBtn).setVisibility(View.VISIBLE);

    }
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        tv.setText(R.string.fail_auth);
        tv.setTextColor(Color.RED);
        im.setImageResource(R.drawable.ic_fingerprint_black_96dp);
    }
    public void doAuth(FingerprintManager manager,
                       FingerprintManager.CryptoObject obj) {
        CancellationSignal signal = new CancellationSignal();
        try {
            manager.authenticate(obj, signal, 0, this, null);
        }
        catch(SecurityException sce) {}
    }
}