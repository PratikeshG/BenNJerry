package vfcorp;

import java.io.IOException;
import java.util.HashSet;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.squareup.connect.Page;
import com.squareup.connect.PageCell;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class PLUDatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PLUDatabaseToSquareCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private String apiUrl;
    private int itemNumberLookupLength;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

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

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        PLUDatabaseToSquareRequest updateRequest = (PLUDatabaseToSquareRequest) message.getPayload();
        message.setProperty("pluDatabaseToSquareRequest", updateRequest, PropertyScope.INVOCATION);

        String deploymentId = updateRequest.getDeployment().getDeployment();

        SquareClient client = new SquareClient(updateRequest.getDeployment().getAccessToken(), apiUrl, "v1",
                updateRequest.getDeployment().getMerchantId(), updateRequest.getDeployment().getLocationId());

        PLUCatalogBuilder catalogBuilder = new PLUCatalogBuilder(databaseUrl, databaseUser, databasePassword);
        catalogBuilder.setItemNumberLookupLength(itemNumberLookupLength);

        Catalog currentCatalog = catalogBuilder.newCatalogFromSquare(client);
        Catalog proposedCatalog = catalogBuilder.newCatalogFromDatabase(currentCatalog, deploymentId,
                updateRequest.getDeployment().getLocationId(), updateRequest.getDeployment().getTimeZone(),
                updateRequest.getDeployment().isPluFiltered());

        CatalogChangeRequest ccr = diffAndLogChanges(currentCatalog, proposedCatalog, deploymentId);

        logger.info(String.format("(%s) Updating account...", deploymentId));
        ccr.setSquareClient(client);
        ccr.call();

        // TODO(bhartard): Determine how to handle fav page/cells after
        // migration to V2 APIs
        updateFavoritesGrid(client);

        logger.info(String.format("(%s) Done updating account.", deploymentId));

        // This request originated from an SFTP PLU file update
        // Need to move it to archive from processing
        if (updateRequest.isProcessingPluFile() && updateRequest.getProcessingFileName() != null) {
            archiveProcessingFile(updateRequest.getProcessingFileName(), updateRequest.getDeployment().getPluPath());
        }
        return null;
    }

    private void archiveProcessingFile(String fileName, String filePath)
            throws JSchException, IOException, SftpException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.rename(filePath + "/processing/" + fileName, filePath + "/archive/" + fileName);

        sftpChannel.disconnect();
        session.disconnect();
    }

    private CatalogChangeRequest diffAndLogChanges(Catalog current, Catalog proposed, String deploymentId) {
        logger.info(String.format("(%s) Current categories total: %d", deploymentId, current.getCategories().size()));
        logger.info(String.format("(%s) Current discounts total: %d", deploymentId, current.getDiscounts().size()));
        logger.info(String.format("(%s) Current fees total: %d", deploymentId, current.getFees().size()));
        logger.info(String.format("(%s) Current items total: %d", deploymentId, current.getItems().size()));

        logger.info(String.format("(%s) Proposed categories total: %d", deploymentId, proposed.getCategories().size()));
        logger.info(String.format("(%s) Proposed discounts total: %d", deploymentId, proposed.getDiscounts().size()));
        logger.info(String.format("(%s) Proposed fees total: %d", deploymentId, proposed.getFees().size()));
        logger.info(String.format("(%s) Proposed items total: %d", deploymentId, proposed.getItems().size()));

        logger.info(String.format("(%s) Performing diff...", deploymentId));
        CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU,
                CatalogChangeRequest.PrimaryKey.NAME);
        logger.info(String.format("(%s) Diff complete.", deploymentId));

        logger.info(String.format("(%s) Diff new mappings total: %d", deploymentId,
                ccr.getMappingsToApply().keySet().size()));
        logger.info(String.format("(%s) Diff create total: %d", deploymentId, ccr.getObjectsToCreate().size()));
        logger.info(String.format("(%s) Diff update total: %d", deploymentId, ccr.getObjectsToUpdate().size()));

        return ccr;
    }

    private void updateFavoritesGrid(SquareClient client) throws Exception {
        Page[] pages = client.pages().list();
        PageCell discountCell = discountCell();

        HashSet<Object> ignoreFields = new HashSet<Object>();
        ignoreFields.add(PageCell.Field.PAGE_ID);

        String pageId = "";
        PageCell currentCell = null;

        for (Page page : pages) {
            if (page.getPageIndex() == 0) {
                pageId = page.getId();
                for (PageCell cell : page.getCells()) {
                    if (cell.getRow() == 0 && cell.getColumn() == 0) {
                        currentCell = cell;
                        break;
                    }
                }
            }
        }

        if (pageId.length() > 0 && (currentCell == null || !currentCell.equals(discountCell, ignoreFields))) {
            logger.info("Updating discount favorites cell...");
            client.cells().update(pageId, discountCell);
        }
    }

    private PageCell discountCell() {
        PageCell cell = new PageCell();
        cell.setRow(0);
        cell.setColumn(0);
        cell.setObjectType("PLACEHOLDER");
        cell.setPlaceholderType("DISCOUNTS_CATEGORY");
        return cell;
    }
}
