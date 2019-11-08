/*
 * Copyright © 2018 Jitterbit, Inc.
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

import org.jitterbit.connector.dropbox.activities.ProcessFileActivity;
import org.jitterbit.connector.sdk.JitterbitConnector;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Test for the Dropbox Connector.
 */
public class ConnectorTestCase {


  @org.junit.Before
  public void setUp() {
    JitterbitConnector.CONNECTOR_CONTEXT.set(new MockConnectorContext());
  }

  @Test
  public void testConnection01() throws Exception {
    DropboxConnector connector = DropboxConnector.INSTANCE;
    connector.testConnection(DropboxConnectorTestCase.newEndpointParams());
    JitterbitConnector.ConnectorContext ctx = JitterbitConnector.CONNECTOR_CONTEXT.get();
    Logger.getLogger(ConnectorTestCase.class.getName()).info("Connection pass for prjId: " + ctx.getProjectId());
    Assert.assertTrue(true);
  }

  public static class MockConnectorContext implements JitterbitConnector.ConnectorContext {
    @Override
    public String getEnvironmentId() {
      return "envId";
    }

    @Override
    public String getProjectId() {
      return "prjId";
    }

    public Logger getLogger() {
      return Logger.getLogger(JitterbitConnector.ConnectorContext.class.getName());
    }
  }

}
