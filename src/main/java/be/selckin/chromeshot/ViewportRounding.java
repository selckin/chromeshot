package be.selckin.chromeshot;

import com.github.kklisura.cdt.protocol.types.page.Viewport;

public enum ViewportRounding {

    NEAREST, OUTER;

    private static final double EPSILON = 1.0E-3;

    public Viewport round(Viewport viewport) {
        return switch (this) {
            case NEAREST -> toNearestViewPort(viewport);
            case OUTER -> toOuterViewPort(viewport);
        };
    }

    private static Viewport toNearestViewPort(Viewport viewport) {
        double x = Math.round(viewport.getX());
        double y = Math.round(viewport.getY());
        double width = Math.round(viewport.getWidth());
        double height = Math.round(viewport.getHeight());

        Viewport clip = new Viewport();
        clip.setX(x);
        clip.setY(y);
        clip.setWidth(width);
        clip.setHeight(height);
        clip.setScale(1.0d);

        return clip;
    }

    private static Viewport toOuterViewPort(Viewport viewport) {
        Viewport clip = new Viewport();
        double x = Math.floor(viewport.getX() + EPSILON);
        double y = Math.floor(viewport.getY() + EPSILON);
        double x2 = Math.ceil(viewport.getX() + viewport.getWidth() - EPSILON);
        double y2 = Math.ceil(viewport.getY() + viewport.getHeight() - EPSILON);

        clip.setX(x);
        clip.setY(y);
        clip.setWidth(x2 - x);
        clip.setHeight(y2 - y);

        clip.setScale(1.0d);

        return clip;
    }


}
