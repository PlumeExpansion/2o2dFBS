package me.plume.sim;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Simulation {
	private Set<Vessel> vessels = new HashSet<>(), toAdd = new HashSet<>(), toClear = new HashSet<>();
	private double dt, time;
	private boolean paused = true;
	private Runnable step;
	private Set<Consumer<Simulation>> inits = new HashSet<>(), preTicks = new HashSet<>(), 
			postTicks = new HashSet<>();
	public Simulation(double dt) {
		this.dt = dt;
		step = () -> {
			vessels.forEach(v -> v.update(time, toAdd, toClear));
			vessels = vessels.stream().filter(v -> !toClear.contains(v)).collect(Collectors.toSet());
			vessels.addAll(toAdd);
			preTicks.forEach(c -> c.accept(this));
			vessels.forEach(v -> v.syncKinematics(dt));
			time += dt;
			postTicks.forEach(c -> c.accept(this));
		};
	}
	public double getTime() { return time; }
	public double getDt() { return dt; }
	public void start() {
		if (!paused) return;
		time = 0;
		inits.forEach(c -> c.accept(this));
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
	public void addPreTick(Consumer<Simulation> c) { preTicks.add(c); }
	public void addPostTick(Consumer<Simulation> c) { postTicks.add(c); }
	public void addInit(Consumer<Simulation> c) { inits.add(c); }
	public boolean removePreTick(Consumer<Simulation> c) { return preTicks.remove(c); }
	public boolean removePostTick(Consumer<Simulation> c) { return postTicks.remove(c); }
	public boolean removeInit(Consumer<Simulation> c) { return inits.remove(c); }
	public Set<Marker> getMarkers() { return vessels.stream().map(v -> v.genMarker()).collect(Collectors.toSet()); }
}
