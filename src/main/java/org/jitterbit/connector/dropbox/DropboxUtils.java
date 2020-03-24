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

import org.apache.commons.io.IOUtils;
import org.jitterbit.connector.sdk.metadata.ActivityRequestResponseMetaData;
import org.jitterbit.connector.sdk.metadata.SchemaMetaData;
import org.jitterbit.connector.sdk.metadata.SchemaMetaData.SchemaContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Contains utilities methods used within the Dropbox connector package.
 */
public class DropboxUtils {

  /**
   * Returns a named resource from a specified class loader. If the <code>cls</code>
   * class loader is null, it uses the current thread context class loader.
   *
   * @param cls class loader to be used to load the resource
   * @param resourceName name of the resource to be loaded
   * @return the named resource
   * @throws IOException if there is an error while loading the resource
   */
  public static String loadResource(ClassLoader cls, String resourceName) throws IOException {
    if (cls == null) {
      cls = Thread.currentThread().getContextClassLoader();
    }
    InputStream is = cls.getResourceAsStream(resourceName);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, "UTF-8");
    return writer.toString();
  }

  /**
   * Loads schemas from a class loader and sets them as the request/response schemas
   * for a specified activity.
   *
   * @param activitySchemaResponse reference to the response where the XML schema is being set
   * @param pathResource path to the resource folder containing the schemas
   * @param requestResourceName path to the request XML schema
   * @param responseResourceName path to the response XML schema
   * @throws IOException if there is an error loading the schema
   */
  public static void setRequestResponseSchemas(ActivityRequestResponseMetaData activitySchemaResponse,
                                               String pathResource,
                                               String requestResourceName,
                                               String responseResourceName) throws IOException {
    if (requestResourceName != null) {
      activitySchemaResponse.setRequestSchema(
        new SchemaMetaData()
          .setName(DropboxConstants.CONNECTOR_NAME + "_" + requestResourceName)
          .setContent(loadResource(DropboxUtils.class.getClassLoader(), pathResource + "/" + requestResourceName)));
    }

    if (responseResourceName != null) {
      activitySchemaResponse.setResponseSchema(
        new SchemaMetaData()
          .setName(DropboxConstants.CONNECTOR_NAME + "_" + responseResourceName)
          .setContent(loadResource(DropboxUtils.class.getClassLoader(), pathResource + "/" + responseResourceName)));
    }
  }
  /**
   * Loads schemas from a class loader and sets them as the request/response schemas
   * for a specified activity.
   *
   * @param activitySchemaResponse reference to the response where the XML schema is being set
   * @param pathResource path to the resource folder containing the schemas
   * @param requestResourceName path to the request XML schema
   * @param reqFileName file name of the request XML schema
   * @param reqProtocol protocol of the request XML schema
   * @param responseResourceName path to the response XML schema
   * @param rspFileName file name of the response XML schema
   * @param rspProtocol protocol of the response XML schema
   * @throws IOException if there is an error loading the schema
   */
  public static void setRequestResponse(ActivityRequestResponseMetaData activitySchemaResponse,
                                        String pathResource,
                                        String requestResourceName,
                                        String reqFileName,
                                        SchemaContentType reqProtocol,
                                        String responseResourceName,
                                        String rspFileName,
                                        SchemaContentType rspProtocol) throws IOException {
    if (requestResourceName != null) {
      activitySchemaResponse.setRequestSchema(
        new SchemaMetaData()
          .setName(DropboxConstants.CONNECTOR_NAME + "_" +  requestResourceName)
          .setSchemaContentType(reqProtocol)
          .setContent(loadResource(DropboxUtils.class.getClassLoader(), pathResource + "/" + reqFileName))
      );
    }

    if (responseResourceName != null) {
      activitySchemaResponse.setResponseSchema(new SchemaMetaData()
        .setName(DropboxConstants.CONNECTOR_NAME + "_" + responseResourceName)
        .setSchemaContentType(rspProtocol)
        .setContent(loadResource(DropboxUtils.class.getClassLoader(), pathResource + "/" + rspFileName)));
    }
  }

  /**
   * Serialize an JAXB-annotated object to an output stream.
   *
   * @param clz class (type) of the object being serialized
   * @param instance object being serialized
   * @param os an output stream
   * @throws Exception if there is an exception serializing the object
   */
  public static void marshall(Class clz, Object instance, OutputStream os) throws Exception {
    JAXBContext context = JAXBContext.newInstance(new Class[]{clz});
    Marshaller m = context.createMarshaller();
    m.marshal(instance, os);
  }

  /**
   * Create an instance of a JAXB object of type <code>clz</code>.
   *
   * @param <T> type of the JAXB instance being created
   * @param clz class of the JAXB instance being created
   * @param is input stream containing the content to be unmarshalled into the JAXB instance
   * @return the object created from the supplied input stream
   * @throws Exception if there is an exception instantiating the object
   */
  public static <T> T unmarshall(Class<T> clz, InputStream is) throws Exception {
    JAXBContext context = JAXBContext.newInstance(new Class[]{clz});
    Unmarshaller um = context.createUnmarshaller();
    return (T) um.unmarshal(is);
  }

  public static String getFileContent(String path) throws Exception {
    return loadResource(DropboxUtils.class.getClassLoader(), path);
  }
}
