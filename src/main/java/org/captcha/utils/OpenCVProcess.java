package org.captcha.utils;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.lang.reflect.Field;

public class OpenCVProcess {

    /**
     * 图片二值化处理
     * @param sourcePath 源图片文件全路径
     * @param targetPath 目标图片全路径
     * @throws IOException
     */
    public static void imgThreshold(String sourcePath, String targetPath) throws IOException {
        String opencvLib = System.getProperty("user.dir") + "\\src\\main\\resources";
        addLibraryDir(opencvLib);
        System.load(opencvLib + "\\opencv_java411.dll");
        // 加载时灰度
        Mat src = Imgcodecs.imread(sourcePath, Imgcodecs.IMREAD_GRAYSCALE);
        // 保存灰度
        // Imgcodecs.imwrite("D:\\tmp\\yzm\\23.jpg", src);
        Mat target = new Mat();
        // 二值化处理
        Imgproc.threshold(src, target, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);//灰度图像二值化
        // 保存二值化后图片 类型1
        Imgcodecs.imwrite(targetPath, target);
        // 保存二值化后图片 类型2
        //Imgproc.adaptiveThreshold(src, target, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 0);
        //Imgcodecs.imwrite("D:\\tmp\\yzm\\22_2.jpg", target);
    }

    /**
     * 加载本地library
     * @param libraryPath
     * @throws IOException
     */
    private static void addLibraryDir(String libraryPath) throws IOException {
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (libraryPath.equals(paths[i])) {
                    return;
                }
            }

            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = libraryPath;
            field.set(null, tmp);
        } catch (IllegalAccessException e) {
            throw new IOException(
                    "Failedto get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException(
                    "Failedto get field handle to set library path");
        }
    }
}
