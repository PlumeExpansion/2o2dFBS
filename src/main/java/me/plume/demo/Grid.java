package me.plume.demo;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Grid {
	static final double maxSep = 200;
	static final int step = 2;
	static final double minWidth = 0.1, maxWidth = 1;
	static final double maxOpacity = 0.5;
	static final double axisOpacity = 1;
	static final double textOpacity = 1;
	static final String hex = "#abe4ff";
	static final Color maxOpac = Color.web(hex, maxOpacity);
	static final Color textOpac = Color.web(hex, textOpacity);
	static final Color axisOpac = Color.web(hex, axisOpacity);
	static final Point2D textOffset = new Point2D(2, -2);
	public static void render(Scene scene, GraphicsContext c, Point2D o, double s) {
		c.setStroke(axisOpac);
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
			c.setStroke(maxOpac);
			vLine(scene, c, x);
			c.setStroke(textOpac);
			c.strokeText(Double.toString(GridPortal.round(x/s-o.getX(), 1)), x+textOffset.getX(), scene.getHeight()+textOffset.getY());
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
			c.setStroke(maxOpac);
			hLine(scene, c, y);
			c.setStroke(textOpac);
			c.strokeText(Double.toString(GridPortal.round(-y/s-o.getY(), 1)), textOffset.getX(), y+textOffset.getY());
		}
	}
	static void vLine(Scene scene, GraphicsContext c, double x) {
		c.strokeLine(x, 0, x, scene.getHeight());
	}
	static void hLine(Scene scene, GraphicsContext c, double y) {
		c.strokeLine(0, y, scene.getWidth(), y);
	}
}
