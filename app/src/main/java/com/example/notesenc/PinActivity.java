package com.example.notesenc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.goodiebag.pinview.Pinview;

public class PinActivity extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if ( getIntent().getIntExtra("check",-1)==0) {
            Intent intent = new Intent();
            setResult(0, intent);
            intent.putExtra("code", -1);
            finish();
        }
        else {
            Intent intent = new Intent();
            setResult(0, intent);
            intent.putExtra("code", -2);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        final TextView msg = findViewById(R.id.msg);
        final View view = new View(this);;
        Pinview p = new Pinview(this);
        p = findViewById(R.id.pinview);
        final int check = getIntent().getIntExtra("check",-1);
        if (check==1) {
            msg.setText(R.string.PIN_request);
        }
        if (check==2) {
            msg.setText(R.string.pin_activity_request);
        }
        if (check==0) {
            msg.setText(R.string.pin_set);
            findViewById(R.id.msg2).setVisibility(View.VISIBLE);
        }
        final SharedPreferences prefs = this.getSharedPreferences(
                "com.example.victor.notes", Context.MODE_PRIVATE);
        p.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                if (check==0) {
                    prefs.edit().putString("PIN", pinview.getValue()).apply();
                    Intent intent = new Intent();
                    setResult(0, intent);
                    intent.putExtra("code", 0);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view = getCurrentFocus();
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    finish();
                }
                else {
                    if ( prefs.getString("PIN", "0000").equals(pinview.getValue())) {
                        Intent intent = new Intent();
                        setResult(1, intent);
                        intent.putExtra("code", 1);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getCurrentFocus();
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        finish();
                    }
                    else {
                        msg.setText("Wrong code\n");
                        msg.setTextColor(Color.RED);
                        for (int i = 0;i < pinview.getPinLength();i++) {
                            pinview.onKey(pinview.getFocusedChild(), KeyEvent.KEYCODE_DEL, new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL));
                        }
                    }
                }
            }
        });


    }
}
