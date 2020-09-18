import uchicago.src.sim.gui.Drawable;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int energy;
	private int x;
	private int y;
	
	RabbitsGrassSimulationAgent(int nrj) {
		energy = nrj;
	}
	
	
	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.blue);
	}
	
	public void step(){
		energy--;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public void setX(int x_) {
		x = x_;
	}
	
	public void setY(int y_) {
		y = y_;
	}
	
	

}
