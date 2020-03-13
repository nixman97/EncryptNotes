package com.example.notesenc;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.IvParameterSpec;

@Entity(tableName = "note")
public class Note {
    @NonNull
    private String title;
    private String body;
    private int position;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    private boolean hidden = false;

    //Encrypts note with AES
    public Note encrypt() {
        if (Utils.getInstance().isEnc()) {
            try {
                String passwd = Utils.getInstance().getPasswd();

                IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(key))
                        .substring(48)
                        .getBytes(StandardCharsets.UTF_8));

                String encTitle = Utils.getInstance().encrypt(iv,title,passwd);
                String encBody = Utils.getInstance().encrypt(iv,body,passwd);
                return new Note(encTitle, encBody, position);
            } catch (Exception ignore) {
                return new Note("", "", 0);
            }
        }
        return this;
    }

    //decrypts note with AES
    Note decrypt() {
        String passwd = Utils.getInstance().getPasswd();

        if (!Utils.getInstance().isEnc())
            return this;
        try {
            IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(this.key))
                    .substring(48)
                    .getBytes(StandardCharsets.UTF_8));

           String decTitle = null,decBody = null;
            if (!title.equals(""))
                decTitle = Utils.getInstance().decrypt(iv,title,passwd);
            if (!body.equals(""))
                decBody = Utils.getInstance().decrypt(iv,body,passwd);
            return new Note(decTitle, decBody, position);
        } catch (Exception e) {
            e.printStackTrace();
            return new Note("", "", 0);
        }
    }

    @Override
    public String toString() {
        return position + title + body;
    }

    int getKey() {
        return key;
    }

    void setKey(int key) {
        this.key = key;
    }

    @PrimaryKey(autoGenerate = true)
    private int key;

    Note(@NonNull String title, String body, int position) {
        this.title = title;
        this.body = body;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @NonNull
    public String getTitle() {
        return this.title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    String getBody() {
        return this.body;
    }

    void setBody(String body) {
        this.body = body;
    }
}
