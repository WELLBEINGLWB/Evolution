package hill;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

import unalcol.optimization.OptimizationFunction;
import unalcol.random.integer.IntUniform;
import unalcol.tracer.Tracer;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

public class HillMazeRand3 extends OptimizationFunction<double[]> {

	
	public int simNumber = 0;
	public float alpha = 0.7f;

	HillMazeRand3(int simNumber) {
		this.simNumber = simNumber;
	}

	HillMazeRand3(int simNumber, float alpha) {
		this.simNumber = simNumber;
		this.alpha = alpha;
	}

	public Double apply(double[] x) {

		// Start Measuring evaluation time
		long startTime = System.currentTimeMillis();

		float ampli = (float) x[0];
		float offset = (float) x[1];
		float phase = (float) (x[2]) * (float) Math.PI;

		// Pack Floats into one String data signal
		FloatWA ControlParam = new FloatWA(3);
		float[] CP = new float[3];
		CP[0] = ampli;
		CP[1] = offset;
		CP[2] = phase;
		System.arraycopy(CP,0,ControlParam.getArray(),0,CP.length);
		char[] p = ControlParam.getCharArrayFromArray();
		CharWA strCP = new CharWA(p.length);
		System.arraycopy(p,0,strCP.getArray(),0,p.length);

		// Morphology Parameters
		int Numberofmodules = 8;
		int[] orientation = new int[] { 1, 0, 1, 0, 1, 0, 1, 0 };

		// Simulation Parameters
		int MaxTime = 40;

		// Pack Integers into one String data signal
		IntWA NumberandOri = new IntWA(Numberofmodules + 3);
		int[] NO = new int[Numberofmodules + 3];
		NO[0] = Numberofmodules;
		NO[1] = MaxTime;
		NO[2] = 0;
		for (int i = 3; i < Numberofmodules + 3; i++) {
			NO[i] = orientation[i - 3];
		}
		System.arraycopy(NO,0,NumberandOri.getArray(),0,NO.length);
		char[] p2 = NumberandOri.getCharArrayFromArray();
		CharWA strNO = new CharWA(p2.length);
		System.arraycopy(p2,0,strNO.getArray(),0,p2.length);

		// 3 Sub-environment maze parameters
		char[][] subenv = new char[][] { { 's', 'l', 's' }, { 'b' },
				{ 's', 'r', 's' } };
		
		// Generate random 3 sub-environment sequence (by Brute Force)
		IntUniform r = new IntUniform(3);
		int[] seq = r.generate(3);
		while (seq[1] == seq[0]) {
			seq[1] = r.next();
		}
		seq[2] = 5 - ((seq[0] + 1) + (seq[1] + 1));

		// Maze Parameters (Already a string)
		char[] mazeseq = new char[] { 's' }; // Default Maze Sequence
		CharWA strSeq = new CharWA(mazeseq.length);
		System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);


		// Array that receives fitness from the simulator or signals a crash
		float[] rfitness = new float[3];

		// Solved environment flag
		float[] pass = new float[] { 0, 0, 0 };

		// Fitness to be returned to Evolutionary Algorithm
		float[] fitness = new float[] { 1, 1, 1 };

		// Number of retries in case of simulator crash
		int maxTries = 5;

		// Retry if there is a simulator crash
		for (int j = 0; j < maxTries; j++) {

			// Simulator interaction start
			remoteApi vrep = new remoteApi(); // Create simulator control object
			vrep.simxFinish(-1); // just in case, close all opened connections
			// Connect with the corresponding simulator remote server
			int clientID = vrep.simxStart("127.0.0.1", 19997 - simNumber, true,
					true, 5000, 5);

			if (clientID != -1) {
				// Set Simulator signal values
				vrep.simxSetStringSignal(clientID, "NumberandOri", strNO,
						vrep.simx_opmode_oneshot_wait);
				vrep.simxSetStringSignal(clientID, "ControlParam", strCP,
						vrep.simx_opmode_oneshot_wait);
				vrep.simxSetStringSignal(clientID, "Maze", strSeq,
						vrep.simx_opmode_oneshot_wait);

				

				try {
					// *******************************************************************************************************************************

					// New Maze Parameters (Already a string)
					mazeseq = subenv[seq[0]];
					strSeq.initArray(mazeseq.length);
					System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);
					vrep.simxSetStringSignal(clientID, "Maze", strSeq,
							vrep.simx_opmode_oneshot_wait);

					// Run Scene in the simulator
					rfitness = RunSimulation(vrep, clientID, MaxTime, simNumber);

					if (rfitness[0] == -1) {
						RestartSim(simNumber, j);
						continue;
					}

					// Retrieve the fitness if there is no crash
					fitness[0] = rfitness[1];
					pass[0] = rfitness[2];
					// *******************************************************************************************************************************
					if (pass[0] == 1) {
						// New Maze Parameters (Already a string)
						mazeseq = subenv[seq[1]];
						strSeq.initArray(mazeseq.length);
						System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);
						vrep.simxSetStringSignal(clientID, "Maze", strSeq,
								vrep.simx_opmode_oneshot_wait);

						// Run Scene in the simulator
						rfitness = RunSimulation(vrep, clientID, MaxTime,
								simNumber);

						if (rfitness[0] == -1) {
							RestartSim(simNumber, j);
							continue;
						}

						// Retrieve the fitness if there is no crash
						fitness[1] = rfitness[1];
						pass[1] = rfitness[2];
					}

					// *******************************************************************************************************************************
					if (pass[0] == 1 && pass[1] == 1) {
						// New Maze Parameters (Already a string)
						mazeseq = subenv[seq[2]];
						strSeq.initArray(mazeseq.length);
						System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);
						vrep.simxSetStringSignal(clientID, "Maze", strSeq,
								vrep.simx_opmode_oneshot_wait);

						// Run Scene in the simulator
						rfitness = RunSimulation(vrep, clientID, MaxTime,
								simNumber);

						if (rfitness[0] == -1) {
							RestartSim(simNumber, j);
							continue;
						}

						// Retrieve the fitness if there is no crash
						fitness[2] = rfitness[1];
						pass[2] = rfitness[2];
					}
					// *******************************************************************************************************************************
				} catch (InterruptedException e) {
					System.err.println("InterruptedException: "
							+ e.getMessage());
				}
				// Close connection with the simulator
				vrep.simxFinish(clientID);

			} else {
				// No connection could be established
				System.out.println("Failed connecting to remote API server");
				System.out.println("Trying again for the " + j + " time");
				continue;
			}

			break;
		}

		// Calculate global fitness and convert it from float to Double

		double fitnessd = (fitness[0] + fitness[1] + fitness[2]) / 3;

		// Calculate evaluation time and print it
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);

		// System.out.println("Total Fitness = "+fitnessd+" myrank "+myRank);
		String tr = new String("->,"+seq[0] + "," + seq[1] + "," + seq[2]
				+ "," + pass[0] + "," + pass[1] + "," + pass[2] + ","
				+ fitness[0] + "," + fitness[1] + "," + fitness[2]);
		
        Tracer.trace(this,tr);
		return fitnessd;
	}

	void RestartSim(int myRank, int j) {

		// Create the command to open the corresponding simulator
		String vrepcommand = new String("./vrep" + myRank + ".sh");

		System.out.println("Restarting simulator and trying again for the " + j
				+ " time in " + myRank);

		try {
			// Command to kill corresponding simulator
			ProcessBuilder qq = new ProcessBuilder("killall", "vrep" + myRank);
			// Specify output file for command line messages
			File log = new File("Simout/log");
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			// Start command process
			Process p = qq.start();
			// Wait for process to finish
			int exitVal = p.waitFor();
			System.out.println("Terminated vrep" + myRank + " with error code "
					+ exitVal);

			// Command to open a simulator with no window
			// qq = new ProcessBuilder(vrepcommand,"-h");
			qq = new ProcessBuilder(vrepcommand, "-h",
					"/home/rodr/EvolWork/Modular/Maze/MazeBuilderR01.ttt");
			// qq = new
			// ProcessBuilder("xvfb-run","--auto-servernum","--server-num=1",vrepcommand,
			// "-h");
			// Open the simulator from its own directory
			qq.directory(new File("/home/rodr/V-REP/Vrep" + myRank + "/"));
			// Specify output file for command line messages of the simulator
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			// Issue command
			qq.start();
			// Wait for the simulator to open
			Thread.sleep(5000);
			System.out.println("Trying again now");
		} catch (Exception e) {
			// Could not reopen the simulator
			System.out.println(e.toString());
			e.printStackTrace();
		}

	}

	float[] RunSimulation(remoteApi vrep, int clientID, int MaxTime,
			int threadnum) throws InterruptedException {

		long startTime = System.currentTimeMillis();
		long stopTime = 0;
		long elapsedTime = 0;
		// Create all output variables
		IntW out = new IntW(0);
		CharWA datastring = new CharWA(1);
		FloatWA out2 = new FloatWA(3);
		float[] fitnessout = new float[3];

		// Start Simulation
		int ret = vrep.simxStartSimulation(clientID,
				vrep.simx_opmode_oneshot_wait);
		System.out.println("Start: " + ret + " sim: " + threadnum);
		// Setting up and waiting for finished flag
		vrep.simxGetIntegerSignal(clientID, "finished", out,
				vrep.simx_opmode_streaming);
		out.setValue(0);

		while (out.getValue() == 0) {

			Thread.sleep(10);

			if (vrep.simxGetIntegerSignal(clientID, "finished", out,
					vrep.simx_opmode_buffer) == vrep.simx_return_ok) {
				// We received a fitness signal and everything is ok
				// System.out.println("Retrieved Signal: "+Integer.toString(out.getValue()));
			} else {
				// We have not received the fitness message and out may contain
				// garbage so we set it up to 0 again
				out.setValue(0);
				stopTime = System.currentTimeMillis();
				elapsedTime = stopTime - startTime;

				if (elapsedTime > 300000) {
					System.out
							.println("Too much time has passed attempting to restart simulator");
					fitnessout[0] = -1; // This signals that the output is not
										// ok and the simulator should be
										// restarted
					fitnessout[1] = 1000;
					return fitnessout;
				}
			}
		}

		// Stop listening to the finished signal
		vrep.simxGetIntegerSignal(clientID, "finished", out,
				vrep.simx_opmode_discontinue);

		// Stop simulation
		vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);

		// Read simulation results
		ret = vrep.simxGetStringSignal(clientID, "Position", datastring,
				vrep.simx_opmode_oneshot_wait);
		if (ret != vrep.simx_return_ok) {
			System.out.println("Position Signal not received");
		}

		out2.initArrayFromCharArray(datastring.getArray());

		if (out2.getArray().length == 0) {
			// If out2 is empty the simulator is not working properly and should
			// be restarted
			System.out.println("out2 is empty");
			fitnessout[0] = -1; // This signals that the output is not ok and
								// the simulator should be restarted
			fitnessout[1] = 1000;
			return fitnessout;
		}

		fitnessout[0] = 0;

		float alpha = this.alpha;
		float beta = 1 - alpha;

		if (out2.getArray()[0] == 0) {
			// The robot could get out of the maze so the fitness is the time it
			// spent normalized
			fitnessout[1] = beta * out2.getArray()[1] / MaxTime;
			fitnessout[2] = 1;
		} else if (out2.getArray()[1] == 0) {
			// The robot could not get out of the maze so the fitness is the
			// distance to goal + the maximum time allowed
			fitnessout[1] = alpha * out2.getArray()[0] + beta * 1.0f;
			fitnessout[2] = 0;
		}

		// Return the fitness of the run
		return fitnessout;

	}

}
