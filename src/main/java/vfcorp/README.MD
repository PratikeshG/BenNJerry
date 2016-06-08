# VFC Runbook

### New location setup
Employees must be configured to include associate/employee number in the "external ID".

Locations must be configured to include store number as a Square location nickname, in brackets. For example, the nickname might be "Location 1 (00231)".

### New device setup
Individual devices must be configured to include the register number as the device name, in brackets. For example, a device name might be "Register B (002)".

### Uploading a full item library
It is strongly recommended to upload full PLUs via the Square web dashboard, as uploading them through the Managed Integrations app takes a long time (10+ hours for 25,000 items) and requires a full redeploy of the Managed Integrations app.

##### Uploading via the Square web dashboard
In order to do a full PLU upload, the account must first be cleared of items. This can be done in any way desired; the fastest is probably to upload a Square item CSV (which can be downloaded from the Square web dashboard) which does not contain any items.

If necessary, a Regulator permission on the account to disable the item upload limit must be toggled (`IGNORE_ITEMS_BULK_OBJECT_LIMIT`).

The PLU must then be converted into a Square CSV. This can be done with a utility class that exists in the Managed Integrations project: `vfcorp.PLUToCSVConverter`. The PLU file and the output file can be set within the class; once it is run, it will produce a CSV that can be uploaded to the Square web dashboard. This will also create the necessary categories.

Finally, if necessary, create a tax and apply it to the newly created items, as the generated CSV will not do this.

##### Uploading via the Managed Integrations app
Before doing any of the following steps, ensure that the necessary tax exists in the account before continuing; otherwise, the tax will have to be manually applied to all new items via the Square web dashboard once the PLU has been ingested.

By default, the PLU ingestion process runs two health checks: the first checks to see if the PLU contains only additions, and the second checks to see if the PLU is suspiciously large. If either condition is true, the PLU ingestion process will thrown an exception and not continue. This makes uploading a full PLU impossible via the Managed Integrations app, which is by design (it guards against accidental upload of a full PLU catalog).

To upload the full PLU via the Managed Integrations app, these checks must be disabled. The checks are controlled within the `[env].vfcorp.properties` file. The two properties that must be set to false are `vfcorp.rpc.onlyAddsCheck` and `vfcorp.rpc.suspiciousNumberOfRecordsCheck`. Once these are set to false and the application is redeployed, full PLUs will be processed successfully by uploading them via the Managed Integrations app.