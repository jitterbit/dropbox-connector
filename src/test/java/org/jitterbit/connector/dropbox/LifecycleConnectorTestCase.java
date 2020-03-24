/*
 * Copyright © 2018-2020 Jitterbit, Inc.
 *
 * Licensed under the JITTERBIT MASTER SUBSCRIPTION AGREEMENT
 * (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * https://www.jitterbit.com/cloud-eula
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.jitterbit.connector.dropbox;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.jitterbit.connector.sdk.DeployedEntity;
import org.jitterbit.connector.sdk.DeployedEntity.ActivityEntity;
import org.jitterbit.connector.sdk.DeployedEntity.EndpointEntity;
import org.jitterbit.connector.sdk.DeployedEntity.OperationEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Tests for the Dropbox connector.
 */
public class LifecycleConnectorTestCase {

  public void setup() {}

  public void tearDown() {}

  private DeployedEntity create(String filename, boolean delete) throws Exception {
    Gson gson = new Gson();
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
    if (is == null) {
      logger.info("Could not load json "  + filename);
    }
    String json = IOUtils.toString(is);
    JsonObject entity = gson.fromJson(json, JsonObject.class);
    JsonElement endpoint = null;
    JsonElement operation = null;
    JsonElement activity = null;
    DeployedEntity d = new DeployedEntity();
    if (!delete) {
      if (entity.get("addedOrModifiedItems").getAsJsonObject().get("endpoints").getAsJsonArray().size() > 0) {
        endpoint = entity.get("addedOrModifiedItems").getAsJsonObject().get("endpoints").getAsJsonArray().get(0);
      }
      if (entity.get("addedOrModifiedItems").getAsJsonObject().get("activities").getAsJsonArray().size() > 0) {
        activity = entity.get("addedOrModifiedItems").getAsJsonObject().get("activities").getAsJsonArray().get(0);
      }
      if (entity.get("addedOrModifiedItems").getAsJsonObject().get("operations").getAsJsonArray().size() > 0) {
        operation = entity.get("addedOrModifiedItems").getAsJsonObject().get("operations").getAsJsonArray().get(0);
      }
      d.setEndpoint(gson.fromJson(endpoint, EndpointEntity.class));
      d.setActivity(gson.fromJson(activity, ActivityEntity.class));
      d.setOperation(gson.fromJson(operation, OperationEntity.class));
      d.setEnvId(entity.get("envId").getAsLong());
    } else {
      if (entity.get("deletedItems").getAsJsonObject().get("deletedEndpointIds").getAsJsonArray().size() > 0) {
        endpoint = entity.get("deletedItems").getAsJsonObject().get("deletedEndpointIds").getAsJsonArray().get(0);
        EndpointEntity e = new EndpointEntity();
        e.setId(gson.fromJson(endpoint, DeployedEntity.Id.class));
        d.setEndpoint(e);
      }
      if (entity.get("deletedItems").getAsJsonObject().get("deletedActivityIds").getAsJsonArray().size() > 0) {
        activity = entity.get("deletedItems").getAsJsonObject().get("deletedActivityIds").getAsJsonArray().get(0);
        ActivityEntity e = new ActivityEntity();
        e.setId(gson.fromJson(activity, DeployedEntity.Id.class));
        d.setActivity(e);
      }
      if (entity.get("deletedItems").getAsJsonObject().get("deletedActivityIds").getAsJsonArray().size() > 0) {
        operation = entity.get("deletedItems").getAsJsonObject().get("deletedActivityIds").getAsJsonArray().get(0);
        OperationEntity e = new OperationEntity();
        e.setId(gson.fromJson(operation, DeployedEntity.Id.class));
        d.setOperation(e);
      }
      d.setEnvId(entity.get("envId").getAsLong());
    }

    logger.info("Deploying to environment " + d.getEnvId());
    return d;
  }

  private synchronized HttpServer createServer(int port, HttpHandler callback) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);
    server.createContext("/jitterbit_sap_destination", callback);
    return server;
  }

  private static Logger logger = Logger.getLogger(ConnectorTestCase.class.getName());
}
