package be.selckin.chromeshot;

import java.io.IOException;

public class StoringScreenshotHandler implements ScreenshotHandler {

    private final Store store;

    public StoringScreenshotHandler(Store store) {
        this.store = store;
    }

    @Override
    public void onScreenShot(NamedNode node, byte[] image) {
        try {
            store.write(image, node.name() + ".png");
        } catch (IOException ex) {
            Main.log.warn("Failed to write to {}", node, ex);
        }
    }

    @Override
    public void onError(NamedNode node, Exception ex) {
        Main.log.info("Failed to capture screenshot of node {}", node, ex);
    }

}
