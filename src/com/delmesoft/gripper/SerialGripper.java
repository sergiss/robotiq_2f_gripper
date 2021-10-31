package com.delmesoft.gripper;

import java.util.Arrays;

import com.delmesoft.gripper.utils.CRC16;
import com.delmesoft.gripper.utils.SyncSerialPort;

public class SerialGripper implements Gripper {

	private final String port;
	private int baudRate;
	
	private SyncSerialPort serialPort;
	
	public SerialGripper(String port) {
		this(port, 115200);
	}

	public SerialGripper(String port, int baudRate) {
		this.port = port;
		this.baudRate = baudRate;
		this.serialPort = new SyncSerialPort();
		this.serialPort.setTimeout(50);
	}

	@Override
	public void connect() throws Exception {
		if (!isConnected()) {
			serialPort.setPort(port);
			serialPort.setBaudRate(baudRate);
			serialPort.connect();
			Thread.sleep(2_000);
			restart();
		}
	}

	public synchronized void restart() throws Exception {
		
		deactivate();
		activate();
		
		while(true) {
			byte[] data = { 0x09, 0x03, 0x07, (byte) 0xD0, 0x00, 0x01, (byte) 0x85, (byte) 0xCF }; // Read Gripper status until the activation is completed
			serialPort.writeBytes(data);
			data = new byte[7];
			serialPort.readBytes(data);
			if(Arrays.equals(data, ACTIVATION_COMPLETE)) {
				break; // OK
			} else if(!Arrays.equals(data, ACTIVATION_PENDING)) {
				throw new RuntimeException("Restart error");
			} 
		}
		
	}
	
	private boolean check() throws Exception {
		byte[] data = new byte[8];
		serialPort.readBytes(data);
		boolean result = Arrays.equals(data, RESPONSE_ACK);
		return result;
	}
	
	private synchronized void deactivate() throws Exception {
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x73, 0x30 }; // Deactivate gripper
		serialPort.writeBytes(data);
		if(!check()) {
			throw new RuntimeException("Deactivate error");
		}
	}

	private synchronized void activate() throws Exception {
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x72, (byte) 0xE1 }; // Activate gripper
		serialPort.writeBytes(data);
		if(!check()) {
			throw new RuntimeException("Activate error");
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return serialPort.isConnected();
	}

	@Override
	public synchronized void disconnect() {
		if (!isConnected()) {
			serialPort.disconnect();
		}
	}

	@Override
	public void pose(double position, double speed, double force) throws Exception {
		final byte p = (byte) (255.0 * position);
		final byte s = (byte) (255.0 * speed);
		final byte f = (byte) (255.0 * force);
		pose(p, s, f);
	}
	
	@Override
	public synchronized void pose(byte position, byte speed, byte force) throws Exception {
		position = (byte) clamp(position & 0xFF, 0, 255);
		speed    = (byte) clamp(speed    & 0xFF, 0, 255);
		force    = (byte) clamp(force    & 0xFF, 0, 255);
        //                09    10    03           E8    00    03    06    09    00    00 
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x09, 0x00, 0x00, position, speed, force, 0x00, 0x00};
		send(data);	
	}
	
	private int clamp(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	protected synchronized void send(byte[] data) throws Exception {
		if(isConnected()) {
			int n = data.length;
			int[] crc = CRC16.calculateCRC(data, 0, n - 2);
			data[n - 2] = (byte) crc[0];
			data[n - 1] = (byte) crc[1];
			serialPort.writeBytes(data);
			if(!check()) {
				throw new RuntimeException("Send data error");
			}
		}
	}

	@Override
	public synchronized State getState() throws Exception {
		// 09 03 07 D0 00 03 04 0E
		byte[] data = { 0x09, 0x03, 0x07, (byte) 0xD0, 0x00, 0x03, 0x04, 0x0E };
		serialPort.writeBytes(data);
		
		data = new byte[11];
		serialPort.readBytes(data);
		return new State(data);
	}
	
	public int getBaudRate() {
		return baudRate;
	}

	public String getPort() {
		return port;
	}

}
