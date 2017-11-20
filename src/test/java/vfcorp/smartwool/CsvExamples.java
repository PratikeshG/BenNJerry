package vfcorp.smartwool;

import java.util.Arrays;
import java.util.List;

public class CsvExamples {
	public static final String testTransactionHeaders = "Date,Time,Time Zone,Gross Sales,Discounts,Net Sales,Gift Card Sales,Tax,Tip,Partial Refunds,Total Collected,Source,Card,Card Entry Methods,Cash,Square Gift Card,Other Tender,Other Tender Type,Other Tender Note,Fees,Net Total,Transaction ID,Payment ID,Card Brand,PAN Suffix,Device Name,Staff Name,Staff ID,Details,Description,Event Type,Location,Dining Option,Customer ID,Customer Name,Customer Reference ID,Device Nickname";
	public static final String testItemHeaders = "Date,Time,Time Zone,Category,Item,Qty,Price Point Name,SKU,Modifiers Applied,Gross Sales,Discounts,Net Sales,Tax,Transaction ID,Payment ID,Device Name,Notes,Details,Event Type,Location,Dining Option,Customer ID,Customer Name,Customer Reference ID";

	//CSV Test 1
	public static final String testTransaction1 = " {\n" +
			"            \"id\": \"32WG7b1gqYzrSFjYu2gVSt5eV\",\n" +
			"            \"location_id\": \"BW93ZCWH452X9\",\n" +
			"            \"created_at\": \"2017-11-16T05:18:15Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"pNPtqi6ShdvIyibJO9FEKQB\",\n" +
			"                    \"location_id\": \"BW93ZCWH452X9\",\n" +
			"                    \"transaction_id\": \"32WG7b1gqYzrSFjYu2gVSt5eV\",\n" +
			"                    \"created_at\": \"2017-11-16T05:18:14Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 216,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 0,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"customer_id\": \"AE8X8MPTGN7DF5M76JAC10VAP0\",\n" +
			"                    \"type\": \"CASH\",\n" +
			"                    \"cash_details\": {\n" +
			"                        \"buyer_tendered_money\": {\n" +
			"                            \"amount\": 216,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"change_back_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"333230AC-5738-49FD-B1A8-758B5C581B39\"\n" +
			"        }";
	public static final String testPayment1 = "{\n" +
			"        \"id\": \"pNPtqi6ShdvIyibJO9FEKQB\",\n" +
			"        \"merchant_id\": \"BW93ZCWH452X9\",\n" +
			"        \"created_at\": \"2017-11-16T05:18:15Z\",\n" +
			"        \"device\": {\n" +
			"            \"id\": \"DEVICE_INSTALLATION_ID:FC61090C-1D72-41C7-803D-95C12B60530D\",\n" +
			"            \"name\": \"Jordan\"\n" +
			"        },\n" +
			"        \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/32WG7b1gqYzrSFjYu2gVSt5eV\",\n" +
			"        \"receipt_url\": \"https://squareup.com/receipt/preview/pNPtqi6ShdvIyibJO9FEKQB\",\n" +
			"        \"inclusive_tax_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"additive_tax_money\": {\n" +
			"            \"amount\": 16,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"tax_money\": {\n" +
			"            \"amount\": 16,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"tip_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"discount_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"total_collected_money\": {\n" +
			"            \"amount\": 216,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"processing_fee_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"net_total_money\": {\n" +
			"            \"amount\": 216,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"refunded_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"swedish_rounding_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"gross_sales_money\": {\n" +
			"            \"amount\": 200,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"net_sales_money\": {\n" +
			"            \"amount\": 200,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"inclusive_tax\": [],\n" +
			"        \"additive_tax\": [\n" +
			"            {\n" +
			"                \"name\": \"Sales Tax\",\n" +
			"                \"rate\": \"0.08000000\",\n" +
			"                \"inclusion_type\": \"ADDITIVE\",\n" +
			"                \"applied_money\": {\n" +
			"                    \"amount\": 16,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"tender\": [\n" +
			"            {\n" +
			"                \"type\": \"CASH\",\n" +
			"                \"name\": \"Cash\",\n" +
			"                \"id\": \"pNPtqi6ShdvIyibJO9FEKQB\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 216,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"tendered_money\": {\n" +
			"                    \"amount\": 216,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"change_back_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"refunded_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"receipt_url\": \"https://squareup.com/receipt/preview/pNPtqi6ShdvIyibJO9FEKQB\",\n" +
			"                \"employee_id\": \"JAZ12K8TQGWNG\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"refunds\": [],\n" +
			"        \"itemizations\": [\n" +
			"            {\n" +
			"                \"name\": \"Item 1\",\n" +
			"                \"quantity\": \"1.00000000\",\n" +
			"                \"notes\": \"Notes\",\n" +
			"                \"item_variation_name\": \"Regular\",\n" +
			"                \"item_detail\": {\n" +
			"                    \"category_name\": \"Cat 1\",\n" +
			"                    \"sku\": \"1\",\n" +
			"                    \"item_id\": \"SC32VVXBYH2HLPWYWBWUOKY7\",\n" +
			"                    \"item_variation_id\": \"6SQ24ZRPP5ZZZGWVC34WBG3H\"\n" +
			"                },\n" +
			"                \"itemization_type\": \"ITEM\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 108,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"single_quantity_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"gross_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"net_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"taxes\": [\n" +
			"                    {\n" +
			"                        \"name\": \"Sales Tax\",\n" +
			"                        \"rate\": \"0.08000000\",\n" +
			"                        \"inclusion_type\": \"ADDITIVE\",\n" +
			"                        \"applied_money\": {\n" +
			"                            \"amount\": 8,\n" +
			"                            \"currency_code\": \"USD\"\n" +
			"                        },\n" +
			"                        \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"discounts\": [],\n" +
			"                \"modifiers\": []\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Item 2\",\n" +
			"                \"quantity\": \"1.00000000\",\n" +
			"                \"item_variation_name\": \"Regular\",\n" +
			"                \"item_detail\": {\n" +
			"                    \"category_name\": \"Cat 2\",\n" +
			"                    \"sku\": \"2\",\n" +
			"                    \"item_id\": \"R57YXFR6GGLZJVCLW2DTSBW7\",\n" +
			"                    \"item_variation_id\": \"B6QYWTSRNBBRQNHPRMJEEHIN\"\n" +
			"                },\n" +
			"                \"itemization_type\": \"ITEM\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 108,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"single_quantity_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"gross_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"net_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"taxes\": [\n" +
			"                    {\n" +
			"                        \"name\": \"Sales Tax\",\n" +
			"                        \"rate\": \"0.08000000\",\n" +
			"                        \"inclusion_type\": \"ADDITIVE\",\n" +
			"                        \"applied_money\": {\n" +
			"                            \"amount\": 8,\n" +
			"                            \"currency_code\": \"USD\"\n" +
			"                        },\n" +
			"                        \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"discounts\": [],\n" +
			"                \"modifiers\": []\n" +
			"            }\n" +
			"        ]\n" +
			"    }";
	public static final String testCustomer1 = "{\n" +
			"        \"id\": \"AE8X8MPTGN7DF5M76JAC10VAP0\",\n" +
			"        \"created_at\": \"2017-11-14T00:15:40.715Z\",\n" +
			"        \"updated_at\": \"2017-11-14T00:15:57Z\",\n" +
			"        \"cards\": [\n" +
			"            {\n" +
			"                \"id\": \"icard-elB96m4EUUFkdOPu3GB\",\n" +
			"                \"card_brand\": \"MASTERCARD\",\n" +
			"                \"last_4\": \"1191\",\n" +
			"                \"exp_month\": 4,\n" +
			"                \"exp_year\": 2021\n" +
			"            }\n" +
			"        ],\n" +
			"        \"given_name\": \"Jordan\",\n" +
			"        \"family_name\": \"Finci\",\n" +
			"        \"email_address\": \"finci@squareup.com\",\n" +
			"        \"preferences\": {\n" +
			"            \"email_unsubscribed\": false\n" +
			"        },\n" +
			"        \"groups\": [\n" +
			"            {\n" +
			"                \"id\": \"FE9Y3Q6AEFD4M.LOYAL\",\n" +
			"                \"name\": \"Regulars\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"FE9Y3Q6AEFD4M.REACHABLE\",\n" +
			"                \"name\": \"Reachable\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"FE9Y3Q6AEFD4M.CARDS_ON_FILE\",\n" +
			"                \"name\": \"Cards on File\"\n" +
			"            }\n" +
			"        ]\n" +
			"    }";
	public static final String testTransactionsCsv1 = "11/15/2017,21:18:15,Pacific Time (US & Canada),$2.00,$0.00,$2.00,$0.00,$0.16,$0.00,$0.00,$2.16,Point of Sale,$0.00,N/A,$2.16,$0.00,$0.00,\"\",\"\",$0.00,$2.16,32WG7b1gqYzrSFjYu2gVSt5eV,pNPtqi6ShdvIyibJO9FEKQB,,,Jordan,Jordan Finci,,http://squareup.com/dashboard/sales/transactions/32WG7b1gqYzrSFjYu2gVSt5eV/by-unit/BW93ZCWH452X9,\"Item 1 (Regular) - Notes, Item 2 (Regular)\",Payment,#1002,\"\",AE8X8MPTGN7DF5M76JAC10VAP0,Jordan Finci,\"\",1002";
	public static final String testItemsCsv1 = "11/15/2017,21:18:15,Pacific Time (US & Canada),Cat 1,Item 1,1.0,Regular,1,\"\",$1.00,$0.00,$1.00,$0.08,32WG7b1gqYzrSFjYu2gVSt5eV,pNPtqi6ShdvIyibJO9FEKQB,Jordan,Notes,http://squareup.com/dashboard/sales/transactions/32WG7b1gqYzrSFjYu2gVSt5eV/by-unit/BW93ZCWH452X9,Payment,#1002,\"\",AE8X8MPTGN7DF5M76JAC10VAP0,Jordan Finci,\"\"\n" +
			"11/15/2017,21:18:15,Pacific Time (US & Canada),Cat 2,Item 2,1.0,Regular,2,\"\",$1.00,$0.00,$1.00,$0.08,32WG7b1gqYzrSFjYu2gVSt5eV,pNPtqi6ShdvIyibJO9FEKQB,Jordan,\"\",http://squareup.com/dashboard/sales/transactions/32WG7b1gqYzrSFjYu2gVSt5eV/by-unit/BW93ZCWH452X9,Payment,#1002,\"\",AE8X8MPTGN7DF5M76JAC10VAP0,Jordan Finci,\"\"";

	public static final String testTransaction2 = "{\n" +
			"            \"id\": \"NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"            \"location_id\": \"BW93ZCWH452X9\",\n" +
			"            \"created_at\": \"2017-11-16T21:32:20Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"rbvd84sEsOzDNhwYkQOOAyMF\",\n" +
			"                    \"location_id\": \"BW93ZCWH452X9\",\n" +
			"                    \"transaction_id\": \"NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"                    \"created_at\": \"2017-11-16T21:30:49Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 100,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 3,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"2565\",\n" +
			"                            \"fingerprint\": \"sq-1-jRqXFuzL9dWcPvzc8XJ6PfFjIvOQ8pKFAg0o8609iQJIBPg0oOegDIAhJ0D-morLrw\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"CONTACTLESS\"\n" +
			"                    }\n" +
			"                },\n" +
			"                {\n" +
			"                    \"id\": \"PYNa8AwEhGKnS6uvSjSeLQB\",\n" +
			"                    \"location_id\": \"BW93ZCWH452X9\",\n" +
			"                    \"transaction_id\": \"NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"                    \"created_at\": \"2017-11-16T21:31:10Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 16,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 0,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CASH\",\n" +
			"                    \"cash_details\": {\n" +
			"                        \"buyer_tendered_money\": {\n" +
			"                            \"amount\": 16,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"change_back_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                },\n" +
			"                {\n" +
			"                    \"id\": \"zA23Y9XvgrFebuvRo2KPTsMF\",\n" +
			"                    \"location_id\": \"BW93ZCWH452X9\",\n" +
			"                    \"transaction_id\": \"NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"                    \"created_at\": \"2017-11-16T21:32:11Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 100,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 3,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"7539\",\n" +
			"                            \"fingerprint\": \"sq-1-YVta8yXmLnXwzLMy2Yzqzq7RYFWbiDQqgU9M3Dd7i_fjjLpa4ElkwkLQYO8GdxJl-g\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"890F7632-CB5E-473C-9B2D-1C7C0E331F7A\"\n" +
			"        }";
	public static final String testPayment2 = "{\n" +
			"        \"id\": \"NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"        \"merchant_id\": \"BW93ZCWH452X9\",\n" +
			"        \"created_at\": \"2017-11-16T21:32:20Z\",\n" +
			"        \"device\": {\n" +
			"            \"id\": \"DEVICE_INSTALLATION_ID:FC61090C-1D72-41C7-803D-95C12B60530D\",\n" +
			"            \"name\": \"Jordan\"\n" +
			"        },\n" +
			"        \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/NndMVtldstvN5iEXH99TTHoeV\",\n" +
			"        \"receipt_url\": \"https://squareup.com/receipt/preview/rbvd84sEsOzDNhwYkQOOAyMF\",\n" +
			"        \"inclusive_tax_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"additive_tax_money\": {\n" +
			"            \"amount\": 16,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"tax_money\": {\n" +
			"            \"amount\": 16,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"tip_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"discount_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"total_collected_money\": {\n" +
			"            \"amount\": 216,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"processing_fee_money\": {\n" +
			"            \"amount\": -6,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"net_total_money\": {\n" +
			"            \"amount\": 210,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"refunded_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"swedish_rounding_money\": {\n" +
			"            \"amount\": 0,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"gross_sales_money\": {\n" +
			"            \"amount\": 200,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"net_sales_money\": {\n" +
			"            \"amount\": 200,\n" +
			"            \"currency_code\": \"USD\"\n" +
			"        },\n" +
			"        \"inclusive_tax\": [],\n" +
			"        \"additive_tax\": [\n" +
			"            {\n" +
			"                \"name\": \"Sales Tax\",\n" +
			"                \"rate\": \"0.08000000\",\n" +
			"                \"inclusion_type\": \"ADDITIVE\",\n" +
			"                \"applied_money\": {\n" +
			"                    \"amount\": 16,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"tender\": [\n" +
			"            {\n" +
			"                \"type\": \"CREDIT_CARD\",\n" +
			"                \"name\": \"Credit Card\",\n" +
			"                \"id\": \"rbvd84sEsOzDNhwYkQOOAyMF\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"card_brand\": \"VISA\",\n" +
			"                \"pan_suffix\": \"2565\",\n" +
			"                \"entry_method\": \"MANUAL\",\n" +
			"                \"refunded_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"receipt_url\": \"https://squareup.com/receipt/preview/rbvd84sEsOzDNhwYkQOOAyMF\",\n" +
			"                \"employee_id\": \"JAZ12K8TQGWNG\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"type\": \"CASH\",\n" +
			"                \"name\": \"Cash\",\n" +
			"                \"id\": \"PYNa8AwEhGKnS6uvSjSeLQB\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 16,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"tendered_money\": {\n" +
			"                    \"amount\": 16,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"change_back_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"refunded_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"receipt_url\": \"https://squareup.com/receipt/preview/PYNa8AwEhGKnS6uvSjSeLQB\",\n" +
			"                \"employee_id\": \"JAZ12K8TQGWNG\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"type\": \"CREDIT_CARD\",\n" +
			"                \"name\": \"Credit Card\",\n" +
			"                \"id\": \"zA23Y9XvgrFebuvRo2KPTsMF\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"card_brand\": \"VISA\",\n" +
			"                \"pan_suffix\": \"7539\",\n" +
			"                \"entry_method\": \"MANUAL\",\n" +
			"                \"refunded_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"receipt_url\": \"https://squareup.com/receipt/preview/zA23Y9XvgrFebuvRo2KPTsMF\",\n" +
			"                \"employee_id\": \"JAZ12K8TQGWNG\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"refunds\": [],\n" +
			"        \"itemizations\": [\n" +
			"            {\n" +
			"                \"name\": \"Item 1\",\n" +
			"                \"quantity\": \"1.00000000\",\n" +
			"                \"notes\": \"Notes\",\n" +
			"                \"item_variation_name\": \"Regular\",\n" +
			"                \"item_detail\": {\n" +
			"                    \"category_name\": \"Cat 1\",\n" +
			"                    \"sku\": \"1\",\n" +
			"                    \"item_id\": \"SC32VVXBYH2HLPWYWBWUOKY7\",\n" +
			"                    \"item_variation_id\": \"6SQ24ZRPP5ZZZGWVC34WBG3H\"\n" +
			"                },\n" +
			"                \"itemization_type\": \"ITEM\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 108,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"single_quantity_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"gross_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"net_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"taxes\": [\n" +
			"                    {\n" +
			"                        \"name\": \"Sales Tax\",\n" +
			"                        \"rate\": \"0.08000000\",\n" +
			"                        \"inclusion_type\": \"ADDITIVE\",\n" +
			"                        \"applied_money\": {\n" +
			"                            \"amount\": 8,\n" +
			"                            \"currency_code\": \"USD\"\n" +
			"                        },\n" +
			"                        \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"discounts\": [],\n" +
			"                \"modifiers\": []\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Item 2\",\n" +
			"                \"quantity\": \"1.00000000\",\n" +
			"                \"item_variation_name\": \"Regular\",\n" +
			"                \"item_detail\": {\n" +
			"                    \"category_name\": \"Cat 2\",\n" +
			"                    \"sku\": \"2\",\n" +
			"                    \"item_id\": \"R57YXFR6GGLZJVCLW2DTSBW7\",\n" +
			"                    \"item_variation_id\": \"B6QYWTSRNBBRQNHPRMJEEHIN\"\n" +
			"                },\n" +
			"                \"itemization_type\": \"ITEM\",\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 108,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"single_quantity_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"gross_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"net_sales_money\": {\n" +
			"                    \"amount\": 100,\n" +
			"                    \"currency_code\": \"USD\"\n" +
			"                },\n" +
			"                \"taxes\": [\n" +
			"                    {\n" +
			"                        \"name\": \"Sales Tax\",\n" +
			"                        \"rate\": \"0.08000000\",\n" +
			"                        \"inclusion_type\": \"ADDITIVE\",\n" +
			"                        \"applied_money\": {\n" +
			"                            \"amount\": 8,\n" +
			"                            \"currency_code\": \"USD\"\n" +
			"                        },\n" +
			"                        \"fee_id\": \"1A56AF6D-2F11-480C-A7E8-11C64447EE2D\"\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"discounts\": [],\n" +
			"                \"modifiers\": []\n" +
			"            }\n" +
			"        ]\n" +
			"    }";
	public static final String testTransactionCsv2 = "11/16/2017,13:32:20,Pacific Time (US & Canada),$2.00,$0.00,$2.00,$0.00,$0.16,$0.00,$0.00,$2.16,Point of Sale,$2.00,\"Tapped, Dipped\",$0.16,$0.00,$0.00,\"\",\"\",($0.06),$2.10,NndMVtldstvN5iEXH99TTHoeV,\"rbvd84sEsOzDNhwYkQOOAyMF, PYNa8AwEhGKnS6uvSjSeLQB, zA23Y9XvgrFebuvRo2KPTsMF\",\"Visa, Visa\",\"2565, 7539\",Jordan,\"Jordan Finci, Jordan Finci, Jordan Finci\",,http://squareup.com/dashboard/sales/transactions/NndMVtldstvN5iEXH99TTHoeV/by-unit/BW93ZCWH452X9,\"Item 1 (Regular) - Notes, Item 2 (Regular)\",Payment,#1002,,,,,1002";

	public static final String k603csv = "11/17/2017,13:07:41,Pacific Time (US & Canada),$34.90,$0.00,$34.90,$0.00,$2.67,$0.00,$0.00,$37.57,Point of Sale,$37.57,Dipped,$0.00,$0.00,$0.00,\"\",\"\",($1.03),$36.54,hm9DxWgr1D5OVHi1MjYz64feV,S1k23eD2DdAkbGxLHzzblwMF,Visa,0357,iPad,Dawn Yderstad,,http://squareup.com/dashboard/sales/transactions/hm9DxWgr1D5OVHi1MjYz64feV/by-unit/9GYN1NWJRGP04,\"W Tiva Crew BLACK (M), W Secret Sleuth (S)\",Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,12:57:08,Pacific Time (US & Canada),$12.95,($6.48),$6.47,$0.00,$0.49,$0.00,$0.00,$6.96,Point of Sale,$6.96,Tapped,$0.00,$0.00,$0.00,\"\",\"\",($0.19),$6.77,lO8VXmUh2t4f6BMMDjw9tjneV,aTVf0mrk7TCKneEk0KoLnuMF,Visa,0904,iPad,Ryan McGrail,,http://squareup.com/dashboard/sales/transactions/lO8VXmUh2t4f6BMMDjw9tjneV/by-unit/9GYN1NWJRGP04,W Secret Sleuth (S),Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,12:12:11,Pacific Time (US & Canada),$27.90,$0.00,$27.90,$0.00,$2.13,$0.00,$0.00,$30.03,Point of Sale,$30.03,Dipped,$0.00,$0.00,$0.00,\"\",\"\",($0.83),$29.20,LVax4uvFgqHxT6wQA1D53TneV,MVA58OjQDI7uDPcxRrjf4vMF,Visa,5412,iPad,Dawn Yderstad,,http://squareup.com/dashboard/sales/transactions/LVax4uvFgqHxT6wQA1D53TneV/by-unit/9GYN1NWJRGP04,2 x M No Show (L),Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,11:16:40,Pacific Time (US & Canada),$128.70,($12.95),$115.75,$0.00,$8.85,$0.00,$0.00,$124.60,Point of Sale,$124.60,Dipped,$0.00,$0.00,$0.00,\"\",\"\",($3.43),$121.17,h4kg0eLNfi9wMbN3Oe9gd79eV,PZ1ZOJMJAn4LoqSbsZWvyuMF,American Express,9004,iPad,Ryan McGrail,,http://squareup.com/dashboard/sales/transactions/h4kg0eLNfi9wMbN3Oe9gd79eV/by-unit/9GYN1NWJRGP04,\"PhD Slope LT Ifrane BLACK (M), Popcorn Cable BLUE ICE H (M), W Wheat Fields KH (M), W Ethno Graphic Crew (M), PhD Run LE LC (L), W PhD OD Lt Micro OATMEAL (M)\",Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,09:36:22,Pacific Time (US & Canada),$20.95,$0.00,$20.95,$0.00,$1.60,$0.00,$0.00,$22.55,Point of Sale,$22.55,Swiped,$0.00,$0.00,$0.00,\"\",\"\",($0.62),$21.93,RiJs1HSWsvBpJEFuAkmLSUyeV,YGuLCvntIHEIZ4UHYyXdSzMF,Visa,4702,iPad,Ryan McGrail,,http://squareup.com/dashboard/sales/transactions/RiJs1HSWsvBpJEFuAkmLSUyeV/by-unit/9GYN1NWJRGP04,M DIGI CHESTNUT (L),Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,09:34:38,Pacific Time (US & Canada),$27.90,$0.00,$27.90,$0.00,$2.13,$0.00,$0.00,$30.03,Point of Sale,$30.03,Dipped,$0.00,$0.00,$0.00,\"\",\"\",($0.83),$29.20,LHjFEhzEnyUUvkaOhIZX2queV,XK9Jacru1mHcoL7CWF7dksMF,Visa,4243,iPad,Ryan McGrail,,http://squareup.com/dashboard/sales/transactions/LHjFEhzEnyUUvkaOhIZX2queV/by-unit/9GYN1NWJRGP04,2 x M No Show (L),Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",\n" +
			"11/17/2017,09:18:47,Pacific Time (US & Canada),$91.00,$0.00,$91.00,$0.00,$6.96,$0.00,$0.00,$97.96,Point of Sale,$97.96,Dipped,$0.00,$0.00,$0.00,\"\",\"\",($2.69),$95.27,jt09RKTnC0100PqEhcAdc1yeV,IKkrAcoWkhB7uquCIfjWpuMF,Visa,3169,iPad,Lori Venturi,,http://squareup.com/dashboard/sales/transactions/jt09RKTnC0100PqEhcAdc1yeV/by-unit/9GYN1NWJRGP04,\"Merino 250 Glove BLUE ICE H (L), Merino 250 Cffd Beanie (1FM), PhD Lt Rvrsble Beanie LIGHT GRAY (1FM)\",Payment,K603Cherry Creek Mall Pop-Up,\"\",\"\",\"\",\"\",";

	public static final List<String> k603transactions = Arrays.asList(new String[] {
			"{\n" +
			"            \"id\": \"hm9DxWgr1D5OVHi1MjYz64feV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T21:07:41Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"S1k23eD2DdAkbGxLHzzblwMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"hm9DxWgr1D5OVHi1MjYz64feV\",\n" +
			"                    \"created_at\": \"2017-11-17T21:07:34Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 3757,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 103,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"0357\",\n" +
			"                            \"fingerprint\": \"sq-1-IuAd9glbl6nRe3ebLmRVD3iG2z4qtbARk8nttK7-Z9IASIrSr5Phos0Awau_2GupWA\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"326B9E29-62B5-4707-BF04-29323624492C\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"W Tiva Crew BLACK\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"MYK3C25KR6DE3FPZA56EUWVW\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 168,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2195,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2195,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 168,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2363,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"W Secret Sleuth\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"722AKYPY6V6KR6YHQ22ZEXLL\",\n" +
			"                        \"variation_name\": \"S\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 99,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 1295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 99,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 1394,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 3757,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 267,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"lO8VXmUh2t4f6BMMDjw9tjneV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T20:57:08Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"aTVf0mrk7TCKneEk0KoLnuMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"lO8VXmUh2t4f6BMMDjw9tjneV\",\n" +
			"                    \"created_at\": \"2017-11-17T20:57:05Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 696,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 19,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"0904\",\n" +
			"                            \"fingerprint\": \"sq-1-QTDxopTyg_qPDIQth7CE8MjU9hwbFr1IcyO1El5kZ6omBmy23U6bqhwGg9NXf9mRpA\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"CONTACTLESS\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"9FC7541A-EDE8-414E-93DE-5326C035CFCB\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"W Secret Sleuth\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"722AKYPY6V6KR6YHQ22ZEXLL\",\n" +
			"                        \"variation_name\": \"S\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 49,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"4VP2Y67ZAGHFJHOSFF64AOFB\",\n" +
			"                                \"name\": \"VF Employee Discount\",\n" +
			"                                \"type\": \"FIXED_PERCENTAGE\",\n" +
			"                                \"percentage\": \"50\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 648,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 1295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 49,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 648,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 696,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 696,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 49,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 648,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"LVax4uvFgqHxT6wQA1D53TneV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T20:12:11Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"MVA58OjQDI7uDPcxRrjf4vMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"LVax4uvFgqHxT6wQA1D53TneV\",\n" +
			"                    \"created_at\": \"2017-11-17T20:11:57Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 3003,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 83,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"5412\",\n" +
			"                            \"fingerprint\": \"sq-1-GsAREbRkjUfEmI3wXgSjta1tKRuv4htd4MFR5iBz1cy9JxEgmDo871pv4KgPYcXEhg\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"B8FCE105-C12C-41C1-98E1-B5291AFF5296\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"M No Show\",\n" +
			"                        \"quantity\": \"2\",\n" +
			"                        \"catalog_object_id\": \"FYBUBB76E5CQLYI44UKBEXJD\",\n" +
			"                        \"variation_name\": \"L\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 213,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1395,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2790,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 213,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 3003,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 3003,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 213,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"h4kg0eLNfi9wMbN3Oe9gd79eV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T19:16:40Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"PZ1ZOJMJAn4LoqSbsZWvyuMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"h4kg0eLNfi9wMbN3Oe9gd79eV\",\n" +
			"                    \"created_at\": \"2017-11-17T19:16:28Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 12460,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 343,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"AMERICAN_EXPRESS\",\n" +
			"                            \"last_4\": \"9004\",\n" +
			"                            \"fingerprint\": \"sq-1-GgBuTRCgvXwZ8m3zOxahG4KrlAIl4W2uxlMTcdZ4TO_PNQ91u4058kLwQYDuhvfqEA\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"7609C548-F967-478C-8879-5F656760154D\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"PhD Slope LT Ifrane BLACK\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"G2OD4UODG4TDRLZIYGKDODKB\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 172,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 251,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2495,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2495,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 172,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 251,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2416,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"Popcorn Cable BLUE ICE H\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"S6VTMNOP2JNR5VVUKSVAC3RF\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 158,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 231,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2295,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 158,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 231,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2222,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"W Wheat Fields KH\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"ZJAXCJJSQYWD4RVYUEGFOMQP\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 165,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 241,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2395,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2395,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 165,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 241,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2319,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"W Ethno Graphic Crew\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"HSL6TTHMTLVSRQ22TVT6BXAX\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 144,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 211,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2095,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2095,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 144,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 211,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2028,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"PhD Run LE LC\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"5UCHFTU2QOYIRHYXJGTWDLKY\",\n" +
			"                        \"variation_name\": \"L\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 123,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 180,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1795,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 1795,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 123,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 180,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 1738,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"W PhD OD Lt Micro OATMEAL\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"6X6UDJSSM5MXHLIZISB6QQUJ\",\n" +
			"                        \"variation_name\": \"M\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 123,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"discounts\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\",\n" +
			"                                \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                                \"type\": \"VARIABLE_AMOUNT\",\n" +
			"                                \"amount_money\": {\n" +
			"                                    \"amount\": 1295,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 181,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                },\n" +
			"                                \"scope\": \"ORDER\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1795,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 1795,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 123,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 181,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 1737,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 12460,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 885,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 1295,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"RiJs1HSWsvBpJEFuAkmLSUyeV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T17:36:22Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"YGuLCvntIHEIZ4UHYyXdSzMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"RiJs1HSWsvBpJEFuAkmLSUyeV\",\n" +
			"                    \"created_at\": \"2017-11-17T17:35:55Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 2255,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 62,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"4702\",\n" +
			"                            \"fingerprint\": \"sq-1-4VMSAIt1jfZpLvpmtnA9PBJArdPbxyH-5RNSz-3vsd4fl9nR-WFsPGXyXoEGVrWlhw\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"SWIPED\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"55577C17-9EEE-4305-8AE8-B7F2711C0275\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"M DIGI CHESTNUT\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"X36CPLCARO43AFEQNND4ITAE\",\n" +
			"                        \"variation_name\": \"L\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 160,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2095,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2095,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 160,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 2255,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 2255,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 160,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"LHjFEhzEnyUUvkaOhIZX2queV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T17:34:38Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"XK9Jacru1mHcoL7CWF7dksMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"LHjFEhzEnyUUvkaOhIZX2queV\",\n" +
			"                    \"created_at\": \"2017-11-17T17:34:32Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 3003,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 83,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"4243\",\n" +
			"                            \"fingerprint\": \"sq-1-UHRPV5siT5lZwVXqZ3g2BrSrJYgVhXv6NGzsn9mmKSH_NTbJudceQIt6m1DqZVuwLg\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"F72DB9B1-D4E2-4A22-BD6D-5B4C984A1675\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"M No Show\",\n" +
			"                        \"quantity\": \"2\",\n" +
			"                        \"catalog_object_id\": \"FYBUBB76E5CQLYI44UKBEXJD\",\n" +
			"                        \"variation_name\": \"L\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 213,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 1395,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2790,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 213,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 3003,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 3003,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 213,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }",
			"{\n" +
			"            \"id\": \"jt09RKTnC0100PqEhcAdc1yeV\",\n" +
			"            \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"            \"created_at\": \"2017-11-17T17:18:47Z\",\n" +
			"            \"tenders\": [\n" +
			"                {\n" +
			"                    \"id\": \"IKkrAcoWkhB7uquCIfjWpuMF\",\n" +
			"                    \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                    \"transaction_id\": \"jt09RKTnC0100PqEhcAdc1yeV\",\n" +
			"                    \"created_at\": \"2017-11-17T17:18:33Z\",\n" +
			"                    \"amount_money\": {\n" +
			"                        \"amount\": 9796,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"processing_fee_money\": {\n" +
			"                        \"amount\": 269,\n" +
			"                        \"currency\": \"USD\"\n" +
			"                    },\n" +
			"                    \"type\": \"CARD\",\n" +
			"                    \"card_details\": {\n" +
			"                        \"status\": \"CAPTURED\",\n" +
			"                        \"card\": {\n" +
			"                            \"card_brand\": \"VISA\",\n" +
			"                            \"last_4\": \"3169\",\n" +
			"                            \"fingerprint\": \"sq-1-9IV90W4txhI243dYfUZsjV-ol_liS3hEbdOr8p-2Ye4ObNzqlqUPVVSh1A9ANSJ9gw\"\n" +
			"                        },\n" +
			"                        \"entry_method\": \"EMV\"\n" +
			"                    }\n" +
			"                }\n" +
			"            ],\n" +
			"            \"product\": \"REGISTER\",\n" +
			"            \"client_id\": \"3B5E15FA-27D9-4C81-9940-6C9D31C72F1F\",\n" +
			"            \"order\": {\n" +
			"                \"location_id\": \"9GYN1NWJRGP04\",\n" +
			"                \"line_items\": [\n" +
			"                    {\n" +
			"                        \"name\": \"Merino 250 Glove BLUE ICE H\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"NNQ56OVBH6IZJGOIREYTTUBV\",\n" +
			"                        \"variation_name\": \"L\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 268,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 3500,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 3500,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 268,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 3768,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"Merino 250 Cffd Beanie\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"3RYNFWKQXFFL7KKRAEWSTVKF\",\n" +
			"                        \"variation_name\": \"1FM\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 214,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2800,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2800,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 214,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 3014,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    },\n" +
			"                    {\n" +
			"                        \"name\": \"PhD Lt Rvrsble Beanie LIGHT GRAY\",\n" +
			"                        \"quantity\": \"1\",\n" +
			"                        \"catalog_object_id\": \"3XP4VBZ34MGXPWAQCJDPTU2I\",\n" +
			"                        \"variation_name\": \"1FM\",\n" +
			"                        \"taxes\": [\n" +
			"                            {\n" +
			"                                \"catalog_object_id\": \"TDIONLNT4H3WQI7HMTVERW5N\",\n" +
			"                                \"name\": \"Sales Tax\",\n" +
			"                                \"type\": \"ADDITIVE\",\n" +
			"                                \"percentage\": \"7.65\",\n" +
			"                                \"applied_money\": {\n" +
			"                                    \"amount\": 214,\n" +
			"                                    \"currency\": \"USD\"\n" +
			"                                }\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"base_price_money\": {\n" +
			"                            \"amount\": 2800,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"gross_sales_money\": {\n" +
			"                            \"amount\": 2800,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_tax_money\": {\n" +
			"                            \"amount\": 214,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_discount_money\": {\n" +
			"                            \"amount\": 0,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        },\n" +
			"                        \"total_money\": {\n" +
			"                            \"amount\": 3014,\n" +
			"                            \"currency\": \"USD\"\n" +
			"                        }\n" +
			"                    }\n" +
			"                ],\n" +
			"                \"total_money\": {\n" +
			"                    \"amount\": 9796,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_tax_money\": {\n" +
			"                    \"amount\": 696,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                },\n" +
			"                \"total_discount_money\": {\n" +
			"                    \"amount\": 0,\n" +
			"                    \"currency\": \"USD\"\n" +
			"                }\n" +
			"            }\n" +
			"        }"
	});
	public static final List<String> k603payments = Arrays.asList(new String[] {
			"{\n" +
			"    \"id\": \"S1k23eD2DdAkbGxLHzzblwMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T21:07:41Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/hm9DxWgr1D5OVHi1MjYz64feV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/S1k23eD2DdAkbGxLHzzblwMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 267,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 267,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 3757,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -103,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 3654,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 3490,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 3490,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 267,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"S1k23eD2DdAkbGxLHzzblwMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3757,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"0357\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/S1k23eD2DdAkbGxLHzzblwMF\",\n" +
			"            \"employee_id\": \"i-ZEsyEC2GvLBmRQfgN_\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"W Tiva Crew BLACK\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"190849882049\",\n" +
			"                \"item_id\": \"KME5AF7IG43CWUPQCS6RR2T7\",\n" +
			"                \"item_variation_id\": \"MYK3C25KR6DE3FPZA56EUWVW\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2363,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2195,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2195,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2195,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 168,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"W Secret Sleuth\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"S\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"605284760345\",\n" +
			"                \"item_id\": \"UZPRIWSK7AFMDFGJG273AY4J\",\n" +
			"                \"item_variation_id\": \"722AKYPY6V6KR6YHQ22ZEXLL\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 1394,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 1295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 1295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 99,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"aTVf0mrk7TCKneEk0KoLnuMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T20:57:08Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/lO8VXmUh2t4f6BMMDjw9tjneV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/aTVf0mrk7TCKneEk0KoLnuMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 49,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 49,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": -648,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 696,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -19,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 677,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 1295,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 647,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 49,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"aTVf0mrk7TCKneEk0KoLnuMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 696,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"0904\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/aTVf0mrk7TCKneEk0KoLnuMF\",\n" +
			"            \"employee_id\": \"k0dvdTWRQVUtqmagcrxw\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"W Secret Sleuth\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"S\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"605284760345\",\n" +
			"                \"item_id\": \"UZPRIWSK7AFMDFGJG273AY4J\",\n" +
			"                \"item_variation_id\": \"722AKYPY6V6KR6YHQ22ZEXLL\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 696,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 1295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -648,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 647,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 49,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"VF Employee Discount\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -648,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"4VP2Y67ZAGHFJHOSFF64AOFB\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"MVA58OjQDI7uDPcxRrjf4vMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T20:12:11Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/LVax4uvFgqHxT6wQA1D53TneV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/MVA58OjQDI7uDPcxRrjf4vMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 213,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 213,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 3003,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -83,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 2920,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 2790,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 2790,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 213,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"MVA58OjQDI7uDPcxRrjf4vMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3003,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"5412\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/MVA58OjQDI7uDPcxRrjf4vMF\",\n" +
			"            \"employee_id\": \"i-ZEsyEC2GvLBmRQfgN_\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"M No Show\",\n" +
			"            \"quantity\": \"2.00000000\",\n" +
			"            \"item_variation_name\": \"L\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"605284760444\",\n" +
			"                \"item_id\": \"SHYJOKL64XXH6Q3PNHCKJAQV\",\n" +
			"                \"item_variation_id\": \"FYBUBB76E5CQLYI44UKBEXJD\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3003,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1395,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2790,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2790,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 213,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"PZ1ZOJMJAn4LoqSbsZWvyuMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T19:16:40Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/h4kg0eLNfi9wMbN3Oe9gd79eV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/PZ1ZOJMJAn4LoqSbsZWvyuMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 885,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 885,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": -1295,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 12460,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -343,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 12117,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 12870,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 11575,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 885,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"PZ1ZOJMJAn4LoqSbsZWvyuMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 12460,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"AMERICAN_EXPRESS\",\n" +
			"            \"pan_suffix\": \"9004\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/PZ1ZOJMJAn4LoqSbsZWvyuMF\",\n" +
			"            \"employee_id\": \"k0dvdTWRQVUtqmagcrxw\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"PhD Slope LT Ifrane BLACK\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Performance Socks\",\n" +
			"                \"sku\": \"889587672035\",\n" +
			"                \"item_id\": \"CMTLNIK4HIYWGIAQZSJ5TQRM\",\n" +
			"                \"item_variation_id\": \"G2OD4UODG4TDRLZIYGKDODKB\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2416,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2495,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2495,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -251,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2244,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 172,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -251,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"Popcorn Cable BLUE ICE H\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"190849882346\",\n" +
			"                \"item_id\": \"GME2Q7VNVTRM7HWZNRPZQT5I\",\n" +
			"                \"item_variation_id\": \"S6VTMNOP2JNR5VVUKSVAC3RF\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2222,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2295,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -231,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2064,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 158,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -231,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"W Wheat Fields KH\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"605284942635\",\n" +
			"                \"item_id\": \"ETFJ6GSBRTR3YJ7QWVZSYITF\",\n" +
			"                \"item_variation_id\": \"ZJAXCJJSQYWD4RVYUEGFOMQP\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2319,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2395,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2395,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -241,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2154,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 165,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -241,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"W Ethno Graphic Crew\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"190849880106\",\n" +
			"                \"item_id\": \"CEV2L75YNWEA2HDGT7U2HTEM\",\n" +
			"                \"item_variation_id\": \"HSL6TTHMTLVSRQ22TVT6BXAX\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2028,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2095,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2095,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -211,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 1884,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 144,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -211,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"PhD Run LE LC\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"L\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Performance Socks\",\n" +
			"                \"sku\": \"605284897805\",\n" +
			"                \"item_id\": \"HWXDS6J3GJYEF5MCV6AN7AA6\",\n" +
			"                \"item_variation_id\": \"5UCHFTU2QOYIRHYXJGTWDLKY\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 1738,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1795,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 1795,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -180,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 1615,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 123,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -180,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"W PhD OD Lt Micro OATMEAL\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"M\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Performance Socks\",\n" +
			"                \"sku\": \"888658691913\",\n" +
			"                \"item_id\": \"TRMTUTL4UOMM2SR2WA4WECMM\",\n" +
			"                \"item_variation_id\": \"6X6UDJSSM5MXHLIZISB6QQUJ\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 1737,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1795,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 1795,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": -181,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 1614,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 123,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [\n" +
			"                {\n" +
			"                    \"name\": \"Buy5Same - REWARDS, Buy 5 Get 1 Free\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": -181,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"discount_id\": \"ZKRBSNUL7QFFXRQ4MVRZZBN2\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"YGuLCvntIHEIZ4UHYyXdSzMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T17:36:22Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/RiJs1HSWsvBpJEFuAkmLSUyeV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/YGuLCvntIHEIZ4UHYyXdSzMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 160,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 160,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 2255,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -62,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 2193,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 2095,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 2095,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 160,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"YGuLCvntIHEIZ4UHYyXdSzMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2255,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"4702\",\n" +
			"            \"entry_method\": \"SWIPED\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/YGuLCvntIHEIZ4UHYyXdSzMF\",\n" +
			"            \"employee_id\": \"k0dvdTWRQVUtqmagcrxw\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"M DIGI CHESTNUT\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"L\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"190849878271\",\n" +
			"                \"item_id\": \"F74YBUC47OUWKNT4GWXCXRAY\",\n" +
			"                \"item_variation_id\": \"X36CPLCARO43AFEQNND4ITAE\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 2255,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2095,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2095,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2095,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 160,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"XK9Jacru1mHcoL7CWF7dksMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T17:34:38Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/LHjFEhzEnyUUvkaOhIZX2queV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/XK9Jacru1mHcoL7CWF7dksMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 213,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 213,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 3003,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -83,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 2920,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 2790,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 2790,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 213,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"XK9Jacru1mHcoL7CWF7dksMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3003,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"4243\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/XK9Jacru1mHcoL7CWF7dksMF\",\n" +
			"            \"employee_id\": \"k0dvdTWRQVUtqmagcrxw\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"M No Show\",\n" +
			"            \"quantity\": \"2.00000000\",\n" +
			"            \"item_variation_name\": \"L\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Lifestyle Socks\",\n" +
			"                \"sku\": \"605284760444\",\n" +
			"                \"item_id\": \"SHYJOKL64XXH6Q3PNHCKJAQV\",\n" +
			"                \"item_variation_id\": \"FYBUBB76E5CQLYI44UKBEXJD\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3003,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 1395,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2790,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2790,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 213,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}",
			"{\n" +
			"    \"id\": \"IKkrAcoWkhB7uquCIfjWpuMF\",\n" +
			"    \"merchant_id\": \"9GYN1NWJRGP04\",\n" +
			"    \"created_at\": \"2017-11-17T17:18:47Z\",\n" +
			"    \"device\": {\n" +
			"        \"id\": \"DEVICE_INSTALLATION_ID:D4691607-99BF-4129-9C62-191C16B07875\",\n" +
			"        \"name\": \"iPad\"\n" +
			"    },\n" +
			"    \"payment_url\": \"https://squareup.com/dashboard/sales/transactions/jt09RKTnC0100PqEhcAdc1yeV\",\n" +
			"    \"receipt_url\": \"https://squareup.com/receipt/preview/IKkrAcoWkhB7uquCIfjWpuMF\",\n" +
			"    \"inclusive_tax_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"additive_tax_money\": {\n" +
			"        \"amount\": 696,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tax_money\": {\n" +
			"        \"amount\": 696,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"tip_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"discount_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"total_collected_money\": {\n" +
			"        \"amount\": 9796,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"processing_fee_money\": {\n" +
			"        \"amount\": -269,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_total_money\": {\n" +
			"        \"amount\": 9527,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"refunded_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"swedish_rounding_money\": {\n" +
			"        \"amount\": 0,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"gross_sales_money\": {\n" +
			"        \"amount\": 9100,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"net_sales_money\": {\n" +
			"        \"amount\": 9100,\n" +
			"        \"currency_code\": \"USD\"\n" +
			"    },\n" +
			"    \"inclusive_tax\": [],\n" +
			"    \"additive_tax\": [\n" +
			"        {\n" +
			"            \"name\": \"Sales Tax\",\n" +
			"            \"rate\": \"0.07650000\",\n" +
			"            \"inclusion_type\": \"ADDITIVE\",\n" +
			"            \"applied_money\": {\n" +
			"                \"amount\": 696,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"tender\": [\n" +
			"        {\n" +
			"            \"type\": \"CREDIT_CARD\",\n" +
			"            \"name\": \"Credit Card\",\n" +
			"            \"id\": \"IKkrAcoWkhB7uquCIfjWpuMF\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 9796,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"card_brand\": \"VISA\",\n" +
			"            \"pan_suffix\": \"3169\",\n" +
			"            \"entry_method\": \"MANUAL\",\n" +
			"            \"refunded_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"receipt_url\": \"https://squareup.com/receipt/preview/IKkrAcoWkhB7uquCIfjWpuMF\",\n" +
			"            \"employee_id\": \"eKVkb2NZybCfuPHfz0BJ\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"refunds\": [],\n" +
			"    \"itemizations\": [\n" +
			"        {\n" +
			"            \"name\": \"Merino 250 Glove BLUE ICE H\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"L\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Accessories\",\n" +
			"                \"sku\": \"190850808458\",\n" +
			"                \"item_id\": \"UVHSYP453XKKSX5N3YGWBNYQ\",\n" +
			"                \"item_variation_id\": \"NNQ56OVBH6IZJGOIREYTTUBV\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3768,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 3500,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 3500,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 3500,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 268,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"Merino 250 Cffd Beanie\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"1FM\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Accessories\",\n" +
			"                \"sku\": \"190850804368\",\n" +
			"                \"item_id\": \"D7A6MZOYS6Q472H66B435E74\",\n" +
			"                \"item_variation_id\": \"3RYNFWKQXFFL7KKRAEWSTVKF\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3014,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 214,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        },\n" +
			"        {\n" +
			"            \"name\": \"PhD Lt Rvrsble Beanie LIGHT GRAY\",\n" +
			"            \"quantity\": \"1.00000000\",\n" +
			"            \"item_variation_name\": \"1FM\",\n" +
			"            \"item_detail\": {\n" +
			"                \"category_name\": \"Accessories\",\n" +
			"                \"sku\": \"190850804108\",\n" +
			"                \"item_id\": \"U5S3FTL6IX7B5SU6UYN4V4T7\",\n" +
			"                \"item_variation_id\": \"3XP4VBZ34MGXPWAQCJDPTU2I\"\n" +
			"            },\n" +
			"            \"itemization_type\": \"ITEM\",\n" +
			"            \"total_money\": {\n" +
			"                \"amount\": 3014,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"single_quantity_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"gross_sales_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"discount_money\": {\n" +
			"                \"amount\": 0,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"net_sales_money\": {\n" +
			"                \"amount\": 2800,\n" +
			"                \"currency_code\": \"USD\"\n" +
			"            },\n" +
			"            \"taxes\": [\n" +
			"                {\n" +
			"                    \"name\": \"Sales Tax\",\n" +
			"                    \"rate\": \"0.07650000\",\n" +
			"                    \"inclusion_type\": \"ADDITIVE\",\n" +
			"                    \"applied_money\": {\n" +
			"                        \"amount\": 214,\n" +
			"                        \"currency_code\": \"USD\"\n" +
			"                    },\n" +
			"                    \"fee_id\": \"TDIONLNT4H3WQI7HMTVERW5N\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"discounts\": [],\n" +
			"            \"modifiers\": []\n" +
			"        }\n" +
			"    ],\n" +
			"    \"extensions\": {\n" +
			"        \"metadata\": {}\n" +
			"    }\n" +
			"}"
	});
}
