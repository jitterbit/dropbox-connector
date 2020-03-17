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
import org.jitterbit.connector.sdk.ConnectionFactory;

import java.util.Locale;
import java.util.Map;

/**
 * Factory that creates a {@link DropboxConnection} instance.
 */
public class DropboxConnectionFactory implements DropboxConstants, ConnectionFactory {

  public static final DropboxConnectionFactory INSTANCE = new DropboxConnectionFactory();

  private DropboxConnectionFactory () {}

  /**
   * Returns a connection to a Dropbox endpoint, created from the specified properties.
   *
   * @param props properties for configuring and creating a Dropbox connection
   * @return the configured connection
   * @throws RuntimeException if the App Key or Access Token properties of the
   * specified properties are empty or null
   */
  @Override
  public Connection createConnection(Map<String, String> props) {
    String accessToken = props.get(ACCESS_TOKEN);
    String appKey = props.get(APP_KEY);
    String locale = !props.containsKey(LOCALE) ? Locale.getDefault().toString() : "EN_US";
    if (accessToken == null || accessToken.length() == 0) {
      throw new RuntimeException("Access Token property cannot be empty. " +
        "Specify the access token associated with the registered Dropbox application.");
    }
    if (appKey == null || appKey.length() == 0) {
      throw new RuntimeException("App Key property cannot be empty. " +
        "Specify the app key associated with the registered Dropbox application.");
    }
    return new DropboxConnection(appKey, accessToken, locale);
  }

  /**
   * Returns the pool size configuration.
   *
   * @return the pool size configuration
   */
  @Override
  public PoolSizeConfiguration getPoolSizeConfiguration() {
    return new PoolSizeConfiguration();
  }
}
