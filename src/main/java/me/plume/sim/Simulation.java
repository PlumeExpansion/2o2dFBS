package me.plume.sim;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Simulation {
	private Set<Vessel> vessels = new HashSet<>(), toAdd = new HashSet<>(), toClear = new HashSet<>();
	private double defaultDt, dt, stepped, time, maxTravel, maxV;
	public void setMaxTravel(double maxTravel) { this.maxTravel = maxTravel; }
	private boolean paused = true;
	private Runnable step;
	private Set<Consumer<Set<Vessel>>> inits = new HashSet<>(), onTicks = new HashSet<>(), 
			onPause = new HashSet<>(), onResume = new HashSet<>();
	private int stepCount, stepCountFinal;
	public int getStepCount() { return stepCountFinal; }
	public Simulation(double deltaT) {
		defaultDt = deltaT;
		step = () -> {
			vessels.forEach(v -> v.update(time, v.getForces()));
			vessels.forEach(v -> v.syncKinematics());
			if (!vessels.isEmpty()) {
				stepCount = 1;
				if (maxTravel != 0) {
					stepped = 0;
					dt = findDt();
					while (!Double.isNaN(dt) && stepped + dt < defaultDt) {
						stepped += dt;
						stepCount++;
						step(dt);
						if (vessels.isEmpty()) return;
						dt = findDt();
					}
					dt = defaultDt - stepped;
					if (dt != 0) step(dt);
				} else step(defaultDt);
				stepCountFinal = stepCount;
			} else {
				time += defaultDt;
				stepCountFinal = 0;
			}
			onTicks.forEach(c -> c.accept(vessels));
		};
	}
	private double findDt() {
		maxV = vessels.stream().mapToDouble(v -> v.getVel().magnitude())
				.reduce((a, b) -> a>b? a : b).getAsDouble();
		return maxTravel / maxV;
	}
	private void step(double dt) {
		vessels.forEach(v -> v.step(dt));
		time += dt;
		vessels.forEach(v -> v.onStep(time, toAdd, toClear));
		vessels = vessels.stream().filter(v -> !toClear.contains(v)).collect(Collectors.toSet());
		vessels.addAll(toAdd);
	}
	public double getTime() { return time; }
	public double getDt() { return defaultDt; }
	public void start() {
		if (!paused) return;
		paused = false;
		time = 0;
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
	public void addOnTick(Consumer<Set<Vessel>> c) { onTicks.add(c); }
	public void addInit(Consumer<Set<Vessel>> c) { inits.add(c); }
	public void addOnPause(Consumer<Set<Vessel>> c) { onPause.add(c); }
	public void addOnResume(Consumer<Set<Vessel>> c) { onResume.add(c); }
	public boolean removeOnTick(Consumer<Set<Vessel>> c) { return onTicks.remove(c); }
	public boolean removeInit(Consumer<Set<Vessel>> c) { return inits.remove(c); }
	public boolean removeOnPause(Consumer<Set<Vessel>> c) { return onPause.remove(c); }
	public boolean removeOnResume(Consumer<Set<Vessel>> c) { return onResume.remove(c); }
	public Set<Marker> getMarkers() { return vessels.stream().map(v -> v.genMarker()).collect(Collectors.toSet()); }
}
