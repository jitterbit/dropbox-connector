# Jitterbit Harmony Dropbox Connector


## Overview

The Jitterbit Harmony Dropbox Connector is accessed from the **Connectivity** tab of the design component palette:

![Connectivity tab](./assets/connectivity-tab.png)

This connector is used to first [configure a Dropbox connection](./connection.md), establishing access to an account
on the Dropbox server, and then used to configure one or more Dropbox activities associated with that connection as
either a source or target within an operation:

- **[Fetch File](./fetch-file-activity.md):** Retrieves data from a Dropbox connection and is intended to be used as
  a source in an operation.

- **[Get File](./get-file-activity.md):** Lets a user (at configuration time) select from different files associated
  with a path (a directory) at Dropbox and specify a schema. At runtime, the activity downloads the file and parses
  it using the specified schema. The activity is intended to be used as a source in an operation.

- **[Process File](./process-file-activity.md):** Retrieves data from a Dropbox connection, processes the file based
  on a specified schema, and is intended to be used as a source in an operation.

- **[Put File](./put-file-activity.md):** Inserts new data into a Dropbox connection and is intended to be used as
  a target in an operation.

The Dropbox connector uses the [Dropbox Java SDK API](https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.1.x/).
Refer to the SDK documentation for information on the schema fields.

The Dropbox connector requires the use of an agent version [10.0](https://success.jitterbit.com/display/DOC/10.0) or
higher.

Together, a specific Dropbox connection and its activities are referred to as a Dropbox endpoint. Once
a connection is configured, activities associated with the endpoint are available using the **Show** dropdown to
filter on **Endpoints**:

![Activities](./assets/dropbox-connection.png)


## Related Pages

- [Jitterbit Harmony Dropbox Connection](./connection.md)
- [Dropbox Registration](./registration.md)
- [Jitterbit Harmony Dropbox Fetch File Activity](./fetch-file-activity.md)
- [Jitterbit Harmony Dropbox Get File Activity](./get-file-activity.md)
- [Jitterbit Harmony Dropbox Process File Activity](./process-file-activity.md)
- [Jitterbit Harmony Dropbox Put File Activity](./put-file-activity.md)
