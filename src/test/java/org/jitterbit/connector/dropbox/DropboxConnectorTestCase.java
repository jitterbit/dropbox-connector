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

import org.jitterbit.connector.dropbox.activities.FetchFileActivity;
import org.jitterbit.connector.dropbox.activities.PutFileActivity;
import org.jitterbit.connector.sdk.Connection;
import org.jitterbit.connector.sdk.Discoverable;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Tests for the Dropbox connector.
 */
public class DropboxConnectorTestCase {

  public static String accessToken = "eD_tD5z4Gw8AAAAAAAAAD9lX1iDaz-wg9R-EEIMz1wO_VdADDJOG0QNAWcPk1j65";
  public static String appKey = "ub8lvcovida9s2u";

  @org.junit.Test
  public void testConnectionNegative01() {
    try {
      DropboxConnection connection = createConnection(null, "");
      connection.open();
      Assert.assertTrue("Expected an exception", false);
    } catch (Exception x) {
      Assert.assertTrue(true);
    }
  }

  @org.junit.Test
  public void testConnectionNegative02() {
    try {
      DropboxConnection connection = createConnection("", null);
      connection.open();
      Assert.assertTrue("Expected an exception", false);
    } catch (Exception x) {
      Assert.assertTrue(true);
    }
  }

  @org.junit.Test
  public void testConnectionNegative03() {
    try {
      DropboxConnection connection = createConnection("", "");
      connection.open();
      Assert.assertTrue("Expected an exception", false);
    } catch (Exception x) {
      Assert.assertTrue(true);
    }
  }

  @org.junit.Test
  public void testConnectionPositive01() {
    try {
      DropboxConnection connection = createConnection(accessToken, appKey);
      connection.open();
      Assert.assertTrue(true);
    } catch (Exception x) {
      Assert.assertTrue(x.getMessage(), false);
    }
  }

  @Test
  public void testActivitySchemaResponseForFetchFilePositive() throws Exception {
    JitterbitActivity activity = new FetchFileActivity.FetchFileActivityFactory().createActivity();
    ActivityFunctionParameters asreq = new ActivityFunctionParameters();
    Discoverable.DiscoverContextRequest<ActivityFunctionParameters> activitySchemaReq =
        new MockConnectorEngine.MockDiscoverContextRequest<>(asreq, newEndpointParams());

    ActivityRequestResponseMetaData schemaRsp = activity.getActivityRequestResponseMetadata(activitySchemaReq);
    Assert.assertNull(schemaRsp.getRequestSchema());
    Assert.assertNotNull(schemaRsp.getResponseSchema());
    Assert.assertEquals(schemaRsp.getResponseSchema().getContent(),
        DropboxUtils.loadResource(null, "xsds/" + DropboxConnector.FETCH_FILE_RSP_XSD));
  }

  @Test
  public void testActivitySchemaResponseForPutFilePositive() throws Exception {
    JitterbitActivity activity =
        new PutFileActivity.PutFileActivityFactory().createActivity();
    ActivityFunctionParameters asreq = new ActivityFunctionParameters();
    Discoverable.DiscoverContextRequest<ActivityFunctionParameters> activitySchemaReq =
        new MockConnectorEngine.MockDiscoverContextRequest<>(asreq, newEndpointParams());
    ActivityRequestResponseMetaData schemaRsp = activity.getActivityRequestResponseMetadata(activitySchemaReq);

    Assert.assertNotNull(schemaRsp.getRequestSchema());
    Assert.assertEquals(schemaRsp.getRequestSchema().getContent(),
        DropboxUtils.loadResource(null, "xsds/" + DropboxConnector.PUT_FILE_REQ_XSD));

    Assert.assertNotNull(schemaRsp.getResponseSchema());
    Assert.assertEquals(schemaRsp.getResponseSchema().getContent(),
        DropboxUtils.loadResource(null, "xsds/" +  DropboxConnector.PUT_FILE_RSP_XSD));
  }


  public static Map<String, String> newEndpointParams() {
    Map<String, String> props = new HashMap<>();
    props.put(DropboxConstants.ACCESS_TOKEN, accessToken);
    props.put(DropboxConstants.APP_KEY, appKey);
    return props;
  }

  private DropboxConnection createConnection(String accessToken, String appKey) {
    Map<String, String> props = new HashMap<>();
    props.put(DropboxConstants.ACCESS_TOKEN, accessToken);
    props.put(DropboxConstants.APP_KEY, appKey);
    DropboxConnection connection =
        (DropboxConnection) DropboxConnector.INSTANCE.getConnectionFactory().createConnection(props);
    return connection;
  }

  private Connection newConnection(Map<String, String> params) {
    return new DropboxConnector.DropboxConnectorFactory().create().getConnectionFactory().createConnection(params);
  }

  private JitterbitActivity.ExecutionContext createExecutionContext(Map<String, String> connectionEndpoint,
      Map<String, String> functionParams) {
    return new MockConnectorEngine.MockExecutionContext(connectionEndpoint, functionParams);
  }

  private static Logger logger = Logger.getLogger(DropboxConnectorTestCase.class.getName());
}
