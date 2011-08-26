package org.vaadin.simplelayout;

import java.util.Iterator;
import java.util.LinkedList;

import org.vaadin.simplelayout.client.ui.VSimpleLayout;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickNotifier;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;

/**
 * Server side component for the VWeeLayout widget.
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(VSimpleLayout.class)
public class SimpleLayout extends AbstractLayout implements LayoutClickNotifier {

    private static final String CLICK_EVENT = EventId.LAYOUT_CLICK;

    boolean stripDimensions = false;

    protected LinkedList<Component> components = new LinkedList<Component>();

    public SimpleLayout() {
    }

    /**
     * Creates a new SimpleLayout
     * 
     * @param stripDimensions
     *            should the layout strip all contained components of their
     *            predefined widths and heights (calls setSizeUndefined when a
     *            component is added to the layout and remove any inline CSS
     *            sizes in the client), allowing easier CSS styling in the
     *            client side.
     *            <p>
     *            Applies only if the layout itself has an undefined size on the
     *            server side. You can still specify the size using CSS. This
     *            works independently for both width and height, so either can
     *            be undefined while the other is defined.
     */
    public SimpleLayout(boolean stripDimensions) {
        this.stripDimensions = stripDimensions;
    }

    /**
     * Add a component into this container. The component is added after the
     * previous component.
     * 
     * @param c
     *            the component to be added.
     */
    @Override
    public void addComponent(Component c) {
        components.add(c);
        try {
            super.addComponent(c);
            requestRepaint();
        } catch (IllegalArgumentException e) {
            components.remove(c);
            throw e;
        }
        if (stripDimensions) {
            c.setSizeUndefined();
        }
    }

    /**
     * Add a component into this container. The component is added after the
     * previous component.
     * 
     * @param c
     *            the component to be added.
     * @param width
     *            set the width of the component. Use <code>null</code> to leave
     *            untouched.
     * @param height
     *            set the height of the component. Use <code>null</code> to
     *            leave untouched.
     */
    public void addComponent(Component c, String width, String height) {
        addComponent(c);
        if (width != null) {
            c.setWidth(width);
        }
        if (height != null) {
            c.setHeight(height);
        }
    }

    /**
     * Adds a component into indexed position in this container.
     * 
     * @param c
     *            the component to be added.
     * @param index
     *            the Index of the component position. The components currently
     *            in and after the position are shifted forwards.
     */
    public void addComponent(Component c, int index) {
        components.add(index, c);
        try {
            super.addComponent(c);
            requestRepaint();
        } catch (IllegalArgumentException e) {
            components.remove(c);
            throw e;
        }
    }

    /**
     * Removes the component from this container.
     * 
     * @param c
     *            the component to be removed.
     */
    @Override
    public void removeComponent(Component c) {
        components.remove(c);
        super.removeComponent(c);
        requestRepaint();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (stripDimensions) {
            target.addAttribute("strip", true);
        }

        // Adds all items in all the locations
        for (Component c : components) {
            // Paint child component UIDL
            c.paint(target);
        }
    }

    public Iterator<Component> getComponentIterator() {
        return components.iterator();
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        // Gets the locations
        int oldLocation = -1;
        int newLocation = -1;
        int location = 0;
        for (final Iterator<Component> i = components.iterator(); i.hasNext();) {
            final Component component = i.next();

            if (component == oldComponent) {
                oldLocation = location;
            }
            if (component == newComponent) {
                newLocation = location;
            }

            location++;
        }

        if (oldLocation == -1) {
            addComponent(newComponent);
        } else if (newLocation == -1) {
            removeComponent(oldComponent);
            addComponent(newComponent, oldLocation);
        } else {
            if (oldLocation > newLocation) {
                components.remove(oldComponent);
                components.add(newLocation, oldComponent);
                components.remove(newComponent);
                components.add(oldLocation, newComponent);
            } else {
                components.remove(newComponent);
                components.add(oldLocation, newComponent);
                components.remove(oldComponent);
                components.add(newLocation, oldComponent);
            }

            requestRepaint();
        }
    }

    /**
     * Returns the index of the given component.
     * 
     * @param component
     *            The component to look up.
     * @return The index of the component or -1 if the component is not a child.
     */
    public int getComponentIndex(Component component) {
        return components.indexOf(component);
    }

    /**
     * Returns the component at the given position.
     * 
     * @param index
     *            The position of the component.
     * @return The component at the given index.
     * @throws IndexOutOfBoundsException
     *             If the index is out of range.
     */
    public Component getComponent(int index) throws IndexOutOfBoundsException {
        return components.get(index);
    }

    /**
     * Returns the number of components in the layout.
     * 
     * @return Component amount
     */
    public int size() {
        return components.size();
    }

    /**
     * NOT SUPPORTED, use CSS for margins instead.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void setMargin(boolean enabled) {
        throw new UnsupportedOperationException(
                "Setting the margins from the server side is not supported. Use CSS instead");
    }

    /**
     * NOT SUPPORTED, use CSS for margins instead.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void setMargin(MarginInfo margins) {
        throw new UnsupportedOperationException(
                "Setting the margins from the server side is not supported. Use CSS instead");
    }

    /**
     * NOT SUPPORTED, use CSS for margins instead.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void setMargin(boolean top, boolean right, boolean bottom,
            boolean left) {
        throw new UnsupportedOperationException(
                "Setting the margins from the server side is not supported. Use CSS instead");
    }

    /**
     * NOT SUPPORTED, use CSS for margins instead.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public MarginInfo getMargin() {
        throw new UnsupportedOperationException(
                "Setting the margins from the server side is not supported. Use CSS instead");
    }

    public void addListener(LayoutClickListener listener) {
        addListener(CLICK_EVENT, LayoutClickEvent.class, listener,
                LayoutClickListener.clickMethod);
    }

    public void removeListener(LayoutClickListener listener) {
        removeListener(CLICK_EVENT, LayoutClickEvent.class, listener);
    }

}
