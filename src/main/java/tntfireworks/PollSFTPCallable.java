package tntfireworks;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import util.TimeManager;

public class PollSFTPCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PollSFTPCallable.class);

    @Value("${tntfireworks.sftp.host}")
    private String sftpHost;
    @Value("${tntfireworks.sftp.port}")
    private int sftpPort;
    @Value("${tntfireworks.sftp.username}")
    private String sftpUser;
    @Value("${tntfireworks.sftp.password}")
    private String sftpPassword;
    @Value("${tntfireworks.sftp.basepath}")
    private String sftpBasePath;
    @Value("${tntfireworks.sftp.archivepath}")
    private String sftpArchivePath;
    @Value("${tntfireworks.sftp.inputpath}")
    private String sftpInputPath;
    @Value("${tntfireworks.sftp.processingpath}")
    private String sftpProcessingPath;
    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {

        Exception lastException = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return getTntSyncToDatabaseRequestsFromSftp();
            } catch (Exception e) {
                lastException = e;
                lastException.printStackTrace();
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw lastException;
    }

    private List<SyncToDatabaseRequest> getTntSyncToDatabaseRequestsFromSftp()
            throws JSchException, IOException, InterruptedException, SftpException, ParseException {
        ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);
        List<SyncToDatabaseRequest> newRequests = getSFTPRequests(sftpChannel);
        SshUtil.closeConnection(sftpChannel);
        return newRequests;
    }

    /*
     * Processes files in the input directory
     *
     * Preconditions: it is expected that the directory structure contain the following folders:
     *
     * \archive
     * \input
     * \input\processing
     *
     */
    private List<SyncToDatabaseRequest> getSFTPRequests(ChannelSftp channel)
            throws InterruptedException, SftpException, ParseException {
        // sftp paths to input/processing directories
        String inputFullPath = sftpBasePath + sftpInputPath;
        String processingFullPath = sftpBasePath + sftpInputPath + sftpProcessingPath;
        String archiveFullPath = sftpBasePath + sftpArchivePath;

        // filesToProcess => total files list in the input folder marketing plans and/or locations
        // inputFilesByPrefix => unique prefixes of files located in input folder
        // sftpRequests => final requests sent for processing
        HashMap<String, LsEntry> filesToProcess = new HashMap<String, LsEntry>();
        HashMap<String, ArrayList<LsEntry>> inputFilesByPrefix;
        ArrayList<SyncToDatabaseRequest> sftpRequests = new ArrayList<SyncToDatabaseRequest>();

        // get all CSVs located in input folder
        Vector<LsEntry> inputFiles = channel.ls(String.format("%s/*.csv", inputFullPath));

        // sort all files into lists according to unique prefix
        inputFilesByPrefix = sortFilesByPrefix(inputFiles);

        // obtain each list of files with the same prefix and find oldest
        for (String filePrefix : inputFilesByPrefix.keySet()) {
            LsEntry targetFile = getOldestFile(inputFilesByPrefix.get(filePrefix));
            if (targetFile != null) {
                filesToProcess.put(filePrefix, targetFile);
            }
        }

        // wait for files to finish uploading to SFTP server
        Thread.sleep(7500);

        // add to processing folder
        logger.info(
                String.format("TNT: Adding files to processing folder. Number of files: '%s'", filesToProcess.size()));
        for (String filePrefix : filesToProcess.keySet()) {
            logger.info("filePrefix=" + filePrefix);
            Boolean processFile = true;
            LsEntry currentFile = filesToProcess.get(filePrefix);
            String currentFilename = currentFile.getFilename();

            // check if file is still uploading
            if (currentFile.getAttrs().getSize() != channel
                    .lstat(String.format("%s/%s", inputFullPath, currentFilename)).getSize()) {
                processFile = false;
                logger.info(String.format("TNT: %s not ready for processing, still uploading to SFTP server.",
                        currentFilename));
            }

            // check if another file is processing
            if (foundExistingFile(channel.ls(String.format("%s/*.csv", processingFullPath)), filePrefix)) {
                processFile = false;
                logger.info(String.format(
                        "TNT: Can't begin processing %s. Already processing another file of the same prefix.",
                        currentFilename));
            }

            // move file to processing directory
            if (processFile) {
                String processingFilename = currentDatestamp() + "_" + currentFilename;
                channel.rename(String.format("%s/%s", inputFullPath, currentFilename),
                        String.format("%s/%s", processingFullPath, processingFilename));
                logger.info(
                        String.format("TNT: Queuing %s for processing (%s)...", currentFilename, processingFilename));

                // create request object to be processed by VM
                SyncToDatabaseRequest newRequest = new SyncToDatabaseRequest(currentFilename, processingFilename,
                        processingFullPath, archiveFullPath);

                sftpRequests.add(newRequest);
            }
        }
        return sftpRequests;
    }

    private boolean foundExistingFile(Vector<LsEntry> inputFiles, String prefix) {
        HashMap<String, ArrayList<LsEntry>> processingFilesByPrefix = sortFilesByPrefix(inputFiles);

        return processingFilesByPrefix.containsKey(prefix);
    }

    private HashMap<String, ArrayList<LsEntry>> sortFilesByPrefix(Vector<LsEntry> inputFiles) {
        logger.info(String.format("TNT: Sorting list of file entries by prefix. Number of files in directory: '%s'",
                inputFiles.size()));
        HashMap<String, ArrayList<LsEntry>> sortedFiles = new HashMap<String, ArrayList<LsEntry>>();

        for (LsEntry file : inputFiles) {
            String prefix = "";

            // prepare regex pattern for group matching
            // ([A-Za-z0-9]+) => find an occurrence of any combination of letters and numbers,
            //                   requires at least 1 char
            // (\\d{8}) => find 8 digits, requires 1 occurrence of 8 digits
            Pattern r = Pattern.compile("([A-Za-z0-9]+)_(\\d{8}).csv");

            // use regex to find date in currentFile
            Matcher m = r.matcher(file.getFilename());
            m.find();

            // parse filename from matched regex
            try {
                prefix = m.group(1);
            } catch (Exception e) {
                logger.warn(String.format("Exception caught: '%s', could not match file '%s'", e.getCause(),
                        file.getFilename()));

            }

            if (!prefix.equals("")) {
                ArrayList<LsEntry> prefixedFileList = sortedFiles.get(prefix);
                if (prefixedFileList == null) {
                    prefixedFileList = new ArrayList<LsEntry>();
                    sortedFiles.put(prefix, prefixedFileList);
                }

                prefixedFileList.add(file);
            }
        }
        return sortedFiles;
    }

    // used to prepend processing filenames
    private String currentDatestamp() throws ParseException {
        String tz = "America/Los_Angeles";
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(tz));
        return TimeManager.toSimpleDateTimeInTimeZone(c, tz, "yyyyMMddHHmmss");
    }

    // assumptions:
    //     1. new marketing updates do not come within 24 hours of each other
    //     2. 1 unit day is used as the smallest unit of measure between files
    //
    // file formats handled:
    //     1. <marketing plan>_<date mmddyyyy>.csv
    //     2. locations_<date mmddyyyy>.csv
    private LsEntry getOldestFile(List<LsEntry> fileList) throws ParseException {
        // only proceed if list.size() > 1
        if (fileList.size() == 0) {
            return null;
        } else if (fileList.size() == 1) {
            return fileList.get(0);
        }

        // iterate over fileList and return oldest file
        LsEntry oldestFile = null;
        Date oldestFileDate = null;
        for (LsEntry entry : fileList) {
            String currentFilename = entry.getFilename();
            Date currentFileDate = new Date();

            // prepare regex for group matching
            Pattern r = Pattern.compile("_(\\d{8}).csv");
            // use regex to find date in currentFile
            Matcher m = r.matcher(currentFilename);
            m.find();

            // parse date from matched regex
            //     ex: 2TNTC_12052016.csv
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
            currentFileDate = sdf.parse(m.group(1));

            if (oldestFile == null || oldestFileDate.compareTo(currentFileDate) > 0) {
                oldestFile = entry;
                oldestFileDate = currentFileDate;
            }
        }
        return oldestFile;
    }
}
