# Building and Packaging the Jitterbit Harmony Dropbox Connector

To build using Maven, edit your `pom.xml` and add this to the dependency section:

```xml
<dependency>
    <groupId>com.jitterbit</groupId>
    <artifactId>jitterbit-connector-sdk</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/jitterbit-connector-sdk-1.0.0.jar</systemPath>
</dependency>
```

## Manifest

As part of the Maven build, these attributes must be present as part of the `MANIFEST.MF` to
indicate that this is a Jitterbit Harmony connector:

```shell
Author: <author-name>
Version: <version>
Jitterbit-Connector: true
Jitterbit-Connector-Key: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
Jitterbit-Connector-Secret: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
Jitterbit-Connector-Endpoint-Id: XXXXX
Jitterbit-Activity-Id-fetch: XXXXX
Jitterbit-Activity-Id-put: XXXXX
Jitterbit-Connector-UI: adapter.json
Class-Path: lib/dropbox-core-sdk-3.0.6.jar
```

Where:

- `Author`: Replace `<author-name>` with your name.
- `Version`: Set an appropriate version identifier.
- `Jitterbit-Connector`: Set to `true`, to indicate that this bundle is a Jitterbit Harmony connector.
- `Jitterbit-Connector-*`: See the [README.md](README.md) file for details on obtaining these attributes using a REST
  API.
- `Jitterbit-Connector-Factory-Class`: (Optional) Provides the factory class that creates an instance of
  `JitterbitConnector`; if not provided, then you are required to use an `@Connector` annotation on the entry
  class that implements the `JitterbitConnector` interface or extends the abstract class `BaseJitterbitConnector`.
- `Jitterbit-Activity-*`: See the [README.md](README.md) file for details on obtaining these attributes using a REST
  API.
- `Jitterbit-Connector-UI`: (Optional) Provides the file that contains the UI declarations for the connector. Default
  name is `adapter.json`.
- `Class-Path`: A list of space-separated URLs that should include all of the third-party JARs that make up the
  connector. In this example, it contains the Dropbox SDK JAR.

For the IDs, the values are obtained using a REST API as described in the [README.md](README.md). In this example,
the connector endpoint ID is `20001`, the Fetch File activity has been assigned `20002`, and the Put File activity
has been assigned `20003`. (The REST API will have returned an ending range activity ID of `20100`.) When building
the connector, replace these values with the values returned by the REST API.

**NOTE:** Incrementing the version will cause the connector to be reloaded from the Private Agent. Use incrementing of
the version during development to ensure that the correct version is used by the Cloud Studio.

## Example

Here is an example fragment for a `pom.xml` that will include the attributes of the `MANIFEST.MF` file in the final
JAR file:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-dependency-plugin</artifactId>
  <executions>
    <execution>
      <id>copy-dependencies</id>
      <phase>prepare-package</phase>
      <goals>
        <goal>copy-dependencies</goal>
      </goals>
      <configuration>
        <includeArtifactIds>dropbox-core-sdk</includeArtifactIds>
          <outputDirectory>${project.build.directory}/classes/lib</outputDirectory>
          <overWriteReleases>false</overWriteReleases>
          <overWriteSnapshots>false</overWriteSnapshots>
          <overWriteIfNewer>true</overWriteIfNewer>
      </configuration>
    </execution>
  </executions>
</plugin>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <version>2.6</version>
  <configuration>
    <archive>
      <manifestFile>src/main/resources/META-INF/MANIFEST.MF</manifestFile>
    </archive>
   </configuration>
</plugin>
```


## For More Information

See the [README.md](README.md) in this directory for additional information.


## Copyright

Copyright © 2018-2019 Jitterbit, Inc.

Licensed under the JITTERBIT MASTER SUBSCRIPTION AGREEMENT (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at

https://www.jitterbit.com/cloud-eula

See the License for the specific language governing permissions and limitations under the License.
