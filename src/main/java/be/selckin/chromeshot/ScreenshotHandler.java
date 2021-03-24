package be.selckin.chromeshot;

public interface ScreenshotHandler {

    void onScreenShot(NamedNode nodeId, byte[] image);

    void onError(NamedNode nodeId, Exception ex);

}
