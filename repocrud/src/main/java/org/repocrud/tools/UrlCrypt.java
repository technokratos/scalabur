package org.repocrud.tools;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.security.crypto.codec.Hex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import static org.apache.commons.lang3.math.NumberUtils.min;

/**
 * @author Denis B. Kulikov<br/>
 * date: 07.11.2018:10:39<br/>
 */
@Slf4j
public class UrlCrypt {

    private static Charset UTF8 = Charset.forName("UTF-8");
    private static Cipher CIPHER;
    private static byte[] ivBytes = "strqazed".getBytes();
    private static IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    static {
        Cipher instance = null;
        try {
            instance = Cipher.getInstance("DES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException|NoSuchPaddingException  e) {
            log.error("Error in init encryption ", e);
        }
        CIPHER = instance;

    }

    public static String encrypt(String input, String key) {
        byte[] encrypted = encrypt(input.getBytes(UTF8), trimKey(key.getBytes(UTF8)));
        //DatatypeConverter.printBase64Binary(encrypted)
        return HexUtils.toHexString(encrypted);
    }
    public static String decrypt(String input64, String key) {
//        byte[] bytes = DatatypeConverter.parseBase64Binary(input64);
        byte[] bytes = HexUtils.fromHexString(input64);
        byte[] decrypt = decrypt(bytes, trimKey(key.getBytes(UTF8)));
        return new String(Objects.requireNonNull(decrypt)).trim();
    }
    public static byte[] encrypt(byte[] input, byte[] keyBytes) {
        try {

            // wrap key data in Key/IV specs to pass to cipher
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            // create the cipher with the algorithm you choose
            // see javadoc for Cipher class for more info, e.g.

            //Encryption would go like this:
            CIPHER.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = new byte[CIPHER.getOutputSize(input.length)];
            int enc_len = CIPHER.update(input, 0, input.length, encrypted, 0);
            enc_len += CIPHER.doFinal(encrypted, enc_len);
            //And decryption like this:
            return encrypted;
        } catch (Exception e) {
            log.error("Impossible encrypt " + Hex.encode(input) + ", key " + new String(keyBytes), e);
            return null;
        }
    }

    private static byte[] trimKey(byte[] keyBytes) {
        byte[] bytes = new byte[8];
        System.arraycopy(keyBytes, 0, bytes, 0, min(bytes.length, keyBytes.length) );
        return bytes;
    }

    public static byte[] decrypt(byte[] encrypted, byte[] keyBytes){
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            CIPHER.init(Cipher.DECRYPT_MODE, key, ivSpec);
            int encLength = encrypted.length;
            byte[] decrypted = new byte[CIPHER.getOutputSize(encLength)];
            int dec_len = CIPHER.update(encrypted, 0, encLength, decrypted, 0);
            dec_len += CIPHER.doFinal(decrypted, dec_len);
            return decrypted;
        } catch (Exception e) {
            log.error("Impossible decrypt " + Hex.encode(encrypted) + ", key " + new String(keyBytes), e);
            return null;
        }
    }
}
