import uchicago.src.sim.gui.Drawable;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private static final int BIRTHTHRESHOLD = 100; // TODO: remove this variable from Agent

	private int energy;
	private int birthThreshold = BIRTHTHRESHOLD; // TODO: remove this variable from Agent
	private int x;
	private int y;
	private int dx; 
	private int dy; 
	private RabbitsGrassSimulationSpace rgSpace; 
	
	
	public RabbitsGrassSimulationAgent(int nrj) {
		energy = nrj;
		setRandomDirection(); 
	}
	
	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.gray);
	}
	
	public void step(boolean reproduce){
		
		// Rabbits on the move
		int newX = x + dx;
	    int newY = y + dy;

	    Object2DGrid grid = rgSpace.getCurrentWildlife();
	    newX = (newX + grid.getSizeX()) % grid.getSizeX();
	    newY = (newY + grid.getSizeY()) % grid.getSizeY();

	    if(tryMove(newX, newY)){
	    	energy += rgSpace.eatGrassAt(x,y); 
	    	setRandomDirection();
	    } else{   	
	    	setRandomDirection();	      
	    }
	    
	    // Reproduction
	    if (energy >= birthThreshold) { // TODO: remove this variable from Agent
	    	reproduce = true;  
	    	energy = (int)energy/2; //TODO!!!!!!!!
	    }
	    
	    // Aging 
		energy--;
	}
	
	private boolean tryMove(int x_, int y_) {
		return rgSpace.moveRabbitAt(x,y,x_,y_); 
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
	
	private void setRandomDirection() {
		do {
			dx = (int)Math.floor(Math.random() * 3) - 1;
		    dy = (int)Math.floor(Math.random() * 3) - 1;
		}while((dx==0) && (dy==0));
	}
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs) {
		rgSpace = rgs; 
	}
	


}
