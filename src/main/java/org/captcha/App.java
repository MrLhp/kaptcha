package org.captcha;

import lombok.extern.slf4j.Slf4j;
import org.captcha.utils.DownloadFile;
import org.captcha.utils.ImageUtils;
import org.captcha.utils.OpenCVProcess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main(String[] args) {
        try {
            for (int i = 0; i < 50; i++) {
                DownloadFile.downloadFile(Constants.getValidUrl(),"d:\\tmp\\yzm\\"+i+".png");
                BufferedImage bufferedImage = ImageUtils.removeBackground("d:\\tmp\\yzm\\" + i + ".png");
                ImageIO.write(bufferedImage,"png", new File("d:\\tmp\\yzm\\removebackground\\" + i + ".png"));
                OpenCVProcess.imgThreshold("d:\\tmp\\yzm\\removebackground\\" + i + ".png","d:\\tmp\\yzm\\removebackground\\"+i+".png");
                ImageUtils.ImageProcessing("d:\\tmp\\yzm\\removebackground\\"+i+".png","d:\\tmp\\yzm\\removeLinePoint\\"+i+".png");
                //BufferedImage iThin = ImageUtils.Xihua(ImageIO.read(new File("d:\\tmp\\yzm\\removeLinePoint\\"+i+".png")),null);
                //ImageIO.write(iThin, "jpg", new File("d:/tmp/yzm/xihua/"+i+".png"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
