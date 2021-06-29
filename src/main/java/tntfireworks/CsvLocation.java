package tntfireworks;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CsvLocation extends CsvRow {

    public static final String HEADER_ROW = "LOC #,ADDRESS #,NAME,ADDRESS,CITY,ST,ZIP,COUNTY,MKT PRG,LEGAL C,DISC,RBU,BP,CO,SA #,SA NAME,CUST #, NAME,SEASON,YEAR,MACHINE TYPE,Square Dashboard Account";

    @Size(min = 1)
    @NotNull
    private String locationNum;
    private String addressNum;
    @Size(min = 1)
    @NotNull
    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String county;
    @Size(min = 1)
    @NotNull
    private String mktPlan;
    private String legal;
    private String disc;
    private String rbu;
    private String bp;
    private String co;
    private String saNum;
    private String saName;
    private String custNum;
    private String custName;
    private String season;
    private String year;
    private String machineType;
    private String deployment;
    private String sqDashboardEmail;

    public void setLocationNum(String locationNum) {
        this.locationNum = locationNum;
    }

    public String getLocationNum() {
        return locationNum;
    }

    public void setAddressNum(String addressNum) {
        this.addressNum = addressNum;
    }

    public String getAddressNum() {
        return addressNum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getZip() {
        return zip;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCounty() {
        return county;
    }

    public void setMktPlan(String mktPlan) {
        this.mktPlan = mktPlan;
    }

    public String getMktPlan() {
        return mktPlan;
    }

    public void setLegal(String legal) {
        this.legal = legal;
    }

    public String getLegal() {
        return legal;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getDisc() {
        return disc;
    }

    public void setRbu(String rbu) {
        this.rbu = rbu;
    }

    public String getRbu() {
        return rbu;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getBp() {
        return bp;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getCo() {
        return co;
    }

    public void setSaNum(String saNum) {
        this.saNum = saNum;
    }

    public String getSaNum() {
        return saNum;
    }

    public void setSaName(String saName) {
        this.saName = saName;
    }

    public String getSaName() {
        return saName;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum;
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustName() {
        return custName;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSeason() {
        return season;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getDeloyment() {
        return deployment;
    }

    public String getSqDashboardEmail() {
        return sqDashboardEmail;
    }

    public void setSqDashboardEmail(String sqDashboardEmail) {
        this.sqDashboardEmail = sqDashboardEmail;
    }

    public static CsvLocation fromLocationFieldsCsvRow(String[] locationFields) {
        if (locationFields.length != 22) {
            throw new IllegalArgumentException();
        }

        // trim and replace with SQL chars
        for (int i = 0; i < locationFields.length; i++) {
            locationFields[i] = locationFields[i].trim();
            locationFields[i] = locationFields[i].replaceAll("'", "''");
        }

        // TODO(wtsang): can use a HashMap + ArrayList to read in fields + add accordingly
        //               and add location constructor to take in HashMap to initialize location
        //      0 - locationNum;
        //      1 - addressNum;
        //      2 - name;
        //      3 - address;
        //      4 - city;
        //      5 - state;
        //      6 - zip;
        //      7 - county;
        //      8 - mktPlan;
        //      9 - legal;
        //      10 - disc;
        //      11 - rbu;
        //      12 - bp;
        //      13 - co;
        //      14 - saNum;
        //      15 - saName;
        //      16 - custNum;
        //      17 - custName;
        //      18 - season;
        //      19 - year;
        //      20 - machineType;
        //      21 - sqDashboardEmail
        //

        // NOTE: unused fields are commented out to avoid ingesting unverified data and having it misused elsewhere
        CsvLocation location = new CsvLocation();
        location.setLocationNum(locationFields[0]);
        location.setAddressNum(locationFields[1]);
        location.setName(locationFields[2]);
        location.setAddress(locationFields[3]);
        location.setCity(locationFields[4]);
        location.setState(locationFields[5]);
        location.setZip(locationFields[6]);
        location.setCounty(locationFields[7]);
        location.setMktPlan(locationFields[8]);
        location.setLegal(locationFields[9]);
        location.setDisc(locationFields[10]);
        location.setRbu(locationFields[11]);
        location.setBp(locationFields[12]);
        location.setCo(locationFields[13]);
        location.setSaNum(locationFields[14]);
        location.setSaName(locationFields[15]);
        location.setCustNum(locationFields[16]);
        location.setCustName(locationFields[17]);
        location.setSeason(locationFields[18]);
        location.setYear(locationFields[19]);
        location.setMachineType(locationFields[20]);
        location.setSqDashboardEmail(locationFields[21]);

        if (!location.isValid()) {
            throw new IllegalArgumentException();
        }

        return location;
    }
}
