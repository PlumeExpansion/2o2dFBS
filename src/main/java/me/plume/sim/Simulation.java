package me.plume.sim;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class Simulation {
	private Set<Vessel> vessels, toAdd, toClear;
	private double dt, time;
	private boolean paused;
	private Runnable step;
	public Simulation(double dt) {
		this.dt = dt;
		step = () -> {
			vessels.forEach(v -> v.update(time, toAdd, toClear));
			vessels = vessels.stream().filter(v -> !toClear.contains(v)).collect(Collectors.toSet());
			vessels.addAll(toAdd);
			preTick();
			vessels.forEach(v -> v.syncKinematics(dt));
			time += dt;
			postTick();
		};
	}
	public double getTime() { return time; }
	public double getDt() { return dt; }
	public void start() {
		if (!paused) return;
		time = 0;
		init();
		resume();
	}
	public void pause() { paused = true; }
	public void resume() {
		if (!paused) return;
		paused = false;
		new Thread(() -> { while (!paused) step.run(); }).start();
	}
	public void step() {
		if (!paused) return;
		step.run();
	}
	public abstract void init();
	public abstract void preTick();
	public abstract void postTick();
}
