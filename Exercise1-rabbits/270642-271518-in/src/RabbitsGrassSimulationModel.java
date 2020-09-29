import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.BasicAction;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 * - Celia Benquet - 271518
 * - Artur Jesslen - 270642
 */
public class RabbitsGrassSimulationModel extends SimModelImpl {		
	
		// CONSTANTS
		// Colors
		private static final Color MUD_COLOR = new Color(102,51,0);
		private static final Color GRASS_COLOR = new Color(0,102,0);
		// Default values
		private static final int GRIDSIZE = 20;
		private static final int NUMINITRABBITS = 2;
		private static final int NUMINITGRASS = 100;
		private static final int GRASSGROWTHRATE = 10;
		private static final int RABBITINITIALENERGY = 500;
		private static final int MAXGRASS = 20;
		private static final int MAXKITTENS = 6;
		private static final int BIRTHTHRESHOLD = 750;
		private static final double MAXENERGYREPRATE = 0.5;
		private static final int PUBERTY = 10;
	
		// Variables
		private Schedule schedule;
		private RabbitsGrassSimulationSpace rgSpace;
		private ArrayList<RabbitsGrassSimulationAgent> rabbitsList;
		private DisplaySurface displayEcosystem;
		
		// Attributes
		private int gridSize = GRIDSIZE;
		private int numInitRabbits = NUMINITRABBITS;
		private int numInitGrass = NUMINITGRASS;
		private int grassGrowthRate = GRASSGROWTHRATE;
		private int maxGrass = MAXGRASS;
		private int maxKittens = MAXKITTENS; 
		private int birthThreshold = BIRTHTHRESHOLD;
		private double maxEnergyRepRate = MAXENERGYREPRATE;
		private int puberty = PUBERTY; 
		
		
	
		// Methods 
		public static void main(String[] args) {
			// Create & Run the simulation
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));

		}
		
		public void setup() {
			rgSpace = null;
			rabbitsList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			if (displayEcosystem != null) {
				displayEcosystem.dispose();
				displayEcosystem = null;
		    }
			
			displayEcosystem = new DisplaySurface(this, "Rabbits Grass Model Window 1");
			registerDisplaySurface("Rabbits Grass Model Window 1", displayEcosystem);
		}

		public void begin() {
			// Initialize the simulation 
			buildModel();
			buildSchedule();
			buildDisplay();
			displayEcosystem.display();
			
		}
		
		public void buildModel() {
			// Initialize environment
			rgSpace = new RabbitsGrassSimulationSpace(gridSize);
			rgSpace.initializeGrass(numInitGrass);
			
			for(int i=0; i < numInitRabbits; i++) {
				addNewRabbit();
			}
		}
		
		public void buildSchedule() {
			// Manage the simulation steps

			class RabbitsGrassSimulationStep extends BasicAction {
				// Execute one step of time of the simulation 
				public void execute() {
					SimUtilities.shuffle(rabbitsList);
					for(int i=0; i < rabbitsList.size(); i++){
						RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
						
						// Rabbits movement and aging
						rgsa.step();					
					}

					// Grass growing
					grassSpreading();
					
					// Rabbits reproduction
					reproduction(); 

					// Rabbits death
					reapDeadRabbits();
					
					// Display updated
					displayEcosystem.updateDisplay(); 		        
				}
		    }
			
			schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
			
			class RabbitsCountLiving extends BasicAction {
				// Count number of rabbits in the simulation
				public void execute(){
					countLivingRabbits();
				}
			}
			schedule.scheduleActionAtInterval(10, new RabbitsCountLiving());
		}
		
		public void buildDisplay() {
			// Create color map to display grass
			ColorMap map = new ColorMap();
		    for(int i = 1; i<maxGrass ; i++){
		    	map.mapColor(i, GRASS_COLOR);
		    }
		    map.mapColor(0, MUD_COLOR);

			Value2DDisplay displayGrass = new Value2DDisplay(rgSpace.getCurrentEcosystem(), map);
		    Object2DDisplay displayRabbits = new Object2DDisplay(rgSpace.getCurrentWildlife());
		    displayRabbits.setObjectList(rabbitsList);
		    displayEcosystem.addDisplayable(displayGrass, "Grass");
		    displayEcosystem.addDisplayable(displayRabbits, "Rabbits");
		    
		}
		
		// Reproduction process for wildlife
		private void reproduction() { 
			// from end of the table so newly created rabbits are not taken into account
			for (int i=rabbitsList.size()-1; i >= 0 ; i--) {
				RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
				// If rabbit has enough energy and is old enough 
				if((rgsa.getEnergy()>=birthThreshold) && (rgsa.getAge()>puberty)) {
					// Random number of kittens in the litter
					int nbKittens = (int)(Math.random()*maxKittens+0.5);
					for (int j=0; j<nbKittens; j++) {
						addNewRabbit();
					}
					// Looses energy (depending on litter size)
					rgsa.setEnergy((int)(rgsa.getEnergy()*(1 - maxEnergyRepRate*nbKittens/maxKittens))); 
				}
			}
		}

		// Grass growth process
		private void grassSpreading() {
			for (int i=0; i < grassGrowthRate; i++) {
				rgSpace.addGrass(maxGrass);
			}
		}
		
		// Add new rabbit randomly in the grid
		private void addNewRabbit() {
			RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(RABBITINITIALENERGY);
			// If empty spot available 
			if (rgSpace.addRabbit(r)) {
				// Add the newborn to the list
				rabbitsList.add(r);
			}
		}

		// Rabbit Death process 
		private void reapDeadRabbits(){
			for(int i = (rabbitsList.size() - 1); i >= 0 ; i--){
		    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		        // If energy too low, the rabbit dies
		    	if(rgsa.getEnergy() <= 0) {
		        	rgSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        	rabbitsList.remove(i);
		        }
			}
		  }
		
		// Count number of living rabbits 
		private int countLivingRabbits(){
		    int livingRabbits = 0;
		    for(int i = 0; i < rabbitsList.size(); i++){
		      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		      if(rgsa.getEnergy() > 0) {
		    	  livingRabbits++;
		      }
		    }
		    return livingRabbits;
		  }

		public String[] getInitParam() {
			// Parameters to be set by users via the Repast UI slider bar
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold", "MaxKittens"};
			return params;
		}

		// GETTERS 
		public String getName() {
			return "Rabbits simulation";
		}
		
		public Schedule getSchedule() {
			return schedule;
		}

		public int getGridSize() {
			return gridSize;
		}
		
		public int getNumInitRabbits() {
			return numInitRabbits;
		}
		
		public int getNumInitGrass() {
			return numInitGrass;
		}
		
		public int getGrassGrowthRate() {
			return grassGrowthRate;
		}
		
		public int getBirthThreshold() {
			return birthThreshold;
		}
		
		public int getMaxKittens() {
			return maxKittens;
		}
		
		// SETTERS
		public void setGridSize(int n) {
			gridSize = n;
		}
		
		public void setNumInitRabbits(int n) {
			numInitRabbits = n;
		}
		
		
		public void setNumInitGrass(int n) {
			numInitGrass = n;
		}
		
		public void setGrassGrowthRate(int n) {
			grassGrowthRate = n;
		}
		
		public void setBirthThreshold(int n) {
			birthThreshold = n;
		}

		public void setMaxKittens(int n) {
			maxKittens = n;
		}
}
