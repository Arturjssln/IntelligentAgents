import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;

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
	
		// Variables
		private Schedule schedule;
		private RabbitsGrassSimulationSpace rgSpace;
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
			
			if (displayEcosystem != null) {
				displayEcosystem.dispose();
				displayEcosystem = null;
		    }
			displayEcosystem = new DisplaySurface(this, "Carry Drop Model Window 1");
			registerDisplaySurface("Carry Drop Model Window 1", displayEcosystem);
		}

		public void begin() {
			buildModel();
			buildSchedule();
			buildDisplay();
			
		}
		
		public void buildModel() {
			rgSpace = new RabbitsGrassSimulationSpace(gridSize);
			rgSpace.initializeGrass(numInitGrass);
		}
		
		public void buildSchedule() {

		}
		
		public void buildDisplay() {

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
