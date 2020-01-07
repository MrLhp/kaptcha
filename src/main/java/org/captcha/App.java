package org.captcha;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.captcha.utils.DownloadFile;
import org.captcha.utils.ImageUtils;
import org.captcha.utils.OpenCVProcess;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    static String yzmPath = "d:\\tmp\\yzm\\";
    static String removebackgroundPath = "d:\\tmp\\yzm\\removebackground\\";
    static String removeLinePointPath = "d:\\tmp\\yzm\\removeLinePoint\\";
    static String splitPath = "d:\\tmp\\yzm\\split\\";
    static String mergePath = "d:\\tmp\\yzm\\merge\\";

    private static void folderMkdir() {
        File file = new File(yzmPath);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(removebackgroundPath);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(removeLinePointPath);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(splitPath);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(mergePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }
    public static void main(String[] args) {
        folderMkdir();
        int count = 0;
        try {
            for (int i = 1; i <= 50; i++) {
                if (count == 100) {
                    return;
                }
                DownloadFile.downloadFile(Constants.getValidUrl(),yzmPath+i+".jpeg");
                BufferedImage bufferedImage = ImageUtils.removeBackground(yzmPath + i + ".jpeg");
                ImageIO.write(bufferedImage,"jpeg", new File(removebackgroundPath + i + ".jpeg"));
                OpenCVProcess.imgThreshold(removebackgroundPath + i + ".jpeg",removebackgroundPath+i+".jpeg");
                ImageUtils.ImageProcessing(removebackgroundPath+i+".jpeg",removeLinePointPath+i+".jpeg");
                List<BufferedImage> imageList = ImageUtils.splitImage(removeLinePointPath + i + ".jpeg");

                if (imageList.size()<=4) {
                    for (int j = 1; j <= imageList.size(); j++) {
                        File splitFile = new File(splitPath + count + "_" + i + ".jpeg");
                        ImageIO.write(imageList.get(j-1),"jpeg", splitFile);
                        File tif = ImageUtils.png2Tif(splitFile);
                        IIOImage img = new IIOImage(ImageIO.read(tif), null, null);
                        ImageUtils.handleTIFFDpi(img.getRenderedImage(),tif.getAbsolutePath(),300);
                        count++;
                    }
                }else{
                    App.log.info("抛弃数据{}",i);
                }
                //BufferedImage iThin = ImageUtils.Xihua(ImageIO.read(new File("d:\\tmp\\yzm\\removeLinePoint\\"+i+".png")),null);
                //ImageIO.write(iThin, "jpg", new File("d:/tmp/yzm/xihua/"+i+".png"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        LinkedList<File> fileList = Stream.of(new File("d:/tmp/yzm/split/").listFiles())
                .flatMap(file -> file.listFiles() == null ?
                        Stream.of(file) : Stream.of(file.listFiles())).filter(file -> file.getName().endsWith("tif"))
                .collect(LinkedList::new,LinkedList::add,LinkedList::addAll);

        ImageUtils.tif2Marge(sortFileByName(fileList, "asc"),
                new File("d:/tmp/yzm/merge/captcha.normal.exp0.tif"),300,300);
    }


    @Test
    public void tiffTest() throws IOException {
        FileImageInputStream fis = null;
        TIFFImageReaderSpi tiffImageReaderSpi = new TIFFImageReaderSpi();
        TIFFImageReader tiffImageReader = new TIFFImageReader(tiffImageReaderSpi);
        fis = new FileImageInputStream(new File("d:/tmp/yzm/merge/captcha.normal.exp0.tif"));
        tiffImageReader.setInput(fis);
        int numPages = tiffImageReader.getNumImages(true);
        for (int i = 0; i < numPages; i++) {
            BufferedImage bi = tiffImageReader.read(i);
            System.out.println(i+" 0 0 "+(bi.getWidth()-1)+" "+(bi.getHeight()-2)+" "+i);
        }
    }


    public static List<File> sortFileByName(List<File> files, final String orderStr) {
        if (!orderStr.equalsIgnoreCase("asc") && orderStr.equalsIgnoreCase("desc")) {
            return files;
        }
        File[] files1 = files.toArray(new File[0]);
        Arrays.sort(files1, new Comparator<File>() {
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                if(orderStr == null || orderStr.length() < 1 || orderStr.equalsIgnoreCase("asc")) {
                    return n1 - n2;
                } else {
                    //降序
                    return n2 - n1;
                }
            }
        });
        return new ArrayList<File>(Arrays.asList(files1));
    }

    private static int extractNumber(String name) {
        int i;
        try {
            String number = name.replaceAll("[^\\d]", "");
            i = Integer.parseInt(number);
        } catch (Exception e) {
            i = 0;
        }
        return i;
    }
}
