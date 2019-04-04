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

import org.jitterbit.connector.dropbox.activities.FetchFileActivity;
import org.jitterbit.connector.sdk.BaseJitterbitConnector;
import org.jitterbit.connector.sdk.ConnectionFactory;
import org.jitterbit.connector.sdk.Discoverable;
import org.jitterbit.connector.sdk.JitterbitConnector;
import org.jitterbit.connector.sdk.annotation.Connector;
import org.jitterbit.connector.sdk.exceptions.TestQueryException;
import org.jitterbit.connector.sdk.metadata.DiscoverableField;
import org.jitterbit.connector.sdk.metadata.DiscoverableObjectMetaData;
import org.jitterbit.connector.sdk.metadata.DiscoverableObjectMetaDataRequest;
import org.jitterbit.connector.sdk.metadata.ObjectName;
import org.jitterbit.connector.sdk.metadata.ObjectSchemaMetaData;
import org.jitterbit.connector.sdk.metadata.SchemaMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Dropbox Connector has four activities (functions): fetch, get, process, and put.
 *
 * <ul>
 *   <li>Fetch File: Retrieves a file from a Dropbox location</li>
 *   <li>Get File: Gets a file from a Dropbox location, after presenting possible files</li>
 *   <li>Process File: Retrieves a file from Dropbox and parses it using a specified schema</li>
 *   <li>Put File: Uploads a file to a Dropbox location</li>
 * </ul>
 *
 */
@Connector(factory = DropboxConnector.DropboxConnectorFactory.class)
public class DropboxConnector extends BaseJitterbitConnector implements DropboxConstants {

  public static final DropboxConnector INSTANCE = new DropboxConnector();

  static {
    connectionFactory = DropboxConnectionFactory.INSTANCE;
  }

  @Override
  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  public String getName() {
    return CONNECTOR_NAME;
  }

  private static ConnectionFactory connectionFactory;

  /**
   * A {@link JitterbitConnector} factory for a Dropbox connector.
   */
  public static class DropboxConnectorFactory implements JitterbitConnector.Factory {
    @Override
    public JitterbitConnector create() {
      return DropboxConnector.INSTANCE;
    }
  }

  private static Logger logger = Logger.getLogger(FetchFileActivity.class.getName());
}
