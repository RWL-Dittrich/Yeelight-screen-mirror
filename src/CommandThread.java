import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class CommandThread implements Runnable {
    private Robot robot;
    private boolean running = false;
    private YeelightDevice device = null;
    private App main;


    public CommandThread(App main) {
        this.main = main;
    }


    @Override
    public void run() {
        running = true;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            main.error();
        }
        try {
            if (main.smooth) {
                device = new YeelightDevice(main.ip, 55443, YeelightEffect.SMOOTH, main.delay);
            } else {
                device = new YeelightDevice(main.ip, 55443, YeelightEffect.SUDDEN, main.delay);

            }

            int displayWidth = getMonitorDisplayMode().getWidth();
            int displayHeight = getMonitorDisplayMode().getHeight() / 2;

            int lastBrightness = 0;


            while (running) {
                try {
                    HSB color = averageColor(robot.createScreenCapture(new Rectangle(0, 0, displayWidth, displayHeight)), 0, 0, displayWidth, displayHeight);

                    //color.setSat(addToSaturation(color.getSat()));
                    Color rgbColor = color.encodeColor();
                    device.setRGB(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue());
                    int currentBrightness = Math.min((int)(color.getBri() * 120f), 100);
                    int changeBrightness = Math.abs(currentBrightness - lastBrightness);
                    if(changeBrightness <= 4) {
                        currentBrightness = lastBrightness;
                    } else if(changeBrightness < 6) {
                        if(currentBrightness > lastBrightness) {
                            currentBrightness = lastBrightness + 1;
                        } else {
                            currentBrightness = lastBrightness - 1;
                        }
                    } else if(changeBrightness < 8) {
                        if (currentBrightness > lastBrightness) {
                            currentBrightness = lastBrightness + 2;
                        } else {
                            currentBrightness = lastBrightness - 2;
                        }
                    }
                    device.setBrightness(currentBrightness);
                    lastBrightness = currentBrightness;
                    //System.out.println(color);
                    Thread.sleep(main.delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        } catch (YeelightSocketException | YeelightResultErrorException e) {
            main.error();
        }
    }


    private static HSB averageColor(BufferedImage bi, int x0, int y0, int w, int h) {
        int x1 = x0 + w;
        int y1 = y0 + h;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = w * h;
        return HSB.decodeColor(new Color((int) (sumr / num), (int) (sumg / num), (int) (sumb / num)));
    }

    private static HSB getMostSaturatedColor(BufferedImage bi, int x0, int y0, int w, int h) {
        HSB mostSaturated = new HSB(0, 0, 0);
        int x1 = x0 + w;
        int y1 = y0 + h;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                HSB pixelHSB = HSB.decodeColor(pixel);
                if (pixelHSB.getSat() > mostSaturated.getSat() && pixelHSB.getBri() > mostSaturated.getBri()) {
                    mostSaturated = pixelHSB;
                }
            }
        }
        return mostSaturated;
    }

    private HSB getMostSaturatedSector(BufferedImage bi, int x0, int y0, int w, int h) {
        int widthSectors = 5;
        int heightSectors = 3;
        int screenSectorWidth = w / widthSectors;
        int screenSectorHeight = h / heightSectors;
        HSB mostSaturated = new HSB(0, 0, 0);
        ArrayList<HSB> colorSectorAverages = new ArrayList<>();
        for (int i = 0; i < widthSectors; i++) {
            for (int j = 0; j < heightSectors; j++) {
                colorSectorAverages.add(averageColor(bi, i * screenSectorWidth, j * screenSectorHeight, screenSectorWidth, screenSectorHeight));
            }
        }

        for (HSB pixelHSB : colorSectorAverages) {
            if (pixelHSB.getSat() > mostSaturated.getSat() && pixelHSB.getBri() > mostSaturated.getBri()) {
                mostSaturated = pixelHSB;
            }
        }
        return mostSaturated;
    }

    private DisplayMode getMonitorDisplayMode() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        DisplayMode mode = gs[0].getDisplayMode();
        System.out.println("Width: " + mode.getWidth() + " Height: " + mode.getHeight());
        return mode;
    }

    public void stop() {
        if(this.running) {
            this.running = false;
            try {
                device.stopServer();
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
            }
        }
    }
}
