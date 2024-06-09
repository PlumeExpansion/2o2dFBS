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
	private Set<Consumer<Set<Vessel>>> inits = new HashSet<>(), preTicks = new HashSet<>(), 
			postTicks = new HashSet<>(), onPause = new HashSet<>(), onResume = new HashSet<>();
	public Simulation(double dt) {
		this.dt = dt;
		step = () -> {
			vessels.forEach(v -> v.update(time, toAdd, toClear));
			vessels = vessels.stream().filter(v -> !toClear.contains(v)).collect(Collectors.toSet());
			vessels.addAll(toAdd);
			preTicks.forEach(c -> c.accept(vessels));
			vessels.forEach(v -> v.syncKinematics(dt));
			time += dt;
			postTicks.forEach(c -> c.accept(vessels));
		};
	}
	public double getTime() { return time; }
	public double getDt() { return dt; }
	public void start() {
		if (!paused) return;
		time = 0;
		paused = false;
		inits.forEach(c -> c.accept(vessels));
		new Thread(() -> { while (!paused) step.run(); }).start();
	}
	public void pause() {
		if (paused) return;
		paused = true;
		onPause.forEach(c -> c.accept(vessels));
	}
	public void resume() {
		if (!paused) return;
		paused = false;
		onResume.forEach(c -> c.accept(vessels));
		new Thread(() -> { while (!paused) step.run(); }).start();
	}
	public void step() {
		if (!paused) return;
		step.run();
	}
	public boolean isPaused() { return paused; }
	public void addPreTick(Consumer<Set<Vessel>> c) { preTicks.add(c); }
	public void addPostTick(Consumer<Set<Vessel>> c) { postTicks.add(c); }
	public void addInit(Consumer<Set<Vessel>> c) { inits.add(c); }
	public void addOnPause(Consumer<Set<Vessel>> c) { onPause.add(c); }
	public void addOnResume(Consumer<Set<Vessel>> c) { onResume.add(c); }
	public boolean removePreTick(Consumer<Set<Vessel>> c) { return preTicks.remove(c); }
	public boolean removePostTick(Consumer<Set<Vessel>> c) { return postTicks.remove(c); }
	public boolean removeInit(Consumer<Set<Vessel>> c) { return inits.remove(c); }
	public boolean removeOnPause(Consumer<Set<Vessel>> c) { return onPause.remove(c); }
	public boolean removeOnResume(Consumer<Set<Vessel>> c) { return onResume.remove(c); }
	public Set<Marker> getMarkers() { return vessels.stream().map(v -> v.genMarker()).collect(Collectors.toSet()); }
}
