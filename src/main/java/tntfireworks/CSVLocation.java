package tntfireworks;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CSVLocation {
	@Size(min=1)
	@NotNull
    private String locationNum;
	@Size(min=1)
	@NotNull
    private String addressNum;
	@Size(min=1)
	@NotNull
    private String name;
	@Size(min=1)
	@NotNull
    private String address;
    private String city;
    private String state;
    private String zip;
    private String county;
    @Size(min=1)
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
    
}
