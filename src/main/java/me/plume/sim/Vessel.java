package me.plume.sim;

import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Point2D;

public abstract class Vessel {
	private Set<Force> forces = new HashSet<>();
	final Set<Force> getForces() { return forces; }
	private Point2D r = Point2D.ZERO, v = Point2D.ZERO, a = Point2D.ZERO;
	private double alpha, omega, theta;
	public final Point2D getPos() { return r; }
	public final Point2D getVel() { return v; }
	public final Point2D getAccel() { return a; }
	public final double getAlpha() { return alpha; }
	public final double getOmega() { return omega; }
	public final double getTheta() { return theta; }
	public Vessel(Point2D pos, Point2D vel) {
		this.r = pos;
		this.v = vel;
	}
	public final void syncKinematics() {
		double torque = 0;
		Point2D force = Point2D.ZERO;
		for (Force f : forces) {
			Point2D r = f.getPos().subtract(calcCM());
			torque += r.crossProduct(f.getForce()).getZ();
			if (r.magnitude() == 0) force = force.add(f.getForce());
			else {
				r = r.normalize();
				force = force.add(r.multiply(f.getForce().dotProduct(r)));
			}
		}
		forces.clear();
		alpha = torque / calcMoment();
		a = force.multiply(1/calcMass());
	}
	public final void step(double dt) {
		theta += alpha*dt*dt/2 + omega*dt;
		omega += alpha*dt;
		r = r.add(a.multiply(dt*dt/2)).add(v.multiply(dt));
		v = v.add(a.multiply(dt));
	}
	public abstract double calcMoment();
	public abstract double calcMass();
	public abstract Point2D calcCM();
	public abstract void update(double time, Set<Force> forces);
	public abstract void onStep(double time, Set<Vessel> toAdd, Set<Vessel> toClear);
	public abstract Marker genMarker();
}
