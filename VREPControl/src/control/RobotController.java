package control;

import java.util.List;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import simvrep.RobotBuilder;
import simvrep.SimulationConfiguration;

public class RobotController {
	
	/**
	 * Signals to set in the simulator
	 */
	public CharWA strCP;
	public CharWA strMH;
	public CharWA strConn;
	
	int[] connectedhandles;
	

	protected String controllerName;
	protected remoteApi vrep;
	protected int clientID;
	protected RobotBuilder robot;
	protected int[] moduleHandlers;
	protected int numberofModules;
	protected int numberofParameters;
	float[] parameters;

	public RobotController(remoteApi vrep, int clientID, RobotBuilder robot, float[] parameters) {
		this.vrep = vrep;
		this.clientID = clientID;
		this.robot = robot;
		moduleHandlers = robot.getModuleHandlersint();
		this.numberofModules = moduleHandlers.length;
		this.numberofParameters = SimulationConfiguration.getControllerparamnumber();
		connectedhandles = robot.getTree().getHandlerListint();
		if (parameters.length >= numberofParameters*numberofModules){
			this.parameters = parameters;
		} else {
			System.err.println("CPGController");
			System.err.println("Error in the number of parameters, parameters lenght=" + parameters.length);
			System.exit(-1);
		}
			adjustParam();
	}
	
	private void adjustParam(){
		float maxPhase = (float) SimulationConfiguration.getMaxPhase();
		float minPhase = (float) SimulationConfiguration.getMinPhase();
		float maxAmplitude = (float) SimulationConfiguration.getMaxAmplitude();
		float minAmplitude  = (float) SimulationConfiguration.getMinAmplitude();
		float maxOffset = (float) SimulationConfiguration.getMaxOffset();
		float minOffset = (float) SimulationConfiguration.getMinOffset();
		float maxFreq = (float) SimulationConfiguration.getMaxAngularFreq();
		float minFreq = (float) SimulationConfiguration.getMinAngularFreq();
		
		//Assuming min raw parameter is 0 and max is 1
		
		float[] grownparam = new float[parameters.length];
		for (int i = 0; i<parameters.length; i = i + numberofParameters){
			for (int j = 0;j<5; j++){
				grownparam[i+j] = (parameters[i+j]*(maxAmplitude-minAmplitude))+minAmplitude;
				grownparam[i+j+5] = (parameters[i+j+5]*(maxOffset-minOffset))+minOffset;
				grownparam[i+j+30] = (parameters[i+j+30]*(maxFreq-minFreq))+minFreq;
			}
			for (int j = 0;j<20; j++){
				grownparam[i+j+10] = (parameters[i+j+10]*(maxPhase-minPhase))+minPhase;
			}
			
			parameters = grownparam;
		}
		
	}
	
public void sendParameters() {
		
		for (int i = 0; i < moduleHandlers.length ;i++){
			moduleHandlers[i] = moduleHandlers[i] + 1;
		}
		
		FloatWA ControlParam = new FloatWA(parameters.length);
		System.arraycopy(parameters,0,ControlParam.getArray(),0,parameters.length);
		char[] p = ControlParam.getCharArrayFromArray();
		strCP = new CharWA(p.length);
		System.arraycopy(p,0,strCP.getArray(),0,p.length);
		
		IntWA Connhandles = new IntWA(connectedhandles.length);
		System.arraycopy(connectedhandles,0,Connhandles.getArray(),0,connectedhandles.length);
		char[] q = Connhandles.getCharArrayFromArray();
		strConn = new CharWA(q.length);
		System.arraycopy(q,0,strConn.getArray(),0,q.length);
		
		IntWA Modhandles = new IntWA(moduleHandlers.length);
		System.arraycopy(moduleHandlers,0,Modhandles.getArray(),0,moduleHandlers.length);
		char[] r = Modhandles.getCharArrayFromArray();
		strMH = new CharWA(r.length);
		System.arraycopy(r,0,strMH.getArray(),0,r.length);
		
		//Pause communication
		vrep.simxPauseCommunication(clientID, true);
		// Set Simulator signal values
		int result1 = vrep.simxSetStringSignal(clientID, "ControlParam", strCP, vrep.simx_opmode_oneshot);
		int result2 = vrep.simxSetStringSignal(clientID, "ConnHandles", strConn, vrep.simx_opmode_oneshot);
		int result3 = vrep.simxSetStringSignal(clientID, "ModHandles", strMH, vrep.simx_opmode_oneshot);
		//Unpause communication
		vrep.simxPauseCommunication(clientID,false);
		
	}
	
	public String getControllerName() {
		return controllerName;
	}

	public int getNumberofModules() {
		return numberofModules;
	}

	public int getNumberofParameters() {
		return numberofParameters;
	}

	public int getParameterSize() {
		return parameters.length;
	}



	public float[] getParameters() {
		return parameters;
	}



	public void setParameters(float[] parameters) {
		this.parameters = parameters;
	}
}
