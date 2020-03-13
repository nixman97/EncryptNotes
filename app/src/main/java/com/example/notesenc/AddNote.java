package com.example.notesenc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.Objects;

public class AddNote extends AppCompatActivity {
    private EditText titleEdit;
    private EditText bodyEdit;
    private NoteDao n;
    int position;
    boolean reason;
    private Note backupNote;
    private String m_Text = "";

    private void configureLayout() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        position = Integer.parseInt(getIntent().getStringExtra("position"));
        titleEdit = findViewById(R.id.editTitle);
        bodyEdit = findViewById(R.id.editBody);
        n = Room.databaseBuilder(this, AppDatabase.class, "db-contacts")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build().getNoteDao();
        if (position == -1) {

            n.increasePositions(-1);
            n.insert(new Note("", "", 0));
            position = 0;
            reason = false;

            Objects.requireNonNull(getSupportActionBar()).setTitle("Add Note");

        } else {
            titleEdit.setText(n.getFromPosition(position).decrypt().getTitle());
            bodyEdit.setText(n.getFromPosition(position).decrypt().getBody());
            Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Note");
            reason = true;

        }
        final Note draftNote = n.getFromPosition(position);
        backupNote = n.getFromPosition(position);
        titleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                draftNote.setTitle(titleEdit.getText().toString());
                draftNote.setTitle(draftNote.encrypt().getTitle());
                n.update(draftNote);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        bodyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                draftNote.setBody(bodyEdit.getText().toString());
                draftNote.setBody(draftNote.encrypt().getBody());
                n.update(draftNote);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_delete);
        if (!reason)
            item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, titleEdit.getText().toString() + "\n" + bodyEdit.getText().toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        if (item.getItemId() == R.id.action_discard) {
            if (!reason) {
                titleEdit.setText("");
                bodyEdit.setText("");
                onBackPressed();
            } else {

                titleEdit.setText(backupNote.decrypt().getTitle());
                bodyEdit.setText(backupNote.decrypt().getBody());
                onBackPressed();
            }
        }
        if (item.getItemId() == R.id.action_delete) {
            n.delete(n.getFromPosition(position));
            n.decreasePositions(position);
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (!reason && TextUtils.isEmpty(titleEdit.getText()) && TextUtils.isEmpty(bodyEdit.getText().toString())) {
            n.delete(n.getFromPosition(position));
            n.decreasePositions(position);
            Intent intent = new Intent();
            intent.putExtra("pos", -2);
            setResult(0, intent);
            finish();
        } else if (TextUtils.isEmpty(titleEdit.getText()) && TextUtils.isEmpty(bodyEdit.getText().toString())) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            n.delete(n.getFromPosition(position));
                            n.decreasePositions(position);
                            Intent intent = new Intent();
                            setResult(0, intent);
                            intent.putExtra("pos", position);
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to remove this note?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
            Intent intent = new Intent();
            setResult(0, intent);
            if (!reason)
                intent.putExtra("pos", -1);
            else
                intent.putExtra("pos", position);


            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        configureLayout();


    }
}
