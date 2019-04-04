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

/**
 * Dropbox Connector-related constants.
 */
public interface DropboxConstants {

  String ACCESS_TOKEN = "access-token";
  String APP_KEY = "app-key";
  String CONNECTOR_NAME = "Dropbox";
  String LOCALE = "locale";

  String FETCH_FILE = "fetch";
  String GET_FILE = "get";
  String PROCESS_FILE = "process";
  String PUT_FILE = "put";

  String FETCH_FILE_REQ_XSD = "fetch-file-request.xsd";
  String FETCH_FILE_RSP_XSD = "fetch-file-response.xsd";

  String PUT_FILE_REQ_XSD = "put-file-request.xsd";
  String PUT_FILE_RSP_XSD = "put-file-response.xsd";

  String FETCH_FILE_REQ_ROOT = "fetchFileRequest";
  String FETCH_FILE_RSP_ROOT = "fetchFileResponse";

  String PUT_FILE_REQ_ROOT = "putFileRequest";
  String PUT_FILE_RSP_ROOT = "putFileResponse";

  String FETCH_FILE_NAMESPACE = "http://org.jitterbit.connector/dropbox/fetchfile";
  String PROCESS_FILE_NAMESPACE = "http://org.jitterbit.connector/dropbox/processfile";
  String PUT_FILE_NAMESPACE = "http://org.jitterbit.connector/dropbox/putfile";
}
