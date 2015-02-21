package org.littleshoot.proxy.impl;

import android.util.Log;

/**
 * <p>
 * A helper class that logs messages for ProxyConnections. All it does is make
 * sure that the Channel and current state are always included in the log
 * messages (if available).
 * </p>
 *
 * <p>
 * Note that this depends on us using a LocationAwareLogger so that we can
 * report the line numbers of the caller rather than this helper class.
 * If the SLF4J binding does not provide a LocationAwareLogger, then a fallback
 * to Logger is provided.
 * </p>
 */
class ProxyConnectionLogger {
    private final ProxyConnection connection;
    private final String TAG = "PROXY-" + this.getClass().getCanonicalName();

    public ProxyConnectionLogger(ProxyConnection connection) {
        this.connection = connection;
    }

    protected void error(String message, Object... params) {
        String paramString = "";
        for (Object o : params) {
            paramString += o.toString() + " : ";
        }
        Log.e(TAG, message + "params: {" + paramString + "}");
    }

    protected void error(String message, Throwable t) {
        Log.e(TAG, message + " :: " + t.getMessage());
    }

    protected void warn(String message, Object... params) {
        String paramString = "";
        for (Object o : params) {
            paramString += o.toString() + " : ";
        }
        Log.w(TAG, message + "params: {" + paramString + "}");
    }

    protected void warn(String message, Throwable t) {
        Log.w(TAG, message + " :: " + t.getMessage());
    }

    protected void info(String message, Object... params) {
        String paramString = "";
        for (Object o : params) {
            paramString += o.toString() + " : ";
        }
        Log.i(TAG, message + "params: {" + paramString + "}");
    }

    protected void info(String message, Throwable t) {
        Log.i(TAG, message + " :: " + t.getMessage());
    }

    protected void debug(String message, Object... params) {
        String paramString = "";
        for (Object o : params) {
            if (o == null) continue;
            paramString += o.toString() + " : ";
        }
        Log.d(TAG, message + "params: {" + paramString + "}");
    }

    protected void debug(String message, Throwable t) {
        Log.i(TAG, message + " :: " + t.getMessage());
    }

    protected void log(int level, String message, Object... params) {
        debug(message, params);
    }

    protected void log(int level, String message, Throwable t) {
        debug(message, t);
    }

    private interface LogDispatch {
        void doLog(int level, String message, Object[] params, Throwable t);
    }

    private String fullMessage(String message) {
        String stateMessage = connection.getCurrentState().toString();
        if (connection.isTunneling()) {
            stateMessage += " {tunneling}";
        }
        String messagePrefix = "(" + stateMessage + ")";
        if (connection.channel != null) {
            messagePrefix = messagePrefix + " " + connection.channel;
        }
        return messagePrefix + ": " + message;
    }
}