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
	
	public RabbitsGrassSimulationSpace(int size) {
		ecosystem = new Object2DGrid(size, size);
		wildlife = new Object2DGrid(size, size);
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				ecosystem.putObjectAt(i, j, 0);
			}
		}
	}
	
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

	public void addGrass() {
		// Look for a random cell
		int x = (int)(Math.random()*(ecosystem.getSizeX()));
		int y = (int)(Math.random()*(ecosystem.getSizeY()));
		int grassValue = (Integer)ecosystem.getObjectAt(x,y);
		// Add one to this cell
		ecosystem.putObjectAt(x, y, grassValue+1);
	}

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
	
	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit){
	    boolean success = false;
	    int count = 0;
	    int countLimit = 10 * wildlife.getSizeX() * wildlife.getSizeY();
	    
	    
	    while(!success && (count < countLimit)) {
	    	int x = (int)(Math.random()*(wildlife.getSizeX()));
		    int y = (int)(Math.random()*(wildlife.getSizeY()));
		    if(!isCellOccupied(x,y)) {
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
	
	public int eatGrassAt(int x, int y) {
		// Return eaten quantity of grass
		int energy = getGrassQuantityAt(x, y); 
		ecosystem.putObjectAt(x, y, 0); //TODO!! eat a small part of the grass only 
		return energy;	
	}
	
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
