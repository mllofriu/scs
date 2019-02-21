package edu.usf.ratsim.model.pablo.multiscale_memory.drawers;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import edu.usf.experiment.display.GuiUtils;
import edu.usf.experiment.display.drawer.Drawer;
import edu.usf.experiment.display.drawer.Scaler;
import edu.usf.experiment.universe.BoundedUniverse;
import edu.usf.experiment.universe.Universe;
import edu.usf.micronsl.port.twodimensional.sparse.Entry;
import edu.usf.micronsl.port.twodimensional.sparse.Float2dSparsePort;
import edu.usf.ratsim.nsl.modules.cell.PlaceCell;

public class ValueTraceDrawer  extends Drawer {

	Coordinate[] centers;
	Float2dSparsePort stateValues;
	public int distanceOption = 1; //0 to use radius and diam, 1 to use minDist and choose automatically
	float minDist = Float.POSITIVE_INFINITY;
	int radius = 2;
	
	
	float maxValue = Float.NEGATIVE_INFINITY;
	
	HashMap<Entry,Float> nonZero = new HashMap<>();
	

	public ValueTraceDrawer(List<PlaceCell> pcs,Float2dSparsePort stateValuePort) {
		centers = new Coordinate[pcs.size()];
		for(int i=0;i<pcs.size();i++){
			centers[i]= pcs.get(i).getPreferredLocation();
		}
		stateValues = stateValuePort;
		
		for(int i=0;i<pcs.size();i++)
			for(int j=i+1;j<pcs.size();j++){
				double dx = centers[i].x-centers[j].x;
				double dy = centers[i].y-centers[j].y;
				minDist = (float)Math.min(minDist, dx*dx+dy*dy);
			}
		minDist = (float)Math.sqrt(minDist)/2;
		
	}

	@Override
	public void draw(Graphics g, java.awt.geom.Rectangle2D.Float panelCoordinates) {
		if(!doDraw) return;
		
		
		
		
		BoundedUniverse bu = (BoundedUniverse)Universe.getUniverse();
		Scaler s = new Scaler(bu.getBoundingRect(), panelCoordinates, true);
		
		int coords[][] = s.scale(centers);
		
		int r = radius;
		if(distanceOption==1) r = s.scaleDistanceX(minDist);
		int d = 2*r;

		
		
		
		for(int i=0; i <stateValues.getNRows();i++){
			
			Float value = nonZero.get(new Entry(i, 0));
			if(value==null) value = 0f;
			
			g.setColor(getColor(value,maxValue));
			g.fillOval(coords[0][i]-r, coords[1][i]-r, d, d);
			
		}
		
		g.setColor(Color.BLACK);
		g.drawString("MAX:   " + maxValue, 20, 20);

	}

	@Override
	public void endEpisode() {
		
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		nonZero.clear();
//		if(stateValues.getNonZero().size() > 0 ) {
//			System.out.println("NON ZERO!!!!!!!!!!");
//		}
		nonZero.putAll(stateValues.getNonZero());
		maxValue = GuiUtils.findMaxInMap(nonZero);
		
	}
	

	
	public Color getColor(float val,float max){
		
		float h = val < 0 ? 0.66f : 0f;
		float s = (float)Math.abs(val)/max;
		float b = 0.8f;
		float alpha = 0.5f;

		return  GuiUtils.getHSBAColor(h,s,b,alpha);
	}
	
	
	public void setRadius(int r){
		radius = r;
	}
}
