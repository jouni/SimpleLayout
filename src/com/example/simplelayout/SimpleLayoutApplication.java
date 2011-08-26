package com.example.simplelayout;

import org.vaadin.simplelayout.SimpleLayout;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SimpleLayoutApplication extends Application {
    @Override
    public void init() {
        Layout l = new SimpleLayout();
        l.setSizeFull();
        // l.setWidth("100%");
        l.addStyleName("test");

        Window mainWindow = new Window("SimpleLayout Test", l);
        mainWindow.setSizeFull();

        Label label = new Label("Here’s a Label");
        mainWindow.addComponent(label);

        Button button = new Button("And a Button!");
        mainWindow.addComponent(button);

        Panel p = new Panel("Yet, a Panel");
        // p.setSizeFull();
        mainWindow.addComponent(p);

        setMainWindow(mainWindow);
        setTheme("test");
    }
}
