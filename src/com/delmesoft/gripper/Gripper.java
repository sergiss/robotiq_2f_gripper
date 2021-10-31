package com.delmesoft.gripper;

public interface Gripper {
	
	public static final byte[] RESPONSE_ACK = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x01, 0x30 };
	public static final byte[] ACTIVATION_COMPLETE = { 0x09, 0x03, 0x02, 0x11, 0x00, 0x55, (byte) 0xD5 };
	public static final byte[] ACTIVATION_PENDING  = { 0x09, 0x03, 0x02, 0x31, 0x00, 0x4C,        0x15 };
		
	/**
	 * Open a connection with the device.
	 * @throws Exception
	 */
	void connect() throws Exception;
	
	/**
	 * Check if there is an open connection
	 * @return true if connected
	 */
	boolean isConnected();
	
	/**
	 * Close current connection.
	 */
	void disconnect();
	
	/**
	 * Move gripper to desired position.
	 * @param position 0 -> close, 1 -> open
	 * @param speed 
	 * @param force
	 */
	void pose(double position, double speed, double force) throws Exception;

	/**
	 * Move gripper to desired position.
	 * @param position 0 -> close, 255 -> open
	 * @param speed 
	 * @param force
	 * @throws Exception 
	 */
	void pose(byte position, byte speed, byte force) throws Exception;

	/**
	 * Returns the current state of the Gripper.
	 * @return Gripper State
	 * @throws Exception
	 */
	State getState() throws Exception;
	
}