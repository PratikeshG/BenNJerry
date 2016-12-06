package tntfireworks;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.TimeManager;

public class PollSFTPRequests implements Callable {
    // logger
    private static Logger logger = LoggerFactory.getLogger(PollSFTPRequests.class);
    
    // list of marketing plans
    public static final String[] PLAN_VALUES = new String[] {"2TNTC", "2TNTSS", "2COK703", "5TNT", 
                                                       "5AZSAT", "4TCSWA", "4TCSUT", "4TSS"};
    public static final HashSet<String> MARKETING_PLANS = new HashSet<String>(Arrays.asList(PLAN_VALUES));
    
    // sftp config
    private static String sftpHost;
    private static int sftpPort;
    private static String sftpUser;
    private static String sftpPassword;
    private static String sftpBasePath;
    private static String sftpArchivePath;
    private static String sftpInputPath;
    private static String sftpProcessingPath;   

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }
    
    public void setSftpBasePath(String sftpBasePath) {
        this.sftpBasePath = sftpBasePath;
    }
    
    public void setSftpArchivePath(String sftpArchivePath) {
        this.sftpArchivePath = sftpArchivePath;
    }
    
    public void setSftpInputPath(String sftpInputPath) {
        this.sftpInputPath = sftpInputPath;
    }
    
    public void setSftpProcessingPath(String sftpProcessingPath) {
        this.sftpProcessingPath = sftpProcessingPath;
    }
    
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        Session session = createSSHSession();
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        System.out.println("SFTP channel created.");

        List<SyncToDatabaseRequest> newRequests = getSFTPRequests(sftpChannel);

        sftpChannel.disconnect();
        System.out.println("SFTP channel disconnected.");
        
        session.disconnect();
        System.out.println("SFTP session disconnected.");

        return newRequests;
    }
    
    private static Session createSSHSession() throws JSchException, IOException {
        JSch jsch = new JSch();

        // initialize session properties
        Session session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        session.setPassword(sftpPassword);
        session.setConfig("StrictHostKeyChecking", "no");
    
        System.out.println("Establishing Connection...");
        session.connect();
        System.out.println("Connection established.");
        
        return session;
    }
    
    private List<SyncToDatabaseRequest> getSFTPRequests(ChannelSftp channel) throws InterruptedException, SftpException, ParseException {       
        // sftp paths to input/processing directories
        String inputFullPath = sftpBasePath + sftpInputPath;
        String processingFullPath = sftpBasePath + sftpInputPath + sftpProcessingPath;
        
        // targetFiles => total files list containing unique marketing plans and/or locations
        // sftpRequests => final requests sent for processing
        HashMap<String, LsEntry> targetFiles = new HashMap<String, LsEntry>();
        ArrayList<SyncToDatabaseRequest> sftpRequests = new ArrayList<SyncToDatabaseRequest>();               

        // find oldest files per marketing plan
        // TODO(wtsang): ignore any locations_*.csv files for now
        for (String plan : MARKETING_PLANS) {
            Vector<LsEntry> list = channel.ls(String.format("%s/%s_*.csv", inputFullPath, plan));
            LsEntry targetFile = getOldestFile(list);
    
            if (targetFile != null)
                targetFiles.put(plan, targetFile);                  
        }
        
        // wait for files to finish uploading to SFTP server
        Thread.sleep(7500);

        // add to processing folder
        for (String plan : targetFiles.keySet()) {
            Boolean processFile = true;
            LsEntry currentFile = targetFiles.get(plan);
            String  currentFilename = currentFile.getFilename();
            
            // check if file is still uploading
            if (currentFile.getAttrs().getSize() !=  channel.lstat(String.format("%s/%s", inputFullPath, currentFilename)).getSize()) {
                processFile = false;
                logger.info(String.format("%s not ready for processing, still uploading to SFTP server.", currentFilename));
            }
            
            // check if another file is processing
            if (channel.ls(String.format("%s/*.csv", processingFullPath)).size() > 0) {
                processFile = false; 
                logger.info(String.format("Can't begin processing %s. Already processing another file.", currentFilename));
            }
            
            // move file to processing directory
            if (processFile) {
                String processingFilename = currentDatestamp() + "_" + currentFilename;
                channel.rename(String.format("%s/%s", inputFullPath, currentFilename), String.format("%s/%s", processingFullPath, processingFilename));

                logger.info(String.format("Queuing %s for processing (%s)...", currentFilename, processingFilename));

                // create request object to be processed by VM
                SyncToDatabaseRequest newRequest = new SyncToDatabaseRequest();
                newRequest.setOriginalFilename(currentFilename);
                newRequest.setProcessingFilename(processingFilename);

                sftpRequests.add(newRequest);             
            }
        }       
        return sftpRequests;
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
    private LsEntry getOldestFile(Vector<LsEntry> fileList) throws ParseException {        
        // only proceed if list.size() > 1 
        if (fileList.size() == 0)
            return null;
        else if (fileList.size() == 1)
            return fileList.get(0);       

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
