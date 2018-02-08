package heavy.tool.test.util;


import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.BackupStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;
import com.elvishew.xlog.printer.flattener.LogFlattener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LogUtil {


    private static int mLogLevel;
    private static String mSavePath;
    private static final String LOG_TAG = "TBRunner";

    private static final int DEFAULT_LOG_FILE_SIZE = 10 * 1024 * 1024;

    private static final int DEFAULT_LOG_FILE_NUMBER = 5;

    public static void init(int logLevel, String savePath) {
        mLogLevel = logLevel;
        mSavePath = savePath;
        initLog();
    }

    public static void v(String TAG, String content) {
        XLog.tag(TAG).v(content);
    }

    public static void d(String TAG, String content) {
        XLog.tag(TAG).d(content);
    }

    public static void i(String TAG, String content) {
        XLog.tag(TAG).i(content);
    }

    public static void w(String TAG, String content) {
        XLog.tag(TAG).w(content);
    }

    public static void e(String TAG, String content) {
        XLog.tag(TAG).e(content);
    }

    public static void xml(String TAG, String xml) {
        XLog.tag(TAG).xml(xml);
    }

    public static void json(String TAG, String json) {
        XLog.tag(TAG).json(json);
    }

    public static void trace(String TAG, int traceDepth) {
        XLog.tag(TAG).st(traceDepth);
    }

    public static void throwable(String TAG, Throwable throwable) {
        XLog.e(TAG, throwable);
    }

    private static void initLog() {
        LogConfiguration config = new LogConfiguration.Builder()
                .tag(LOG_TAG)                                         // 指定 TAG，默认为 "X-LOG"
                .t()                                                   // 允许打印线程信息，默认禁止
                //.st(2)                                                 // 允许打印深度为2的调用栈信息，默认禁止
                //.b()                                                   // 允许打印日志边框，默认禁止
                //.jsonFormatter(new MyJsonFormatter())                  // 指定 JSON 格式化器，默认为 DefaultJsonFormatter
                //.xmlFormatter(new MyXmlFormatter())                    // 指定 XML 格式化器，默认为 DefaultXmlFormatter
                //.throwableFormatter(new MyThrowableFormatter())        // 指定可抛出异常格式化器，默认为 DefaultThrowableFormatter
                //.threadFormatter(new MyThreadFormatter())              // 指定线程信息格式化器，默认为 DefaultThreadFormatter
                //.stackTraceFormatter(new MyStackTraceFormatter())      // 指定调用栈信息格式化器，默认为 DefaultStackTraceFormatter
                //.borderFormatter(new MyBoardFormatter())               // 指定边框格式化器，默认为 DefaultBorderFormatter
                //.addObjectFormatter(AnyClass.class,                    // 为指定类添加格式化器
                //        new AnyClassObjectFormatter())                 // 默认使用 Object.toString()
                .build();

        Printer androidPrinter = new AndroidPrinter();             // 通过 android.util.Log 打印日志的打印器
        Printer filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                .Builder(mSavePath) // 指定保存日志文件的路径
                .logFormatter(new LogFlattener() {

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

                    @Override
                    public CharSequence flatten(int i, String s, String s1) {
                        return dateFormat.format(System.currentTimeMillis()) +
                                "[" + s + "]" + s1 + "\n";
                    }
                })
                .fileNameGenerator(new FileNameGenerator() {

                    File lastFile = null;
                    String lastLogFileName = null;

                    @Override
                    public boolean isFileNameChangeable() {
                        return true;
                    }

                    @Override
                    public String generateFileName(int i, long l) {

                        if (lastFile == null || lastFile.length() > DEFAULT_LOG_FILE_SIZE) {
                            lastLogFileName = DateTimeUtil.currentInSperator() + ".txt";
                            lastFile = new File(mSavePath + File.separator + lastLogFileName);
                        }
                        return lastLogFileName;
                    }
                })        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                .backupStrategy(new BackupStrategy() {
                    @Override
                    public boolean shouldBackup(File file) {

                        File path = file.getParentFile();

                        File[] files = path.listFiles();

                        while (files.length > DEFAULT_LOG_FILE_NUMBER) {
                            File oldestFile = null;
                            for (File temp : files) {
                                if (oldestFile == null || oldestFile.lastModified() > temp.lastModified()) {
                                    oldestFile = temp;
                                }
                            }
                            if (oldestFile != null) {
                                oldestFile.delete();
                            }
                            files = path.listFiles();
                        }

                        //for some time the file name generator does't give an new file name, bak up here.
                        if(file.length() > DEFAULT_LOG_FILE_SIZE){
                            return true;
                        }
                        return false;
                    }
                })                // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                //.logFlattener(new MyLogFlattener())                    // 指定日志平铺器，默认为 DefaultLogFlattener
                .build();

        XLog.init(mLogLevel,                                    // 指定日志级别，低于该级别的日志将不会被打印
                config,                                                // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
                androidPrinter,                                        // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter
                filePrinter);
    }
}
