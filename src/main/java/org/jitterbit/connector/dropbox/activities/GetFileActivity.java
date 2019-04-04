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

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jitterbit.connector.dropbox.DropboxConnection;
import org.jitterbit.connector.dropbox.DropboxConstants;
import org.jitterbit.connector.dropbox.Messages;
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.annotation.Activity;
import org.jitterbit.connector.sdk.exceptions.ActivityExecutionException;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.jitterbit.connector.sdk.metadata.DiscoverableField;
import org.jitterbit.connector.sdk.metadata.DiscoverableObject;
import org.jitterbit.connector.sdk.metadata.DiscoverableObjectRequest;
import org.jitterbit.connector.sdk.metadata.SchemaMetaData;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 * Implements the get file activity of a Dropbox Connector. This activity
 * lets a user select from different files associated with a path at
 * configuration time. The activity downloads a file and parses it using the specified schema.
 * <p>
 * When the activity is being executed by the runtime, the <code>filename</code>
 * and <code>path</code> are obtained from the configuration
 * of the function that is exposed in the Jitterbit Harmony Cloud Studio UI from the
 * execution context: see {@link #execute(ExecutionContext)}. The <code>path</code> parameter
 * is declared as part of the <code>adapter.json</code> file and configured in the
 * Cloud Studio UI by the end-user. The available files are discovered based on
 * the <code>path</code>, and the user specifies the desired <code>filename</code>.
 * </p>
 * <p>
 * The response of this activity will be written to the response payload (see
 * {@link ExecutionContext#getResponsePayload()} as an XML document that conforms
 * with the <code>resources/fetch-file-response.xsd</code>.
 * </p>
 */
@Activity(
  name = DropboxConstants.GET_FILE,
  factory = GetFileActivity.GetFileActivityFactory.class)
public class GetFileActivity extends BaseDropboxActivity {

  public GetFileActivity() {}

  @Override
  public String getName() {
    return GET_FILE;
  }
  /**
   * Gets a file from Dropbox. The folder and filename properties are provided
   * as part of the `context`.
   * @param context the context for the activity
   * @throws ActivityExecutionException if there is an error while executing the activity
   */
  @Override
  public void execute(ExecutionContext context) throws ActivityExecutionException {
    logger.info("Executing Activity: " + getName());
    DropboxConnection connection = (DropboxConnection) context.getConnection();
    String folder = context.getFunctionParameters().get("folder");
    String obj = context.getFunctionParameters().get("list-object");
    try {
      JsonElement json = new JsonParser().parse(obj);
      String filename = json.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
      DbxClientV2 client = connection.getClient();
      String path = getPath(folder, filename);
      logger.info("Downloading " + path);
      client.files().download(path).download(context.getResponsePayload().getOutputStream());
    } catch (Throwable t) {
      logger.log(Level.SEVERE, t.getLocalizedMessage(), t);
      throw new ActivityExecutionException(Messages.DROPBOX_CODE06,
        Messages.getMessage(Messages.DROPBOX_CODE06_MSG, new Object[]{getName(), t.getLocalizedMessage()}), t);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  /**
   * Returns the request/response associated with this activity. For the <code>GET</code>
   * activity, only the response data structure is being returned.
   *
   * @param activityConfigProps the properties for the activity
   * @return the response metadata of the activity
   * @throws DiscoveryException if there is an error while configuring the activity
   */
  @Override
  public ActivityRequestResponseMetaData getActivityRequestResponseMetadata(
      DiscoverContextRequest<ActivityFunctionParameters> activityConfigProps)
      throws DiscoveryException {
    String folder = activityConfigProps.getRequest().getProperties().get("folder");
    String filename = activityConfigProps.getRequest().getObjectName();
    ActivityRequestResponseMetaData activitySchemaResponse = new ActivityRequestResponseMetaData();
    DropboxConnection connection = (DropboxConnection) activityConfigProps.getConnection();
    try {
      DbxClientV2 client = connection.getClient();
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      String path = getPath(folder, filename);
      client.files().download(path).download(os);
      String schemaContent = new String(os.toByteArray());
      SchemaMetaData md = new SchemaMetaData()
          .setContent(schemaContent)
          .setName(filename + ".xsd")
          .setSchemaContentType(
              filename.endsWith(".json") || filename.endsWith(".JSON") ? SchemaMetaData.SchemaContentType.JSON :
                SchemaMetaData.SchemaContentType.XML
          );
      activitySchemaResponse.setResponseRootElement(new QName(""));
      activitySchemaResponse.setResponseSchema(md);
      return activitySchemaResponse;
    } catch (Exception x) {
      logger.log(java.util.logging.Level.SEVERE, x.getLocalizedMessage(), x);
      throw new DiscoveryException(Messages.DROPBOX_CODE02,
          Messages.getMessage(Messages.DROPBOX_CODE02_MSG, new Object[]{getName()}), x);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  @Override
  public List<DiscoverableObject> getObjectList(DiscoverContextRequest<DiscoverableObjectRequest> objectListRequest)
      throws DiscoveryException {
    // return the list of files from the folder
    logger.info("Executing Activity: " + getName());
    List<DiscoverableObject> res = new LinkedList<>();
    String folder = "";
    DropboxConnection connection = null;
    try {
      Iterator<DiscoverableField.Properties> it = objectListRequest.getRequest().getSelectionPath().iterator();
      folder = objectListRequest.getRequest().getProperties().get("folder");
      if (folder == null) {
        folder = "";
      }
      connection = (DropboxConnection) objectListRequest.getConnection();
      ListFolderResult result = connection.getClient().files().listFolder(folder);
      for (Metadata m: result.getEntries()) {
        if (m instanceof FileMetadata && m.getName().endsWith(".xml") ||
          m.getName().endsWith("json") ||
          m.getName().endsWith(".XML") ||
          m.getName().endsWith(".JSON")) {
          res.add(new DiscoverableObject()
            .setObjectName(m.getName())
            .setObjectDesc(((FileMetadata) m).getServerModified().toString())
            .setObjectType(m.getName().endsWith(".json") || m.getName().endsWith(".JSON") ? "JSON" : "XML")
            .setParentObjectType(m.getParentSharedFolderId()));
        }
      }
      return res;
    } catch (Throwable t) {
      t.printStackTrace();

      throw new DiscoveryException(Messages.DROPBOX_CODE05,
        Messages.getMessage(Messages.DROPBOX_CODE05_MSG, new Object[]{folder, t.getLocalizedMessage()}), t);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  private String getPath(String folder, String filename) {
    return "".equals(folder) ? "/" + filename :
        (folder.endsWith("/") ? folder + filename : folder + "/" + filename);
  }

  /**
   *  Default Factory for the activity
   */
  public static class GetFileActivityFactory implements JitterbitActivity.Factory {
      @Override
      public JitterbitActivity createActivity() {
          return new GetFileActivity();
      }
  }

  private static Logger logger = Logger.getLogger(GetFileActivity.class.getName());
}
