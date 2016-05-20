# Urbanspace Runbook

### Adding new locations
To add a new location to the reports that are generated, the only step that is necessary is to create the right row in the MySQL database used by the Managed Integrations app.

The row must include a generated token with expiry date and the correct location, all of which can be done through the OAuth flows provided by the Managed Integrations app. The new location must have its `merchantAlias` set explicitly, as this will not be done via the OAuth flows provided by the app. Once the row is correctly set up in the MySQL database, it will show up in the batch of reports that are generated.