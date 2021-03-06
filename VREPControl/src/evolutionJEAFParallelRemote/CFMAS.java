package evolutionJEAFParallelRemote;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import simvrep.SimulationOld;
import mpi.MPI;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import es.udc.gii.common.eaf.problem.objective.ObjectiveFunction;

public class CFMAS extends ObjectiveFunction {

	public float alpha = 0.7f;

	public double evaluate(double[] values) {

		// Create objects to save information into a txt file
		FileWriter fichero = null;
		PrintWriter pw = null;

		// Retrieve process number from mpj
		int myRank = MPI.COMM_WORLD.Rank();
		myRank = myRank + EvolJEAFMazeR.startNumber;
		// Start Measuring evaluation time
		long startTime = System.currentTimeMillis();

		// CPG Parameters
		float ampli = (float) values[0];
		float offset = (float) values[1];
		float phase = (float) (values[2]) * (float) Math.PI;

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
		int MaxTime = 120;

		// Pack Integers into one String data signal
		IntWA NumberandOri = new IntWA(Numberofmodules + 3);
		int[] NO = new int[Numberofmodules + 3];
		NO[0] = Numberofmodules;
		NO[1] = MaxTime;
		NO[2] = myRank;
		for (int i = 3; i < Numberofmodules + 3; i++) {
			NO[i] = orientation[i - 3];
		}
		System.arraycopy(NO,0,NumberandOri.getArray(),0,NO.length);
		char[] p2 = NumberandOri.getCharArrayFromArray();
		CharWA strNO = new CharWA(p2.length);
		System.arraycopy(p2,0,strNO.getArray(),0,p2.length);

		// All combinations of sub-environments
		char[][] subenvperm = new char[][] {
				{ 's', 'l', 's', 'b', 's', 'r', 's' },
				{ 's', 'l', 's', 's', 'r', 's', 'b' },
				{ 'b', 's', 'l', 's', 's', 'r', 's' },
				{ 'b', 's', 'r', 's', 's', 'l', 's' },
				{ 's', 'r', 's', 's', 'l', 's', 'b' },
				{ 's', 'r', 's', 'b', 's', 'l', 's' } };

		// Maze Parameters (Already a string)
		char[] mazeseq = new char[] { 's' }; // Default Maze Sequence
		CharWA strSeq = new CharWA(mazeseq.length);
		System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);


		// Array that receives fitness from the simulator or signals a crash
		float[] rfitness = new float[3];

		// Fitness to be returned by each combination of sub-environments for a
		// given individual
		float[] fitness = new float[] { 1, 1, 1, 1, 1, 1 };

		// Number of retries in case of simulator crash
		int maxTries = 5;

		// Retry if there is a simulator crash
		for (int j = 0; j < maxTries; j++) {

			// Simulator interaction start
			remoteApi vrep = new remoteApi(); // Create simulator control object
			SimulationOld sim = new SimulationOld(myRank);
			vrep.simxFinish(-1); // just in case, close all opened connections
			// Connect with the corresponding simulator remote server
			int clientID = vrep.simxStart("127.0.0.1", 19997 - myRank, true,
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
					for (int i = 0; i < subenvperm.length; i++) {

						// New Maze Parameters (Already a string)
						mazeseq = subenvperm[i];
						strSeq.initArray(mazeseq.length);
						System.arraycopy(mazeseq,0,strSeq.getArray(),0,mazeseq.length);
						vrep.simxSetStringSignal(clientID, "Maze", strSeq,
								vrep.simx_opmode_oneshot_wait);

						// Run Scene in the simulator
						rfitness = sim.RunSimulation(vrep, clientID, MaxTime,
								this.alpha);

						if (rfitness[0] == -1) {
							sim.RestartSim(j);
							continue;
						}

						// Retrieve the fitness if there is no crash
						fitness[i] = rfitness[1];
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

		float sum = 0;
		for (int i = 0; i < fitness.length; i++) {
			sum = sum + fitness[i];
		}

		double fitnesst = sum;

		try {
			fichero = new FileWriter("Testout/Indiv" + myRank + ".txt", true);
			pw = new PrintWriter(fichero);
			// Discovering Generation number based on the output file

			// int numgen = generation("Testout/TestS2430.txt")+1;

			int numgen = 0;

			// if (myRank<10){
			// numgen = generation("Testout/TestS2430.txt")+1;
			// }else if (myRank<20){
			// numgen = generation("Testout/TestS24310.txt")+1;
			// }else {
			// numgen = generation("Testout/TestS24320.txt")+1;
			// }

			if (myRank < 8) {
				numgen = generation("Testout/TestSMAS0.txt") + 1;
			} else {
				numgen = generation("Testout/TestSMAS8.txt") + 1;
			}

			pw.println(numgen + "," + ampli + "," + offset + "," + phase + ","
					+ fitness[0] + "," + fitness[1] + "," + fitness[2] + ","
					+ fitness[3] + "," + fitness[4] + "," + fitness[5]+ "," + fitnesst);
			// pw.println(fitnessd + "-" + reportDate);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fichero)
					fichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		// Calculate evaluation time and print it
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);

		return fitnesst;
	}

	public void reset() {

	}

	public int generation(String string) {
		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile(string, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer == fileLength) {
						continue;
					}
					break;

				} else if (readByte == 0xD) {
					if (filePointer == fileLength - 1) {
						continue;
					}
					break;
				}

				sb.append((char) readByte);

			}

			if (fileLength <= 0) {
				return -1;
			} else {
				String lastLine = sb.reverse().toString();
				String number = lastLine.substring(0, lastLine.indexOf(" "));
				// System.out.println(lastLine.indexOf(" "));
				return Integer.parseInt(number);
			}

		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
					/* ignore */
				}
		}
	}

}
