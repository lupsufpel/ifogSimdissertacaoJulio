package org.fog.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.PhysicalTopology;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.utils.JsonToTopology;

/**
 * Simulation setup for some generic monitoring fog application
 * 
 * @author Harshit Gupta
 *
 */
public class VrGameFromjson {
	static double EEG_TRANSMISSION_TIME = 5.1;

	public static void main(String[] args) {

		Log.printLine("Starting VRGame...");
		Log.printLine("ê laiáaaa...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "vr_game";

			FogBroker broker = new FogBroker("broker");

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());

			/*
			 * Creating the physical topology from specified JSON file
			 */
			PhysicalTopology physicalTopology = JsonToTopology.getPhysicalTopology(broker.getId(), appId,
					"topologies/vrGamePriority");

			Controller controller = new Controller("master-controller", physicalTopology.getFogDevices(),
					physicalTopology.getSensors(), physicalTopology.getActuators());

			controller.submitApplication(application, 0,
					new ModulePlacementEdgewards(physicalTopology.getFogDevices(), physicalTopology.getSensors(),
							physicalTopology.getActuators(), application, ModuleMapping.createModuleMapping()));

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	@SuppressWarnings({ "serial" }) // passar o genericJson pra ca
	private static Application createApplication(String appId, int userId) {
		// distributed data flow

		Application application = Application.createApplication(appId, userId); // creates an empty application model
																				// (empty directed graph)

		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("client", 10); // adding module Client to the application model
		application.addAppModule("concentration_calculator", 10); // adding module Concentration Calculator to the
																	// application model
		application.addAppModule("connector", 10); // adding module Connector to the application model
		// directed acyclic graph

		/*
		 * Connecting the application modules (vertices) in the application model
		 * (directed graph) with edges
		 */
		if (EEG_TRANSMISSION_TIME == 10)
			application.addAppEdge("EEG", "client", 2000, 500, "EEG", Tuple.UP, AppEdge.SENSOR); // adding edge from EEG
																									// (sensor) to
																									// Client module
																									// carrying tuples
																									// of type EEG
		else
			application.addAppEdge("EEG", "client", 3000, 500, "EEG", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client", "concentration_calculator", 3500, 500, "_SENSOR", Tuple.UP, AppEdge.MODULE); // adding
																														// edge
																														// from
																														// Client
																														// to
																														// Concentration
																														// Calculator
																														// module
																														// carrying
																														// tuples
																														// of
																														// type
																														// _SENSOR
		application.addAppEdge("concentration_calculator", "connector", 100, 1000, 1000, "PLAYER_GAME_STATE", Tuple.UP,
				AppEdge.MODULE); // adding periodic edge (period=1000ms) from Concentration Calculator to
									// Connector module carrying tuples of type PLAYER_GAME_STATE
		application.addAppEdge("concentration_calculator", "client", 14, 500, "CONCENTRATION", Tuple.DOWN,
				AppEdge.MODULE); // adding edge from Concentration Calculator to Client module carrying tuples of
									// type CONCENTRATION
		application.addAppEdge("connector", "client", 100, 28, 1000, "GLOBAL_GAME_STATE", Tuple.DOWN, AppEdge.MODULE); // adding
																														// periodic
																														// edge
																														// (period=1000ms)
																														// from
																														// Connector
																														// to
																														// Client
																														// module
																														// carrying
																														// tuples
																														// of
																														// type
																														// GLOBAL_GAME_STATE
		application.addAppEdge("client", "DISPLAY", 1000, 500, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR); // adding
																													// edge
																													// from
																													// Client
																													// module
																													// to
																													// Display
																													// (actuator)
																													// carrying
																													// tuples
																													// of
																													// type
																													// SELF_STATE_UPDATE
		application.addAppEdge("client", "DISPLAY", 1000, 500, "GLOBAL_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR); // adding
																														// edge
																														// from
																														// Client
																														// module
																														// to
																														// Display
																														// (actuator)
																														// carrying
																														// tuples
																														// of
																														// type
																														// GLOBAL_STATE_UPDATE
		// Each edge is characterized by the type of tuple it carries

		// application.addAppEdge("EGG","cloud", 100, 100, );
		/*
		 * Defining the input-output relationships (represented by selectivity) of the
		 * application modules.
		 */
		application.addTupleMapping("client", "EEG", "_SENSOR", new FractionalSelectivity(0.9)); // 0.9 tuples of type
																									// _SENSOR are
																									// emitted by Client
																									// module per
																									// incoming tuple of
																									// type EEG
		application.addTupleMapping("client", "CONCENTRATION", "SELF_STATE_UPDATE", new FractionalSelectivity(1.0)); // 1.0
																														// tuples
																														// of
																														// type
																														// SELF_STATE_UPDATE
																														// are
																														// emitted
																														// by
																														// Client
																														// module
																														// per
																														// incoming
																														// tuple
																														// of
																														// type
																														// CONCENTRATION
		application.addTupleMapping("concentration_calculator", "_SENSOR", "CONCENTRATION",
				new FractionalSelectivity(1.0)); // 1.0 tuples of type CONCENTRATION are emitted by Concentration
													// Calculator module per incoming tuple of type _SENSOR
		application.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE",
				new FractionalSelectivity(1.0)); // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module
													// per incoming tuple of type GLOBAL_GAME_STATE
		// porcentagem da tupla (terceiro item) emitida pelo cliente
		/*
		 * Defining application loops to monitor the latency of. Here, we add only one
		 * loop for monitoring : EEG(sensor) -> Client -> Concentration Calculator ->
		 * Client -> DISPLAY (actuator)
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
			{
				add("EEG");
				add("client");
				add("concentration_calculator");
				add("client");
				add("DISPLAY");
			}
		});
		// final AppLoop loop2 = new AppLoop(new
		// ArrayList<String>(){{add("client");add("EGG");add("DISPLAY");}});
		List<AppLoop> loops = new ArrayList<AppLoop>() {
			{
				add(loop1);
			}
		};

		application.setLoops(loops);

		return application;
	}
}