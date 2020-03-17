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

package org.jitterbit.connector.dropbox.activities;

import org.jitterbit.connector.dropbox.DropboxConstants;
import org.jitterbit.connector.sdk.DeployedEntity;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;

import java.util.logging.Logger;

/**
 * Abstract class that all Dropbox connector activities extend.
 *
 */
public abstract class BaseDropboxActivity implements JitterbitActivity, DropboxConstants {

  public BaseDropboxActivity() {
    state = State.STARTED;
  }

  @Override
  public ActivityRequestResponseMetaData
        getActivityRequestResponseMetadata(DiscoverContextRequest<ActivityFunctionParameters> activityConfigProps)
      throws DiscoveryException {
    return null;
  }

  @Override
  public void onStart() {
    logger.info("onStart() - " + entity.toString());
  }

  @Override
  public void onStop() {
    logger.info("onStop() - " + entity.toString());
  }

  @Override
  public void onDeploy(DeployedEntity entity) {
    this.entity = entity;
    logger.info("onDeploy() - " + entity.toString());
  }

  @Override
  public void onUnDeploy(DeployedEntity entity) {
    logger.info("onUnDeploy() - " + entity.toString());
  }

  @Override
  public State state() {
    return state;
  }

  protected String getOperationGuid() {
    if (entity != null &&
        entity.getOperation() != null &&
        entity.getOperation().getGuid() != null) {
      return entity.getOperation().getGuid();
    }
    return "";
  }
  protected State state = State.INIT;

  protected DeployedEntity entity;

  private static Logger logger = Logger.getLogger(BaseDropboxActivity.class.getName());
}
