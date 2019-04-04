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
import org.jitterbit.connector.sdk.JitterbitActivity;
import org.jitterbit.connector.sdk.annotation.Activity;
import org.jitterbit.connector.sdk.exceptions.ActivityExecutionException;
import org.jitterbit.connector.sdk.metadata.ActivityFunctionParameters;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.jitterbit.connector.sdk.metadata.DiscoverableObject;
import org.jitterbit.connector.sdk.metadata.DiscoverableObjectRequest;
import org.jitterbit.connector.sdk.metadata.SchemaMetaData.SchemaContentType;


import java.util.List;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 * Implements the process file activity of a Dropbox Connector. This activity
 * lets a user select from different XML schemas associated with the connector at
 * configuration time. The activity downloads a file and parses it using the specified schema.
 * <p>
 * When the activity is being executed by the runtime, the <code>filename</code>
 * and <code>folder</code> parameters can be obtained as part of the configuration
 * of the function that is exposed in the Jitterbit Harmony Cloud Studio UI from the
 * execution context: see {@link #execute(ExecutionContext)}. These two parameters
 * are declared as part of the <code>adapter.json</code> file and configured in the
 * Cloud Studio UI by the end-user.
 * </p>
 * <p>
 * The response of this activity will be written to the response payload (see
 * {@link ExecutionContext#getResponsePayload()} as an XML document that conforms
 * with the <code>resources/fetch-file-response.xsd</code>.
 * </p>
 */
@Activity(
  name = DropboxConstants.PROCESS_FILE,
  factory = ProcessFileActivity.ProcessFileActivityFactory.class)
public class ProcessFileActivity extends BaseDropboxActivity {

  private List<DiscoverableObjectModel> objects = new java.util.LinkedList<>();

  ProcessFileActivity() {
    init();
  }

  @Override
  public String getName() {
    return PROCESS_FILE;
  }

  /**
   * Processes a file from Dropbox. The folder and filename properties are provided
   * as part of the <code>context</code>.
   *
   * @param context the context for the activity
   * @throws ActivityExecutionException if there is an error while executing the activity
   */
  @Override
  public void execute(JitterbitActivity.ExecutionContext context) throws ActivityExecutionException {
    logger.info("Executing Activity: " + getName());
    String folder = "";
    String path = "";
    DbxDownloader<FileMetadata> result;
    try {
      String fileName = context.getFunctionParameters().get("fileName");
      folder = context.getFunctionParameters().get("folder");
      if (folder == null || folder.length() == 0) {
        folder = "/";
      }
      if (folder.endsWith("/")) {
        path = folder + fileName;
      } else {
        path = folder + "/" + fileName;
      }

      DropboxConnection connection = (DropboxConnection) context.getConnection();
      DbxClientV2 client = connection.getClient();
      result = client.files().download(path);
      result.download(context.getResponsePayload().getOutputStream());
    } catch (Throwable x) {
      x.printStackTrace();
      logger.severe("Dropbox " + x.getLocalizedMessage());
      throw new ActivityExecutionException(Messages.DROPBOX_CODE03,
        Messages.getMessage(Messages.DROPBOX_CODE03_MSG, new Object[]{folder}), x);
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
   * Returns the request/response associated with this activity. For the <code>PROCESS</code>
   * activity, only the response data structure is being returned.
   *
   * @param activityConfigProps the properties for the activity
   * @return the response metadata of the activity
   * @throws DiscoveryException if there is an error while configuring the activity
   */
  @Override
  public ActivityRequestResponseMetaData
    getActivityRequestResponseMetadata(DiscoverContextRequest<ActivityFunctionParameters> activityConfigProps)
      throws DiscoveryException {
    ActivityRequestResponseMetaData activitySchemaResponse = new ActivityRequestResponseMetaData();
    try {
      String objName = activityConfigProps.getRequest().getObjectName();
      DiscoverableObjectModel selectedObject = lookupObject(objName);
      if (selectedObject == null) {
        throw new DiscoveryException("200", "Object " + objName + " could not be found");
      }
      String path = selectedObject.path; // "support-xsds";
      String ext = selectedObject.ext;
      String rootName = objName;
      String resourceName =  objName + "." + ext;
      String fileName = selectedObject.filename;
      DropboxUtils.setRequestResponse(
          activitySchemaResponse,
          path,
          null,
          null,
          null,
          resourceName,
          fileName,
          selectedObject.schemaContentType);
      activitySchemaResponse.setResponseRootElement(QName.valueOf(rootName));
      return activitySchemaResponse;
    } catch (Exception x) {
      logger.log(java.util.logging.Level.SEVERE, x.getLocalizedMessage(), x);
      throw new DiscoveryException(Messages.DROPBOX_CODE02,
          Messages.getMessage(Messages.DROPBOX_CODE02_MSG, new Object[]{getName()}), x);
    }
  }

  /**
   * Returns the list of discovered objects requested for the activity.
   *
   * @param objectListRequest the list of objects requested for the activity
   * @return the list of discovered objects
   * @throws DiscoveryException if there is an error while configuring the activity
   */
  @Override
  public List<DiscoverableObject> getObjectList(DiscoverContextRequest<DiscoverableObjectRequest> objectListRequest)
      throws DiscoveryException {
    List<DiscoverableObject> objs = new java.util.LinkedList<>();
    for (DiscoverableObjectModel m: objects) {
      objs.add(m.obj);
    }
    return objs;
  }

  private DiscoverableObjectModel lookupObject(String objName) {
    for (DiscoverableObjectModel o: objects) {
      if (o.obj.getObjectName().equals(objName)) {
        return o;
      }
    }
    return null;
  }

  private void init() {
    DiscoverableObjectModel model = new DiscoverableObjectModel();
    DiscoverableObject obj = new DiscoverableObject();
    obj.setObjectName("account")
        .setObjectType("xsd")
        .setObjectDesc("XML Schema Structure associated with Account objects");
    model.path = "support-xsds";
    model.obj = obj;
    model.ext = "xsd";
    model.schemaContentType = org.jitterbit.connector.sdk.metadata.SchemaMetaData.SchemaContentType.XSD;
    model.filename = obj.getObjectName() + ".xsd";
    objects.add(model);

    model = new DiscoverableObjectModel();
    obj = new DiscoverableObject();
    obj.setObjectName("customers")
        .setObjectType("json")
        .setObjectDesc("JSON Schema Structure associated with Customers objects");
    model.path = "sample-json";
    model.obj = obj;
    model.ext = "xsd";
    model.schemaContentType = org.jitterbit.connector.sdk.metadata.SchemaMetaData.SchemaContentType.JSON;
    model.filename = obj.getObjectName() + ".json";
    objects.add(model);

    model = new DiscoverableObjectModel();
    obj = new DiscoverableObject();
    obj.setObjectName("contacts")
        .setObjectType("xml")
        .setObjectDesc("XML Sample Structure associated with Contacts objects");
    model.path = "sample-xml";
    model.obj = obj;
    model.ext = "xsd";
    model.schemaContentType = org.jitterbit.connector.sdk.metadata.SchemaMetaData.SchemaContentType.XML;
    model.filename = obj.getObjectName() + ".xml";

    objects.add(model);
  }

  /**
   * Factory for creating the activity.
   */
  public static class ProcessFileActivityFactory implements JitterbitActivity.Factory {
    @Override
    public JitterbitActivity createActivity() {
      return new ProcessFileActivity();
    }
  }

  static class DiscoverableObjectModel {
    public DiscoverableObject obj;
    public String path;
    public String ext;
    public SchemaContentType schemaContentType;
    public String filename;
  }

  private static Logger logger = Logger.getLogger(ProcessFileActivity.class.getName());
}
