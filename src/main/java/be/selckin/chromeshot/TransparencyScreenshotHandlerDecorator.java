package be.selckin.chromeshot;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TransparencyScreenshotHandlerDecorator implements ScreenshotHandler {

    private final ScreenshotHandler decorated;
    private final Color transparentColor;

    public TransparencyScreenshotHandlerDecorator(ScreenshotHandler decorated, Color transparentColor) {
        this.decorated = decorated;
        this.transparentColor = transparentColor;
    }

    @Override
    public void onScreenShot(NamedNode nodeId, byte[] imageData) {
        try (var imageDataStream = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(imageDataStream);
            BufferedImage filteredImage = applyFilter(image, createFilter(transparentColor));
            byte[] filteredImageData = getImageBytes(filteredImage);
            decorated.onScreenShot(nodeId, filteredImageData);
        } catch (Exception ex) {
            onError(nodeId, ex);
        }
    }

    private byte[] getImageBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageStream);
        return imageStream.toByteArray();
    }

    private static ImageFilter createFilter(Color transparentColor) {
        return new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                // first 256 bits are alpha channel
                return (rgb | 0xFF000000) == transparentColor.getRGB()
                        ? 0x00FFFFFF & rgb
                        : rgb;
            }
        };
    }

    private static BufferedImage applyFilter(BufferedImage source, ImageFilter filter) {
        ImageProducer filteredImageProducer = new FilteredImageSource(source.getSource(), filter);
        Image filtered = Toolkit.getDefaultToolkit().createImage(filteredImageProducer);
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = target.createGraphics();
        g2.drawImage(filtered, 0, 0, null);
        g2.dispose();

        return target;
    }

    @Override
    public void onError(NamedNode nodeId, Exception ex) {
        decorated.onError(nodeId, ex);
    }

}
