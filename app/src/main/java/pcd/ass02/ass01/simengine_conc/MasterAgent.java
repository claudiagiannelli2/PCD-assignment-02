package pcd.ass02.ass01.simengine_conc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MasterAgent extends Thread {
	// coordina la simulazione
	// usa un trigger come monitor per sincronizzarsi con il worker
	// ogni worker gestisce un certo numero di agenti
	private boolean toBeInSyncWithWallTime;
	private int nStepsPerSec;
	private int numSteps;

	private long currentWallTime;
	
	private AbstractSimulation sim;
	private Flag stopFlag;
	private Semaphore done;
	private int nWorkers;
	private ExecutorService executorService;

	
	public MasterAgent(AbstractSimulation sim, int nWorkers, int numSteps, Flag stopFlag, Semaphore done, boolean syncWithTime) {
		toBeInSyncWithWallTime = false;
		this.sim = sim;
		this.stopFlag = stopFlag;
		this.numSteps = numSteps;
		this.done = done;
		this.nWorkers = nWorkers;
		
		if (syncWithTime) {
			this.syncWithTime(25);
		}
	}

	public void run() {
		
		log("booted");
		
		var simEnv = sim.getEnvironment();
		var simAgents = sim.getAgents();
		
		simEnv.init();
		for (var a: simAgents) {
			a.init(simEnv);
		}

		int t = sim.getInitialTime();
		int dt = sim.getTimeStep();
		
		sim.notifyReset(t, simAgents, simEnv);
		
		Trigger canDoStep = new Trigger(nWorkers);
		CyclicBarrier jobDone = new CyclicBarrier(nWorkers + 1);
		
		log("creating workers...");
		
		int nAssignedAgentsPerWorker = simAgents.size()/nWorkers;
		
		int index = 0;
		List<WorkerAgent> workers = new ArrayList<>();
		for (int i = 0; i < nWorkers - 1; i++) {
			List<AbstractAgent> assignedSimAgents = new ArrayList<>();
			for (int j = 0; j < nAssignedAgentsPerWorker; j++) {
				assignedSimAgents.add(simAgents.get(index));
				index++;
			}
			executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
			WorkerAgent worker = new WorkerAgent("worker-"+i, assignedSimAgents, dt, canDoStep, jobDone, stopFlag);
			try {
		      executorService.execute(() -> {
			  worker.start();
		    });
		    } finally {
             executorService.shutdown();
             try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
             } catch (InterruptedException e) {
             // Gestione dell'eccezione
             e.printStackTrace();
            }
        }
			//worker.start();
			workers.add(worker);
		}
		
		List<AbstractAgent> assignedSimAgents = new ArrayList<>();
		while (index < simAgents.size()) {
			assignedSimAgents.add(simAgents.get(index));
			index++;
		}

		WorkerAgent worker = new WorkerAgent("worker-"+(nWorkers-1), assignedSimAgents, dt, canDoStep, jobDone, stopFlag);
		worker.start();
		workers.add(worker);

		log("starting the simulation loop.");

		int step = 0;
		currentWallTime = System.currentTimeMillis();
        // il master eseguono le azioni proposte dai worker
		try {
			while (!stopFlag.isSet() &&  step < numSteps) {
				
				simEnv.step(dt);
				simEnv.cleanActions();

				/* trigger workers to do their work in this step */	
				canDoStep.trig();
				
				/* wait for workers to complete */
				// aspetta che tutti i worker siano arrivati
				jobDone.await();

				/* executed actions */
				simEnv.processActions();
								
				sim.notifyNewStep(t, simAgents, simEnv);
	
				if (toBeInSyncWithWallTime) {
					syncWithWallTime();
				}
				
				/* updating logic time */
				
				t += dt;
				step++;
			}	
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		log("done");
		stopFlag.set();
		canDoStep.trig();

		done.release();
	}

	private void syncWithTime(int nStepsPerSec) {
		this.toBeInSyncWithWallTime = true;
		this.nStepsPerSec = nStepsPerSec;
	}

	private void syncWithWallTime() {
		try {
			long newWallTime = System.currentTimeMillis();
			long delay = 1000 / this.nStepsPerSec;
			long wallTimeDT = newWallTime - currentWallTime;
			currentWallTime = System.currentTimeMillis();
			if (wallTimeDT < delay) {
				Thread.sleep(delay - wallTimeDT);
			}
		} catch (Exception ex) {}
		
	}
	
	private void log(String msg) {
		System.out.println("[MASTER] " + msg);
	}
	
	
}
