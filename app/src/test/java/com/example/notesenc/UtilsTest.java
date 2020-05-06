package com.example.notesenc;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.IvParameterSpec;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

//Testul de integrare este urmatorul: P=>A=>B=>C
@RunWith(RobolectricTestRunner.class)
//P
public class UtilsTest {
    //P
    @Test
    public void integrationTest() {
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(2))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        String password = "HEYSecurity1234";
        String tobeEncrypted = "Noi sunem tari";
        assertTrue(Utils.getInstance().checkPasswordSecurity(password));
        String encrypted = Utils.getInstance().encrypt(iv, tobeEncrypted, password);
        String decryped = Utils.getInstance().decrypt(iv, encrypted, password);
        assertEquals(tobeEncrypted, decryped);


    }

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
        /////assertTrue(Utils.getInstance().checkPasswordSecurity("121HQWheoppiiuhgv"));
    }

    //A
    @Test
    public void checkPassword() {
        assertTrue(Utils.getInstance().checkPasswordSecurity("HEYSecurity1234"));
        assertFalse(Utils.getInstance().checkPasswordSecurity("HEYSecurity1"));

    }

    //B
    @Test
    public void encryptTest() {
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(2))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        String encrypted = Utils.getInstance().encrypt(iv, "Echipa", "NoiSuntemEchipaAA1234");
        assertEquals("U2LBnwYEVs6b1q+5uCKoCg==\n", encrypted);
    }

    //C
    @Test
    public void decryptTest() {
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(2))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        String decrypted = Utils.getInstance().decrypt(iv, "U2LBnwYEVs6b1q+5uCKoCg==\n", "NoiSuntemEchipaAA1234");
        assertEquals("Echipa", decrypted);
    }

    @Test
    public void topDownTestingPAB() {
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(2))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        String password = "123ThisIsAGoodPassword56";
        String toBeEncrypted = "We are the best team";
        assertTrue(Utils.getInstance().checkPasswordSecurity(password));


    }

    public void topDownTestingPA() {
        String password = "HEYSecurity1234";
        assertTrue(Utils.getInstance().checkPasswordSecurity(password));
    }
    public void topDownTestingPABC() {
        String password = "HEYSecurity1234";
        assertTrue(Utils.getInstance().checkPasswordSecurity(password));
        IvParameterSpec iv = new IvParameterSpec(Utils.getInstance().hashBasedCheck(String.valueOf(2))
                .substring(48)
                .getBytes(StandardCharsets.UTF_8));
        String tobeEncrypted = "Noi sunem tari";
        assertTrue(Utils.getInstance().checkPasswordSecurity(password));
        String encrypted = Utils.getInstance().encrypt(iv, tobeEncrypted, password);
        String decryped = Utils.getInstance().decrypt(iv, encrypted, password);
        assertEquals(tobeEncrypted, decryped);
    }
}