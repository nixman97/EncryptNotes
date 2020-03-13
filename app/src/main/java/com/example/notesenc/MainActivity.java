package com.example.notesenc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity implements CustomAdapter.ItemClickListener {
    CustomAdapter adapter;
    private long mLastClickTime = 0;
    ConfigureRecycler r;
    private String pass;
    FingerprintManager fingerprintManager;
    private MenuItem fg;
    SearchView searchView;

    private void decryptAll(NoteDao n) {
        for (int i = 0; i < n.getNotes().size() - 1; i++) {
            Note cur = n.getFromPosition(i);
            Note curDec = cur.decrypt();
            cur.setTitle(curDec.getTitle());
            cur.setBody(curDec.getBody());
            n.update(cur);
        }
        Utils.getInstance().setEnc(false);
        n.delete(n.getFromPosition(-1));
        r.notesList.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    private void encryptAll(NoteDao n) {
        for (int i = n.getNotes().size() - 2; i >= 0; i--) {
            Note cur = n.getFromPosition(i);
            cur.setTitle(cur.encrypt().getTitle());
            cur.setBody(cur.encrypt().getBody());
            n.update(cur);
        }

        r.notesList.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    public void getPassword(final String information, final MenuItem item, final String action) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password");
        builder.setCancelable(false);
        builder.setMessage(information);

        // Set up the input

        final EditText input = new EditText(this);
        input.setTransformationMethod(new PasswordTransformationMethod());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                invalidateOptionsMenu();
                pass = input.getText().toString();
                NoteDao n = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db-contacts")
                        .allowMainThreadQueries()
                        .build().getNoteDao();
                if (action.equals("check")) {
                    if (n.getFromPosition(-1).getBody().equals(Utils.getInstance().hashBasedCheck(pass))) {
                        Utils.getInstance().setPasswd(pass);
                        configureLayout();

                    } else {
                        getPassword("Wrong password, please try again", item, action);
                        builder.setTitle("Wrong pass");
                    }
                }

                if (action.equals("enc")) {
                    Utils.getInstance().setEnc(true);
                    Utils.getInstance().setPasswd(pass);
                    n.insert(new Note("enc", Utils.getInstance().hashBasedCheck(pass), -1));
                    item.setIcon(R.drawable.ic_no_encryption_black_24dp);
                    item.setTitle("Disable encryption");

                    try {


                        encryptAll(n);
                        Toast.makeText(getApplicationContext(), "Encryption enabled", Toast.LENGTH_SHORT).show();


                    } catch (Exception ignore) {

                    }

                }
            }


        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (action.equals("check"))
                    finish();
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, "db-contacts")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        NoteDao n = database.getNoteDao();
        if (requestCode == 0) {
            r.getNotesList().getRecycledViewPool().clear();
            if (data.getIntExtra("pos", -2) == -2)
                return;
            if (data.getIntExtra("pos", -2) == -1) {
                r.getNotesList().smoothScrollToPosition(0);
                adapter.notifyItemInserted(0);

                n.search("%" + searchView.getQuery().toString() + "%");
            } else {
                adapter.notifyItemChanged(data.getIntExtra("pos", -2));
            }
        }
        if (requestCode == 1) {
            if (data.getIntExtra("code", -2) == 1) {
                r.getNotesList().setVisibility(View.VISIBLE);
                findViewById(R.id.addBtn).setVisibility(View.VISIBLE);
                findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
                findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
                findViewById(R.id.usePin).setVisibility(View.INVISIBLE);
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = this.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                invalidateOptionsMenu();

            }
            if (data.getIntExtra("code", -2) == 0) {
                SharedPreferences prefs = this.getSharedPreferences(
                        "com.example.victor.notes", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("finger", true).apply();
                fg.setTitle("Disable encryption");
                Toast.makeText(getApplicationContext(), "Fingerprint enabled", Toast.LENGTH_SHORT).show();
            }
            if (data.getIntExtra("code", -2) == -1) {

                Toast.makeText(getApplicationContext(), "Fingerprint not enabled", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 2) {
            if (resultCode == 2)
                return;
            if (resultCode == 1) {
                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Wrong QR code. Please try again");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }

            String notes = data.getStringExtra("notes");
            String pwd = data.getStringExtra("pwd");
            IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf("a"))
                    .substring(48)
                    .getBytes(StandardCharsets.UTF_8));
            notes = Utils.getInstance().decrypt(iv, notes, pwd);
            try {
                JSONArray notesArr = new JSONArray(notes);
                for (int i = 0; i < notesArr.length(); i++) {
                    JSONObject jsonNote = notesArr.getJSONObject(i);
                    n.increasePositions(-1);
                    n.insert(new Note(jsonNote.getString("title"), jsonNote.getString("body"), 0));
                    r.notesList.getRecycledViewPool().clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Successfully imported", Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, "db-contacts")
                .allowMainThreadQueries()
                .build();
        NoteDao n = database.getNoteDao();
        System.out.println(item.getItemId());
        if (item.getItemId() == R.id.action_send) {
            List<Note> notesList = new ArrayList<>();
            if (Utils.getInstance().isEnc()) {
                for (int i = 0; i < n.getNotes().size() - 1; i++) {
                    notesList.add(n.getFromPosition(i).decrypt());
                }
            } else {
                notesList = n.getNotes();
            }
            String notes = new Gson().toJson(notesList);
            Intent intent = new Intent(MainActivity.this, QRActivity.class);
            intent.putExtra("N", notes);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_receive) {
            Intent intent = new Intent(MainActivity.this, ReceiveActivity.class);
            startActivityForResult(intent, 2);

        }
        if (item.getItemId() == R.id.action_toggle_encryption) {
            if (!Utils.getInstance().isEnc()) {
                getPassword("Please set the new encryption password", item, "enc");
            }
            if (Utils.getInstance().isEnc()) {
                item.setIcon(R.drawable.ic_enhanced_encryption_black_24dp);
                item.setTitle("Enable encryption");

                decryptAll(n);
                Toast.makeText(this, "Encryption disabled", Toast.LENGTH_SHORT).show();


            }
        }
        if (item.getItemId() == R.id.action_toggle_fingerprint) {
            if (!checkFinger()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Your device does not have a fingerprint sensor.")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            } else {
                if (item.getTitle().toString().equals("Enable fingerprint")) {

                    fg = item;
                    Intent add = new Intent(MainActivity.this, PinActivity.class);
                    add.putExtra("check", 0); //open for add
                    startActivityForResult(add, 1);


                } else {
                    SharedPreferences prefs = this.getSharedPreferences(
                            "com.example.victor.notes", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("finger", false).apply();
                    Toast.makeText(getApplicationContext(), "Fingerprint disabled", Toast.LENGTH_SHORT).show();
                    item.setTitle("Enable fingerprint");

                }
            }

        }
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu


            (Menu menu) {
        MenuItem item = menu.findItem(R.id.action_toggle_encryption);
        if (Utils.getInstance().isEnc()) {
            item.setTitle("Disable encryption");
            item.setIcon(R.drawable.ic_no_encryption_black_24dp);
        }

        item = menu.findItem(R.id.action_toggle_fingerprint);
        SharedPreferences prefs = this.getSharedPreferences(
                "com.example.victor.notes", Context.MODE_PRIVATE);
        if (prefs.getBoolean("finger", false))
            item.setTitle("Disable fingerprint");
        if (findViewById(R.id.notesList).getVisibility() == View.INVISIBLE) {
            menu.findItem(R.id.action_toggle_encryption).setVisible(false);
            menu.findItem(R.id.action_toggle_fingerprint).setVisible(false);
        } else {
            menu.findItem(R.id.action_toggle_encryption).setVisible(true);
            menu.findItem(R.id.action_toggle_fingerprint).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.m_menu, menu);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                try {
                    adapter.getFilter().filter(query);
                } catch (Exception ignore) {
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkFinger() {
        // Keyguard Manager
        KeyguardManager keyguardManager = (KeyguardManager)
                getSystemService(KEYGUARD_SERVICE);
        // Fingerprint Manager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager)
                    getSystemService(FINGERPRINT_SERVICE);

            try {
                // Check if the fingerprint sensor is present
                if (!fingerprintManager.isHardwareDetected()) {
                    // Update the UI with a message
                    return false;
                }
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    return false;
                }
                if (!keyguardManager.isKeyguardSecure()) {
                    return false;
                }
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        } else {
            return false;
        }
        return true;
    }

    //confiugres activity layout
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configureLayout() {
        //sets the use pin button when fingerprint is enabled
        Button usePin = findViewById(R.id.usePin);
        usePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add = new Intent(MainActivity.this, PinActivity.class);
                add.putExtra("check", 2); //open for add
                startActivityForResult(add, 1);
            }
        });
        //
        r = new ConfigureRecycler(this);
        adapter = r.getAdapter();
        FloatingActionButton addBtn = findViewById(R.id.addBtn);
        //prevents doubletouch
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent add = new Intent(MainActivity.this, AddNote.class);
                add.putExtra("position", "-1"); //open for add
                startActivityForResult(add, 0);

            }
        });
        SharedPreferences prefs = this.getSharedPreferences(
                "com.example.victor.notes", Context.MODE_PRIVATE);
        if (prefs.getBoolean("finger", false)) {
            final FingerprintHandler fph = new FingerprintHandler(this, (TextView) findViewById(R.id.textView3), (ImageView) findViewById(R.id.imageView), r.notesList);
            // We are ready to set up the cipher and the key
            try {
                generateKey();
                Cipher cipher = generateCipher();
                FingerprintManager.CryptoObject cryptoObject =
                        new FingerprintManager.CryptoObject(Objects.requireNonNull(cipher));
                fph.doAuth((FingerprintManager)
                        getSystemService(FINGERPRINT_SERVICE), cryptoObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, "db-contacts")
                .allowMainThreadQueries()
                .build();


        NoteDao n = database.getNoteDao();
        String notes = new Gson().toJson(n.getNotes());

        n.showAll();
        try {
            if (n.getFromPosition(-1).getTitle().equals("enc")) {
                Utils.getInstance().setEnc(true);
                getPassword(getString(R.string.pass_solicitation), null, "check");
            }
        } catch (Exception ignore) {
            Utils.getInstance().setEnc(false);
            configureLayout();

        }

        //   startActivity(intent);

    }


    @Override
    public void onItemClick(int position) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        Intent add = new Intent(MainActivity.this, AddNote.class);
        add.putExtra("position", String.valueOf(position)); //open for add

        startActivityForResult(add, 0);
        // Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();

    }

    KeyStore keyStore;
    KeyGenerator keyGenerator;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() {
        try {
            // Get the reference to the key store
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            // Key generator to generate the key
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder("a",
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private Cipher generateCipher() {
        try {
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            SecretKey key = (SecretKey) keyStore.getKey("a",
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }

    }

}

