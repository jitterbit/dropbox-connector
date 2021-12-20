package org.jitterbit.connector.verbose.logging.dropbox;

import com.google.common.net.HttpHeaders;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;
import org.jitterbit.connector.dropbox.DropboxConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.stream.Collectors;    

/**
 * @author jitendra.singh
 */
public class VerboseLogger implements DropboxConstants {

    private static final Logger logger = Logger.getLogger(VerboseLogger.class);

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String className, String methodName, String message) {
        logger.info(className + "." + methodName + ", msg: " + message);
    }

    public static void debug(String className, String methodName, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(className + "." + methodName + ", msg: " + message);
        }
    }

    public static void debug(String className, String methodName, HttpRequestBase httpRequest)
            throws MalformedURLException {
        if (logger.isDebugEnabled()) {
            JSONObject request = new JSONObject();
            try {
                request.put(URL, httpRequest.getURI().toURL());
                request.put(QUERY_PARAMS, httpRequest.getURI().getQuery());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            request.put(HTTP_METHOD, httpRequest.getMethod());
            request.put(HEADERS, Arrays.stream(httpRequest.getAllHeaders()).map(head -> {
                if (head.getName().equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    return new String(head.getName() + ": ********");
                }
                return new String(head.getName() + ": " + head.getValue());
            }).collect(Collectors.toList()));
            logger.debug(className + "." + methodName + ", msg: Request Send: " + request);
        }
    }

    public static void error(String className, String methodName, String message) {
        logger.error(className + "." + methodName + ", msg: " + message);
    }

    public static void error(String className, String methodName, String errorMessage, Throwable error) {
        logger.error(className + "." + methodName + ", msg: " + errorMessage, error);
    }
}
