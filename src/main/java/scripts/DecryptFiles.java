package scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecryptFiles {
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_FILE_ENCRYPTION_KEY");
    private final static String PATH = System.getenv("SCRIPT_FILE_PATH");

    private final static String FILE_PATTERN = ".secure";
    private final static String LOGGER_RUNNING = "Running decryption script for file pattern '%s' on path: %s";
    private final static String LOGGER_DECRYPTING_START = "Decrypting %s";
    private final static String LOGGER_DECRYPTING_DONE = "Done: %s";
    private final static String LOGGER_DONE = "Script complete.";

    private static Logger logger = LoggerFactory.getLogger(DecryptFiles.class);

    public static void main(String[] args) throws Exception {
        logger.info(String.format(LOGGER_RUNNING, FILE_PATTERN, PATH));

        File dir = new File(PATH);
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_PATTERN));

        for (File f : files) {
            String encryptedName = f.getName();
            String decryptedName = encryptedName.split(FILE_PATTERN, 2)[0];

            logger.info(String.format(LOGGER_DECRYPTING_START, encryptedName));

            InputStream is = new FileInputStream(f);
            byte[] buffer = decryptBytes(ENCRYPTION_KEY, is);

            File targetFile = new File(PATH + "/" + decryptedName);
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);

            logger.info(String.format(LOGGER_DECRYPTING_DONE, decryptedName));
        }

        logger.info(LOGGER_DONE);
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }
        baos.flush();
        return baos.toByteArray();
    }

    private static byte[] decryptBytes(String encryptionKey, InputStream data) throws IOException {
        BasicBinaryEncryptor byteEncryptor = new BasicBinaryEncryptor();
        byteEncryptor.setPassword(encryptionKey);
        return byteEncryptor.decrypt(inputStreamToByteArray(data));
    }
}
