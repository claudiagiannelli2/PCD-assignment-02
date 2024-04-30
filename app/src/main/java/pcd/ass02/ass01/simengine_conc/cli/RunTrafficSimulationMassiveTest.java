package pcd.ass02.ass01.simengine_conc.cli;

import pcd.ass02.ass01.simengine_conc.Flag;
import pcd.ass02.ass01.simtraffic_conc_examples.*;

public class RunTrafficSimulationMassiveTest {

	public static void main(String[] args) {		

		int numCars = 5000;
		int nSteps = 100;
		
		var simulation = new TrafficSimulationSingleRoadMassiveNumberOfCars(numCars);
		simulation.setup();
		
		/*
		RoadMassiveSimView view = new RoadMassiveSimView();
		view.display();
		simulation.addSimulationListener(view);		
		*/
		
		log("Running the simulation: " + numCars + " cars, for " + nSteps + " steps ...");
		
		Flag stopFlag = new Flag();
		simulation.run(nSteps, stopFlag, false);

		long d = simulation.getSimulationDuration();
		log("Completed in " + d + " ms - average time per step: " + simulation.getAverageTimePerStep() + " ms");
	}
	
	private static void log(String msg) {
		System.out.println("[ SIMULATION ] " + msg);
	}
}
