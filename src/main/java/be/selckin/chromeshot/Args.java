package be.selckin.chromeshot;

import com.beust.jcommander.Parameter;

import java.awt.Color;
import java.nio.file.Path;

@SuppressWarnings("FieldMayBeFinal")
public class Args {

    @Parameter(names = {"--help"}, description = "Print usage", help = true)
    private boolean help = false;

    @Parameter(names = {"-p", "--port"}, description = "Port remote chrome debugger is listening on")
    private Integer port = 9222;


    @Parameter(names = {"-h", "--host"}, description = "Host remote chrome debugger is listening on")
    private String host = "localhost";


    @Parameter(names = {"-t", "--target"}, description = "Target directory to save the screenshot", required = true)
    private Path target;


    @Parameter(names = {"--tab-url"}, description = "Limit to tabs with a url that have this prefix")
    private String tabUrl;


    @Parameter(names = {"--grab-selector"}, description = "Screenshot all element found by this selector using document.querySelectorAll()", required = true)
    private String grabSelector;

    @Parameter(names = {"--file-attribute"}, description = "Attribute name that contains the filename to save the screenshot as. Attribute is expected to be on the --grab-selector element, or a child element of that specified with --file-selector")
    private String fileNameAttribute;


    @Parameter(names = {"--file-selector"}, description = "The selector to find the child of --grab-selector that has the --file-attribute attribute")
    private String fileNameSelector;


    @Parameter(names = {"--transparent-color"}, description = "The color that should be turned to transparency in the resulting PNG as comma-separated RGB, e.g. \"1,2,3\"")
    private String transparentColor;

    @Parameter(names = {"--rounding"}, description = "The rounding mode for viewport coordinates and width/height (which aren't necessarily integers).")
    private ViewportRounding viewportRounding = ViewportRounding.OUTER;

    @Parameter(names = {"--box"}, description = "The box model to capture")
    private ViewBox viewBox = ViewBox.BORDER;

    public boolean isHelp() {
        return help;
    }

    public Integer getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public Path getTarget() {
        return target;
    }

    public String getTabUrl() {
        return tabUrl;
    }

    public String getGrabSelector() {
        return grabSelector;
    }

    public String getFileNameAttribute() {
        return fileNameAttribute;
    }

    public String getFileNameSelector() {
        return fileNameSelector;
    }

    /**
     * @throws RuntimeException if the specified string can't be turned onto a {@link Color}
     */
    public Color transparentColor() throws RuntimeException {
        if (transparentColor == null || transparentColor.isBlank())
            return null;
        String[] rgb = transparentColor.split(",");
        return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
    }

    public ViewportRounding getViewportRounding() {
        return viewportRounding;
    }

    public ViewBox getViewBox() {
        return viewBox;
    }
}
