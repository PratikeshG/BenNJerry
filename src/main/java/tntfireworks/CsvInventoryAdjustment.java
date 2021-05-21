package tntfireworks;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CsvInventoryAdjustment extends CsvRow {

    public static final String HEADER_ROW = "Revenue Business Unit,Business Unit,Loc No,Address Number,Alpha Name,Item No,Description,UPC Code,Pkg,Ship Condition,QTY Adjustment,Selling UOM,UM,Order Amount,Primary DG,CsCv,S/O Season,LT,QTY Reset,Reset";
    public static final int HEADER_LENGTH = 20;

    private String rbu;
    private String bu;
    @Size(min = 1)
    @NotNull
    private String locationNum;
    private String address;
    private String alphaName;
    @Size(min = 1)
    @NotNull
    private String itemNum;
    private String description;
    @Size(min = 1)
    @NotNull
    private String upc;
    private String pkg;
    private String shipCondition;
    private String qtyAdj;
    private String sellingUom;
    private String um;
    private String orderAmt;
    private String primaryDg;
    private String cscv;
    private String soSeason;
    private String lt;
    @Size(min = 1)
    @NotNull
    private String qtyReset;
    private String reset;

    public CsvInventoryAdjustment(String[] inventoryFields) {
        if (inventoryFields.length != HEADER_LENGTH) {
            throw new IllegalArgumentException();
        }

        // trim and replace with SQL chars
        for (int i = 0; i < inventoryFields.length; i++) {
            inventoryFields[i] = inventoryFields[i].trim();
            inventoryFields[i] = inventoryFields[i].replaceAll("'", "''");
        }

        // initialize class values
        rbu = inventoryFields[0];
        bu = inventoryFields[1];
        locationNum = inventoryFields[2];
        address = inventoryFields[3];
        alphaName = inventoryFields[4];
        itemNum = inventoryFields[5];
        description = inventoryFields[6];
        upc = inventoryFields[7];
        pkg = inventoryFields[8];
        shipCondition = inventoryFields[9];
        qtyAdj = inventoryFields[10];
        sellingUom = inventoryFields[11];
        um = inventoryFields[12];
        orderAmt = inventoryFields[13];
        primaryDg = inventoryFields[14];
        cscv = inventoryFields[15];
        soSeason = inventoryFields[16];
        lt = inventoryFields[17];
        qtyReset = inventoryFields[18];
        reset = inventoryFields[19];
    }

    public String getRbu() {
        return rbu;
    }

    public void setRbu(String rbu) {
        this.rbu = rbu;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

    public String getLocationNum() {
        return locationNum;
    }

    public void setLocationNum(String locationNum) {
        this.locationNum = locationNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlphaName() {
        return alphaName;
    }

    public void setAlphaName(String alphaName) {
        this.alphaName = alphaName;
    }

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getShipCondition() {
        return shipCondition;
    }

    public void setShipCondition(String shipCondition) {
        this.shipCondition = shipCondition;
    }

    public String getQtyAdj() {
        return qtyAdj;
    }

    public void setQtyAdj(String qtyAdj) {
        this.qtyAdj = qtyAdj;
    }

    public String getSellingUom() {
        return sellingUom;
    }

    public void setSellingUom(String sellingUom) {
        this.sellingUom = sellingUom;
    }

    public String getUm() {
        return um;
    }

    public void setUm(String um) {
        this.um = um;
    }

    public String getOrderAmt() {
        return orderAmt;
    }

    public void setOrderAmt(String orderAmt) {
        this.orderAmt = orderAmt;
    }

    public String getPrimaryDg() {
        return primaryDg;
    }

    public void setPrimaryDg(String primaryDg) {
        this.primaryDg = primaryDg;
    }

    public String getCsCv() {
        return cscv;
    }

    public void setCsCv(String cscv) {
        this.cscv = cscv;
    }

    public String getSoSeason() {
        return soSeason;
    }

    public void setSoSeason(String soSeason) {
        this.soSeason = soSeason;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public String getQtyReset() {
        return qtyReset;
    }

    public void setQtyReset(String qtyReset) {
        this.qtyReset = qtyReset;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }
}
