import java.awt.Color;
import java.io.IOException;
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

// Log info
import java.time.Clock;
import java.time.Instant;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
		private String fileName;
		
		
	
		// Methods 
		public static void main(String[] args) {
			// Create & Run the simulation
			
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			
			// Do "not" modify the following lines of parsing arguments
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));

			// Prepare log file
			Clock clock = Clock.systemDefaultZone();
			Instant instant = clock.instant();
			model.fileName = "log/stats-"+instant.toString()+".csv";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(model.fileName, true));
				writer.write("Rabbits,Grass\n");
				writer.close();
			} catch (IOException e) {
				System.out.println("Error while writing header on log file");
			}
		}
		
		public void setup() {
			// Reset the simulation
			
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
			// Initialise the simulation 
			
			buildModel();
			buildSchedule();
			buildDisplay();
			displayEcosystem.display();
			
		}
		
		public void buildModel() {
			// Initialise the environment, Create grid, Place rabbits and grass
			
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

			class LogStats extends BasicAction {
				// Count living rabbits to get population plot
				public void execute(){
					logStats();
				}
			}
			schedule.scheduleActionAtInterval(1, new LogStats());

		}
		
		public void buildDisplay() {
			// Display wildlife and ecosystem in the grid 

			// Create color map for display 
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
		
		private void reproduction() { 
			// Reproduction process for wildlife
			
			//TODO! t'avais une explication mais je pense pas que ce soit ca c'est
			//peut etre une histoire d'acces a un array quand t'en enleve dans la mort ??
			
			
			
			for (int i=rabbitsList.size()-1; i >= 0 ; i--) {
				RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
				// If rabbit has enough energy and is old enough 
				if((rgsa.getEnergy()>=birthThreshold) && (rgsa.getAge()>puberty)) {
					// Random number of kittens in the litter
					int nbKittens = (int)(Math.random()*maxKittens);
					for (int j=0; j<nbKittens; j++) {
						addNewRabbit();
					}
					// Looses energy, depending on litter size
					rgsa.setEnergy((int)(rgsa.getEnergy()*(1 - maxEnergyRepRate*nbKittens/maxKittens))); 
				}
			}
		}

		private void grassSpreading() {
			// Grass growth process
			
			for (int i=0; i < grassGrowthRate; i++) {
				rgSpace.addGrass();
			}
		}
		
		private void addNewRabbit() {
			// Add new rabbit randomly in the grid
			
			RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(RABBITINITIALENERGY);
			// If empty spot available 
			if (rgSpace.addRabbit(r)) {
				// Add the newborn to the list
				rabbitsList.add(r);
			}
		}
		
		private void reapDeadRabbits(){
			// Rabbit Death process 
			
			for(int i = (rabbitsList.size() - 1); i >= 0 ; i--){
		    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		        // If energy too low, the rabbit dies 
		    	if(rgsa.getEnergy() < 1) {
		    		// Remove it from the grid 
		        	rgSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        	// Remove it from the rabbit list
		        	rabbitsList.remove(i);
		        }
			}
		  }
		
		private int countLivingRabbits(){
			// Count number of living rabbits 
			
		    int livingRabbits = 0;
		    for(int i = 0; i < rabbitsList.size(); i++){
		      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		      // If rabbit still has energy, add it to the count
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
		
		private void logStats() {
			// Write rabbit count in a file to analyse 

			int livingRabbits = countLivingRabbits(); 
			
			String content = livingRabbits +"," + rgSpace.countGrass() + '\n';
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
				writer.write(content);
				writer.close();
			} catch (IOException e) {
				System.out.println("Error while writing log file");
			}
		}
}
