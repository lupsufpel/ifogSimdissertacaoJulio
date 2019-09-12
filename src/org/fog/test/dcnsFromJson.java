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
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.PhysicalTopology;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.utils.JsonToTopology;

/**
 * Simulation setup for case study 2 - Intelligent Surveillance
 * 
 * @author Harshit Gupta
 *
 */
public class dcnsFromJson {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static int numOfAreas = 1;
	static int numOfCamerasPerArea = 4;

	public static void main(String[] args) {

		Log.printLine("Starting dcns...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "dcns_game";

			FogBroker broker = new FogBroker("broker");

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());

			/*
			 * Creating the physical topology from specified JSON file
			 */
			PhysicalTopology physicalTopology = JsonToTopology.getPhysicalTopology(broker.getId(), appId,
					"topologies/dcns_game_topo");

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

	@SuppressWarnings({ "serial" })
	private static Application createApplication(String appId, int userId) {

		Application application = Application.createApplication(appId, userId);
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("object_detector", 10);
		application.addAppModule("motion_detector", 10);
		application.addAppModule("object_tracker", 10);
		application.addAppModule("user_interface", 10);

		/*
		 * Connecting the application modules (vertices) in the application model
		 * (directed graph) with edges
		 */
		application.addAppEdge("CAMERA", "motion_detector", 1000, 20000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding
																												// edge
																												// from
																												// CAMERA
																												// (sensor)
																												// to
																												// Motion
																												// Detector
																												// module
																												// carrying
																												// tuples
																												// of
																												// type
																												// CAMERA
		application.addAppEdge("motion_detector", "object_detector", 2000, 2000, "MOTION_VIDEO_STREAM", Tuple.UP,
				AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of
									// type MOTION_VIDEO_STREAM
		application.addAppEdge("object_detector", "user_interface", 500, 2000, "DETECTED_OBJECT", Tuple.UP,
				AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of
									// type DETECTED_OBJECT
		application.addAppEdge("object_detector", "object_tracker", 1000, 100, "OBJECT_LOCATION", Tuple.UP,
				AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of
									// type OBJECT_LOCATION
		application.addAppEdge("object_tracker", "PTZ_CONTROL", 100, 28, 100, "PTZ_PARAMS", Tuple.DOWN,
				AppEdge.ACTUATOR); // adding edge from Object Tracker to PTZ CONTROL (actuator) carrying tuples of
									// type PTZ_PARAMS

		/*
		 * Defining the input-output relationships (represented by selectivity) of the
		 * application modules.
		 */
		application.addTupleMapping("motion_detector", "CAMERA", "MOTION_VIDEO_STREAM", new FractionalSelectivity(1.0)); // 1.0
																															// tuples
																															// of
																															// type
																															// MOTION_VIDEO_STREAM
																															// are
																															// emitted
																															// by
																															// Motion
																															// Detector
																															// module
																															// per
																															// incoming
																															// tuple
																															// of
																															// type
																															// CAMERA
		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "OBJECT_LOCATION",
				new FractionalSelectivity(1.0)); // 1.0 tuples of type OBJECT_LOCATION are emitted by Object Detector
													// module per incoming tuple of type MOTION_VIDEO_STREAM
		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "DETECTED_OBJECT",
				new FractionalSelectivity(0.05)); // 0.05 tuples of type MOTION_VIDEO_STREAM are emitted by Object
													// Detector module per incoming tuple of type MOTION_VIDEO_STREAM

		/*
		 * Defining application loops (maybe incomplete loops) to monitor the latency
		 * of. Here, we add two loops for monitoring : Motion Detector -> Object
		 * Detector -> Object Tracker and Object Tracker -> PTZ Control
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
			{
				add("motion_detector");
				add("object_detector");
				add("object_tracker");
			}
		});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>() {
			{
				add("object_tracker");
				add("PTZ_CONTROL");
			}
		});
		List<AppLoop> loops = new ArrayList<AppLoop>() {
			{
				add(loop1);
				add(loop2);
			}
		};

		application.setLoops(loops);
		return application;
	}
}