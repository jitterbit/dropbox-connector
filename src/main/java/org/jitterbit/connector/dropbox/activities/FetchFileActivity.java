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

package org.jitterbit.connector.dropbox.activities;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import org.jitterbit.connector.dropbox.DropboxConnection;
import org.jitterbit.connector.dropbox.DropboxConstants;
import org.jitterbit.connector.dropbox.DropboxUtils;
import org.jitterbit.connector.dropbox.Messages;
import org.jitterbit.connector.dropbox.schema.FetchFileResponse;
import org.jitterbit.connector.sdk.Discoverable;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.annotation.Activity;
import org.jitterbit.connector.sdk.exceptions.ActivityExecutionException;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.jitterbit.connector.sdk.util.Utils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 * Implements the Fetch File activity of a Dropbox connector. This activity
 * downloads the metadata and content associated with a specified file.
 * <p>
 * When the activity is being executed by the runtime, the <code>filename</code>
 * and <code>folder</code> parameters can be obtained as part of the configuration
 * of the function that is exposed in the Jitterbit Harmony Cloud Studio UI from the
 * execution context: see {@link #execute(ExecutionContext)}. These two parameters
 * are declared as part of the <code>adapter.json</code> file and configured in the
 * Cloud Studio UI by the end user.
 * </p>
 * <p>
 * The <code>request</code>, conforming with the
 * <code>resources/xsds/fetch-file-request.xsd</code> XML Schema, represents the input
 * associated to this activity. An end user will have the option to map data
 * to it that describes the file to be downloaded.
 * </p>
 * <p>
 * The response of this activity will be written to the response payload (see
 * {@link ExecutionContext#getResponsePayload()} as an XML document that conforms
 * with the <code>resources/xsds/fetch-file-response.xsd</code>.
 * </p>
 */
@Activity(
    name = DropboxConstants.FETCH_FILE,
    factory = FetchFileActivity.FetchFileActivityFactory.class)
public class FetchFileActivity extends BaseDropboxActivity {

  FetchFileActivity() {
  }

  @Override
  public String getName() {
    return FETCH_FILE;
  }

  /**
   * Fetches a file from Dropbox. The folder and filename properties are provided
   * as part of the <code>context</code>.
   *
   * @param context the context for the activity
   * @throws ActivityExecutionException if there is an error while executing the activity
   */
  @Override
  public void execute(JitterbitActivity.ExecutionContext context) throws ActivityExecutionException {
    logger.info("Executing Activity: " + getName());
    String path = "";
    try {
      String folder = context.getFunctionParameters().get("folder");
      String fileName = context.getFunctionParameters().get("fileName");
      if (folder == null || folder.length() == 0) {
        folder = "/";
      }
      if (folder.endsWith("/")) {
        path = folder + fileName;
      } else {
        path = folder + "/" + fileName;
      }
      logger.info("Fetching: " + path);
      DropboxConnection connection = (DropboxConnection) context.getConnection();
      DbxClientV2 client = connection.getClient();

      DbxDownloader<FileMetadata> result = client.files().download(path);
      FetchFileResponse opRsp = new FetchFileResponse();

      opRsp.setName(result.getResult().getName());
      opRsp.setClientModified(Utils.convertDateTo(result.getResult().getClientModified()));
      opRsp.setServerModified(Utils.convertDateTo(result.getResult().getServerModified()));
      opRsp.setRev(result.getResult().getRev());
      opRsp.setSize(BigInteger.valueOf(result.getResult().getSize()));

      if (result.getResult().getSharingInfo() != null) {
        FetchFileResponse.SharingInfo sharing = new FetchFileResponse.SharingInfo();
        sharing.setReadOnly(result.getResult().getSharingInfo().getReadOnly());
        sharing.setParentSharedFolderId(result.getResult().getSharingInfo().getParentSharedFolderId());
        sharing.setModifiedBy(result.getResult().getSharingInfo().getModifiedBy());
        opRsp.setSharingInfo(sharing);
      }

      // Download content
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      result.download(baos);
      opRsp.setContent(baos.toByteArray());

      // Marshall the response to the response payload output stream
      DropboxUtils.marshall(FetchFileResponse.class, opRsp, context.getResponsePayload().getOutputStream());
    } catch (Throwable x) {
      x.printStackTrace();
      logger.severe("Dropbox " + x.getLocalizedMessage());
      throw new ActivityExecutionException(Messages.DROPBOX_CODE03,
          Messages.getMessage(Messages.DROPBOX_CODE03_MSG, new Object[]{path}), x);
    } finally {
      try {
        context.getResponsePayload().getOutputStream().flush();
        context.getResponsePayload().getOutputStream().close();
      } catch (Exception x) {
        logger.warning(x.getMessage());
      }
    }
  }

  /**
   * Returns the request/response associated with this activity. For the <code>FETCH</code>
   * activity, only the response data structure is being returned.
   *
   * @param activityConfigProps the properties for the activity
   * @return the response metadata of the activity
   * @throws DiscoveryException if there is an error while configuring the activity
   */
  @Override
  public ActivityRequestResponseMetaData
        getActivityRequestResponseMetadata(Discoverable.DiscoverContextRequest<ActivityFunctionParameters>
          activityConfigProps)
      throws DiscoveryException {
    ActivityRequestResponseMetaData activitySchemaResponse = new ActivityRequestResponseMetaData();
    try {
      DropboxUtils.setRequestResponseSchemas(activitySchemaResponse,
          "xsds",
          null,
          FETCH_FILE_RSP_XSD);
      activitySchemaResponse.setResponseRootElement(QName.valueOf("{" + FETCH_FILE_NAMESPACE + "}" +
         FETCH_FILE_RSP_ROOT));
      return activitySchemaResponse;
    } catch (Exception x) {
      logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
      throw new Discoverable.DiscoveryException(Messages.DROPBOX_CODE02,
          Messages.getMessage(Messages.DROPBOX_CODE02_MSG, new Object[]{getName()}), x);
    }
  }

  /**
   * Factory for creating the activity.
   */
  public static class FetchFileActivityFactory implements JitterbitActivity.Factory {
    @Override
    public JitterbitActivity createActivity() {
      return new FetchFileActivity();
    }
  }

  private static Logger logger = Logger.getLogger(FetchFileActivity.class.getName());
}
