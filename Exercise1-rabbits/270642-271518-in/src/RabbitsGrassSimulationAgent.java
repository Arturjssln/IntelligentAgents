import uchicago.src.sim.gui.Drawable;
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
	
	
	public void draw(SimGraphics arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void setX(int x_) {
		x = x_;
	}
	
	public void setY(int y_) {
		y = y_;
	}

}
