package me.kaneki.download.utils;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/13
 * @Email kaneki.cjb@alibaba-inc.com
 */
public class StringUtil {

    private static float KB = 1024;
    private static float MB = 1024 * KB;
    private static float GB = 1024 * MB;

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size >= GB) {
            return String.format("%.1f GB", size / GB);
        } else if (size >= MB) {
            float value = size / MB;
            return String.format(value > 100 ? "%.0f MB" : "%.1f MB", value);
        } else if (size >= KB) {
            float value = size / KB;
            return String.format(value > 100 ? "%.0f KB" : "%.1f KB", value);
        } else {
            return String.format("%d B", size);
        }
    }

}
