package com.example.notesenc;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.IvParameterSpec;

import static org.junit.Assert.*;
public class UtilsTest {
    @Before
    public void loadTests() {

    }
    @Test
    public void encTestNotFail() {
        assertTrue(Utils.getInstance().checkPasswordSecurity("1234AAHQ<Echipaechi>v"));

    }
    @Test
    public void encTestFail() {
        //ECP
        assertTrue(Utils.getInstance().checkPasswordSecurity("121HQWheoppiiuhgv"));


    }
}