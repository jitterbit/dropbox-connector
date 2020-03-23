/*
 * Copyright © 2018-2019 Jitterbit, Inc.
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the Dropbox connector.
 */
public class ConnectorTestCase {

  @Test
  public void testConnection01() throws Exception {
    DropboxConnector connector = DropboxConnector.INSTANCE;
    connector.testConnection(DropboxConnectorTestCase.newEndpointParams());
    Assert.assertTrue(true);
  }

}
