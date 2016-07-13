package paradies.pandora;

import com.squareup.connect.Merchant;
import com.squareup.connect.diff.Catalog;

public class CatalogCallablePayload {
	
	private Merchant location;
	private Catalog catalog;
	
	public CatalogCallablePayload() {
		
	}
	
	public CatalogCallablePayload(Merchant location, Catalog catelog) {
		this.location = location;
		this.catalog = catelog;
	}

	public Merchant getLocation() {
		return location;
	}

	public void setLocation(Merchant location) {
		this.location = location;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}
}