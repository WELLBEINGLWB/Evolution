package evolutionJEAFParallelRemote;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;

import mpi.MPI;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import es.udc.gii.common.eaf.problem.objective.ObjectiveFunction;
import evolutionJEAFParallel.EvolJEAFSeq;

public class CalcFitnessF8CAvrgR extends ObjectiveFunction{
	
public double evaluate(double[] values) {
		FileWriter fichero = null;
		PrintWriter pw = null;
    
		int maxTries = 3;
		int myRank = MPI.COMM_WORLD.Rank();
		myRank = myRank + EvolJEAFAvrgR.startNumber;
		//System.out.println("Currently managing vrep"+myRank);
		//String vrepcommand = new String("/home/rodrigo/V-REP/Vrep"+myRank+"/vrep"+myRank+".sh -h");
		//String vrepkill = new String("killall -r vrep"+myRank);
		long startTime = System.currentTimeMillis();
		//System.out.println("Best Fitness in the previous generation "+EvolutionJEAFFixed8Chain.algorithm.getBestIndividual().getFitness());
		//System.out.println("Amplid = "+values[0]+" in "+myRank);
		//System.out.println("Offsetd = "+values[1]+" in "+myRank);
		//System.out.println("Phased = "+values[2]+" in "+myRank);
		float ampli = (float) values[0];		
		float offset = (float) values[1];
		float phase = (float) (values[2])*(float)Math.PI;
		//System.out.println("Ampli = "+ampli+" in "+myRank);
		//System.out.println("Offset = "+offset+" in "+myRank);
		//System.out.println("Phase = "+phase+" in "+myRank);
		//int tam = (int) (values[3]*15);
		//if(tam<2)
			//tam = 2;
		//else if(tam>15)
			//tam = 15;
		
		/*double temp = 0;
		int[] z = new int[8];
        for(int i=0;i<8;i++){
			  temp = values[i+3];
			  if (temp>0)
				  z[i] = 1;
			  else
				  z[i] = 0;
		  }*/
		
	
		
		//Simulation Parameters
        float[] rfitness = new float[2];
        float[] fitness = new float[] {1000,1000,1000,1000};
        //float[] fitness = new float[] {1000,1000,1000,1000};
		int Numberofmodules = 8;//Default 2
		int MaxTime = 40;
		int[] orientation = new int[] {0,1,0,1,0,1,0,1}; //Default
		//int[] orientation = z;
		float CPGAmpli = ampli;//Default 0.5f
		float CPGOff = offset;//Default 0.1f
		float CPGPhase = phase;//Default (float) (-Math.PI/6)
	
		
		//Pack Integers into one String data signal
		IntWA NumberandOri= new IntWA(Numberofmodules+2);
		int[] NO = new int[Numberofmodules+2];
		NO[0] = Numberofmodules;
		NO[1] = MaxTime;
		for (int i=2;i<Numberofmodules+2;i++)
		{
			NO[i] = orientation[i-2];
		}
		System.arraycopy(NO,0,NumberandOri.getArray(),0,NO.length);
		char[] p2 = NumberandOri.getCharArrayFromArray();
		CharWA strNO = new CharWA(p2.length);
		System.arraycopy(p2,0,strNO.getArray(),0,p2.length);
		
		//Pack Floats into one String data signal
		FloatWA ControlParam =new FloatWA(3);
		float[] CP = new float[3];
		CP[0] = CPGAmpli;
		CP[1] = CPGOff;
		CP[2] = CPGPhase;
		//CP[3] = GoalX;
		//CP[4] = GoalY;
		System.arraycopy(CP,0,ControlParam.getArray(),0,CP.length);
		char[] p = ControlParam.getCharArrayFromArray();
		CharWA strCP = new CharWA(p.length);
		System.arraycopy(p,0,strCP.getArray(),0,p.length);
		
		
		
		/*Repeat after restarting simulator in case of crash*/
		for(int j=0;j<maxTries;j++){
		
		
		
		
		
		
		//System.out.println("Program started");
		remoteApi vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		
		int clientID = vrep.simxStart("127.0.0.1",19997-myRank,true,true,5000,5);
		
		if (clientID!=-1)
		{
			//System.out.println("Connected to remote API server");
			
			//Setting simulation parameters		
		int ret = vrep.simxSetStringSignal(clientID, "NumberandOri", strNO, vrep.simx_opmode_oneshot_wait);	
		ret = vrep.simxSetStringSignal(clientID, "ControlParam", strCP, vrep.simx_opmode_oneshot_wait) ;
//*******************************************************************************************************************************
	
		
		rfitness = RunScene(vrep, clientID, "/home/rodr/EvolWork/Modular/ModularCPGA1.ttt",  MaxTime);
		if(rfitness[0]==-1){
			RestartSim( myRank, j);
			continue;
		}
		fitness[0] = rfitness[1]; 
		//System.out.println("Fitness 1 = "+fitness[0]);
		
//*******************************************************************************************************************************		
		
		rfitness = RunScene(vrep, clientID, "/home/rodr/EvolWork/Modular/ModularCPGA2.ttt",  MaxTime);
		if(rfitness[0]==-1){
			RestartSim( myRank, j);
			continue;
		}
		fitness[1] = rfitness[1];
		//System.out.println("Fitness 2 = "+fitness[1]);
		
		
//*******************************************************************************************************************************		
		
		rfitness = RunScene(vrep, clientID, "/home/rodr/EvolWork/Modular/ModularCPGA3.ttt",  MaxTime);
		if(rfitness[0]==-1){
			RestartSim( myRank, j);
			continue;
		}
		fitness[2] = rfitness[1];
		//System.out.println("Fitness 3 = "+fitness[2]);
		
//*******************************************************************************************************************************		
		
		rfitness = RunScene(vrep, clientID, "/home/rodr/EvolWork/Modular/ModularCPGA4.ttt",  MaxTime);
		if(rfitness[0]==-1){
			RestartSim( myRank, j);
			continue;
		}
		fitness[3] = rfitness[1]; 
		//System.out.println("Fitness 4 = "+fitness[3]);
		
	  //if (ret==vrep.simx_return_ok)
		//System.out.println("Last command OK");
	    //else
		//System.out.format("Remote API function call returned with error code: %d\n",ret);
	    vrep.simxFinish(clientID);				
		}
		else{
			System.out.println("Failed connecting to remote API server");
			System.out.println("Trying again for the "+j+" time");
			continue;
		}
			
		//System.out.println("Program ended");
	
		//System.out.println("Minimum fitness = "+Float.toString(minimum(fitness)));
	
		//double fitnessd = (fitness[0]+fitness[1]+fitness[2]+fitness[3])/4;
	    
	    
		break;
		}
		
		double fitnessd;
		
		fitnessd = (fitness[0]+fitness[1]+fitness[2]+fitness[3])/4;
		 
		try
        {
            fichero = new FileWriter("Testout/Indiv"+myRank+".txt", true);
            pw = new PrintWriter(fichero);

                pw.println(fitnessd+"-"+fitness[0]+"-"+fitness[1]+"-"+fitness[2]+"-"+fitness[3]);

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
		
		
		
		long stopTime = System.currentTimeMillis();
	      long elapsedTime = stopTime - startTime;
	      System.out.println(elapsedTime);
	      
		//System.out.println("Total Fitness = "+fitnessd+" myrank "+myRank);
		
		return fitnessd;  
        
    }
	
    public void reset() {
    	
    }
    
    void RestartSim(int myRank,int j){
    	//String vrepcommand = new String("/home/rodrigo/V-REP/Vrep"+myRank+"/vrep"+myRank+".sh -h");
    	String vrepcommand = new String("./vrep"+myRank+".sh");
		//String vrepkill = new String("killall -r vrep"+myRank);	   	
    	System.out.println("Restarting simulator and trying again for the "+j+" time in "+myRank);
  	  /*try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(vrepkill);
            Thread.sleep(2000);
            rt.exec(vrepcommand);
            Thread.sleep(3000);
            System.out.println("Trying again now");
  	  } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }*/
    	try {
    		ProcessBuilder qq=new ProcessBuilder("killall","-r","vrep"+myRank);
 			File log = new File("Simout/log");
 			qq.redirectErrorStream(true);
 			qq.redirectOutput(Redirect.appendTo(log));
 			Process p = qq.start();
            int exitVal = p.waitFor();
            System.out.println("Terminated vrep"+myRank+" with error code "+exitVal);  		
    		
    		
			qq = new ProcessBuilder(vrepcommand,"-h");
			//Map<String, String> env = qq.environment();
			qq.directory(new File("/home/rodr/V-REP/Vrep"+myRank+"/"));
			//qq.inheritIO();
			//File log = new File("Simout/log");
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			qq.start();
            Thread.sleep(5000);
            System.out.println("Trying again now");
		} catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    	
    	
    	
    }
    
    float[] RunScene(remoteApi vrep, int clientID,String scene, int MaxTime){
    			
    			IntW out = new IntW(0);
    			CharWA datastring = new CharWA(1);
    			FloatWA out2 = new FloatWA(3);
    			float[] fitnessout = new float[2];
    			
    	//Load scene maze and start the simulation
    			int ret = vrep.simxLoadScene(clientID, scene, 0, vrep.simx_opmode_oneshot_wait);	
    			ret = vrep.simxStartSimulation(clientID, vrep.simx_opmode_oneshot_wait);
    			
    			//Setting up and waiting for finished flag
    			ret = vrep.simxGetIntegerSignal(clientID, "finished", out,vrep.simx_opmode_streaming);	
    			out.setValue(0);
    			//long startTime = System.currentTimeMillis();
    			while(out.getValue()==0)
    			{
    				if (vrep.simxGetIntegerSignal(clientID, "finished", out, vrep.simx_opmode_buffer)==vrep.simx_return_ok)
    				{
    					//System.out.println("Retrieved Signal: "+Integer.toString(out.getValue()));
    				}else
    				{
    					out.setValue(0);
    					//System.out.println("Retrieved Signal: "+Integer.toString(out.getValue()));
    				}
    			}
    			
    			vrep.simxGetIntegerSignal(clientID, "finished", out,vrep.simx_opmode_discontinue);
    			
    			//Stopping simulation
    		       ret = vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);
    		       //System.out.println(ret);
    		       
    		    //Reading simulation results    
    		       ret = vrep.simxGetStringSignal(clientID, "Position", datastring , vrep.simx_opmode_oneshot_wait);
    		       if(ret != vrep.simx_return_ok){
    		    	   System.out.println("Position Signal not received");
    		       }
    		       out2.initArrayFromCharArray(datastring.getArray());
    		      // System.out.println("Return = "+Float.toString(out2.getArray()[0])+" "+Float.toString(out2.getArray()[1]));
    		       
    		      if(out2.getArray().length==0) {
    		    	  System.out.println("out2 is empty");
    		    	  fitnessout[0] = -1;
		              fitnessout[1] = 1000;
		              return fitnessout;
    		      }
    		      
    		      fitnessout[0] = 0;
    		    //Scaling fitness   
    		    if (out2.getArray()[0]==0)
    		    {
    		    	fitnessout[1] = out2.getArray()[1];
    		    }else if(out2.getArray()[1]==0)
    		    {
    		     fitnessout[1] = (out2.getArray()[0]*(100f/7f))+(float)MaxTime;	
    		    }
    			//System.out.println("Fitness = "+Float.toString(fitness[0]));
    			
    			//Closing first maze
    		    while(vrep.simxCloseScene(clientID, vrep.simx_opmode_oneshot_wait)!=vrep.simx_return_ok)		   
    		    {	   	
    		    }
    		    
    		    return fitnessout;
    }
    
    public static float maximum(float[] inputset)
    {
    
    	float maxValue = 0;
    	for (int i=0;i<inputset.length;i++){
    		if(inputset[i]>=maxValue){
    			maxValue = inputset[i];
    		}
    	}
    	
    	return maxValue;
    }
    

}
