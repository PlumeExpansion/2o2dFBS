package me.plume.demo;

import java.util.Optional;
import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.plume.sim.Marker;
import me.plume.sim.Simulation;

public class GridPortal {
	private Color bgColor = Color.BLACK;
	private double statDelay = 1.0 /5;
	private double scaleFactor = 1.1;
	private double trackSceneDist = 50;
	private String title = "Grid Title";
	public void setBackgroundColor(Color color) { this.bgColor = color; }
	public void setStatFreq(double statFreq) { statDelay = 1.0 / statFreq; }
	public void setTrackSceneDist(double trackSceneDist) { this.trackSceneDist = trackSceneDist; }
	public void setTitle(String title) { this.title = title; }
	private Simulation sim;
	private double delay;
	private Stage window;
	private Scene scene;
	private Canvas canvas;
	private GraphicsContext c;
	private Set<Marker> markers;
	private Point2D camOffset = Point2D.ZERO;
	private double defaultScale, scale;
	public GridPortal(Stage window, Scene scene, Simulation sim, double fps, Point2D offset, double scale) {
		this.window = window;
		this.scene = scene;
		this.sim = sim;
		if (window.getTitle() != null) title = window.getTitle();
		delay = 1.0/fps;
		this.camOffset = offset;
		this.scale = defaultScale = scale;
		initSim();
		canvas = new Canvas();
		c = canvas.getGraphicsContext2D();
		if (!(scene.getRoot() instanceof Pane)) scene.setRoot(new Pane());
		((Pane) scene.getRoot()).getChildren().add(canvas);
		addSceneListeners();
		canvas.setWidth(scene.getWidth());
		canvas.setHeight(scene.getHeight());
		window.setScene(scene);
		window.setOnCloseRequest(e -> sim.pause());
	}
	private long start, pause, frameN, elapsedFrameN, statCount, statN, elapsedStatN, buffer, now, last;
	private double fpsSum, fpsAvg, bufferSum, bufferAvg;
	private void initSim() {
		sim.addInit(vs -> { start = System.currentTimeMillis(); frameN = statN = 0; });
		sim.addOnPause(vs -> pause = System.currentTimeMillis());
		sim.addOnResume(vs -> {
			start += System.currentTimeMillis()-pause;
		});
		sim.addOnTick(vs -> {
			if (!sim.isPaused()) {
				elapsedFrameN = (long) (sim.getTime()/delay);
				if (elapsedFrameN < frameN) return;
				frameN = elapsedFrameN + 1;
				buffer = (long) (sim.getTime()*1000-(System.currentTimeMillis()-start));
				if (buffer > 0) try { Thread.sleep((long) (sim.getTime()*1000-(System.currentTimeMillis()-start))); } catch (Exception e) { e.printStackTrace(); }
				now = System.currentTimeMillis();
				fpsSum += 1000.0/(now-last);
				last = now;
				bufferSum += buffer;
				statCount++;
				elapsedStatN = (long) ((System.currentTimeMillis()-start)/1000.0/statDelay);
				if (elapsedStatN >= statN) {
					statN = elapsedStatN + 1;
					fpsAvg = fpsSum / statCount;
					bufferAvg = bufferSum / statCount;
					fpsSum = bufferSum = statCount = 0;
				}
			} else pause += sim.getDt()*1000;
			syncSimToScene();
		});
	}
	public void syncSimToScene() {
		markers = sim.getMarkers();
		Platform.runLater(() -> {
			if (sim.isPaused()) window.setTitle("[%s] - {steps: %s} - {time: %s s} - {dt: %s ms}".formatted(
					title, sim.getStepCount(), round(sim.getTime(), 1), round(sim.getDt()*1000, 1)
					));
			else window.setTitle("[%s] - {steps: %s} - {fps: %s} - {buffer: %s ms} - {%s s behind}".formatted(
				title, sim.getStepCount(), round(fpsAvg, 1), round(Math.max(0, bufferAvg), 1), bufferAvg>0? 0 : round(-bufferAvg/1000, 1)
					));
			if (track != null) {
				Optional<Marker> marker = markers.stream().filter(m -> m.getVessel() == track.getVessel()).findFirst();
				if (marker.isPresent()) track(marker.get());
				else track(null);
			}
			render();
		});
	}
	private Point2D trackOffset = Point2D.ZERO;
	private Point2D dragStartCamOffset, dragStartSceneCoord;
	private double scrollStartScale, inverseScaleDifference;
	private boolean mouseCenteredScale;
	private Marker track;
	private void addSceneListeners() {
		scene.widthProperty().addListener((obv, ov, nv) -> {
			canvas.setWidth(nv.doubleValue());
			camOffset = camOffset.add((nv.doubleValue()-ov.doubleValue())/scale/2, 0);
			render();
		});
		scene.heightProperty().addListener((obv, ov, nv) -> {
			canvas.setHeight(nv.doubleValue());
			camOffset = camOffset.add(0, -(nv.doubleValue()-ov.doubleValue())/scale/2);
			render();
		});
		var pressHandler = scene.getOnMousePressed();
		scene.setOnMousePressed(e -> {
			if (pressHandler != null) pressHandler.handle(e);
			if (e.getButton() == MouseButton.MIDDLE) {
				dragStartCamOffset = camOffset;
				dragStartSceneCoord = new Point2D(e.getSceneX(), e.getSceneY());
			} else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() > 1) {
				Optional<Marker> marker = markers.stream().filter(m -> m.getPos().add(camOffset).add(trackOffset)
						.multiply(scale).distance(e.getSceneX(), -e.getSceneY()) <= trackSceneDist)
				.sorted().findFirst();
				if (marker.isPresent()) track(marker.get());
				else track(null);
				render();
			}
		});
		var dragHandler = scene.getOnMouseDragged();
		scene.setOnMouseDragged(e -> {
			if (dragHandler != null) dragHandler.handle(e);
			if (e.getButton() == MouseButton.MIDDLE) {
				if (dragStartSceneCoord == null) dragStartSceneCoord = new Point2D(e.getSceneX(), e.getSceneY());
				camOffset = dragStartCamOffset.add(
						(e.getSceneX()-dragStartSceneCoord.getX())/scale, 
						-(e.getSceneY()-dragStartSceneCoord.getY())/scale);
				render();
			}
		});
		var releaseHandler = scene.getOnMouseReleased();
		scene.setOnMouseReleased(e -> {
			if (releaseHandler != null) releaseHandler.handle(e);
		});
		var scrollHandler = scene.getOnScroll();
		scene.setOnScroll(e -> {
			if (scrollHandler != null) scrollHandler.handle(e);
			if (e.getDeltaY() != 0) {
				scrollStartScale = scale;
				if (e.getDeltaY() > 0) scale *= scaleFactor;
				else scale /= scaleFactor;
				inverseScaleDifference = 1/scrollStartScale - 1/scale;
				if (mouseCenteredScale) 
					camOffset = camOffset.add(
						-e.getSceneX()*inverseScaleDifference, 
						e.getSceneY()*inverseScaleDifference);
				else
					camOffset = camOffset.add(
						-scene.getWidth()/2*inverseScaleDifference, 
						scene.getHeight()/2*inverseScaleDifference);
				resetDrag();
				render();
			}
		});
		var keyPressHandler = scene.getOnKeyPressed();
		scene.setOnKeyPressed(e -> {
			if (keyPressHandler != null) keyPressHandler.handle(e);
			if (e.getCode() == KeyCode.M) mouseCenteredScale = !mouseCenteredScale;
			else if (e.getCode() == KeyCode.DECIMAL) {
				scale = defaultScale;
				center();
				render();
			} else if (e.getCode() == KeyCode.ESCAPE && track != null) track(null);
			else if (e.getCode() == KeyCode.SPACE) {
				if (sim.isPaused()) sim.resume();
				else sim.pause();
			} else if (e.getCode() == KeyCode.PERIOD) sim.step();
		});
	}
	public Simulation getSim() { return sim; }
	private void track(Marker marker) {
		if (marker != null) {
			if (track == null) center();
			track = marker;
			trackOffset = track.getPos().multiply(-1);
		} else {
			track = null;
			camOffset = camOffset.add(trackOffset);
			resetDrag();
			trackOffset = Point2D.ZERO;
		}
	}
	private void center() {
		camOffset = new Point2D(scene.getWidth()/2/scale, -scene.getHeight()/2/scale);
		resetDrag();
	}
	private void resetDrag() {
		dragStartCamOffset = camOffset;
		dragStartSceneCoord = null;
	}
	private void render() {
		c.setFill(bgColor);
		c.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		Grid.render(scene, c, camOffset.add(trackOffset), scale);
		markers.forEach(m -> m.render(c, camOffset.add(trackOffset), scale));
	}
	private double round(double r, int n) {
		return Math.round(r*Math.pow(10, n))/Math.pow(10, n);
	}
}
