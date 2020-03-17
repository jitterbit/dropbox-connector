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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message templates used in thrown exceptions related to the Dropbox connector.
 */
public class Messages {

  public static final String DROPBOX_CODE01 = "Dropbox01";
  public static final String DROPBOX_CODE01_MSG = "dropbox.01";
  // "Error loading the request and response schema for activity {0}"

  public static final String DROPBOX_CODE02 = "Dropbox02";
  public static final String DROPBOX_CODE02_MSG = "dropbox.02";
  // "Error loading the request and response schema for {0}"

  public static final String DROPBOX_CODE03 = "Dropbox03";
  public static final String DROPBOX_CODE03_MSG = "dropbox.03";
  // "Error downloading {0}"

  public static final String DROPBOX_CODE04 = "Dropbox04";
  public static final String DROPBOX_CODE04_MSG = "dropbox.04";

  public static final String DROPBOX_CODE05 = "Dropbox05";
  public static final String DROPBOX_CODE05_MSG = "dropbox.05";

  public static final String DROPBOX_CODE06 = "Dropbox06";
  public static final String DROPBOX_CODE06_MSG = "dropbox.06";
  // "Error uploading {0}"

  public static final String DROPBOX_CODE07 = "Dropbox07";
  public static final String DROPBOX_CODE07_MSG = "dropbox.07";
  // "Error creating connection {0}"

  /**
   * Returns a formatted message using a template from a keyed message store
   * and specified parameters.
   *
   * Eventually, these messages will need to be read from a property bundle file.
   *
   * @param key message template key
   * @param params parameters for the message template
   * @return formatted message
   */
  public static String getMessage(String key, Object[] params) {
    ResourceBundle bundle = ResourceBundle.getBundle("dropbox_messages");
    String msg = bundle.getString(key);
    return MessageFormat.format(msg, params);
  }

}
