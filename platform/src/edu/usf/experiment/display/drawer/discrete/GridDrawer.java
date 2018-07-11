package edu.usf.experiment.display.drawer.discrete;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.vividsolutions.jts.geom.Coordinate;

import edu.usf.experiment.display.drawer.Drawer;
import edu.usf.experiment.display.drawer.Scaler;
import edu.usf.experiment.universe.GridUniverse;

public class GridDrawer implements Drawer {
	
	private GridUniverse u;

	public GridDrawer(GridUniverse bu){
		u = bu;
	}

	@Override
	public void draw(Graphics g, Scaler s) {
		g.setColor(Color.GRAY);
		for (float x = 0; x <= u.getGridWidth(); x++){
			Point p0 = s.scale(new Coordinate((double)x, 0));
			Point p1 = s.scale(new Coordinate((double)x, u.getGridHeight()));
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		for (float y = 0; y <= u.getGridHeight(); y++){
			Point p0 = s.scale(new Coordinate(0, y));
			Point p1 = s.scale(new Coordinate(u.getGridWidth(), y));
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}	
	}
	
	@Override
	public void clearState() {
		
	}
}