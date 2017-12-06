# TNT Fireworks Reporting ReadMe

## Overview

The tntfireworks.reporting package contains all custom reporting logic for TNT Fireworks as defined by their reporting requirements.

The naming convention - 'Report \<number\>' - is defined by TNT Fireworks and this file contains a description of each. 

Note: 
- Report 1 is the only report sent on a monthly basis
- Report 8 is the only report that is required to be on the SFTP server. Other reports are only sent to the SFTP if the file size exceeds the defined maximum of the mailer service (currently Amazon SES)
- The email list for each report type is defined in the tntfireworks.reporting.properties file

## Report Types

#### Report 1 - "Settlements Report" - Emailed first of each month

Report 1 is emailed on the first of each month and contains settlements entries for the previous month of each TNT location. Each row in this file contains a settlements entry for a single location and multiple entries can exist for a single location. The Connect V1 Settlement endpoint is the only source of information for this report.

#### Report 2 - "Transactions Report" - Emailed daily

Report 2 contains daily payment information for all TNT locations and is sent daily. Connect V1 Payment and Connect V2 Transaction endpoints are used in conjunction to build the report as the TenderCardDetails.EntryMethod field does not exist in Connect V1. Additional information such as zip- code/state is pulled from a database for each TNT location and included in each row/entry of the report.

#### Report 3 - "Abnormal Transactions Report" - Emailed daily

Report 3 contains a predefined list of transaction behavior that TNT Fireworks wants to track to monitor potentially fraudulent transactions. The report logic only checks for potential fraudulent activity for a single day. The following is the list of transaction behaviors defined as alerts:

- "Alert 1" Card Present Transasction exceeds $1000
- "Alert 2" Card Not Present Transaction exceeds $500
- "Alert 3" Card Not Present transaction >3 times in one day at same location
- "Alert 4" Same card used 4 or more times across entire master account in one day 
- "Alert 5" Same dollar amount run on card-tender consecutively at same location 3 or more times in one day 
- "Alert 6" Card Not Present Transaction exceeds $150

Each row in the file represents a single transaction and is associated with a transaction id and alert type. If a transaction is detected in multiple alerts, the transaction will be listed in a separate row for each triggered alert. Hence, the same transaction ID can occur multiple times in a report.

#### Report 5/6 - "Location Sales Report" - Emailed daily

Report 5 and 6 were merged into one report. Report 5 initially contained aggregate daily and "YTD" seasonal credit card sales information per location. Report 6 was the same report, but included cash sales information. 

Report 5 now contains columns for both 1) credit card sales and 2) credit card + cash sales.  Each row in the report represents aggregate sales for a single location. The season range is determined as the period between the current date for the report (unless explicitly set elsewhere) and the set start date for the season as defined in tntfireworks.reporting.properties file as 'startOfSeason'. In order to calculate the seasonal sales amount for each location, payment and transaction data is pulled for the entire season range.

#### Report 7 - "Item Sales Report" - Emailed daily

Report 7 contains item-level sales data. Daily sales for each item is counted and aggregated, and reported for each location. The "YTD" sales amount/quantity, or seasonal amount/quantity, is also tracked. Each row of the report includes the item number, daily totals, seasonal totals, and location number. Because itemized transaction information is not included in the Connect V2 Transaction endpoint, payment data is pulled from Connect V1 Payments.

#### Report 8 - "Credit Debit Report" - Placed on SFTP daily

Report 8 contains information related to the total number of daily credits and debits for each TNT Location. This report is placed on the SFTP instead of emailed, as TNT's E1 (EnterpriseOne) system is set to automatically ingest report from the SFTP.

A credit is defined as a credit card refund and debit is defined as a credit card sale. The number of debits, credits, and total ticket count (# debits + # credits) is tracked within this report. The net deposit amount (debit amount + credit amount) is also calculated. 

A "load number" is also set each season with a starting value of 1 and stored in a database. Each time the report is generated, the current load number is included in the report and subsequently incremented. This is a requirement set forth by TNT and is used for their internal systems.