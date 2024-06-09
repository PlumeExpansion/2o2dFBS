package me.plume.sim;

import javafx.geometry.Point2D;

public class Force {
	private Point2D pos, force;
	public Force(Point2D pos, Point2D force) {
		this.pos = pos;
		this.force = force;
	}
	public Point2D getPos() { return pos; }
	public Point2D getForce() { return force; }
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Force)) return false;
		Force f = (Force) o;
		return pos.equals(f.getPos()) && force.equals(f.getForce());
	}
}
