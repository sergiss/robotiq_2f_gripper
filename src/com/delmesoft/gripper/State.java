package com.delmesoft.gripper;
public class State {
	
	private byte[] data;
	
	public State() {}
	
	public State(byte[] data) {
		this.data = data;
		// System.out.println(Arrays.toString(data));
	}
		
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Activation status, echo of the rACT bit (activation bit).
	 * @return true - Gripper activation
	 */
	public boolean isActive() { // gACT
		return (data[3] & 0b00000001) == 0b00000001;
	}
	
	/**
	 * Action status, echo of the rGTO bit (go to bit)
	 * @return true - Go to Position Request
	 */
	public boolean isOnGoToPosition() { // gGTO
		return (data[3] & 0b00001000) == 0b00001000;
	}
	
	/**
	 *  Gripper status, returns the current status & motion of the Gripper fingers.
	 * @return 
	 * 0x00 - Gripper is in reset ( or automatic release ) state. See Fault Status if Gripper is activated.
	 * 0x01 - Activation in progress.
	 * 0x02 - Not used.
	 * 0x03 - Activation is completed.
	 */
	public int getGripperStatus() { // gSTA
		return (data[3] >> 4) & 0b11;
	}
	
	/**
	 *  Object detection status, is a built-in feature that provides information on possible object pick-up. Ignore if gGTO == 0.
	 * @return 
	 * 0x00 - Fingers are in motion towards requested position. No object detected.
	 * 0x01 - Fingers have stopped due to a contact while opening before requested position. Object detected opening.
	 * 0x02 - Fingers have stopped due to a contact while closing before requested position. Object detected closing.
	 * 0x03 - Fingers are at requested position. No object detected or object has been loss / dropped.
	 */
	public int getObjectDetectionStatus() { // gOBJ
		return (data[3] >> 6) & 0b11;
	}
	
	public int getFaultStatus() {
		return data[5]; // TODO : fault status
	}
	
	public byte getPositionRequestEcho() {
		return data[6];
	}
		
	public byte getPosition() {
		return data[7];
	}
	
	/**
	 * Return current in mA.
	 * @return Current in mA.
	 */
	public int getCurrent() {
		return data[8] * 10;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("State [isActive()=");
		builder.append(isActive());
		builder.append(", isOnGoToPosition()=");
		builder.append(isOnGoToPosition());
		builder.append(", getGripperStatus()=");
		builder.append(getGripperStatus());
		builder.append(", getObjectDetectionStatus()=");
		builder.append(getObjectDetectionStatus());
		builder.append(", getFaultStatus()=");
		builder.append(getFaultStatus());
		builder.append(", getPositionRequestEcho()=");
		builder.append(getPositionRequestEcho());
		builder.append(", getPosition()=");
		builder.append(getPosition());
		builder.append(", getCurrent()=");
		builder.append(getCurrent());
		builder.append("]");
		return builder.toString();
	}

}