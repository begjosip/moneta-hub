package com.moneta.hub.moneta.util;

import jakarta.validation.constraints.NotBlank;
import lombok.experimental.UtilityClass;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@UtilityClass
public class SecurityUtil {

    private static final String KEY_ALGORITHM = "AES";

    private static final String ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding";

    private static final String SECRET_KEY = "TBPIBQSTDWMFJIS8SHDDNA2XJOJJMOG4";

    public static String encryptUsername(@NotBlank String username)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey());
        byte[] encryptedBytes = cipher.doFinal(username.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static SecretKey generateKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), KEY_ALGORITHM);
    }

}
