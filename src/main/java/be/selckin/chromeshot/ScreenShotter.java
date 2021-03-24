package be.selckin.chromeshot;

import com.github.kklisura.cdt.protocol.commands.DOM;
import com.github.kklisura.cdt.protocol.commands.Emulation;
import com.github.kklisura.cdt.protocol.commands.Page;
import com.github.kklisura.cdt.protocol.commands.Runtime;
import com.github.kklisura.cdt.protocol.types.dom.BoxModel;
import com.github.kklisura.cdt.protocol.types.dom.Rect;
import com.github.kklisura.cdt.protocol.types.page.CaptureScreenshotFormat;
import com.github.kklisura.cdt.protocol.types.page.Viewport;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.exceptions.ChromeDevToolsInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

public class ScreenShotter implements AutoCloseable {
    protected static final Logger log = LoggerFactory.getLogger("main");

    private final ScreenMode mode;
    private final ViewportRounding rounding;
    private final ChromeDevToolsService devToolsService;
    private final Page page;
    private final DOM dom;
    private final Emulation emulation;
    private final Runtime runtime;


    public ScreenShotter(ScreenMode mode, ViewportRounding rounding, ChromeDevToolsService devToolsService) {
        this.mode = Objects.requireNonNull(mode);
        this.rounding = Objects.requireNonNull(rounding);
        this.devToolsService = Objects.requireNonNull(devToolsService);
        this.page = devToolsService.getPage();
        this.dom = devToolsService.getDOM();
        this.emulation = devToolsService.getEmulation();
        this.runtime = devToolsService.getRuntime();
    }

    public List<Integer> querySelectorAll(String selector) {
        return querySelectorAll(dom.getDocument().getNodeId(), selector);
    }

    public List<Integer> querySelectorAll(Integer nodeId, String selector) {
        return dom.querySelectorAll(nodeId, selector);
    }

    public Integer querySelector(Integer nodeId, String selector) {
        return dom.querySelector(nodeId, selector);
    }

    @Nullable
    public String findAttributeValue(Integer nodeId, String attributeName) {
        List<String> attributes = dom.getAttributes(nodeId);
        if (attributes == null)
            return null;

        for (int i = 0; i + 1 < attributes.size(); i += 2) {
            String key = attributes.get(i);
            String value = attributes.get(i + 1);

            if (attributeName.equalsIgnoreCase(key))
                return value;
        }
        return null;
    }

    public void screenshot(List<NamedNode> nodes, ScreenshotHandler callback) {
        page.bringToFront();

        if (mode == ScreenMode.DEVICE_OVERRIDE) {
            Rect contentSize = page.getLayoutMetrics().getContentSize();
            emulation.setDeviceMetricsOverride(contentSize.getWidth().intValue(), contentSize.getHeight().intValue(), 1.0d, false);
        }

        for (int i = 0; i < nodes.size(); i++) {
            NamedNode node = nodes.get(i);
            log.info("Taking screenshot {}/{} -- {}", i + 1, nodes.size(), node.name());

            dom.scrollIntoViewIfNeeded(node.nodeId(), null, null, null);

            Viewport viewPort = findViewPort(dom, node.nodeId());
            if (mode == ScreenMode.SCROLL) {
                int scrollX = evaluateInt("window.scrollX");
                int scrollY = evaluateInt("window.scrollY");
                viewPort.setX(viewPort.getX() + scrollX);
                viewPort.setY(viewPort.getY() + scrollY);
            }
            viewPort = rounding.round(viewPort);

            try {
                String encodedImage = page.captureScreenshot(CaptureScreenshotFormat.PNG, 100, viewPort, true);

                callback.onScreenShot(node, Base64.getDecoder().decode(encodedImage));
            } catch (ChromeDevToolsInvocationException ex) {
                callback.onError(node, ex);
            }
        }

        if (mode == ScreenMode.DEVICE_OVERRIDE) {
            emulation.clearDeviceMetricsOverride();
        }
    }

    private Integer evaluateInt(String s) {
        return (Integer) runtime.evaluate(s).getResult().getValue();
    }

    @Override
    public void close() {
        devToolsService.close();
    }

    private static Viewport findViewPort(DOM dom, Integer integer) {
        BoxModel boxModel = dom.getBoxModel(integer, null, null);

        return toViewPort(boxModel.getBorder());
    }

    // https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Quad
    // An array of quad vertices, x immediately followed by y for each point, points clock-wise.
    private static Viewport toViewPort(List<Double> quads) {
        double x = min(quads.get(0), quads.get(2), quads.get(4), quads.get(6));
        double y = min(quads.get(1), quads.get(3), quads.get(5), quads.get(7));
        double width = max(quads.get(0), quads.get(2), quads.get(4), quads.get(6)) - x;
        double height = max(quads.get(1), quads.get(3), quads.get(5), quads.get(7)) - y;

        Viewport clip = new Viewport();
        clip.setX(x);
        clip.setY(y);
        clip.setWidth(width);
        clip.setHeight(height);

        clip.setScale(1.0d);

        return clip;
    }

    private static double min(double... values) {
        return reduce(Math::min, values);
    }

    private static double max(double... values) {
        return reduce(Math::max, values);
    }

    private static double reduce(DoubleBinaryOperator operator, double... values) {
        double value = values[0];
        for (int i = 1, valuesLength = values.length; i < valuesLength; i++) {
            value = operator.applyAsDouble(value, values[i]);
        }
        return value;
    }
}
