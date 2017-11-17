package vfcorp.smartwool;

public class CsvExamples {
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
			"11/15/17,21:18:15,Pacific Time (US & Canada),Cat 2,Item 2,1.0,Regular,2,\"\",$1.00,$0.00,$1.00,$0.08,32WG7b1gqYzrSFjYu2gVSt5eV,pNPtqi6ShdvIyibJO9FEKQB,Jordan,\"\",http://squareup.com/dashboard/sales/transactions/32WG7b1gqYzrSFjYu2gVSt5eV/by-unit/BW93ZCWH452X9,Payment,#1002,\"\",AE8X8MPTGN7DF5M76JAC10VAP0,Jordan Finci,\"\"";

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
	public static final String testTransactionCsv2 = "11/16/2017,13:32:20,Pacific Time (US & Canada),$2.00,$0.00,$2.00,$0.00,$0.16,$0.00,$0.00,$2.16,Point of Sale,$2.00,\"Tapped, Dipped\",$0.16,$0.00,$0.00,\"\",\"\",($0.06),$2.10,NndMVtldstvN5iEXH99TTHoeV,\"rbvd84sEsOzDNhwYkQOOAyMF, PYNa8AwEhGKnS6uvSjSeLQB, zA23Y9XvgrFebuvRo2KPTsMF\",\"Visa, Visa\",\"2565, 7539\",Jordan,\"Jordan Finci, Jordan Finci, Jordan Finci\",,http://squareup.com/dashboard/sales/transactions/NndMVtldstvN5iEXH99TTHoeV/by-unit/BW93ZCWH452X9,\"Item 1 (Regular) - Notes, Item 2 (Regular)\",Payment,#1002,\"\",\", , \",\", , \",\", , \",1002";
}
