/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import java.util.Comparator;

/**
 * This class represents a simulation event which is passed between the entities
 * in the simulation.
 * 
 * @author Costas Simatos
 * @see Simulation
 * @see SimEntity
 */
public class SimEvent implements Cloneable, Comparable<SimEvent>, Comparator<SimEvent> {

	/** internal event type **/
	private final int etype;

	/** time at which event should occur **/
	private final double time;

	/** time that the event was removed from the queue for service **/
	private double endWaitingTime;

	/** id of entity who scheduled event **/
	private int entSrc;

	/** id of entity event will be sent to **/
	private int entDst;

	/** the user defined type of the event **/
	private final int tag;

	/** any data the event is carrying **/
	private final Object data;

	private long serial = -1;

	// Internal event types

	public static final int ENULL = 0;

	public static final int SEND = 1;

	public static final int HOLD_DONE = 2;

	public static final int CREATE = 3;

	private long startTime;

	private int priority;

	public boolean Processed;

	public float fuzzyScheduler;

	/**
	 * Create a blank event.
	 */
	public SimEvent() {
		etype = ENULL;
		time = -1L;
		endWaitingTime = -1.0;
		entSrc = -1;
		entDst = -1;
		tag = -1;
		data = null;

		this.startTime = System.nanoTime();
	}

	// ------------------- PACKAGE LEVEL METHODS --------------------------
	SimEvent(int evtype, double time, int src, int dest, int tag, Object edata) {
		etype = evtype;
		this.time = time;
		entSrc = src;
		entDst = dest;
		this.tag = tag;
		data = edata;

		this.startTime = System.nanoTime();
	}

	SimEvent(int evtype, double time, int src, int dest, int tag, Object edata, int priority) {
		etype = evtype;
		this.time = time;
		entSrc = src;
		entDst = dest;
		this.tag = tag;
		data = edata;
		this.priority = priority;

		this.startTime = System.nanoTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + entDst;
		result = prime * result + entSrc;
		result = prime * result + etype;
		result = prime * result + priority;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimEvent other = (SimEvent) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (entDst != other.entDst)
			return false;
		if (entSrc != other.entSrc)
			return false;
		if (etype != other.etype)
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}

	public double getTime() {
		return time;
	}

	public int getPriority() {

		return this.priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	SimEvent(int evtype, double time, int src) {
		etype = evtype;
		this.time = time;
		entSrc = src;
		entDst = -1;
		tag = -1;
		data = null;
	}

	protected void setSerial(long serial) {
		this.serial = serial;
	}

	/**
	 * Used to set the time at which this event finished waiting in the event
	 * 
	 * @param end_waiting_time
	 */
	protected void setEndWaitingTime(double end_waiting_time) {
		endWaitingTime = end_waiting_time;
	}

	@Override
	public String toString() {
		return "Event tag = " + tag + " source = " + CloudSim.getEntity(entSrc).getName() + " destination = "
				+ CloudSim.getEntity(entDst).getName();
	}

	/**
	 * The internal type
	 * 
	 * @return
	 */
	public int getType() {
		return etype;
	}

	// ------------------- PUBLIC METHODS --------------------------

	/**
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	/*
	 * public int compareTo(SimEvent event) {//metodo que define a prioridade do
	 * processamento if (event == null) { return 1; } else if (time < event.time) {
	 * return -1; } else if (time > event.time) { return 1; } else if (serial <
	 * event.serial) { return -1; } else if (this == event) { return 0; } else {
	 * return 1; } }
	 */

	public int compareTo(SimEvent event) {// metodo que define a prioridade do processamento

		if (event == null) {
			return 1;
		}

		else if ((this.priority > event.priority) && ((this.startTime - event.startTime) < 10)) {// se a prioridade dp
																									// evento que chega
																									// é mais alta que o
																									// evento atual,
																									// processa o evento
																									// novo
			// System.out.println(this.startTime - event.startTime);
			return -1;
		}
		// 100 nano segundos, será que é um valor aceitavel?
		else if (time < event.time) {
			return -1;
		} else if (time > event.time) {
			return 1;
		} else if (serial < event.serial) {
			return -1;
		} else if (this == event) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Get the unique id number of the entity which received this event.
	 * 
	 * @return the id number
	 */
	public int getDestination() {
		return entDst;
	}

	/**
	 * Get the unique id number of the entity which scheduled this event.
	 * 
	 * @return the id number
	 */
	public int getSource() {
		return entSrc;
	}

	/**
	 * Get the simulation time that this event was scheduled.
	 * 
	 * @return The simulation time
	 */
	public double eventTime() {
		return time;
	}

	/**
	 * Get the simulation time that this event was removed from the queue for
	 * service.
	 * 
	 * @return The simulation time
	 */
	public double endWaitingTime() {
		return endWaitingTime;
	}

	/**
	 * Get the user-defined tag of this event
	 * 
	 * @return The tag
	 */
	public int type() {
		return tag;
	}

	/**
	 * Get the unique id number of the entity which scheduled this event.
	 * 
	 * @return the id number
	 */
	public int scheduledBy() {
		return entSrc;
	}

	/**
	 * Get the user-defined tag of this event.
	 * 
	 * @return The tag
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * Get the data passed in this event.
	 * 
	 * @return A reference to the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Create an exact copy of this event.
	 * 
	 * @return The event's copy
	 */
	@Override
	public Object clone() {
		return new SimEvent(etype, time, entSrc, entDst, tag, data);
	}

	/**
	 * Set the source entity of this event.
	 * 
	 * @param s The unique id number of the entity
	 */
	public void setSource(int s) {
		entSrc = s;
	}

	/**
	 * Set the destination entity of this event.
	 * 
	 * @param d The unique id number of the entity
	 */
	public void setDestination(int d) {
		entDst = d;
	}

	@Override
	public int compare(SimEvent o1, SimEvent o2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
