package me.plume.sim;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public abstract class Marker {
	private Point2D pos;
	private double theta;
	private Vessel vessel;
	public Marker(Point2D pos, double theta, Vessel vessel) {
		this.pos = pos;
		this.theta = theta;
		this.vessel = vessel;
	}
	public Point2D getPos() { return pos; }
	public double getTheta() { return theta; }
	public Vessel getVessel() { return vessel; }
	public abstract void render(GraphicsContext c, Point2D offset, double scale);
}
