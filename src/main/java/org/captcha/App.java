package org.captcha;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import lombok.extern.slf4j.Slf4j;
import org.captcha.utils.DownloadFile;
import org.captcha.utils.ImageUtils;
import org.captcha.utils.OpenCVProcess;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main(String[] args) {
        String yzmPath = "d:\\tmp\\yzm\\";
        String removebackgroundPath = "d:\\tmp\\yzm\\removebackground\\";
        String removeLinePointPath = "d:\\tmp\\yzm\\removeLinePoint\\";
        String splitPath = "d:\\tmp\\yzm\\split\\";
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
        int count = 0;
        try {
            for (int i = 1; i <= 50; i++) {
                DownloadFile.downloadFile(Constants.getValidUrl(),yzmPath+i+".png");
                BufferedImage bufferedImage = ImageUtils.removeBackground(yzmPath + i + ".png");
                ImageIO.write(bufferedImage,"png", new File(removebackgroundPath + i + ".png"));
                OpenCVProcess.imgThreshold(removebackgroundPath + i + ".png",removebackgroundPath+i+".png");
                ImageUtils.ImageProcessing(removebackgroundPath+i+".png",removeLinePointPath+i+".png");
                List<BufferedImage> imageList = ImageUtils.splitImage(removeLinePointPath + i + ".png");
                if (count == 12) {
                    break;
                }
                if (imageList.size()<=4) {
                    for (int j = 1; j <= imageList.size(); j++) {
                        File splitFile = new File(splitPath+"captcha.normal.exp" + i + "" + j + ".png");
                        ImageIO.write(imageList.get(j-1),"png", splitFile);
                        // ImageUtils.png2Tif(splitFile);
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
        List<File> fileList = Stream.of(new File("d:/tmp/yzm/split/").listFiles())
                .flatMap(file -> file.listFiles() == null ?
                        Stream.of(file) : Stream.of(file.listFiles()))
                .collect(toList());
        try {
            ImageUtils.tif2Marge(fileList, new File("d:/tmp/yzm/merge/captcha.normal.exp1.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("/path/to/tiff");
    }


    @Test
    public void tiffTest() throws IOException {
        FileImageInputStream fis = null;
        TIFFImageReaderSpi tiffImageReaderSpi = new TIFFImageReaderSpi();
        TIFFImageReader tiffImageReader = new TIFFImageReader(tiffImageReaderSpi);
        fis = new FileImageInputStream(new File("d:/tmp/yzm/merge/captcha.normal.exp1.tif"));
        tiffImageReader.setInput(fis);
        int numPages = tiffImageReader.getNumImages(true);
        for (int i = 0; i < numPages; i++) {
            BufferedImage bi = tiffImageReader.read(i);
            System.out.println("0 0 0 "+bi.getWidth()+" "+bi.getHeight()+" "+i);
        }
    }
}
