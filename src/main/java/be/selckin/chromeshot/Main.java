package be.selckin.chromeshot;

import be.selckin.chromeshot.ScreenShotter.ScreenMode;
import be.selckin.chromeshot.ScreenShotter.ScreenshotHandler;
import com.beust.jcommander.JCommander;
import com.github.kklisura.cdt.services.ChromeService;
import com.github.kklisura.cdt.services.exceptions.ChromeDevToolsInvocationException;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import com.github.kklisura.cdt.services.factory.impl.DefaultWebSocketContainerFactory;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;
import com.github.kklisura.cdt.services.types.ChromeVersion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    static {
        // For them big screenies https://github.com/kklisura/chrome-devtools-java-client/blob/master/cdt-examples/src/main/java/com/github/kklisura/cdt/examples/IncreasedIncomingBufferInTyrusExample.java#L45
        System.setProperty(
                DefaultWebSocketContainerFactory.WEBSOCKET_INCOMING_BUFFER_PROPERTY,
                Long.toString((long) DefaultWebSocketContainerFactory.MB * 24));
    }

    protected static final Logger log = LoggerFactory.getLogger("main");

    protected static final String URL_NEEDLE = "://";

    private Main() {
    }

    public static void main(String[] argv) {
        Args args = new Args();

        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        try {
            main(args);
        } catch (ChromeServiceException ex) {
            log.error("Failed", ex);
            System.exit(1);
        }
    }

    private static void main(Args args) {
        ChromeService chromeService = new ChromeServiceImpl(args.getHost(), args.getPort());

        try {
            ChromeVersion version = chromeService.getVersion();
            log.info("Found {} @ {}", version.getBrowser(), version.getWebSocketDebuggerUrl());
        } catch (ChromeServiceException ex) {
            log.error("Failed to connect to chrome on {}:{}", args.getHost(), args.getPort());
            return;
        }

        Store store = new Store(args.getTarget());

        List<ChromeTab> tabs = getTabs(chromeService, toTabFilter(args));
        for (ChromeTab tab : tabs) {

            try (ScreenShotter screenShotter = new ScreenShotter(ScreenMode.DEVICE_OVERRIDE, chromeService.createDevToolsService(tab))) {

                List<NamedNode> nodes = findNodes(screenShotter, args);

                screenShotter.screenshot(nodes, new ScreenshotHandler() {
                    @Override
                    public void onScreenShot(NamedNode node, byte[] image) {
                        try {
                            store.write(image, node.getName() + ".png");
                        } catch (IOException ex) {
                            log.warn("Failed to write to {}", node, ex);
                        }
                    }

                    @Override
                    public void onError(NamedNode node, ChromeDevToolsInvocationException ex) {
                        log.info("Failed to capture screenshot of node {}", node, ex);
                    }
                });
            }
        }
    }

    private static List<NamedNode> findNodes(ScreenShotter screenShotter, Args args) {
        List<Integer> nodeIds = screenShotter.querySelectorAll(args.getGrabSelector());

        String fileNameAttribute = args.getFileNameAttribute();
        if (fileNameAttribute == null) {
            return toIndexNamedNodes(nodeIds);
        } else {
            List<NamedNode> named = new ArrayList<>();
            for (Integer nodeId : nodeIds) {
                String name = screenShotter.findAttributeValue(toFilenameNode(screenShotter, args, nodeId), fileNameAttribute);
                if (name != null) {
                    named.add(new NamedNode(nodeId, name));
                } else {
                    log.warn("Could not find file name attribute for node {}", nodeId);
                }
            }
            return named;

        }
    }

    private static Integer toFilenameNode(ScreenShotter screenShotter, Args args, Integer nodeId) {
        String fileNameSelector = args.getFileNameSelector();
        if (fileNameSelector == null)
            return nodeId;

        return screenShotter.querySelector(nodeId, fileNameSelector);
    }

    @NotNull
    private static List<NamedNode> toIndexNamedNodes(List<Integer> nodeIds) {
        List<NamedNode> named = new ArrayList<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            named.add(new NamedNode(nodeIds.get(i), Integer.toString(i)));
        }
        return named;
    }

    private static Predicate<ChromeTab> toTabFilter(Args args) {
        Predicate<ChromeTab> filter = ChromeTab::isPageType;

        String tabUrlPrefix = args.getTabUrl();
        if (tabUrlPrefix != null) {
            filter = filter.and(toTabPrefixFilter(tabUrlPrefix));
        }
        return filter;
    }

    private static Predicate<ChromeTab> toTabPrefixFilter(String dirtyPrefix) {
        String prefix = dirtyPrefix.toLowerCase(Locale.ROOT).trim();

        if (urlProtocolNeedleOffset(prefix) < 0) {
            return tab -> {
                String url = tab.getUrl().toLowerCase(Locale.ROOT);
                int protoOffset = urlProtocolNeedleOffset(url);
                if (protoOffset >= 0) {
                    return url.substring(protoOffset).startsWith(prefix);
                }
                return url.startsWith(dirtyPrefix);
            };
        } else {
            return tab -> tab.getUrl().startsWith(prefix);
        }
    }

    private static int urlProtocolNeedleOffset(String prefix) {
        int idx = prefix.indexOf(URL_NEEDLE);
        // Only allow it at the start of the url to be paranoid
        if (idx >= 0 && idx <= 8)
            return idx + URL_NEEDLE.length();
        else
            return -1;
    }

    private static List<ChromeTab> getTabs(ChromeService chromeService, Predicate<ChromeTab> filter) {
        return chromeService.getTabs().stream().filter(filter).collect(Collectors.toList());
    }

    public static class NamedNode {
        private final Integer nodeId;
        private final String name;

        public NamedNode(Integer nodeId, String name) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.name = Objects.requireNonNull(name);
        }

        public Integer getNodeId() {
            return nodeId;
        }

        public String getName() {
            return name;
        }
    }
}
