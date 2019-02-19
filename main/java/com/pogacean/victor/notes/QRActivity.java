package com.pogacean.victor.notes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.crypto.spec.IvParameterSpec;

public class QRActivity extends AppCompatActivity {
    ImageView imageView;

    public void getQR(String code) {
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    code,
                    BarcodeFormat.QR_CODE,
                    500, 500, null
            );
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int[] pixels = new int[bitMatrix.getWidth() * bitMatrix.getHeight()];
        for (int y = 0; y < bitMatrix.getHeight(); y++) {
            int offset = y * bitMatrix.getWidth();

            for (int x = 0; x < bitMatrix.getWidth(); x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitmap.getWidth(), bitMatrix.getHeight());
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);

    }

    public void backupToServer(final String notes, String fullCode) {
        String code = fullCode.substring(0,10);
        final ProgressDialog dialog = ProgressDialog.show(this, "Uploading notes to server", "Please wait ...", true);
        dialog.show();
        final AlertDialog alertDialog = new AlertDialog.Builder(QRActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Please check your internet connection");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        QRActivity.this.finish();
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://apphost.ga/store.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("OK")) {
                            dialog.dismiss();

                            alertDialog.show();
                        }
                        findViewById(R.id.text).setVisibility(View.VISIBLE);
                        findViewById(R.id.text2).setVisibility(View.VISIBLE);

                        dialog.dismiss();
                        getQR(fullCode);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();
                alertDialog.show();

            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", code);
                params.put("note", notes);

                return params;
            }
        };
    queue.add(stringRequest);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.qrCode);
        String notes = getIntent().getStringExtra("N");
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder code = new StringBuilder();
        Random rand = new Random();
        for (int i=0;i<20;i++) {
            code.append(chars.charAt(rand.nextInt(36)));
        }
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf("a"))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        notes = Utils.getInstance().encrypt(iv,notes,code.substring(10));
        backupToServer(notes, code.toString());

    }
}
