package org.ldscd.callingworkflow.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handles all security methods.  Encryption, decryption etc.
 */
public class SecurityUtil {

    /* Fields */
    private static final String TAG = "SecurityUtil.Class";
    private static final String UTF8 = "UTF-8";
    private static final String ALGORITHM = "AES";
    private static final String DIGEST = "SHA-1";
    private static final String ENCRYPT_TYPE = "AES/CBC/PKCS5Padding";

    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static byte[] iv;

    /* Methods */
    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(UTF8);
            sha = MessageDigest.getInstance(DIGEST);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(Context context, String value) {
        try {
            /* Unique Id per device.  This has been proven to not be 100% safe but a good solution. */
            setKey(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return Base64.encodeToString(cipher.doFinal(value.getBytes(UTF8)), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Could not encrypt the value");
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(Context context, String value) {
        try {
            setKey(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return new String(cipher.doFinal(Base64.decode(value, Base64.DEFAULT)));
        } catch (Exception e) {
            Log.e(TAG, "Could not decrypt the value: " + e.getMessage());
            return null;
        }
    }
}