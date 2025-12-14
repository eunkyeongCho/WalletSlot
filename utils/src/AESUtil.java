import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES 양방향 암호화 Util
 */

public class AESUtil {


    // Field
    public static String base64Key = "Xm/4GeyvCqWw7QpXfYh5+qJxwHt6v0TfZ8GslrklJ4o=";
    public static SecretKey encryptionKey;
    private static final String ALGORITHM = "AES";

    // Static Block
    static {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        encryptionKey =  new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    // Method
    // 암호화
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 복호화
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decoded));
    }

    public static void main(String[] args) throws Exception {

        // 사용예시
        System.out.println("0020858708374729" + ": " + encrypt("0020858708374729", encryptionKey));
    }
}
