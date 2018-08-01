package com.jrecord.common;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 日志工具
 */
public class ILog {
    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR = 4;
    public static final int LEVEL_FATAL = 5;

    private static int sLevel = LEVEL_VERBOSE;
    /**
     * set log level, the level lower than this level will not be logged
     *
     * @param level
     */
    public static void setLogLevel(int level) {
        sLevel = level;
    }
    public static int getLogLevel() {
        return sLevel;
    }
    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     */
    public static void v(String tag, String msg) {
        if (sLevel > LEVEL_VERBOSE) {
            return;
        }
        Log.v(tag, formatMsg(msg));
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void v(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_VERBOSE) {
            return;
        }
        Log.v(tag, formatMsg(msg), throwable);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void v(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_VERBOSE) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.v(tag, formatMsg(msg));
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        Log.d(tag, formatMsg(msg));
        writeLogToFile(tag, formatMsg(msg));
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void d(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.d(tag, formatMsg(msg));
        writeLogToFile(tag, formatMsg(msg));
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void d(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        Log.d(tag, formatMsg(msg), throwable);
        writeLogToFile(tag, formatMsg(msg), throwable);
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (sLevel > LEVEL_INFO) {
            return;
        }
        Log.i(tag, formatMsg(msg));
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void i(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_INFO) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.i(tag, formatMsg(msg));
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_INFO) {
            return;
        }
        Log.i(tag, formatMsg(msg), throwable);
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (sLevel > LEVEL_WARNING) {
            return;
        }
        Log.w(tag, formatMsg(msg));
        writeLogToFile(tag, formatMsg(msg));
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void w(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_WARNING) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.w(tag, formatMsg(msg));
        writeLogToFile(tag, formatMsg(msg));
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void w(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_WARNING) {
            return;
        }
        Log.w(tag, formatMsg(msg), throwable);
        writeLogToFile(tag, formatMsg(msg), throwable);
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        Log.e(tag, formatMsg(msg));
        writeLogToFile(tag, "Error : " + msg);
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void e(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.e(tag, formatMsg(msg));
        writeLogToFile(tag, "Error : " + msg);
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void e(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        Log.e(tag, formatMsg(msg), throwable);
        writeLogToFile(tag, formatMsg(msg), throwable);
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     */
    public static void f(String tag, String msg) {
        if (sLevel > LEVEL_FATAL) {
            return;
        }
        Log.wtf(tag, formatMsg(msg));
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void f(String tag, String msg, Object... args) {
        if (sLevel > LEVEL_FATAL) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.wtf(tag, formatMsg(msg));
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void f(String tag, String msg, Throwable throwable) {
        if (sLevel > LEVEL_FATAL) {
            return;
        }
        Log.wtf(tag, formatMsg(msg), throwable);
    }

    private static String formatMsg(String msg) {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        /*
        0 = {StackTraceElement@4554} "dalvik.system.VMStack.getThreadStackTrace(Native Method)"
        1 = {StackTraceElement@4555} "java.lang.Thread.getStackTrace(Thread.java:1566)"
        2 = {StackTraceElement@4556} "com.jrecord.common.ILog.formatMsg(ILog.java:305)"
        3 = {StackTraceElement@4557} "com.jrecord.common.ILog.d(ILog.java:86)"
         4 = {StackTraceElement@4558} "com.jrecord.MainActivity.onCreate(MainActivity.java:14)"
         */
        if (traceElements != null && traceElements.length > 4) {
            StackTraceElement traceElement = traceElements[4];
            msg += " ["+traceElement.getClassName()+"."+traceElement.getMethodName()+"("+traceElement.getFileName()+":"+traceElement.getLineNumber()+")]";
        }

        return msg;
    }
    private static LogWriter sWriter;
    private static void writeLogToFile(String tag, String text) {
        writeLogToFile(tag, text, null);
    }
    private static void writeLogToFile(String tag, String text, Throwable throwable) {
        String msg = text;
        if (null != throwable) {
            msg = text + "\n" + Log.getStackTraceString(throwable);
        }
        String logText = String.format("%s %s : %s", AppUtils.getCurrentTime(), tag, msg);

        if (null == sWriter) {
            sWriter = new LogWriter();
            sWriter.start();
        }
        sWriter.addLog(logText);
    }

    /**
     * 跟随应用虚拟机启动的守护线程
     */
    static class LogWriter extends Thread {
        private final Queue<String> logs = new LinkedBlockingQueue<>(500);
        public LogWriter() {
            super("LogWriter");
        }

        public void addLog(String text) {
            logs.add(text);
        }

        @Override
        public synchronized void start() {
            setDaemon(true);
            super.start();
        }

        @Override
        public void run() {
            super.run();
            BufferedWriter bw = null;
            FileWriter fw = null;
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), ".logCache");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
                File file = new File(dir, df.format(date) + ".log");
                if (!file.exists()) {
                    file.createNewFile();
                }

                fw = new FileWriter(file, true);
                bw = new BufferedWriter(fw);
                while (true) {
                    if (logs.isEmpty()) {
                        synchronized (logs) {
                            Thread.sleep(10000);
                        }
                    } else {
                        String msg = null;
                        while ((msg = logs.poll()) != null) {
                            bw.write(msg);
                            bw.newLine();
                        }
                    }
                }

            } catch (Exception e) {
                Log.e("LogWriter", "LogWriter Error ", e);
            } finally {
                if (null != fw) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != bw) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}