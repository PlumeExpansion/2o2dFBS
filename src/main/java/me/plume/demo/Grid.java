package me.plume.demo;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Grid {
	static final double maxSep = 400;
	static final int step = 5;
	static final double minWidth = 0.1, maxWidth = 1;
	static final double maxOpacity = .5;
	static final String hex = "#b0e1ff";
	static final Color fullOpac = Color.web(hex, maxOpacity);
	public static void render(Scene scene, GraphicsContext c, Point2D o, double s) {
		c.setStroke(Color.web(hex, maxOpacity));
		c.setLineWidth(maxWidth);
		vLine(scene, c, o.getX()*s);
		hLine(scene, c, -o.getY()*s);
		double sep = maxSep*(1+1/step)/step*s;
		if (sep>maxSep) {
			sep *= Math.pow(step, (int) (Math.log(maxSep/sep)/Math.log(step))-1);
		} else if (sep<maxSep/step) {
			sep *= Math.pow(step, (int) (Math.log(maxSep/step/sep)/Math.log(step))+1);
		}
		double x = o.getX()*s%sep - sep;
		double y = -o.getY()*s%sep - sep;
		double f = (sep/maxSep-1/step)/(1-1/step);
		Color dynamOpac = Color.web(hex, maxOpacity*f);
		double w = (maxWidth-minWidth)*f+minWidth;
		while (x <= scene.getWidth()) {
			for (int i = 0; i < step-1; i++) {
				x+=sep/step;
				c.setLineWidth(w);
				c.setStroke(dynamOpac);
				vLine(scene, c, x);
			}
			x+=sep/step;
			c.setLineWidth(maxWidth);
			c.setStroke(fullOpac);
			vLine(scene, c, x);
		}
		while (y <= scene.getHeight()) {
			for (int i = 0; i < step-1; i++) {
				y+=sep/step;
				c.setLineWidth(w);
				c.setStroke(dynamOpac);
				hLine(scene, c, y);
			}
			y+=sep/step;
			c.setLineWidth(maxWidth);
			c.setStroke(fullOpac);
			hLine(scene, c, y);
		}
	}
	static void vLine(Scene scene, GraphicsContext c, double x) {
		c.strokeLine(x, 0, x, scene.getHeight());
	}
	static void hLine(Scene scene, GraphicsContext c, double y) {
		c.strokeLine(0, y, scene.getWidth(), y);
	}
}
