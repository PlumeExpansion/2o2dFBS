package me.plume.demo;

import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.plume.sim.Marker;
import me.plume.sim.Simulation;

public class GridPortal {
	private static final Color BG_COLOR = Color.BLACK;
	private static final double STAT_DELAY = 1.0 / 5;
	private static final String TITLE = "Grid Portal";
	private Simulation sim;
	private double delay;
	private long start, pause, frameN, statCount, statN, buffer, now, last;
	private double fpsSum, fpsAvg, bufferSum, bufferAvg;
	private Scene scene;
	private Canvas canvas;
	private GraphicsContext c;
	private Set<Marker> markers;
	private Point2D offset = Point2D.ZERO;
	private double scale = 1;
	public GridPortal(Stage window, Scene scene, Simulation sim, double fps, Point2D offset, double scale) {
		this.sim = sim;
		this.scene = scene;
		delay = 1.0/fps;
		this.offset = offset;
		this.scale = scale;
		sim.addInit(vs -> { start = System.currentTimeMillis(); frameN = statN = 0; });
		sim.addOnPause(vs -> pause = System.currentTimeMillis());
		sim.addOnResume(vs -> start += System.currentTimeMillis()-pause);
		sim.addPostTick(vs -> {
			if (sim.isPaused()) return;
			if (sim.getTime()/delay < frameN) return;
			frameN++;
			buffer = (long) (sim.getTime()*1000-(System.currentTimeMillis()-start));
			if (buffer > 0) try { Thread.sleep((long) (sim.getTime()*1000-(System.currentTimeMillis()-start))); } catch (Exception e) { e.printStackTrace(); }
			now = System.currentTimeMillis();
			fpsSum += 1000.0/(now-last);
			last = now;
			bufferSum += buffer;
			statCount++;
			if (sim.getTime()/STAT_DELAY >= statN) {
				fpsAvg = fpsSum / statCount;
				bufferAvg = bufferSum / statCount;
				fpsSum = bufferSum = statCount = 0;
				statN++;
			}
			markers = sim.getMarkers();
			Platform.runLater(() -> {
				window.setTitle("[%s] - {fps: %s} - {buffer: %s ms} - {%s s behind}".formatted(
					TITLE, round(fpsAvg, 1), round(bufferAvg, 1), bufferAvg>0? 0 : round(-bufferAvg/1000, 1)
				));
				render();
			});
		});
		canvas = new Canvas();
		c = canvas.getGraphicsContext2D();
		((Pane) scene.getRoot()).getChildren().add(canvas);
		scene.widthProperty().addListener((obv, ov, nv) -> {
			canvas.setWidth(nv.doubleValue());
			render();
		});
		scene.heightProperty().addListener((obv, ov, nv) -> {
			canvas.setHeight(nv.doubleValue());
			render();
		});
		canvas.setWidth(scene.getWidth());
		canvas.setHeight(scene.getHeight());
		window.setScene(scene);
		window.setOnCloseRequest(e -> sim.pause());
	}
	public Simulation getSim() { return sim; }
	private void render() {
		c.setFill(BG_COLOR);
		c.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		markers.forEach(m -> m.render(c, offset, scale));
	}
	private double round(double r, int n) {
		return Math.round(r*Math.pow(10, n))/Math.pow(10, n);
	}
}
