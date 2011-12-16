package com.example.simplelayout;

import org.vaadin.simplelayout.SimpleLayout;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class SimpleLayoutApplication extends Application {
	@Override
	public void init() {
		final SimpleLayout l = new SimpleLayout();
		l.setSizeFull();
		// l.setWidth("100%");
		l.addStyleName("test");

		final Window mainWindow = new Window("SimpleLayout Test", l);
		mainWindow.setSizeFull();

		final Label label = new Label("Here’s a Label");
		label.setCaption("The caption of the label");
		mainWindow.addComponent(label);

		final Label label2 = new Label("Here’s Label #2");
		label2.setCaption("The caption of the label #2");

		Button button = new Button("And a Button!", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				label.setCaption(null);
				label2.setCaption(null);
				Label three = new Label("Newly added");
				three.setCaption("One more caption");
				l.addComponent(three, 2);
			}
		});
		mainWindow.addComponent(button);
		mainWindow.addComponent(label2);

		Panel p = new Panel("Yet, a Panel");
		// p.setSizeFull();
		mainWindow.addComponent(p);

		setMainWindow(mainWindow);
		setTheme("test");
	}
}
