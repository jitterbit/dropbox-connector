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

import org.jitterbit.connector.sdk.Connection;
import org.jitterbit.connector.sdk.Discoverable;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.Payload;

import java.util.Map;

/**
 * Tests for the Dropbox connector.
 */
public class MockConnectorEngine {

  public static Connection newConnection(Map<String, String> params) {
    return new DropboxConnector.DropboxConnectorFactory().create().getConnectionFactory().createConnection(params);
  }

  public static class MockExecutionContext implements JitterbitActivity.ExecutionContext {

    public MockExecutionContext(Map<String, String> endpointParams, Map<String, String> functionParams) {
      this.connection = newConnection(endpointParams);
      this.functionParams = functionParams;
    }
    @Override
    public Connection getConnection() {
      return connection;
    }

    @Override
    public Map<String, String> getFunctionParameters() {
      return functionParams;
    }

    @Override
    public boolean persistenceRequired() {
      return false;
    }

    @Override
    public Payload getRequestPayload() {
      return reqPayload;
    }

    @Override
    public Payload getResponsePayload() {
      return rspPayload;
    }
    Connection connection;
    Map<String, String> endpointParams;
    Map<String, String> functionParams;
    Payload reqPayload = new Payload.StringPayload("");
    Payload rspPayload = new Payload.StringPayload("");
  }

  public static class MockDiscoverContextRequest<T> implements Discoverable.DiscoverContextRequest {

    public MockDiscoverContextRequest(T req, Map<String, String> endpointParams) {
      this.req = req;
      this.con = newConnection(endpointParams);
    }
    @Override
    public Object getRequest() {
      return req;
    }

    @Override
    public Connection getConnection() {
      return con;
    }

    private T req;
    private Connection con;
  }
}
