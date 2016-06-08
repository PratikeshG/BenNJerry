# Managed Integrations Runbook

### Recipes

##### Environment variables necessary for successful deployment
To successfully deploy the managed-integrations Mule project, the following environment variables must be defined:

- `mule.env` - the deployment environment. Possible values are “development”, “staging”, “production”
- `mule.decryptionKey` - the password used to decrypt encrypted properties. Currently in possession of the Sales Engineering team.

In AnyPoint Studio, these variables are defined in `mule-project.xml` under the “Environment variables” tab. On CloudHub, they are defined in the deployment console, under the “Properties” tab.

##### General deployment guidelines

- Check `mule-project.xml` to see which deployment environment the deploy should go to (`development`, `staging` or `production`).
- Check the `{environment}.mi.properties` file to see which flows are inactive. Generally, all flows except the ones necessary should be inactive.
- If deploying to Cloudhub, on production, the name of the deployment must be `managed-integrations-prod`; on staging, the name of the deployment must be `managed-integrations`.

##### Creating a new OAuth token for a deployment

New OAuth tokens can be created for apps by visiting `/createToken?deployment=[deployment]&client_id=[client_id]`, and substituting the appropriate values into the URI. The flow will trigger the correct state parameters to be passed, and will go to the correct callback address to process the result.

Currently, this cannot be done through CloudHub; this is because the Mule application has not yet been set up to run via HTTPS. To sidestep this, run a local application on the desired environment and visit the link.

##### Creating new flows

Which flows run is determined by the property `inactiveFlows`. If a flow is to run, the name of the flow must not be preceded by a value contained in the `inactiveFlows` property. For instance, if the value `abc` is contained in the `inactiveFlows` property, all flows with names beginning with `abc` will not run. Otherwise, they will run. However, flows that are not preceded by but do contain `abc` will run regardless; flows are only inactive if they start with a value in the `inactiveFlows` property.

##### Deploy a new properties file

To add a new properties file to the environment, first create a new `.properties` file in the `src/main/resources` directory. For every new set of properties to be added, three properties files will have to be added: one for development, one for staging, and one for production. Each properties file must be prefaced with `development`, `staging`, or `production`. The platform will automatically choose which properties file to deploy, based on the deployment environment.

To add the properties file to the list of properties files pulled into the environment, edit the `global.xml` Mule configuration file in the `src/main/app` directory. Edit the `Secure Property Placeholder` element to include the new properties files. If editing raw XML, find the `secure-property-placeholder:config` element and append `,${mule.env}.[chosen name of property file].properties` to the end of the `location` parameter. Once saved and redeployed, the properties defined in the new files will be available for reference.

##### Encrypting properties

To encrypt properties, open the chosen `.properties` file by right-clicking on it and selecting Open With -> Mule Properties Editor. If not already done, click on the “Configure encryption properties” button on the top toolbar to set the properties encryption password (currently in possession of the Sales Engineering team).

To encrypt a property, double-click on it, and on the pop-up, click “Encrypt”.

##### Using the Cloudhub object store

The only object store that will persist on Cloudhub has a partition name of `_defaultUserObjectStore`. To retrieve it, run the following the context of a Java class that implements `org.mule.api.lifecycle.Callable`:

```
ObjectStore<?> objectStore = eventContext
        .getMuleContext()
        .getRegistry()
        .lookupObject("_defaultUserObjectStore");
```

This is the only object store that will persist across deployments. All other object stores are non-persistent on Cloudhub.

If at all possible, avoid administering the object store from the Anypoint Console. The reason for this is because the “Empty” button deletes the entire object store with no confirmation. This is an inherently risky operation and should be avoided.

##### Creating queued-asynchronous processing strategies
Queued-asynchronous processing strategies serve different purposes in the Managed Integrations app; one function is to run flows that have been forked in parallel to decrease processing times. If configured incorrectly, flows that have queued-asynchronous processing strategies may produce the following exception:

```
org.mule.api.service.FailedToQueueEventException: Interrupted while queueing event for "SEDA Stage [flow name].stage1". Message payload is of type: [payload type]
```

Further investigation of this exception may reveal the following exception message:

```
The queue for 'SEDA Stage [flow name].stage1' did not accept new event within 30000 MILLISECONDS. Message payload is of type: [payload type]
```

This occurs when a message is queued by the flow, and all of threads in its allotted threadpool are busy processing other messages. When a message is queued in such a manner, it will wait for a configurable amount of time (default is 30,000 milliseconds) before throwing the above exception. Specifically, the message is waiting for a thread to become available, and the time it waits for is the "thread wait timeout" time.

Often, the flows utilizing the queued-asynchronous processing strategies are slow consumers, with each thread taking longer than 30 seconds to complete. If this is the case, the "thread wait timeout" setting must be increased. Specifically, the `threadWaitTimeout` attribute of the `queued-asynchronous-processing-strategy` element must be set to a higher value. This prevents the flow from timing out prematurely, but still provides a safeguard against processes that lock during execution.