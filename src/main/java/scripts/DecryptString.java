package scripts;

import util.SquarePayload;

public class DecryptString {
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String INPUT_STRING = System.getenv("SCRIPT_STRING_INPUT");

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(INPUT_STRING);
        System.out.println(account.getAccessToken(ENCRYPTION_KEY));
    }
}
