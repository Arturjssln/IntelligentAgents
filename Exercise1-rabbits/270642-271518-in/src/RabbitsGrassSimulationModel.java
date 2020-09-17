import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 * - Célia Benquet - 271518
 * - Artur Jesslen - 270642
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
		
		// Default values
		private static final int GRIDSIZE = 20;
		private static final int NUMINITRABBITS = 100; //TODO!!
		private static final int NUMINITGRASS = 100; //TODO!!
		private static final int GRASSGROWTHRATE = 100; //TODO!!
		private static final int BIRTHTHRESHOLD = 100; //TODO!!
		private static final int RABBITINITIALENERGY = 100; //TODO!!
	
		// Variables
		private Schedule schedule;
		private RabbitsGrassSimulationSpace rgSpace;
		
		private ArrayList rabbitsList;
		
		private DisplaySurface displayEcosystem;
		
		// Attributes
		private int gridSize = GRIDSIZE;
		private int numInitRabbits = NUMINITRABBITS;
		private int numInitGrass = NUMINITGRASS;
		private int grassGrowthRate = GRASSGROWTHRATE;
		private int birthThreshold = BIRTHTHRESHOLD;
		
		
	
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
			
		}
		
		public void setup() {
			rgSpace = null;
			rabbitsList = new ArrayList();
			
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

		}
		
		public void buildDisplay() {
			ColorMap map = new ColorMap();

		    for(int i = 1; i<16; i++){
		      map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
		    }
		    map.mapColor(0, Color.white);

		    Value2DDisplay displayGrass = new Value2DDisplay(rgSpace.getCurrentEcosystem(), map);

		    displayEcosystem.addDisplayable(displayGrass, "Grass");
		}
		
		private void addNewRabbit() {
			RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(RABBITINITIALENERGY);
			rabbitsList.add(r);
			rgSpace.addRabbit(r);
		}

		public String[] getInitParam() {
			// TODO Auto-generated method stub
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
			return params;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return "Rabbits simulation";
		}
		
		public Schedule getSchedule() {
			// TODO Auto-generated method stub
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
}
