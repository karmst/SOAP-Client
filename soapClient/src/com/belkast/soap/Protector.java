package com.belkast.soap;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Protector
    {
        private static final String ALGORITHM = "AES";
        
        public static char[] encrypt(byte[] varKey, String valueToEnc) throws Exception
            {
                System.out.println("## Encrypting password ##");
                Key key = generateKey(varKey);
                Cipher c = Cipher.getInstance("AES");
                c.init(1, key);
                byte[] encValue = c.doFinal(valueToEnc.getBytes());
                char[] encryptedValue = Base64.encode(encValue);
                return encryptedValue;
            }
 
        public static String decrypt(byte[] varKey, String encryptedValue) throws Exception
            {
                System.out.println("## Decrypting password ##");
                Key key = generateKey(varKey);
                Cipher c = Cipher.getInstance("AES");
                c.init(2, key);
                byte[] decordedValue = Base64.decode(encryptedValue);
                byte[] decValue = c.doFinal(decordedValue);
                String decryptedValue = new String(decValue);
                return decryptedValue;
            }

        private static Key generateKey(byte[] varKey) throws Exception
            {
                Key key = new SecretKeySpec(varKey, "AES");
                return key;
            }
    }