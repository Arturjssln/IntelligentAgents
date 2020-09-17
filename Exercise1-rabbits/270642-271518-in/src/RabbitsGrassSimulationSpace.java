import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid ecosystem;
	
	public RabbitsGrassSimulationSpace(int size) {
		ecosystem = new Object2DGrid(size, size);
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
	
	public int getGrassQuantityAt(int x, int y) {
		return (Integer)ecosystem.getObjectAt(x,y);
	}
}
