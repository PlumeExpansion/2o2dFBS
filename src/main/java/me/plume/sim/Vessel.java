package me.plume.sim;

import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Point2D;

public abstract class Vessel {
	private Set<Force> forces = new HashSet<>();
	private Point2D r = Point2D.ZERO, v = Point2D.ZERO, a = Point2D.ZERO;
	private double alpha, omega, theta;
	public Point2D getPos() { return r; }
	public Point2D getVel() { return v; }
	public Point2D getAccel() { return a; }
	public double getAlpha() { return alpha; }
	public double getOmega() { return omega; }
	public double getTheta() { return theta; }
	public void syncKinematics(double dt) {
		double torque = 0;
		Point2D force = Point2D.ZERO;
		for (Force f : forces) {
			Point2D r = f.getPos().subtract(calcCM());
			torque += r.crossProduct(f.getForce()).getZ();
			r = r.normalize();
			force = force.add(r.multiply(f.getForce().dotProduct(r)));
		}
		alpha = torque / calcMoment();
		theta += alpha*dt*dt/2 + omega*dt;
		omega += alpha*dt;
		a = force.multiply(1/calcMass());
		r = r.add(a.multiply(dt*dt/2)).add(v.multiply(dt));
		v = v.add(a.multiply(dt));
	}
	public abstract double calcMoment();
	public abstract double calcMass();
	public abstract Point2D calcCM();
	public abstract void update(double time, Set<Vessel> toAdd, Set<Vessel> toClear);
	public abstract Marker genMarker();
}
