package me.plume.demo;

import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.plume.sim.Force;
import me.plume.sim.Marker;
import me.plume.sim.Simulation;
import me.plume.sim.Vessel;

public class Launcher extends Application {
	public void start(Stage window) {
		window.setTitle("Projectile Motion");
		Simulation sim = new Simulation(0.01);
		sim.setMaxTravel(1);
		sim.addInit(vs -> {
			vs.add(new Vessel(new Point2D(0, 15), new Point2D(5, 0)) {
				public double calcMoment() {
					return calcMass()*2*2;
				}
				public double calcMass() {
					return 10;
				}
				public Point2D calcCM() {
					return Point2D.ZERO;
				}
				public void onStep(double time, Set<Vessel> toAdd, Set<Vessel> toClear) {
					if (getPos().getY()<=0) {
						System.out.println("time to impact: " + sim.getTime());
					}
				}
				public void update(double time, Set<Force> forces) {
					forces.add(new Force(Point2D.ZERO, new Point2D(0, -9.8*10)));
					if (getPos().getY()<=0) forces.add(new Force(Point2D.ZERO, new Point2D(0, 9.8*10)));
				}
				public Marker genMarker() {
					return new Marker(getPos(), getTheta(), this) {
						public void render(GraphicsContext c, Point2D o, double s) {
							c.setFill(Color.RED);
							c.fillOval((getPos().getX()+o.getX()-2)*s, (-getPos().getY()-o.getY()-2)*s, 4*s, 4*s);
						}
					};
				}
			});
		});
		GridPortal portal = new GridPortal(window, new Scene(new Pane(), 1200, 800), sim, 60, Point2D.ZERO, 5);
		portal.center();
		portal.getSim().start();
		portal.syncSimToScene();
		window.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
