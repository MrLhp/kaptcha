package org.captcha.utils;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import com.github.jaiimageio.plugins.tiff.TIFFTag;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
     *
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
            array = ImageUtils.array;
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

    /**
     * 验证码分割
     *
     * @param sourcePath 图片文件路径
     * @return
     * @throws Exception
     */
    public static java.util.List<BufferedImage> splitImage(String sourcePath) throws Exception {
        BufferedImage img = ImageIO.read(new File(sourcePath));
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
            while (i < weightlist.size() && weightlist.get(i) > 1 || (i + 1 < weightlist.size() && weightlist.get(i + 1) > 0) || (i + 2 < weightlist.size() && weightlist.get(i + 2) > 0)) {
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

    public static File png2Tif(File file) throws FileNotFoundException, IOException {
        File f2 = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) + "tif");
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    BufferedImage.TYPE_BYTE_BINARY);//TYPE_BYTE_BINARY 压缩大小为原来的 24分之一
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
            boolean rn = ImageIO.write(newBufferedImage, "tiff", f2);
            if (!rn) {
                ImageUtils.log.warn("warn:" + file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 3)
                        + "tif exist");
            } else {
                //删除原有的png图片
                // file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f2;
    }

    /**
     * 使用ImageIO 合并tiff文件
     * jai-imageio-core-1.3.1.jar下载页面
     * https://bintray.com/jai-imageio/maven/jai-imageio-core-standalone/1.3.1
     * 参考
     * jai-imageio-core
     *https://github.com/jai-imageio/jai-imageio-core
     * tif,tiff图片的合并与拆分
     * https://blog.csdn.net/qq13398600329/article/details/80491325
     * ImageBuffer 生成tif 代码报空，tif 压缩
     * https://blog.csdn.net/u014510302/article/details/50234599
     * 利用ImageIO压缩图片
     * https://blog.csdn.net/thewindkee/article/details/52693371
     * @param fileList tiff文件集合
     * @param descFile 目标输出路径
     * @return
     * @throws IOException
     */
    public static String tif2Marge(List<File> fileList, File descFile) throws IOException {

        boolean bres = true;
        //tiff格式的图片读取器;
        TIFFImageReader tiffImageReader = new TIFFImageReader(new TIFFImageReaderSpi());
        FileImageInputStream fis = null;

        List<BufferedImage> biList = new ArrayList<BufferedImage>();

        for (File f : fileList) {
            String fileName = f.getName();

            if (!fileName.endsWith(".tif")) {
                continue;
            }

            String key = fileName.replace(".tif", "");
            try {
                fis = new FileImageInputStream(f);
                tiffImageReader.setInput(fis);

                biList.add(tiffImageReader.read(0));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (tiffImageReader != null) {
                    tiffImageReader.dispose();
                }
                if (fis != null) {
                    try {
                        fis.flush();
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                f.delete();

            }
        }
        //tiff格式图片输出流
        TIFFImageWriter tiffImageWriter = new TIFFImageWriter(new TIFFImageWriterSpi());
        //使用  CCITT T.6  进行压缩  压缩大小为原来的 十分之一
        ImageWriteParam writerParams = tiffImageWriter.getDefaultWriteParam();
        writerParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writerParams.setCompressionType("CCITT T.6");
        writerParams.setCompressionQuality(0.5f);

        try {

            //先指定一个文件用于存储输出的数据
            tiffImageWriter.setOutput(new FileImageOutputStream(descFile));
            //指定第一个tif文件写到指定的文件中
            BufferedImage bufferedImage_0 = biList.get(0);
            //IIOImage类是用于存储    图片/缩略图/元数据信息    的引用类
            IIOImage iioImage_0 = new IIOImage(bufferedImage_0, null, null);
            //write方法,将给定的IIOImage对象写到文件系统中;
            tiffImageWriter.write(null, iioImage_0, writerParams);
            for (int i = 1; i < biList.size(); i++) {
                //判断该输出流是否可以插入新图片到文件系统中的
                if (tiffImageWriter.canInsertImage(i)) {
                    //根据顺序获取缓冲中的图片;
                    BufferedImage bufferedImage = biList.get(i);
                    IIOImage iioImage = new IIOImage(bufferedImage, null, null);
                    //将文件插入到输出的多图片文件中的指定的下标处
                    tiffImageWriter.writeInsert(i, iioImage, writerParams);
                }
            }
            bres = true;
        } catch (IOException e) {
            e.printStackTrace();
            bres = false;
        } finally {
            return descFile.getCanonicalPath() + "|" + (Math.round(descFile.length() / 10)) / 100.0;
        }
    }

    /**
     * 从一个分页的tiff文件中拆分各页,并从0开始命名每一页
     * @param fTiff 源tiff文件
     * @param decDir
     *            tiff目标路径,目标文件将会以0001.tif,0002.tif...置于此路径下
     * @return true表示成功,false表示失败
     */
    public static boolean makeSingleTif(File fTiff, File decDir) {
        boolean bres = true;
        FileImageInputStream fis = null;
        try {
            //java1.8的ImageIO不支持原文中以"TIFF"名字获取ImageReader,具体原因是
            //ImageReaderSpi中有一个注册器,会先在内存中注册各个名称的读取器,而这个注册器恰好没有TIFF格式的
            //所以需要jai-imageio-1.1.jar提供TIFFImageReader;
            TIFFImageReaderSpi tiffImageReaderSpi = new TIFFImageReaderSpi();
            TIFFImageReader tiffImageReader = new TIFFImageReader(tiffImageReaderSpi);

            fis = new FileImageInputStream(fTiff);
            tiffImageReader.setInput(fis);

            int numPages = tiffImageReader.getNumImages(true);
            for (int i = 0; i < numPages; i++) {

                BufferedImage bi = tiffImageReader.read(i);

                File tif = new File(decDir.getPath() + File.separator
                        + String.format("" + i) + ".tif");
// 此处我注销了原文中的写Tiff文件的方法其原因是如果采用此方法会导致个别图片拆不出来,所以改用ImageWriter,个人推测是因为原文采用的方式涉及到图片的每个像素
//                bres = createTiff(tif,new RenderedImage[]{bi},dpiData,false);
                //TIFFImageWriter与reader是同样的原理;
                FileImageOutputStream fios = new FileImageOutputStream(tif);
                TIFFImageWriterSpi tiffImageWriterSpi = new TIFFImageWriterSpi();
                TIFFImageWriter tiffImageWriter = new TIFFImageWriter(tiffImageWriterSpi);
                tiffImageWriter.setOutput(fios);

                tiffImageWriter.write(bi);
            }

        } catch (Exception e) {
            e.printStackTrace();
            bres = false;

        } finally {

            if (fis != null) {
                try {
                    fis.flush();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        return bres;
    }

    /**
     * 设置图片DPI
     * @param file 图片源文件路径
     * @param xDensity
     * @param yDensity
     */
    public static void handleJPEGDpi(File file, int xDensity, int yDensity) {
        try {
            BufferedImage image = ImageIO.read(file);
            JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(file));
            JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image);
            jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
            jpegEncodeParam.setQuality(0.75f, false);
            jpegEncodeParam.setXDensity(xDensity);
            jpegEncodeParam.setYDensity(yDensity);
            jpegEncoder.encode(image, jpegEncodeParam);
            image.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleTIFFDpi(RenderedImage image, String outputFilePath, int dpi) {
        try {
            if (image != null) {
                TIFFEncodeParam param = new TIFFEncodeParam();
                param.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
                com.sun.media.jai.codec.TIFFField[] extras = new com.sun.media.jai.codec.TIFFField[2];
                extras[0] = new com.sun.media.jai.codec.TIFFField(282, TIFFTag.TIFF_RATIONAL, 1, (Object) new long[][]{{(long) dpi, 1}, {0, 0}});
                extras[1] = new com.sun.media.jai.codec.TIFFField(283, TIFFTag.TIFF_RATIONAL, 1, (Object) new long[][]{{(long) dpi, 1}, {0, 0}});
                param.setExtraFields(extras);
                File outputFile = new File(outputFilePath);
                outputFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                TIFFImageEncoder encoder = new TIFFImageEncoder(outputStream, param);
                encoder.encode(image);
                outputStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
