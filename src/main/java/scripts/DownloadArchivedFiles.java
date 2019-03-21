package scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.api.services.storage.model.StorageObject;

import util.CloudStorageApi;

public class DownloadArchivedFiles {
    private final static String[] LOCATIONS = { "00313" }; // ex: { "00313", "00512"}
    private final static String SEARCH_FILE_PREFIX = "20181124"; // ex: 20181124
    private final static String SEARCH_FILE_PATH = "TNF/%s/TLOG/%s"; // ex: TNF/%s/TLOG/%s
    private final static String DESTINATION_PATH = System.getenv("SCRIPT_FILE_DESTINATION_PATH"); // MODIFY

    // Do not edit below this line
    private final static String ARCHIVE_BUCKET = "managed-integrations-prod-archive";
    private final static String FILE_ENCRYPTION_KEY = System.getenv("SCRIPT_FILE_ENCRYPTION_KEY");
    private final static String GOOGLE_CREDENTIALS = "google.json"; // Located in src/main/resources
    private final static String SECURE_FILE_EXTENSION = ".secure";

    private static Logger logger = LoggerFactory.getLogger(DownloadArchivedFiles.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running...");

        JSONParser parser = new JSONParser();
        Resource resource = new ClassPathResource(GOOGLE_CREDENTIALS);
        Object credentials = parser.parse(new FileReader(resource.getFile()));
        CloudStorageApi cloudStorage = new CloudStorageApi(credentials.toString());

        for (String locationId : LOCATIONS) {
            for (StorageObject o : cloudStorage.listObjects(ARCHIVE_BUCKET,
                    String.format(SEARCH_FILE_PATH, locationId, SEARCH_FILE_PREFIX))) {

                logger.info("Downloading and decrypting file: " + o.getName());
                InputStream is = cloudStorage.downloadAndDecryptObject(FILE_ENCRYPTION_KEY, ARCHIVE_BUCKET,
                        o.getName());

                String[] tmp = o.getName().split("/");
                String fileName = tmp[tmp.length - 1];

                if (fileName.endsWith(SECURE_FILE_EXTENSION)) {
                    fileName = fileName.substring(0, fileName.length() - SECURE_FILE_EXTENSION.length());
                }

                try {
                    OutputStream os = new FileOutputStream(new File(DESTINATION_PATH + fileName));

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    //read from is to buffer
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    //flush OutputStream to write any buffered data to file
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        logger.info("done");
    }
}
