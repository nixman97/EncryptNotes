package com.example.notesenc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ReceiveActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    @Override
    public void onBackPressed() {
        setResult(2);
        finish();
    }

    private void receiveFromServer(final String code) {
        final ProgressDialog dialog = ProgressDialog.show(this, "Uploading notes to server", "Please wait ...", true);
        dialog.show();
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(ReceiveActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Please check your internet connection");
        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        setResult(2,intent);
                        finish();
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://apphost.ga/get.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dialog.dismiss();

                        Intent intent = new Intent();
                        intent.putExtra("notes", response);
                        intent.putExtra("pwd", code.substring(10));
                        if (response.equals("")) {
                            setResult(1, intent);
                            finish();
                        }
                        setResult(0, intent);
                        ReceiveActivity.this.finish();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("errrorrr");
                dialog.dismiss();
                alertDialog.show();


            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("code", code.substring(0, 10));

                return params;
            }
        };
        queue.add(stringRequest);
    }

    private ZXingScannerView mScannerView;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_receive);


        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(granted -> {
            if (granted) {
                mScannerView = new ZXingScannerView(this);
                setContentView(mScannerView);
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(ReceiveActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("No permission to open camera. Please grant permission!");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        setResult(2,intent);
                        ReceiveActivity.this.finish();
                    }

                });
                alertDialog.show();

            }

        });


    }


    @Override
    public void handleResult(Result result) {
        receiveFromServer(result.getText());

    }
}
