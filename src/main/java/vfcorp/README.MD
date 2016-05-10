# VFCorp - Implementation Details

## Functional Limitations

### TLOG

Record 000 (Transaction Header) does not support cancels or post voids, tax exemptions, training mode, transaction error codes, RetailStore features, customer softwarre features, or a tax calculator because none of these features are avilable in Square.

Record 001 (Merchandise Item) does not support voided or exchanged transactions, dynamic price lookups, item sending, or associate sales as these features are not provided by the POS.

Record 017 (Store Close) is not supported because none of the required features are provided in Square (weather on closing, holiday on closing, special event on closing).

Record 023 (Authorzation Code) does not support authorization codes, because there is no ability to enter authorization codes into the POS.

Record 025 (Item Tax Merchandise Non Merchandise Items Fees) only supports static calculation of taxes.

Record 026 (Associate) does not support team numbers, because employees do not have team numbers.

Record 034 (Tender Count) does not support a count in a drawer (as opposed to an amount in a drawer) because Square does not track the number of a given tender received or given (i.e. number of cheques).

Record 055 (Line Item Accounting String) does not support non-merchandise or rentals.

Record 056 (Line Item Associate And Discount Accounting String) does not support associate discounting in general, as discounts are not differentiated in Square by whether or not they are associate discounts.

Record 061 (Tender) does not support currency exchange because the POS does not perform currency exchange.

Record 065 (Credit Card Tender) does not support full account numbers, expiry dates, authorization data, or settlement data because the API does not make this information available.

Record 071 (Event Giveback) does not support event, deal or coupon numbers, transaction discounts, or component types because there is no dynamic pricing engine available.

Debit transactions will show up as credit card transactions, due to a limitation in Square.

All transactions will only exist in one currency (the currency being used in the country in which the Square account is configured).

Refund transactions will only be itemized if they are FULL refunds, with a refund reason of "Returned Goods". If this is not the case, the refunds will show up in the TLOG, but will not have items.

For non-full refunds, all transaction money will show up solely as transaction total, without breaking down the refund into various taxes or fees.

There are certain cases where payments will not be pulled from the API. One such case is for offline payments; these payments will not be pulled from the API until they are reconciled, at which point they may not be reported if the window in which they're processed has passed. This must be handled at some point. 

### RPC

The only information that gets pulled into Square is the following:

  - Record 01 (Item Record): Item Number, Retail Price, Department Number, Class Number, Description
  - Record 29 (Item Alernate Description): Item Alternate Description

All other information is ignored.

## Account and Device Configuration

Items must be configured to include PLUs as a Square SKU.

Employees must be configured to include associate/employee number in the "external ID".

Locations must be configured to include store number as a Square location nickname, in brackets. For example, the nickname might be "Location 1 (00231)".

Individual devices must be configured to include the register number as the device name, in brackets. For example, a device name might be "Register B (002)".

The account used to access the SFTP server must be able to create new files, delete files, and create new directories.