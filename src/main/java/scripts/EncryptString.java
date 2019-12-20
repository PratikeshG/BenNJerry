package scripts;

import org.jasypt.util.text.BasicTextEncryptor;

public class EncryptString {
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String INPUT_STRING = System.getenv("SCRIPT_INPUT_STRING");

    public static void main(String[] args) throws Exception {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(ENCRYPTION_KEY);
        System.out.println(textEncryptor.encrypt(INPUT_STRING));
    }
}