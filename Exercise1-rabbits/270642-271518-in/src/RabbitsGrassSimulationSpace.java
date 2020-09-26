import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author 
 * - Celia Benquet - 271518
 * - Artur Jesslen - 270642
 */

public class RabbitsGrassSimulationSpace {
	
	// Attributes
	private Object2DGrid ecosystem;
	private Object2DGrid wildlife;
	
	// Constructor 
	public RabbitsGrassSimulationSpace(int size) {		
		ecosystem = new Object2DGrid(size, size);
		wildlife = new Object2DGrid(size, size);
		
		// Initialise the grid with 0s 
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				ecosystem.putObjectAt(i, j, 0);
			}
		}
	}
	
	// Put the initial quantity of grass in the grid 
	public void initializeGrass(int n) {
	    for(int i = 0; i < n; i++){
	    	int x, y;
	    	
	    	// Look for an empty cell
	    	do {
	    		x = (int)(Math.random()*(ecosystem.getSizeX()));
	    		y = (int)(Math.random()*(ecosystem.getSizeY()));
	    	} while((Integer)ecosystem.getObjectAt(x,y) != 0);
	    	// Fill the empty cell with a grass object
	    	ecosystem.putObjectAt(x, y, 1);
	    }
	}

	public void addGrass(int maxValue) {
		
		// Look for a random cell
		int x = (int)(Math.random()*(ecosystem.getSizeX()));
		int y = (int)(Math.random()*(ecosystem.getSizeY()));
		int grassValue = (Integer)ecosystem.getObjectAt(x,y);
		
		// Add one to this cell
		if (grassValue < maxValue) {
			ecosystem.putObjectAt(x, y, grassValue+1);
		}
	}

	// Count quantity of grass per cell
	public int countGrass() {
		int grassQuantity = 0;
		for (int i=0; i < ecosystem.getSizeX(); i++) {
			for (int j=0; j < ecosystem.getSizeY(); j++) {
				grassQuantity += (Integer)ecosystem.getObjectAt(i,j);
			}
		}
		return grassQuantity;
	}
	
	public boolean isCellOccupied(int x, int y){
		return (wildlife.getObjectAt(x,y) != null);
	}
	
	//Add rabbit to simulation (return false if it was not added)
	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit){
		
	    boolean success = false;
	    int count = 0;
	    // Maximal number of tries to find an empty cell
	    int countLimit = 10 * wildlife.getSizeX() * wildlife.getSizeY();
		
		// Try adding rabbit until success
		while(!success && (count < countLimit)) {
	    	// Randomly select a cell
	    	int x = (int)(Math.random()*(wildlife.getSizeX()));
		    int y = (int)(Math.random()*(wildlife.getSizeY()));
		    // Check if it is occupied		    
		    if(!isCellOccupied(x,y)) {
		    	// Udpate the rabbit position
		    	wildlife.putObjectAt(x, y, rabbit);
		    	rabbit.setX(x);
		    	rabbit.setY(y);
		    	rabbit.setRabbitsGrassSimulationSpace(this);
		    	success = true;
		    }
		    count++;
	    }
	    return success;
	  }
	
	public void removeRabbitAt(int x, int y) {
		wildlife.putObjectAt(x, y, null);
	}
	
	// Return eaten quantity of grasss
	public int eatGrassAt(int x, int y) {	
		int energy = getGrassQuantityAt(x, y); 
		ecosystem.putObjectAt(x, y, 0);
		return energy;	
	}
	
	// Move rabbit to the given new position
	public boolean moveRabbitAt(int x, int y, int x_, int y_){		
	    if(!isCellOccupied(x_, y_)){
	    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)wildlife.getObjectAt(x, y);
	    	removeRabbitAt(x,y);
	    	rgsa.setX(x_);
	    	rgsa.setY(y_);
	    	wildlife.putObjectAt(x_, y_, rgsa);
	    	return true;
	    }
	    return false;
	}

	// GETTERS 
	public int getGrassQuantityAt(int x, int y) {
		return (Integer)ecosystem.getObjectAt(x, y);
	}
	
	public Object2DGrid getCurrentEcosystem(){
	    return ecosystem;
	}
	
	public Object2DGrid getCurrentWildlife(){
		return wildlife;
	}
	
}
