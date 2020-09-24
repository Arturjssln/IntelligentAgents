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
		private static final int NUMINITRABBITS = 1; //TODO!!
		private static final int NUMINITGRASS = 100; //TODO!!
		private static final int GRASSGROWTHRATE = 100; //TODO!!
		private static final int RABBITINITIALENERGY = 1000; //TODO!!
		private static final int MAXGRASS = 16; //TODO!!
		private static final int MAXKITTENS = 6;
		private static final int BIRTHTHRESHOLD = 1100; //TODO!!
		private static final double MAXENERGYREPRATE = 0.5; //TODO!!
	
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
		private String fileName;
		
		
	
		// Methods 
		public static void main(String[] args) {
			
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
				writer.write("Rabbits, Grass\n");
				writer.close();
			} catch (IOException e) {
				System.out.println("Error while writing header on log file");
			}
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
			buildModel();
			buildSchedule();
			buildDisplay();
			displayEcosystem.display();
			
		}
		
		public void buildModel() {
			rgSpace = new RabbitsGrassSimulationSpace(gridSize);
			rgSpace.initializeGrass(numInitGrass);
			
			for(int i=0; i < numInitRabbits; i++) {
				addNewRabbit();
			}
		}
		
		public void buildSchedule() {
			class RabbitsGrassSimulationStep extends BasicAction {
				public void execute() {
					SimUtilities.shuffle(rabbitsList);
					for(int i=0; i < rabbitsList.size(); i++){
						//boolean reproduce = false;
						RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
						
						//rgsa.step(reproduce);
						rgsa.step();					
					}

					// Grass growing
					grassSpreading();
					
					// Reproduction
					reproduction(); 

					// Kill Rabbits
					reapDeadRabbits();
					
					// Update Display 
					displayEcosystem.updateDisplay(); 
			        
				}
		    }
			
			schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
			
			class RabbitsCountLiving extends BasicAction {
				public void execute(){
					countLivingRabbits();
				}
			}
			schedule.scheduleActionAtInterval(10, new RabbitsCountLiving());

			class LogStats extends BasicAction {
				public void execute(){
					logStats();
				}
			}
			schedule.scheduleActionAtInterval(1, new LogStats());

		}
		
		public void buildDisplay() {
			
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
			// Starting from end of the list to ensure that newborn are not giving birth
			for (int i=rabbitsList.size()-1; i >= 0 ; i--) {
				RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
				if(rgsa.getEnergy() >= birthThreshold) {
					int nbKittens = (int)(Math.random()*maxKittens);
					for (int j=0; j<nbKittens; j++) {
						addNewRabbit();
					}
					rgsa.setEnergy((int)(rgsa.getEnergy()*(1 - maxEnergyRepRate*nbKittens/maxKittens))); 
				}
			}
		}

		private void grassSpreading() {
			for (int i=0; i < grassGrowthRate; i++) {
				rgSpace.addGrass();
			}
		}
		
		private void addNewRabbit() {
			RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(RABBITINITIALENERGY);
			if (rgSpace.addRabbit(r)) {
				rabbitsList.add(r);
			}
		}
		
		private void reapDeadRabbits(){
			for(int i = (rabbitsList.size() - 1); i >= 0 ; i--){
		    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		        if(rgsa.getEnergy() < 1) {
		        	rgSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        	rabbitsList.remove(i);
		        }
			}
		  }
		
		private int countLivingRabbits(){
		    int livingRabbits = 0;
		    for(int i = 0; i < rabbitsList.size(); i++){
		      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		      if(rgsa.getEnergy() > 0) livingRabbits++;
		    }
			System.out.println("Number of living rabbits is: " + livingRabbits);
			
		    return livingRabbits;
		  }

		public String[] getInitParam() {
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
			return params;
		}

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

		private void logStats() {
			// Count rabbits 
			int livingRabbits = 0;
		    for(int i = 0; i < rabbitsList.size(); i++){
		      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)rabbitsList.get(i);
		      if(rgsa.getEnergy() > 0) livingRabbits++;
		    }
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
