package me.plume.demo;

import javafx.application.Application;
import javafx.stage.Stage;
import me.plume.sim.Simulation;

public class Launcher extends Application {
	public void start(Stage window) {
		GridPortal portal = new GridPortal(window, new Simulation(0.01), 60, 1200, 800);
		portal.getSim().start();
		window.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
