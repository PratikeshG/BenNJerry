//package util;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.security.GeneralSecurityException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import org.jasypt.util.binary.BasicBinaryEncryptor;
//import org.mule.util.IOUtils;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.InputStreamContent;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.storage.Storage;
//import com.google.api.services.storage.model.Bucket;
//import com.google.api.services.storage.model.Objects;
//import com.google.api.services.storage.model.StorageObject;
//
//public class CloudStorageApi {
//    private static final String APPLICATION_NAME = "Square-Bridge/1.0";
//
//    // Global configuration of Google Cloud Storage OAuth 2.0 scope
//    // https://cloud.google.com/storage/docs/authentication#oauth-scopes
//    private static final String STORAGE_WRITE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";
//    private static final String ENCODING_TEXT_PLAIN = "text/plain";
//    private static final String ENCODING_UTF8 = "UTF-8";
//    private static final String ENCODING_APPLICATION_OCTET = "application/octet-stream";
//
//    private Storage storage;
//
//    public CloudStorageApi(String credentialsJson) throws GeneralSecurityException, IOException {
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//        InputStream jsonStream = new ByteArrayInputStream(credentialsJson.getBytes(ENCODING_UTF8));
//        GoogleCredential credential = GoogleCredential.fromStream(jsonStream)
//                .createScoped(Collections.singleton(STORAGE_WRITE_SCOPE));
//        storage = new Storage.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    public Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
//        Storage.Buckets.Get bucketRequest = storage.buckets().get(bucketName);
//        bucketRequest.setProjection("full");
//        return bucketRequest.execute();
//    }
//
//    public List<StorageObject> listObjects(String bucketName) throws IOException {
//        return listObjects(bucketName, "");
//    }
//
//    public List<StorageObject> listObjects(String bucketName, String prefix) throws IOException {
//        Storage.Objects.List listRequest = storage.objects().list(bucketName).setPrefix(prefix);
//
//        List<StorageObject> results = new ArrayList<StorageObject>();
//
//        Objects objects = null;
//        do {
//            objects = listRequest.execute();
//            if (objects != null && objects.getItems() != null) {
//                results.addAll(objects.getItems());
//                listRequest.setPageToken(objects.getNextPageToken());
//            }
//        } while (null != objects.getNextPageToken());
//
//        return results;
//    }
//
//    public StorageObject uploadObject(String bucketName, String objectName, InputStream data)
//            throws UnsupportedEncodingException, IOException {
//        return uploadObject(bucketName, objectName, data, ENCODING_TEXT_PLAIN);
//    }
//
//    public StorageObject uploadObject(String bucketName, String objectName, String data)
//            throws UnsupportedEncodingException, IOException {
//        return uploadObject(bucketName, objectName, new ByteArrayInputStream(data.getBytes(ENCODING_UTF8)),
//                ENCODING_TEXT_PLAIN);
//    }
//
//    public StorageObject uploadObject(String bucketName, String objectName, File data)
//            throws FileNotFoundException, IOException {
//        return uploadObject(bucketName, objectName, new FileInputStream(data), ENCODING_APPLICATION_OCTET);
//    }
//
//    public StorageObject encryptAndUploadObject(String encryptionKey, String bucketName, String objectName, String data)
//            throws UnsupportedEncodingException, IOException {
//        byte[] encryptedData = encryptBytes(encryptionKey, new ByteArrayInputStream(data.getBytes(ENCODING_UTF8)));
//        return uploadObject(bucketName, objectName, new ByteArrayInputStream(encryptedData), ENCODING_TEXT_PLAIN);
//    }
//
//    public StorageObject encryptAndUploadObject(String encryptionKey, String bucketName, String objectName,
//            InputStream data) throws UnsupportedEncodingException, IOException {
//        byte[] encryptedData = encryptBytes(encryptionKey, data);
//        return uploadObject(bucketName, objectName, new ByteArrayInputStream(encryptedData), ENCODING_TEXT_PLAIN);
//    }
//
//    public StorageObject encryptAndUploadObject(String encryptionKey, String bucketName, String objectName, File data)
//            throws FileNotFoundException, IOException {
//        byte[] encryptedData = encryptBytes(encryptionKey, new FileInputStream(data));
//        return uploadObject(bucketName, objectName, new ByteArrayInputStream(encryptedData),
//                ENCODING_APPLICATION_OCTET);
//    }
//
//    public StorageObject uploadObject(String bucketName, String objectName, InputStream data, String contentType)
//            throws IOException {
//        InputStreamContent mediaContent = new InputStreamContent(contentType, data);
//        Storage.Objects.Insert insertObject = storage.objects().insert(bucketName, null, mediaContent)
//                .setName(objectName);
//        // The media uploader gzips content by default, and alters the Content-Encoding accordingly.
//        // GCS dutifully stores content as-uploaded. This line disables the media uploader behavior,
//        // so the service stores exactly what is in the InputStream, without transformation.
//        insertObject.getMediaHttpUploader().setDisableGZipContent(true);
//        return insertObject.execute();
//    }
//
//    public StorageObject uploadObjectWithMetadata(StorageObject object, InputStream data) throws IOException {
//        InputStreamContent mediaContent = new InputStreamContent(object.getContentType(), data);
//        Storage.Objects.Insert insertObject = storage.objects().insert(object.getBucket(), object, mediaContent);
//        insertObject.getMediaHttpUploader().setDisableGZipContent(true);
//        return insertObject.execute();
//    }
//
//    public StorageObject renameObject(String bucketName, String objectName, String destBucket, String deskObjectName)
//            throws IOException {
//        Storage.Objects.Copy copyObject = storage.objects().copy(bucketName, objectName, destBucket, deskObjectName,
//                null);
//        StorageObject copiedObj = copyObject.execute();
//        Storage.Objects.Delete deleteObject = storage.objects().delete(bucketName, objectName);
//        deleteObject.execute();
//        return copiedObj;
//    }
//
//    public InputStream downloadObject(String bucketName, String objectName) throws IOException {
//        Storage.Objects.Get getObject = storage.objects().get(bucketName, objectName);
//        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
//        return getObject.executeMediaAsInputStream();
//    }
//
//    public InputStream downloadAndDecryptObject(String encryptionKey, String bucketName, String objectName)
//            throws IOException {
//        Storage.Objects.Get getObject = storage.objects().get(bucketName, objectName);
//        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
//        return new ByteArrayInputStream(decryptBytes(encryptionKey, getObject.executeMediaAsInputStream()));
//    }
//
//    private byte[] inputStreamToByteArray(InputStream inputStream) {
//        return IOUtils.toByteArray(inputStream);
//    }
//
//    private byte[] encryptBytes(String encryptionKey, InputStream data) throws IOException {
//        BasicBinaryEncryptor byteEncryptor = new BasicBinaryEncryptor();
//        byteEncryptor.setPassword(encryptionKey);
//        return byteEncryptor.encrypt(inputStreamToByteArray(data));
//    }
//
//    private byte[] decryptBytes(String encryptionKey, InputStream data) throws IOException {
//        BasicBinaryEncryptor byteEncryptor = new BasicBinaryEncryptor();
//        byteEncryptor.setPassword(encryptionKey);
//        return byteEncryptor.decrypt(inputStreamToByteArray(data));
//    }
//
//}
