package scripts;

import util.SquarePayload;

public class DecryptString {
	private final static String ENCRYPTION_KEY = "aq46KENmD=CsdCjVLc9q@RXJYniZBfNXRFavWLrjXEkHEwhACLmZQK%H9&REKJLk"; // prod
    private final static String INPUT_STRING = "9XzLUw/zJI04SSzB0Jlwou9WSGdx2CXtYiQEEtHj6EI5DEGn+J1/1tkaiN8eMBAzKfcXoBtz58pgsopRxcv6likFaySLZuQPgnCOsh/XZ4g=";

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(INPUT_STRING);
        System.out.println(account.getAccessToken(ENCRYPTION_KEY));
    }
}
