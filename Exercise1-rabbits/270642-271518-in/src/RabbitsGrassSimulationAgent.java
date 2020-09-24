import uchicago.src.sim.gui.Drawable;

import javax.swing.ImageIcon;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author 
 * - Celia Benquet - 271518
 * - Artur Jesslen - 270642
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	// CONSTANTS
	public static final double PROBACHANGEDIRECTION = 0.1;

	// Variables
	private int energy;
	private int age; 
	private int x;
	private int y;
	private int dx; 
	private int dy; 
	private RabbitsGrassSimulationSpace rgSpace; 

	// Attributes
	private final double probaChangeDirection = PROBACHANGEDIRECTION;
	
	
	public RabbitsGrassSimulationAgent(int nrj) {
		// Constructor 
		
		energy = nrj;
		age = 0; 
		setRandomDirection(); 
	}
	
	public void draw(SimGraphics G) {
		// Draw rabbits as an image
		G.drawImageToFit((new ImageIcon("img/rabbit.png")).getImage());
	}
	
	public void step(){
		// Step of time of the simulation for the agent 
		
		// Rabbits on the move
		int newX = x + dx;
	    int newY = y + dy;

	    Object2DGrid grid = rgSpace.getCurrentWildlife();
	    // Get new direction
	    newX = (newX + grid.getSizeX()) % grid.getSizeX();
	    newY = (newY + grid.getSizeY()) % grid.getSizeY();


	    // If new cell available 
	    if(tryMove(newX, newY)){
	    	// Eat the grass in the cell 
			energy += rgSpace.eatGrassAt(x,y);
			
			// Probability to change direction of no obstacle encountered
			if (Math.random() < probaChangeDirection) {
				setRandomDirection();
			}
				
		} else{   	
	    	setRandomDirection();	      
		}
			
	    // Aging 
		energy--;
		age++; 
	}
	
	private boolean tryMove(int x_, int y_) {
		// Test if new cell available
		return rgSpace.moveRabbitAt(x,y,x_,y_); 
	}
	
	// GETTERS
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public int getAge() {
		return age;
	}
	
	// SETTERS 
	
	private void setRandomDirection() {
		// Set random direction in one of the 4 available dof 
		
		do {
			dx = (int)Math.floor(Math.random() * 3) - 1;
		    dy = (int)Math.floor(Math.random() * 3) - 1;
		} while(((dx==0) && (dy==0)) || (dx * dy != 0));
	}
	
	public void setX(int x_) {
		x = x_;
	}
	
	public void setY(int y_) {
		y = y_;
	}
	
	public void setEnergy(int nrj) {
		energy = nrj; 
	}
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs) {
		rgSpace = rgs; 
	}
	


}
