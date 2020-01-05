package org.captcha;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

public class OcrTest {
    public static void main( String[] args )
    {
        try {
            String opencvLib = System.getProperty("user.dir") + "\\src\\main\\resources";
            addLibraryDir(opencvLib);
            System.load(opencvLib+"\\opencv_java411.dll");
            File outputfile = new File("d:\\tmp\\yzm\\22_3.jpg");

            BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(Paths.get("d:\\tmp\\yzm\\22_1.jpg")));
            BufferedImage reline = reline(bufferedImage);
            ImageIO.write(reline,"jpg", outputfile);
//            ImageIO.write(cleanLinesInImage(reline), "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage cleanLinesInImage(BufferedImage oriBufferedImage)  throws IOException{
        BufferedImage bufferedImage = oriBufferedImage;
        int h = bufferedImage.getHeight();
        int w = bufferedImage.getWidth();

        // 灰度化
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                int argb = bufferedImage.getRGB(x, y);
                // 图像加亮（调整亮度识别率非常高）
                int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
                int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
                if (r >= 255)
                {
                    r = 255;
                }
                if (g >= 255)
                {
                    g = 255;
                }
                if (b >= 255)
                {
                    b = 255;
                }
                gray[x][y] = (int) Math
                        .pow((Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2)
                                * 0.6274 + Math.pow(b, 2.2) * 0.0753), 1 / 2.2);
            }
        }

        // 二值化
        int threshold = ostu(gray, w, h);
        BufferedImage binaryBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                if (gray[x][y] > threshold)
                {
                    gray[x][y] |= 0x00FFFF;
                } else
                {
                    gray[x][y] &= 0xFF0000;
                }
                binaryBufferedImage.setRGB(x, y, gray[x][y]);
            }
        }
        File file = new File("d:\\tmp\\yzm\\22.jpg");
        ImageIO.write(binaryBufferedImage,"jpg",file);

        //这里开始是利用opencv的api进行处理
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat imread = Imgcodecs.imread(file.getAbsolutePath());
        Mat target = new Mat();
//        Core.bitwise_not(imread,target);
        Mat kelner = Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3), new Point(-1, -1));

        //膨胀
        Imgproc.dilate(imread,target,kelner);
        //腐蚀
        Imgproc.erode(target,target,kelner);
        oriBufferedImage = Mat2BufImg(target,".png");
        file.delete();
        return oriBufferedImage;
    }

    public static void addLibraryDir(String libraryPath) throws IOException {
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

    public static BufferedImage Mat2BufImg(Mat matrix, String fileExtension) {
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

    public static BufferedImage reline(BufferedImage curImg) {
        if (curImg != null) {
            int width = curImg.getWidth();
            int height = curImg.getHeight();
            int px = 3;
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int argb = curImg.getRGB(x, y);
                    int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
                    int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                    int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
                    int sum = r + g + b;
                    if (!map.containsKey(sum)) {
                        map.put(sum, 1);
                    } else {
                        int num = map.get(sum);
                        map.remove(sum);
                        map.put(sum, num + 1);
                    }
                }
            }
            List<Integer> list;
            list = new ArrayList<Integer>();
            for (Integer in : map.keySet()) {
                // map.keySet()返回的是所有key的值
                Integer n = map.get(in);// 得到每个key多对用value的值
                list.add(n);
            }
            Collections.sort(list);
            // 四种颜色的rgb
            int num1 = 0;
            int num2 = 0;
            int num3 = 0;
            int num4 = 0;
            if (list.size() > 4) {
                num1 = list.get(list.size() - 5);
                num2 = list.get(list.size() - 4);
                num3 = list.get(list.size() - 3);
                num4 = list.get(list.size() - 2);
            }
            List<Integer> keylist = new ArrayList<Integer>();
            for (Integer key : map.keySet()) {
                if (map.get(key) == num1 || map.get(key) == num2 || map.get(key) == num3 || map.get(key) == num4) {
                    keylist.add(key);
                }
            }
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int argb = curImg.getRGB(x, y);
                    int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
                    int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                    int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
                    int sum = r + g + b;
                    int sum1 = 0;
                    int sum2 = 0;
                    int sum3 = 0;
                    int sum4 = 0;
                    int sum5 = 0;
                    int sum6 = 0;
                    boolean flag = true;
                    for (int i = 1; i <= px && y + i < height && y - i > 0 && x - i > 0 && x + i < width; i++) {
                        int upargb = curImg.getRGB(x, y - i);
                        int endargb = curImg.getRGB(x, y + i);
                        int rightupargb = curImg.getRGB(x + i, y + i);
                        int leftupargb = curImg.getRGB(x - i, y + i);
                        int leftdownargb = curImg.getRGB(x - i, y - i);
                        int rightdownargb = curImg.getRGB(x + i, y - i);
                        int r1 = (int) (((upargb >> 16) & 0xFF) * 1.1 + 30);
                        int g1 = (int) (((upargb >> 8) & 0xFF) * 1.1 + 30);
                        int b1 = (int) (((upargb >> 0) & 0xFF) * 1.1 + 30);
                        sum1 = r1 + g1 + b1;
                        int r2 = (int) (((endargb >> 16) & 0xFF) * 1.1 + 30);
                        int g2 = (int) (((endargb >> 8) & 0xFF) * 1.1 + 30);
                        int b2 = (int) (((endargb >> 0) & 0xFF) * 1.1 + 30);
                        sum2 = r2 + g2 + b2;
                        int r3 = (int) (((rightupargb >> 16) & 0xFF) * 1.1 + 30);
                        int g3 = (int) (((rightupargb >> 8) & 0xFF) * 1.1 + 30);
                        int b3 = (int) (((rightupargb >> 0) & 0xFF) * 1.1 + 30);
                        sum3 = r3 + g3 + b3;
                        int r4 = (int) (((leftupargb >> 16) & 0xFF) * 1.1 + 30);
                        int g4 = (int) (((leftupargb >> 8) & 0xFF) * 1.1 + 30);
                        int b4 = (int) (((leftupargb >> 0) & 0xFF) * 1.1 + 30);
                        sum4 = r4 + g4 + b4;
                        int r5 = (int) (((leftdownargb >> 16) & 0xFF) * 1.1 + 30);
                        int g5 = (int) (((leftdownargb >> 8) & 0xFF) * 1.1 + 30);
                        int b5 = (int) (((leftdownargb >> 0) & 0xFF) * 1.1 + 30);
                        sum5 = r5 + g5 + b5;
                        int r6 = (int) (((rightdownargb >> 16) & 0xFF) * 1.1 + 30);
                        int g6 = (int) (((rightdownargb >> 8) & 0xFF) * 1.1 + 30);
                        int b6 = (int) (((rightdownargb >> 0) & 0xFF) * 1.1 + 30);
                        sum6 = r6 + g6 + b6;

                        if (keylist.contains(sum1) || keylist.contains(sum2) || keylist.contains(sum3)
                                || keylist.contains(sum4) || keylist.contains(sum5) || keylist.contains(sum6)) {
                            flag = false;
                        }
                    }
                    if (!(keylist.contains(sum)) && flag) {
                        curImg.setRGB(x, y, Color.white.getRGB());
                    }
                }
            }

        }
        return curImg;
    }

    public static int ostu(int[][] gray, int w, int h) {
        int[] histData = new int[w * h];
        // Calculate histogram
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int red = 0xFF & gray[x][y];
                histData[red]++;
            }
        }

        // Total number of pixels
        int total = w * h;

        float sum = 0;
        for (int t = 0; t < 256; t++)
            sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histData[t]; // Weight Background
            if (wB == 0)
                continue;

            wF = total - wB; // Weight Foreground
            if (wF == 0)
                break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB; // Mean Background
            float mF = (sum - sumB) / wF; // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        return threshold;
    }

}
