package be.selckin.chromeshot;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;

public class Args {

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
}
