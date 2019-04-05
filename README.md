# Jitterbit Harmony Dropbox Connector

This is an example custom Jitterbit Harmony Connector that interacts with [Dropbox](http://www.dropbox.com), developed
using the [Dropbox Java SDK API](https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.1.x/) and
the [Jitterbit Harmony Connector SDK (v1.0.0)](https://developer.jitterbit.com/connector-sdk/javadocs/).

__IT IS NOT FOR PRODUCTION USE.__ It is intended as a starting point for developing custom Harmony Connectors. These
instructions assume that you are familiar with Jitterbit Harmony, Jitterbit Harmony Cloud Studio, Jitterbit Harmony
Private Agents, and Java development. If you are not familiar with Jitterbit Harmony, please see [our
documentation](http://success.jitterbit.com) for more information.


## Documentation

Complete documentation on the Dropbox connector and its activities is included in the [docs](./docs) directory.
See its [index page](./docs/index.md) for information on how to use the Dropbox connector inside Cloud Studio.

The documentation can also be used as a template for preparing the documentation of a custom connector.


## Prerequisite Requirements

- Register with the [Dropbox Developer Program](http://www.dropbox.com/developers), create
  an application, and retrieve its `app-key` and `access-token` keys.
- Register with [Jitterbit Harmony](https://login.jitterbit.com) and register a Private Agent.
- Install [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- Install [Maven](http://maven.apache.org/download.cgi).
- Install [Docker](https://www.docker.com/get-docker) (optional, but highly recommended).
- Install [Postman](https://www.getpostman.com/) (optional, but highly recommended).
- Download the [Jitterbit Harmony Docker Private Agent image](https://hub.docker.com/r/jitterbit/agent/) using

  `$ docker pull jitterbit/agent:latest`

- Clone this example (see [Cloning the Dropbox Connector Repository](#cloning-the-dropbox-connector-repository)),
which includes a copy of the Connector SDK library.


## Connector Concepts

- **Connector:** A collection of activities that interacts with a system. As part of the connector's packaging, it
  provides a file which declares the UI of the interface for entering user-provided input to create an authenticated
  connection. A connector must implement the `JitterbitConnector` interface.
- **Connection:** Authenticated access to a data resource that has been configured using a connector.
- **Activity:** An interaction with a connection that is added to a Harmony operation and configured as a source or
  target. An activity represents the smallest unit of execution within an operation. It matches to a node within a Cloud
  Studio operation. Every activity needs to implement a `JitterbitActivity` interface, with these two parts:
  - **`Configuration`:** Performed by an end-user using the Cloud Studio UI.
  - **`Execution`:** Executes an activity according to the user configuration.
- **Endpoint:** A specific connection and its activities. An endpoint represents a connection configured to a specific
  system, with endpoint information provided by an end-user using the Cloud Studio UI.
- **Payload:** Represents a request or response (input or output) of an activity during execution; maps to a `Payload`
  interface.
- **Request or Response:** The request or response of an activity developed with the Connector SDK (v1.0.0) will
  be in XML format only.
- **`adapter.json`:** A JSON file containing the declarative Cloud Studio UI associated with an endpoint and the
  activities that make up a connector. As the developer, you provide the parameters that need to be configured and the
  types associated with each parameter. For more complex interfaces, there are a set of widgets available as part of the
  Cloud Studio UI.

These terms are slightly different than those used in the Cloud Studio documentation, as that documentation is designed
for end-users of Cloud Studio rather than developers of connectors.


## Registering, Building, and Running

Follow these steps to register, build, and run the Dropbox Connector:

- [Registering the Connector with Jitterbit Harmony](#registering-the-connector-with-jitterbit-harmony)
- [Cloning the Dropbox Connector Repository](#cloning-the-dropbox-connector-repository)
- [Building and Packaging](#building-and-packaging)
- [Deploying and Running](#deploying-and-running)


### Registering the Connector with Jitterbit Harmony

All custom connectors need to be registered with Jitterbit Harmony _prior_ to being built.

You'll need to obtain from Jitterbit Harmony, for the connector, these values:
- Connector key
- Connector secret
- Endpoint entity ID
- Starting activity ID
- Ending activity ID

To obtain these, use the Postman collection included in this example at
`postman/JitterbitConnectorManagerAPI.collection.json`.

Import the environment variables from `postman/jitterbit-env-variable-postman.json` into Postman and associate them
with the collection before running the REST API.

Update the email, password, and connector name environment variables appropriately. The connector name and version should
not require changing. (In any case, the value the connector name is registered under must match the value in the
`adapter.json` used to describe the UI.)

Now, from within Postman, first run the `Log In User` and then run the `Register a Connector` APIs.

(The other APIs in the collection can be used to list and delete existing connectors that you have registered, and to
confirm the validity of a connector's key and secret.)

The response returned by the `Register a Connector` API contains the key-values that are needed for the connector's
manifest to complete the registration of the connector. You should receive a response such as:

```json
{
    "status": true,
    "operation": "Add connector",
    "key": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    "secret": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    "endpointEntityId": "20101",
    "functionEntityStartId": "20102",
    "functionEntityEndId": "20200"
}
```

As can be seen from this example, your connector has been registered, has an entity ID of `20101`, and can have
activity entity IDs ranging from `20102` through `20200`.


| `Register a Connector` API | Manifest File Keys                          |
| -------------------------- | ------------------------------------------- |
| `key`                      | `Jitterbit-Connector-Key`                   |
| `secret`                   | `Jitterbit-Connector-Secret`                |
| `endpointEntityId`         | `Jitterbit-Connector-Endpoint-EntityTypeId` |
| `functionEntityStartId`    | `Jitterbit-Activity-EntityTypeId-fetch`     |
| `functionEntityEndId`      | `Jitterbit-Activity-EntityTypeId-put`       |

The key and secret returned will become, following the table above, the `Jitterbit-Connector-Key` and
`Jitterbit-Connector-Secret` in the manifest for the container.

The connector has been allocated an endpoint ID and a range of activity (function) endpoint IDs, from the start
through to the finish. Because the Dropbox Connector has only two activities (fetch and put), you can use the start
and end values with these two activities. (If you have more than two activities, you would assign individual IDs to each
activity that fall in your assigned range.)


### Cloning the Dropbox Connector Repository

You can use these commands to clone and start in this repository:

    $ git clone https://github.com/jitterbit/dropbox-connector.git
    $ cd dropbox-connector


### Building and Packaging

As part of the build and packaging process, the bundle JAR that contains the connector implementation will need to
include attributes specific to the connector(s) within the `MANIFEST.MF` file. The key-values obtained in the
registration process need to be included in the manifest, as described above. For additional details, check the
[PACKAGING.md](PACKAGING.md).

Skipping the details, and to directly build the Dropbox Connector, run this command:

    $ mvn jaxb2:xjc compile install


### Deploying and Running

The Docker image of the `Private Agent` allows you to mount a host directory to a container directory such as
`/mnt/connectors`. To deploy the Dropbox Connector JAR after it has been built, you can run the agent as a Docker
container with the build `target` directory mounted as the connector directory. Within the Docker container, the agent
will scan the `/mnt/connectors` directory for JAR files that contain a connector implementation (only one connector
implementation is allowed in each JAR file).

For example:

    $ docker run -ti --name=jitterbit-agent -p 3000:3000 \
                 -v "$(pwd)/dropbox-connector/target:/mnt/connectors" \
                 --env-file "$(pwd)/private-agent.env" \
                 jitterbit/agent:latest

where `private-agent.env` contains the environment variables used for connecting to the Jitterbit Harmony Platform.
Substitute the appropriate values:

```Shell
   HARMONY_USERNAME="<email-address>"
   HARMONY_PASSWORD="<password>"
   HARMONY_ORG="<your-organization-name>"
   HARMONY_AGENT_GROUP="<your-agent-group-name>"
   HARMONY_AGENT="<your-agent-name>"
```

In a different command prompt (or Power Shell, for Windows OS systems) you can check that the container is up by
using:

    $ docker ps -a

    CONTAINER ID  IMAGE                   COMMAND                  CREATED        STATUS        PORTS                                                         NAMES
    x3160xx34840  jitterbit/agent:latest  "./bootstrap.sh ./bin…"  2 minutes ago  Up 2 minutes  3000/tcp, 46908-46909/tcp, 46912/tcp, 0.0.0.0:3000->30000/tcp  jitterbit-agent

**NOTE:** Once the container is up and passes the health check, you will need to log in to the Harmony Portal and then
open the Management Console to check if the agent has been registered and is running with the platform. Check the
[Harmony documentation](https://success.jitterbit.com/display/DOC/Agents+%3E+Agents) for details.

In a different command prompt (or Power Shell, for Windows OS systems), you can access the container using a
command to open a shell in the Docker image:

    $ docker exec -ti jitterbit-agent /bin/bash

    root@b3160de34840:/opt/jitterbit#

At this point, you have full access to the agent container. You can stop and restart the agent using the
`/opt/jitterbit/bin/jitterbit` utility. For a help message, from within the container use:

    # /opt/jitterbit/bin/jitterbit help

From within the container, tailing the Tomcat logs to check the connector logs:

    # tail -f tomcat/logs/catalina.out

    27-Feb-2018 01:37:34.937 INFO [localhost-startStop-1] org.jitterbit.server.connector.ConnectorContextServletListener.contextInitialized Servlet Context /axis
    2018-02-27 01:37:34,940  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorLoader:44 - Loading connectors...
    2018-02-27 01:37:34,942  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorLoader:125 - Connector Directory: /opt/jitterbit/Connectors
    2018-02-27 01:37:34,977  WARN com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:70 - Could not load Jitterbit Connector via manifest.mf; no Main-Class or Jitterbit-Connector-Factory-Class attributes defined.
    2018-02-27 01:37:34,977  WARN com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:71 - Falling back to annotation system
    2018-02-27 01:37:34,978  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:204 - Temporary Directory for undeploying connectors bundle: /opt/jitterbit/tomcat/temp
    2018-02-27 01:37:35,144  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:149 - Loading annotated class java.lang.Class
    2018-02-27 01:37:35,144  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:161 - Register Activity Factory with id: 10014 name: fetch factory: org.jitterbit.connector.dropbox.activities.FetchFileActivity$FetchFileActivityFactory
    2018-02-27 01:37:35,145  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:161 - Register Activity Factory with id: 10011 name: get factory: org.jitterbit.connector.dropbox.activities.GetFileActivity$GetFileActivityFactory
    2018-02-27 01:37:35,145  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:161 - Register Activity Factory with id: 10012 name: put factory: org.jitterbit.connector.dropbox.activities.PutFileActivity$PutFileActivityFactory
    2018-02-27 01:37:35,145  INFO com.jitterbit.integration.server.api.ws.connectorframework.ConnectorClassLoader:161 - Register Activity Factory with id: 10013 name: syncUpFile factory: org.jitterbit.connector.dropbox.activities.GetFileActivity$GetFileActivityFactory
    27-Feb-2018 01:37:35.163 INFO [localhost-startStop-1] org.jitterbit.connector.sdk.BaseJitterbitConnector.onInit onInit() connector name: DropBox

If you see messages similar to the above in the logs, it means that the Dropbox Connector has been successfully
started. You can now log in to the Harmony Portal, open Cloud Studio, and begin using the connector in new projects.

Once you create a new project within the organization that the Private Agent is associated with, you should see the
Dropbox Connector in the palette. You can then configure the connector as any other connector in Cloud Studio.


### Useful Commands

Restarting the Private Agent from within the container:

    # /opt/jitterbit/bin/jitterbit restart

You can also restart individual components that make up the agent using the same utility. Help for the utility is
available using:

    # /opt/jitterbit/bin/jitterbit help


### Remote Debugging

You can remote debug the connector using an IDE (Eclipse or IntelliJ) that attaches to the Docker agent container.

To do so, add an environment variable, either in an environment file passed to the Docker command or by adding an option
`-e` to the Docker command:

    -e "CATALINA_OPTS='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'"

Add the port `5005` to the Docker command by adding an option `-p 5005:5005` to the command.

Start the agent as described above, and then you should be able to connect your debugging session to the agent on
port `5005`.


## Support and Questions

If you have support issues, questions, or comments about either the example Dropbox Connector or the Connector SDK, get
in touch with our expert support team by submitting a support case.

See [Getting Support](https://success.jitterbit.com/display/DOC/Getting+Support).


## Copyright

Copyright © 2018-2019 Jitterbit, Inc.

Licensed under the JITTERBIT MASTER SUBSCRIPTION AGREEMENT (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at

https://www.jitterbit.com/cloud-eula

See the License for the specific language governing permissions and limitations under the License.
