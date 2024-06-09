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
	private long last, elapsed, buffer, frameN, statN, statCount;
	private double fpsSum, fpsAvg, bufferSum, bufferAvg;
	private Scene scene;
	private Pane pane;
	private Canvas canvas;
	private GraphicsContext c;
	private Set<Marker> markers;
	private Point2D offset;
	private double scale;
	public GridPortal(Stage window, Simulation sim, double fps, double width, double height) {
		this.sim = sim;
		delay = 1.0/fps;
		sim.addInit(s -> frameN = statN = 0);
		sim.addPostTick(s -> {
			if (s.getTime()/delay < frameN) return;
			frameN++;
			elapsed = System.currentTimeMillis()-last;
			buffer = (long) (delay*1000-elapsed);
			if (last != 0 && buffer >= 0) {
				try { Thread.sleep(buffer); } catch (Exception e) { e.printStackTrace(); }
			}
			last = System.currentTimeMillis();
			fpsSum += 1000.0/(elapsed+buffer);
			bufferSum += buffer;
			statCount++;
			if (s.getTime()/STAT_DELAY >= statN) {
				fpsAvg = fpsSum / statCount;
				bufferAvg = bufferSum / statCount;
				fpsSum = bufferSum = statCount = 0;
				statN++;
			}
			markers = s.getMarkers();
			Platform.runLater(() -> window.setTitle("[%s] - {fps: %s} - {buffer: %s}".formatted(
					TITLE, ((int) fpsAvg*100)/100.0, ((int) bufferAvg*100)/100.0)
			));
			render();
		});
		canvas = new Canvas();
		c = canvas.getGraphicsContext2D();
		pane = new Pane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		scene.widthProperty().addListener((obv, ov, nv) -> {
			canvas.setWidth(nv.doubleValue());
			render();
		});
		scene.heightProperty().addListener((obv, ov, nv) -> {
			canvas.setHeight(nv.doubleValue());
			render();
		});
		canvas.setWidth(width);
		canvas.setHeight(height);
		window.setScene(scene);
		window.setOnCloseRequest(e -> sim.pause());
	}
	public Simulation getSim() { return sim; }
	private void render() {
		c.setFill(BG_COLOR);
		c.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		markers.forEach(m -> m.render(c, offset, scale));
	}
}
