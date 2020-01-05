package org.captcha.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    private static int whiteThreshold = 300;

    public static BufferedImage removeBackground(String picFile) throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
        final int width = img.getWidth();
        final int height = img.getHeight();
        for (int x = 1; x < width - 1; ++x) {
            for (int y = 1; y < height - 1; ++y) {
                if (getColorBright(img.getRGB(x, y)) < 100) {
                    /*if (isBlackOrWhite(img.getRGB(x - 1, y)) + isBlackOrWhite(img.getRGB(x + 1, y))
                            + isBlackOrWhite(img.getRGB(x, y - 1)) + isBlackOrWhite(img.getRGB(x, y + 1)) == 4) {
                        img.setRGB(x, y, Color.WHITE.getRGB());
                    }*/
                    img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        for (int x = 1; x < width - 1; ++x) {
            for (int y = 1; y < height - 1; ++y) {
                if (getColorBright(img.getRGB(x, y)) < 100) {
                    /*if (isBlackOrWhite(img.getRGB(x - 1, y)) + isBlackOrWhite(img.getRGB(x + 1, y))
                            + isBlackOrWhite(img.getRGB(x, y - 1)) + isBlackOrWhite(img.getRGB(x, y + 1)) == 4) {
                        img.setRGB(x, y, Color.WHITE.getRGB());
                    }*/
                    img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        img = img.getSubimage(1, 1, img.getWidth() - 2, img.getHeight() - 2);
        return img;
    }

    private static int getColorBright(int colorInt) {
        final Color color = new Color(colorInt);
        return color.getRed() + color.getGreen() + color.getBlue();

    }

    private static int isBlackOrWhite(int colorInt) {
        if (getColorBright(colorInt) < 30 || getColorBright(colorInt) > 730) {
            return 1;
        }
        return 0;
    }


    public static void ImageProcessing(String sourceFilePath, String targetFilePath) throws IOException {


        BufferedImage bufferedImage = ImageIO.read(new File(sourceFilePath));
        int h = bufferedImage.getHeight();
        int w = bufferedImage.getWidth();

        // 灰度化
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bufferedImage.getRGB(x, y);
                // 图像加亮（调整亮度识别率非常高）
                int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
                int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
                if (r >= 255) {
                    r = 255;
                }
                if (g >= 255) {
                    g = 255;
                }
                if (b >= 255) {
                    b = 255;
                }

                //此处根据实际需要进行设定阈值
                gray[x][y] = (int) Math.pow((
                        Math.pow(r, 2.2) * 0.2973
                                + Math.pow(g, 2.2) * 0.6274
                                + Math.pow(b, 2.2) * 0.0753), 1 / 2.2);
            }
        }

        // 二值化
        int threshold = ostu(gray, w, h);
        BufferedImage binaryBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (gray[x][y] > threshold) {
                    gray[x][y] |= 0x00FFFF;
                } else {
                    gray[x][y] &= 0xFF0000;
                }
                binaryBufferedImage.setRGB(x, y, gray[x][y]);
            }
        }

        //去除干扰点 或 干扰线（运用八领域，即像素周围八个点判定，根据实际需要判定）
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {

                boolean lineFlag = false;//去除线判定
                int pointflagNum = 0;//去除点判定

                if (isBlack(binaryBufferedImage.getRGB(x, y))) {
                    //左右像素点为"白"即空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x - 1, y)) && isWhite(binaryBufferedImage.getRGB(x + 1, y))) {
                        lineFlag = true;
                        pointflagNum += 2;
                    }
                    //上下像素点为"白"即空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x, y + 1)) && isWhite(binaryBufferedImage.getRGB(x, y - 1))) {
                        lineFlag = true;
                        pointflagNum += 2;
                    }
                    //斜上像素点为"白"即空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x - 1, y + 1)) && isWhite(binaryBufferedImage.getRGB(x + 1, y - 1))) {
                        lineFlag = true;
                        pointflagNum += 2;
                    }
                    if (isWhite(binaryBufferedImage.getRGB(x + 1, y + 1)) && isWhite(binaryBufferedImage.getRGB(x - 1, y - 1))) {
                        lineFlag = true;
                        pointflagNum += 2;
                    }
                    //去除干扰线
                    if (lineFlag) {
                        binaryBufferedImage.setRGB(x, y, -1);
                    }
                    //去除干扰点
                    if (pointflagNum > 3) {
                        binaryBufferedImage.setRGB(x, y, -1);
                    }
                }
            }
        }


        // 矩阵打印
        //for (int y = 0; y < h; y++) {
        //    for (int x = 0; x < w; x++) {
        //        if (isBlack(binaryBufferedImage.getRGB(x, y))) {
        //            System.out.print("*");
        //        } else {
        //            System.out.print(" ");
        //        }
        //    }
        //    System.out.println();
        //}

        ImageIO.write(binaryBufferedImage, "png", new File(targetFilePath));
    }

    private static boolean isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
            return true;
        }
        return false;
    }

    private static boolean isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
            return true;
        }
        return false;
    }

    /**
     * 二值化
     * @param gray
     * @param w
     * @param h
     * @return
     */
    private static int ostu(int[][] gray, int w, int h) {
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

    public static BufferedImage Xihua(BufferedImage image, Integer[] array) {
        if (array == null) {
            array=ImageUtils.array;
        }
        int num = 10;
        BufferedImage iXihua = image;
        for (int i = 0; i < num; i++) {
            VThin(iXihua, array);
            HThin(iXihua, array);
        }
        return iXihua;
    }

    private static BufferedImage VThin(BufferedImage image, Integer[] array) {
        int h = image.getHeight();
        int w = image.getWidth();
        int NEXT = 1;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (NEXT == 0) {
                    NEXT = 1;
                } else {
                    int M;
                    if (0 < j && j < w - 1) {
                        if (isBlack(image.getRGB(j - 1, i)) && isBlack(image.getRGB(j, i)) && isBlack(image.getRGB(j + 1, i))) {
                            M = 0;
                        } else {
                            M = 1;
                        }
                    } else {
                        M = 1;
                    }
                    if (isBlack(image.getRGB(j, i)) && M != 0) {
                        int[] a = {0, 0, 0, 0, 0, 0, 0, 0, 0};
                        for (int k = 0; k < 3; k++) {
                            for (int l = 0; l < 3; l++) {
                                if ((-1 < (i - 1 + k) && (i - 1 + k) < h) && (-1 < (j - 1 + l) && (j - 1 + l) < w) && isWhite(image.getRGB(j - 1 + l, i - 1 + k))) {
                                    a[k * 3 + l] = 1;
                                }
                            }
                        }
                        int sum = a[0] * 1 + a[1] * 2 + a[2] * 4 + a[3] * 8 + a[5] * 16 + a[6] * 32 + a[7] * 64 + a[8] * 128;
                        if (array[sum] == 0) {
                            image.setRGB(j, i, Color.black.getRGB());
                        } else {
                            image.setRGB(j, i, Color.white.getRGB());
                        }
                        if (array[sum] == 1) {
                            NEXT = 0;
                        }
                    }
                }
            }
        }
        return image;
    }

    private static BufferedImage HThin(BufferedImage image, Integer[] array) {
        int h = image.getHeight();
        int w = image.getWidth();
        int NEXT = 1;
        for (int j = 0; j < w; j++) {
            for (int i = 0; i < h; i++) {
                if (NEXT == 0) {
                    NEXT = 1;
                } else {
                    int M;
                    if (0 < i && i < h - 1) {
                        if (isBlack(image.getRGB(j, i - 1)) && isBlack(image.getRGB(j, i)) && isBlack(image.getRGB(j, i + 1))) {
                            M = 0;
                        } else {
                            M = 1;
                        }
                    } else {
                        M = 1;
                    }
                    if (isBlack(image.getRGB(j, i)) && M != 0) {
                        int[] a = {0, 0, 0, 0, 0, 0, 0, 0, 0};
                        for (int k = 0; k < 3; k++) {
                            for (int l = 0; l < 3; l++) {
                                if ((-1 < (i - 1 + k) && (i - 1 + k) < h) && (-1 < (j - 1 + l) && (j - 1 + l) < w) && isWhite(image.getRGB(j - 1 + l, i - 1 + k))) {
                                    a[k * 3 + l] = 1;
                                }
                            }
                        }
                        int sum = a[0] * 1 + a[1] * 2 + a[2] * 4 + a[3] * 8 + a[5] * 16 + a[6] * 32 + a[7] * 64 + a[8] * 128;
                        if (array[sum] == 0) {
                            image.setRGB(j, i, Color.black.getRGB());
                        } else {
                            image.setRGB(j, i, Color.white.getRGB());
                        }
                        if (array[sum] == 1) {
                            NEXT = 0;
                        }
                    }
                }
            }
        }
        return image;
    }

    public static java.util.List<BufferedImage> splitImage(BufferedImage img) throws Exception {
        final java.util.List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        final int width = img.getWidth();
        final int height = img.getHeight();
        final List<Integer> weightlist = new ArrayList<Integer>();
        for (int x = 0; x < width; ++x) {
            int count = 0;
            for (int y = 0; y < height; ++y) {
                if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
                    count++;
                }
            }
            weightlist.add(count);
        }
        for (int i = 0; i < weightlist.size(); i++) {
            int length = 0;
            while (i < weightlist.size() && weightlist.get(i) > 0 || (i+1<weightlist.size()&&weightlist.get(i+1)>0)) {
                i++;
                length++;
            }
            if (length > 30) {
                subImgs.add(
                        CommonUtil.removeBlank(img.getSubimage(i - length, 0, length / 2, height), whiteThreshold, 0));
                subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length / 2, 0, length / 2, height),
                        whiteThreshold, 0));
            } else if (length > 5) {
                subImgs.add(img.getSubimage(i - length, 0, length, height));
            }
        }

        return subImgs;
    }

    private static Integer[] array = {0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1,
            1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1,
            1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1,
            1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1,
            1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1,
            1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 0,
            1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0};

}
