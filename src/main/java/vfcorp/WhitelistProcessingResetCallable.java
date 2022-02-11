package vfcorp;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.services.storage.model.StorageObject;

import util.CloudStorageApi;

public class WhitelistProcessingResetCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(WhitelistProcessingResetCallable.class);

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    private static final String PROCESSING_PREFIX = "processing_";
    private static final String WHITELIST_DIRECTORY_FORMAT = "whitelist/%s/";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.INVOCATION);
        String directoryKey = String.format(WHITELIST_DIRECTORY_FORMAT, brand);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

        List<StorageObject> processingFiles = cloudStorage.listObjects(archiveBucket, directoryKey + PROCESSING_PREFIX);
        if (processingFiles.size() > 0) {
            for (StorageObject processingFile : processingFiles) {
                String processingFilename = processingFile.getName();
                logger.info(String.format("Found and re-queuing stale whitelist %s...", processingFilename));

                String resetFilename = processingFilename.split(PROCESSING_PREFIX)[1];

                cloudStorage.renameObject(archiveBucket, processingFilename, archiveBucket,
                        directoryKey + resetFilename);
            }
        }

        return 1;
    }
}
