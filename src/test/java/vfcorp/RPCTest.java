package vfcorp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import org.junit.Test;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class RPCTest {
	
	@Test
	public void convertItem_updateCategoryWithItemsInIt_catalogItemsAreInNewCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0431130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		String oldName = "203 1130 OLD HATS";
		
		Category category = new Category();
		category.setName(oldName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		Item item = new Item();
		item.setCategory(category);
		current.addItem(item, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);

		assertTrue("only one catalog exists", result.getCategories().size() == 1);
		assertTrue("catalog has new name", ((Category) result.getCategories().values().toArray()[0]).getName().equals(expectedName));
		assertTrue("only one item exists", result.getItems().size() == 1);
		assertTrue("item belongs to new category", ((Item) result.getItems().values().toArray()[0]).getCategory().getId().equals(category.getId()));
	}
	
	@Test
	public void convertItem_createNewItemThatDoesntExist_catalogContainsNewItem() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01100731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedSku = "731516292893";
		
		Fee fee = new Fee();
		fee.setName("fee");
		current.addFee(fee, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains new item", result.getItems().containsKey(expectedSku));
		assertTrue("catalog doesn't contain any other items", result.getItems().size() == 1);
		assertTrue("item contains correct tax", ((Item) result.getItems().values().toArray()[0]).getFees()[0].equals(fee));
	}
	
	@Test
	public void convertItem_createNewItemThatAlreadyExists_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01100731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedName = "HAT - HR6108 101 0111 STRIPE W/ MESH HAT";
		String expectedSku = "731516292893";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku(expectedSku);
		itemVariation.setPriceMoney(new Money(2450));
		itemVariation.setUserData("203 1130");
		
		Item item = new Item();
		item.setName(expectedName);
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("item doesn't change", result.getItems().get(expectedSku).equals(item, new HashSet<Object>()));
		assertTrue("variation doesn't change", result.getItems().get(expectedSku).getVariations()[0].equals(itemVariation, new HashSet<Object>()));
	}
	
	@Test
	public void convertItem_createNewItemWithSameSkuAsExistingItem_catalogContainsUpdatedItem() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01100731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedName = "HAT - HR6108 101 0111 STRIPE W/ MESH HAT";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku("731516292893");
		itemVariation.setPriceMoney(new Money(5555));
		itemVariation.setUserData("123 4567");
		
		Item item = new Item();
		item.setName("different name");
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		ItemVariation expectedVariation = new ItemVariation("Regular");
		expectedVariation.setSku("731516292893");
		expectedVariation.setPriceMoney(new Money(2450));
		expectedVariation.setUserData("203 1130");
		
		Item expectedItem = new Item();
		expectedItem.setName(expectedName);
		expectedItem.setVariations(new ItemVariation[]{itemVariation});
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog only contains one item", result.getItems().size() == 1);
	}
	
	@Test
	public void convertItem_updateNewItemThatDoesntExist_catalogContainsNewItem() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01300731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedSku = "731516292893";
		
		Fee fee = new Fee();
		fee.setName("fee");
		current.addFee(fee, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains new item", result.getItems().containsKey(expectedSku));
		assertTrue("catalog doesn't contain any other items", result.getItems().size() == 1);
		assertTrue("item contains correct tax", ((Item) result.getItems().values().toArray()[0]).getFees()[0].equals(fee));
	}
	
	@Test
	public void convertItem_updateNewItemThatAlreadyExists_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01300731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedName = "HAT - HR6108 101 0111 STRIPE W/ MESH HAT";
		String expectedSku = "731516292893";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku(expectedSku);
		itemVariation.setPriceMoney(new Money(2450));
		itemVariation.setUserData("203 1130");
		
		Item item = new Item();
		item.setName(expectedName);
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("item doesn't change", result.getItems().get(expectedSku).equals(item, new HashSet<Object>()));
		assertTrue("variation doesn't change", result.getItems().get(expectedSku).getVariations()[0].equals(itemVariation, new HashSet<Object>()));
	}
	
	@Test
	public void convertItem_updateNewItemWithSameSkuAsExistingItem_catalogContainsUpdatedItem() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01300731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedName = "HAT - HR6108 101 0111 STRIPE W/ MESH HAT";
		String expectedSku = "731516292893";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku(expectedSku);
		itemVariation.setPriceMoney(new Money(5555));
		itemVariation.setUserData("123 4567");
		
		Item item = new Item();
		item.setName("different name");
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		ItemVariation expectedVariation = new ItemVariation("Regular");
		expectedVariation.setSku(expectedSku);
		expectedVariation.setPriceMoney(new Money(2450));
		expectedVariation.setUserData("203 1130");
		
		Item expectedItem = new Item();
		expectedItem.setName(expectedName);
		expectedItem.setVariations(new ItemVariation[]{itemVariation});
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog only contains one item", result.getItems().size() == 1);
	}
	
	@Test
	public void convertItem_deleteItemThatDoesntExist_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01200731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku("different sku");
		itemVariation.setPriceMoney(new Money(5555));
		itemVariation.setUserData("123 4567");
		
		Item item = new Item();
		item.setName("different name");
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog still contains item", result.getItems().containsKey("different sku"));
	}
	
	@Test
	public void convertItem_deleteItemThatExists_catalogDoesntContainDeletedItem() throws Exception {
		Catalog current = new Catalog();
		String recordString = "01200731516292893          203 1130                                                                          000000000000000                HAT                     1000000245000000024500000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0210004649786              1       0073151629\n                                                                                                                        29200731516292893          01\n29100731516292893          01HR6108 101 0111 STRIPE W/ MESH HAT\n36100731516292893          5    1HR6108";
		String expectedName = "HAT - HR6108 101 0111 STRIPE W/ MESH HAT";
		String expectedSku = "731516292893";
		
		ItemVariation itemVariation = new ItemVariation("Regular");
		itemVariation.setSku(expectedSku);
		itemVariation.setPriceMoney(new Money(2450));
		itemVariation.setUserData("203 1130");
		
		Item item = new Item();
		item.setName(expectedName);
		item.setVariations(new ItemVariation[]{itemVariation});
		
		current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains no items", result.getItems().size() == 0);
	}
	
	@Test
	public void convertCategory_createNewCategoryThatDoesntExist_catalogContainsNewCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0411130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains new category", result.getCategories().containsKey(expectedName));
		assertTrue("catalog doesn't contain any other categories", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_createNewCategoryInFilledCatalog_catalogContainsNewCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0411130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		Category category = new Category();
		category.setName("category");
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains new category", result.getCategories().containsKey(expectedName));
		assertTrue("catalog contains two categories", result.getCategories().size() == 2);
	}
	
	@Test
	public void convertCategory_createNewCategoryThatAlreadyExists_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0411130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		Category category = new Category();
		category.setName(expectedName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);

		assertTrue("catalog contains expected category", result.getCategories().containsKey(expectedName));
		assertTrue("catalog only contains one category", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_createNewCategoryWithDepartmentAndClassNumberSameAsExistingCategory_catalogContainsNewCategoryAndDeletesOldCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0411130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		String currentCategoryName = "203 1130 HATS";
		
		Category category = new Category();
		category.setName(currentCategoryName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains expected category", result.getCategories().containsKey(expectedName));
		assertFalse("catalog does not contain old category", result.getCategories().containsKey(currentCategoryName));
		assertTrue("catalog doesn't contain any other categories", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_updateCategoryThatDoesntExist_catalogContainsNewCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0431130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains new category", result.getCategories().containsKey(expectedName));
		assertTrue("catalog doesn't contain any other categories", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_updateCategoryThatAlreadyExists_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0431130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		Category category = new Category();
		category.setName(expectedName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);

		assertTrue("catalog contains expected category", result.getCategories().containsKey(expectedName));
		assertTrue("catalog only contains one category", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_updateCategoryWithDepartmentAndClassNumberSameAsExistingCategory_catalogContainsNewCategoryAndDeletesOldCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0431130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		String currentCategoryName = "203 1130 HATS";
		
		Category category = new Category();
		category.setName(currentCategoryName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog contains expected category", result.getCategories().containsKey(expectedName));
		assertFalse("catalog does not contain old category", result.getCategories().containsKey(currentCategoryName));
		assertTrue("catalog doesn't contain any other categories", result.getCategories().size() == 1);
	}
	
	@Test
	public void convertCategory_deleteCategoryThatDoesntExist_catalogDoesntChange() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0421130203 FASHION HATS";
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog doesn't have any categories", result.getCategories().size() == 0);
	}
	
	@Test
	public void convertCategory_deleteCategoryThatExists_catalogDoesntContainDeletedCategory() throws Exception {
		Catalog current = new Catalog();
		String recordString = "0421130203 FASHION HATS";
		String expectedName = "203 1130 FASHION HATS";
		
		Category category = new Category();
		category.setName(expectedName);
		current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog result = epicor.rpc().convert(current);
		
		assertTrue("catalog doesn't have any categories", result.getCategories().size() == 0);
	}
	
	@Test
	public void convertItem_ingestPLUWithDeleteItemAlternateDescription_IgnoresItemAlternateDescription() throws Exception {
		String recordString = "01100885928005807          11  1102                         00CC401                                          000000000000000                PLASMATIC JKT W BLU F14 1000001999900000299000000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n29200885928005807          01                                        \n29100885928005807          01PLASMATIC JKT W BLU F14 00CC401F7S      \n36100885928005807          5    ";
		Catalog empty = new Catalog();
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog catalog = epicor.rpc().convert(empty);
		
		assertTrue("catalog contains correctly named item", catalog.getItems().keySet().contains("PLASMATIC JKT W BLU F14 - PLASMATIC JKT W BLU F14 00CC401F7S"));
	}
	
	@Test
	public void convertItem_ingestPLUWithManyItemAlternateDescriptions_IgnoresItemAlternateDescriptions() throws Exception {
		String recordString = "01100885928005807          11  1102                         00CC401                                          000000000000000                PLASMATIC JKT W BLU F14 1000001999900000299000000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n29200885928005807          01                                        \n29200885928005807          01                                        \n29200885928005807          01                                        \n29200885928005807          01                                        \n29200885928005807          01                                        \n36100885928005807          5    ";
		Catalog empty = new Catalog();
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog catalog = epicor.rpc().convert(empty);
		
		assertTrue("catalog contains correctly named item", catalog.getItems().keySet().contains("PLASMATIC JKT W BLU F14"));
	}
	
	@Test
	public void convertItem_ingestPLUWithItemInterruptedByCategory_CorrectlyGetsItems() throws Exception {
		String recordString = "01100885928005807          11  1102                         00CC401                                          000000000000000                PLASMATIC JKT W BLU F14 1000001999900000299000000000000000000000000000000000000000000                                  000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010\n0411092200 MENS WATCHES  \n01100048283543357          301 3313                                                                          000000000000000                FSH KNIT                100000036500000003650000000000000000000000000000000000000000002022016                          000001000                             00001110               00000000000000000000000000000000000000000000000000000000000000000000000000000000000    00000000000                         010";
		Catalog empty = new Catalog();
		
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(new BufferedInputStream(new ByteArrayInputStream(recordString.getBytes(StandardCharsets.UTF_8))));
		
		Catalog catalog = epicor.rpc().convert(empty);
		
		assertTrue("catalog contains first item", catalog.getItems().keySet().contains("PLASMATIC JKT W BLU F14"));
		assertTrue("catalog contains second item", catalog.getItems().keySet().contains("FSH KNIT"));
		assertTrue("catalog contains category", catalog.getCategories().keySet().contains("200 1092 MENS WATCHES"));
	}
}
