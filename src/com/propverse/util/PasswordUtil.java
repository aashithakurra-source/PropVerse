package com.propverse.util;
import javax.crypto.*;import javax.crypto.spec.PBEKeySpec;import java.security.*;import java.util.*;
public final class PasswordUtil {
 private PasswordUtil(){} public static String hash(char[] password){byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return "pbkdf2-sha256$210000$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,210000));}
 public static boolean verify(char[] password,String encoded){try{String[] p=encoded.split("\\$");return p.length==4&&p[0].equals("pbkdf2-sha256")&&MessageDigest.isEqual(derive(password,Base64.getDecoder().decode(p[2]),Integer.parseInt(p[1])),Base64.getDecoder().decode(p[3]));}catch(RuntimeException e){return false;}}
 private static byte[] derive(char[] p,byte[] salt,int rounds){try{PBEKeySpec spec=new PBEKeySpec(p,salt,rounds,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}}catch(GeneralSecurityException e){throw new IllegalStateException(e);}}
}
