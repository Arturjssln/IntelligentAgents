import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
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
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				wildlife.putObjectAt(i, j, false);
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
	
	public int getGrassQuantityAt(int x, int y) {
		return (Integer)ecosystem.getObjectAt(x,y);
	}
	
	public Object2DGrid getCurrentEcosystem(){
	    return ecosystem;
	}
	
	public boolean isCellOccupied(int x, int y){
		return (boolean)wildlife.getObjectAt(x, y);
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
		    	success = true;
		    }
		    count++;
	    }
	    return success;
	  }
	
	
}
