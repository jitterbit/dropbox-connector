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

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.WriteMode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jitterbit.connector.dropbox.DropboxConnection;
import org.jitterbit.connector.dropbox.DropboxConstants;
import org.jitterbit.connector.dropbox.DropboxUtils;
import org.jitterbit.connector.dropbox.Messages;
import org.jitterbit.connector.dropbox.schema.PutFileRequest;
import org.jitterbit.connector.dropbox.schema.PutFileResponse;
import org.jitterbit.connector.sdk.Discoverable;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.annotation.Activity;
import org.jitterbit.connector.sdk.exceptions.ActivityExecutionException;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.jitterbit.connector.verbose.logging.dropbox.VerboseLogger;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

/**
 * Implements the Put File activity of a Dropbox connector. This activity
 * saves (puts) a file to Dropbox. The activity has both a request and a response.
 * <p>
 * When the activity is being executed by the runtime, the <code>filename</code>
 * and <code>folder</code> parameters can be obtained as part of the configuration
 * of the function that is exposed in the Jitterbit Harmony Cloud Studio UI from the
 * execution context: see {@link #execute(ExecutionContext)}. These two parameters
 * are declared as part of the <code>adapter.json</code> file and are configured in the
 * Cloud Studio UI by the end user.
 * </p>
 * <p>
 * The <code>request</code>, conforming with the
 * <code>resources/xsds/put-file-request.xsd</code> XML Schema, represents the input
 * associated to this activity. An end-user will have the option to map data
 * (including the content) to it that needs to be saved.
 * </p>
 * <p>
 * The response of this activity will be written to the response payload (see
 * {@link ExecutionContext#getResponsePayload()} as an XML document that conforms
 * with the <code>resources/xsds/put-file-response.xsd</code>.
 * </p>
 */
@Activity(
    name = DropboxConstants.PUT_FILE,
    factory = PutFileActivity.PutFileActivityFactory.class)
public class PutFileActivity extends BaseDropboxActivity {

  PutFileActivity() {
  }

  @Override
  public String getName() {
    return PUT_FILE;
  }

  /**
   * Puts a file to Dropbox. The folder and filename properties are provided
   * as part of the <code>context</code>.
   *
   * @param context the context for the activity
   * @throws ActivityExecutionException if there is an error while executing the activity
   * @throws RuntimeException if there is an error while closing the activity
   */
  @Override
  public void execute(ExecutionContext context) throws ActivityExecutionException {
    logger.info("Executing Activity: " +  getName());
    DropboxConnection connection = null;
    String dropboxPath = null;
    String filename = null;
    String folder = "/";
    PutFileResponse response = new PutFileResponse();
    try {
      folder = context.getFunctionParameters().get("folder");
      logger.info("Uploading to folder: " + folder);
      filename = context.getFunctionParameters().get("fileName");
      logger.info("Uploading to filename: " + filename);
      connection = (DropboxConnection) context.getConnection();
      DbxClientV2 client = connection.getClient();

      PutFileRequest req = DropboxUtils.unmarshall(PutFileRequest.class, context.getRequestPayload().getInputStream());
      //Verbose Logger
      VerboseLogger.debug(PutFileActivity.class.getName(), "execute",
          new ObjectMapper().writer().withRootName("request").writeValueAsString(req));
      dropboxPath = getPath(filename, folder, req);
      logger.info("Uploading file: " + dropboxPath);
      // Upload file
      FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
          .withMute(req.isMute())
          .withAutorename(req.isAutorename())
          .withMode(req.getMode() != null && req.getMode().length() > 0 ?
              WriteMode.update(req.getMode()) : WriteMode.OVERWRITE)
          .withClientModified(new Date())
          .start()
          .uploadAndFinish(new ByteArrayInputStream(req.getContent()));
      
      response.setName(metadata.getName());
      response.setPathLower(metadata.getPathLower());
      response.setContentHash(metadata.getContentHash());
      response.setId(metadata.getId());
      response.setRev(metadata.getRev());
      response.setSize(BigInteger.valueOf(metadata.getSize()));
    //Verbose Logger
      VerboseLogger.debug(PutFileActivity.class.getName(), "execute",
          new ObjectMapper().writer().withRootName("response").writeValueAsString(response));
      // Marshall the response to the response payload output stream
      DropboxUtils.marshall(PutFileResponse.class, response, context.getResponsePayload().getOutputStream());
    } catch (Throwable x) {
      logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
      throw new ActivityExecutionException(Messages.DROPBOX_CODE04,
          Messages.getMessage(Messages.DROPBOX_CODE04_MSG, new Object[]{dropboxPath}), x);
    } finally {
      try {
        context.getResponsePayload().getOutputStream().flush();
        context.getResponsePayload().getOutputStream().close();
        if (connection != null) {
          connection.close();
        }
      } catch (Exception x) {
        String message = "Getting exception while closing: " + x.getLocalizedMessage();
        logger.severe(message);
        x.printStackTrace();
        throw new RuntimeException(message, x);
      }
    }
  }

  /**
   * Returns the request/response associated with this activity. For the <code>PUT</code>
   * activity, both the request and response data structures are being returned.
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
          PUT_FILE_REQ_XSD,
          PUT_FILE_RSP_XSD);
      activitySchemaResponse
          .setRequestRootElement(new QName(PUT_FILE_NAMESPACE, PUT_FILE_REQ_ROOT))
          .setResponseRootElement(new QName(PUT_FILE_NAMESPACE, PUT_FILE_RSP_ROOT));
    //Verbose Logging for Request and Response Schema
      if (VerboseLogger.getLogger().isDebugEnabled()) {
          JSONObject requestSchemaJson = new JSONObject();
          requestSchemaJson.put("schemaName", activitySchemaResponse.getRequestSchema().getName());
          requestSchemaJson.put("content", activitySchemaResponse.getRequestSchema().getContent());
          requestSchemaJson.put("content-type", activitySchemaResponse.getRequestSchema().getSchemaContentType());
          VerboseLogger.debug(PutFileActivity.class.getName(), "getActivityRequestResponseMetadata", "requestSchema: "
              + requestSchemaJson.toString());
          JSONObject responseSchemaJson = new JSONObject();
          responseSchemaJson.put("schemaName", activitySchemaResponse.getResponseSchema().getName());
          responseSchemaJson.put("content", activitySchemaResponse.getResponseSchema().getContent());
          responseSchemaJson.put("content-type", activitySchemaResponse.getResponseSchema().getSchemaContentType());
          VerboseLogger.debug(PutFileActivity.class.getName(), "getActivityRequestResponseMetadata", "responseSchema: "
              + responseSchemaJson.toString());
        }
      return activitySchemaResponse;
    } catch (Exception x) {
      logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
      throw new DiscoveryException(Messages.DROPBOX_CODE01,
          Messages.getMessage(Messages.DROPBOX_CODE01_MSG, new Object[]{getName()}), x);
    }
  }

  private String getPath(String filename, String folder, PutFileRequest req) {
    if (req.getPath() != null && req.getPath().length() > 0) {
      return req.getPath();
    }
    if (folder.length() > 0) {
      if (folder.endsWith("/")) {
        return folder + filename;
      } else {
        return folder + "/" + filename;
      }
    }
    return filename;
  }

  /**
   * Factory for creating the activity.
   */
  public static class PutFileActivityFactory implements Factory {
    @Override
    public JitterbitActivity createActivity() {
      return new PutFileActivity();
    }
  }

  private static Logger logger = Logger.getLogger(PutFileActivity.class.getName());
}
