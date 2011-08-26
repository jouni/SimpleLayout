package org.vaadin.simplelayout.client.ui;

import java.util.Set;

import org.vaadin.csstools.client.CSSRule;
import org.vaadin.csstools.client.ComputedStyle;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.LayoutClickEventHandler;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VSimpleLayout extends ComplexPanel implements Container {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-simplelayout";

    private LayoutClickEventHandler clickEventHandler = new LayoutClickEventHandler(
            this, EventId.LAYOUT_CLICK) {

        @Override
        protected <H extends EventHandler> HandlerRegistration registerHandler(
                H handler, Type<H> type) {
            return addDomHandler(handler, type);
        }

        @Override
        protected Paintable getChildComponent(
                com.google.gwt.user.client.Element element) {
            return getComponent(element);
        }
    };

    private Paintable getComponent(Element element) {
        return Util.getChildPaintableForElement(client, VSimpleLayout.this,
                (com.google.gwt.user.client.Element) element.cast());
    }

    /** Current margin values */
    protected int[] margin;

    /** Current border sizes */
    protected int[] border;

    /** Current padding values */
    protected int[] padding;

    /**
     * Current inner size (excluding margins, border and padding) in pixels
     */
    protected int width = -1;
    protected int height = -1;

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    ApplicationConnection client;

    private String lastStyleName;

    private boolean hasWidth = false;

    private boolean hasHeight = false;

    private boolean rendering;

    private boolean stripDimensions = false;

    private static boolean floatAdded = false;

    protected CSSRule css = null;

    public VSimpleLayout() {
        this(Document.get().createDivElement());

    }

    public VSimpleLayout(Element rootElement) {
        setElement(rootElement);
        setStyleName(CLASSNAME);
        if (!floatAdded) {
            CSSRule rule = CSSRule.create("." + CLASSNAME);
            rule.setProperty("float", "left");
            floatAdded = true;
        }
    }

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        rendering = true;

        this.client = client;
        paintableId = uidl.getId();
        stripDimensions = uidl.hasAttribute("strip");

        updateExtraSizeInfo();

        if (client.updateComponent(this, uidl, true)) {
            rendering = false;
            return;
        }

        clickEventHandler.handleEventHandlerRegistration(client);

        updateActualSize();

        // Iterate through Paintables in UIDL, add new ones and remove any
        // old ones.
        final int uidlCount = uidl.getChildCount();
        int uidlPos = 0;
        int customUidlCount = 0;

        for (; uidlPos < uidlCount; uidlPos++) {

            final UIDL childUIDL = (uidlPos < uidlCount) ? uidl
                    .getChildUIDL(uidlPos) : null;

            if (childUIDL.getTag().equals("custom")) {
                // Ignore this child element, it is not rendered. Only used in
                // extending classes (and should always be the last UIDL item).
                customUidlCount++;
                continue;
            }

            final Widget uidlWidget = childUIDL != null ? (Widget) client
                    .getPaintable(childUIDL) : null;

            if (uidlWidget.getParent() == null
                    || getChildren().get(uidlPos) != uidlWidget) {
                insertWidget(uidlWidget, uidlPos, getElement());
            }

            ((Paintable) uidlWidget).updateFromUIDL(childUIDL, client);

            // As requested many times on the forum, clear any width/height
            // inline styles from components (only if the layout has undefined
            // size)
            if (!hasWidth && stripDimensions) {
                uidlWidget.getElement().getStyle().clearWidth();
            }
            if (!hasHeight && stripDimensions) {
                uidlWidget.getElement().getStyle().clearHeight();
            }

        } // All UIDL widgets painted

        // All remaining widgets are removed
        removeChildrenAfter(uidlPos - customUidlCount);

        Util.runWebkitOverflowAutoFix(getElement());

        rendering = false;
    }

    protected void insertWidget(Widget w, int pos, Element el) {
        /*
         * Widget is either new or has changed place
         */
        w.removeFromParent();

        // Logical attach
        getChildren().insert(w, pos);

        // Physical attach
        DOM.insertChild((com.google.gwt.user.client.Element) el.cast(),
                w.getElement(), pos);

        // Adopt.
        adopt(w);
    }

    protected void updateExtraSizeInfo() {
        ComputedStyle cs = new ComputedStyle(getElement());
        margin = cs.getMargin();
        border = cs.getBorder();
        padding = cs.getPadding();
    }

    @Override
    public void setStyleName(String styleName) {
        super.setStyleName(styleName);
        if (isAttached() && !styleName.equals(lastStyleName)) {
            updateExtraSizeInfo();
            lastStyleName = styleName;
        }
    }

    /**
     * Only pixel values are accepted.
     */
    @Override
    public void setWidth(String w) {
        String toBeWidth = "";
        hasWidth = false;
        if (w != null && !"".equals(w)) {
            hasWidth = true;
            // Assume pixel values are always passed from ApplicationConnection
            int newWidth = ComputedStyle.parseInt(w) - margin[1] - margin[3]
                    - border[1] - border[3] - padding[1] - padding[3];
            if (newWidth < 0) {
                newWidth = 0;
            }
            toBeWidth = newWidth + "px";

            // We use the stylesheet for controlling size. Allows developers to
            // more easily override it.
            if (css == null) {
                css = CSSRule.create("." + getStylePrimaryName() + "-"
                        + paintableId);
            }
            getElement()
                    .addClassName(getStylePrimaryName() + "-" + paintableId);
            css.setProperty("width", toBeWidth);
        } else if (css != null) {
            css.setProperty("width", "");
        }

        if (!rendering) {
            updateActualSize();
            updateRelativeSizes();
            Util.runWebkitOverflowAutoFix(getElement());
        }
    }

    /**
     * Only pixel values are accepted.
     */
    @Override
    public void setHeight(String h) {
        String toBeHeight = "";
        hasHeight = false;

        if (h != null && !"".equals(h)) {
            hasHeight = true;
            // Assume pixel values are always passed from ApplicationConnection
            int newHeight = ComputedStyle.parseInt(h) - margin[0] - margin[2]
                    - border[0] - border[2] - padding[0] - padding[2];
            if (newHeight < 0) {
                newHeight = 0;
            }
            toBeHeight = newHeight + "px";

            // We use the stylesheet for controlling size. Allows developers to
            // more easily override it.
            if (css == null) {
                css = CSSRule.create("." + getStylePrimaryName() + "-"
                        + paintableId);
            }
            getElement()
                    .addClassName(getStylePrimaryName() + "-" + paintableId);
            css.setProperty("height", toBeHeight);
        } else if (css != null) {
            css.setProperty("height", "");
        }

        if (!rendering) {
            updateActualSize();
            updateRelativeSizes();
            Util.runWebkitOverflowAutoFix(getElement());
        }
    }

    protected void removeChildrenAfter(int pos) {
        int toRemove = getChildren().size() - pos;
        while (toRemove-- > 0) {
            Widget child = getChildren().get(pos);
            remove(child);
            client.unregisterPaintable((Paintable) child);
        }
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        if (!hasWidth && !hasHeight) {
            return null;
        }
        RenderSpace space = new RenderSpace(-1, -1, true);
        if (hasWidth) {
            space.setWidth(getOffsetWidth() - padding[1] - padding[3]
                    - border[1] - border[3]);
        }
        if (hasHeight) {
            space.setHeight(getOffsetHeight() - padding[0] - padding[2]
                    - border[0] - border[2]);
        }
        return space;
    }

    public boolean hasChildComponent(Widget component) {
        for (Widget w : getChildren()) {
            if (w == component) {
                return true;
            }
        }
        return false;
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        int index = getWidgetIndex(oldComponent);
        if (index >= 0) {
            remove(oldComponent);
            insert(newComponent, getElement(), index, true);
        }
    }

    public boolean requestLayout(Set<Paintable> children) {
        for (Paintable p : children) {
            Widget w = (Widget) p;
            if (!hasWidth && stripDimensions) {
                w.getElement().getStyle().clearWidth();
            }
            if (!hasHeight && stripDimensions) {
                w.getElement().getStyle().clearHeight();
            }
        }
        Util.runWebkitOverflowAutoFix(getElement());
        if (hasWidth && hasHeight) {
            return true;
        } else {
            // Size may have changed
            // TODO optimize this: cache size if not fixed, handle both width
            // and height separately
            return false;
        }
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO should we render captions, icons and error messages? Being a
        // simple layout, after all...
    }

    private void updateRelativeSizes() {
        for (Widget w : getChildren()) {
            if (w instanceof Paintable) {
                client.handleComponentRelativeSize(w);
            }
        }
    }

    public void updateActualSize() {
        ComputedStyle cs = new ComputedStyle(getElement());
        width = cs.getIntProperty("width");
        height = cs.getIntProperty("height");
    }
}
