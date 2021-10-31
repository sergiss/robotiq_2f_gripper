package com.delmesoft.gripper;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.delmesoft.gripper.utils.CRC16;

public class TcpGripper implements Gripper {

	private static final int TIMEOUT = 10_000;

	private String host;
	private int port;

	private Socket socket;
	private InputStream is;
	private OutputStream os;

	public TcpGripper() {
		this("localhost", 21098);
	}

	public TcpGripper(String host) {
		this(host, 21098);
	}

	public TcpGripper(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public synchronized void connect() throws Exception {
		if (!isConnected()) {
			socket = new Socket(host, port);
			socket.setSoTimeout(TIMEOUT);
			is = socket.getInputStream();
			os = socket.getOutputStream();
			restart();
		}
	}

	public synchronized void restart() throws Exception {

		deactivate();
		activate();

		while (true) {
			// Read Gripper status until the activation is completed
			byte[] data = { 0x09, 0x03, 0x07, (byte) 0xD0, 0x00, 0x01, (byte) 0x85, (byte) 0xCF };
			os.write(data);
			os.flush();
			data = new byte[7];
			readBytes(data);
			if (Arrays.equals(data, ACTIVATION_COMPLETE)) {
				break; // OK
			} else if (!Arrays.equals(data, ACTIVATION_PENDING)) {
				throw new RuntimeException("Restart error");
			}
		}

	}

	@Override
	public synchronized boolean isConnected() {
		return socket != null;
	}

	@Override
	public synchronized void disconnect() {
		if (isConnected()) {
			try {
				socket.close();
			} catch (Exception e) {
			} finally {
				socket = null;
			}
		}
	}

	private boolean check() throws Exception {
		byte[] data = new byte[8];
		readBytes(data);
		boolean result = Arrays.equals(data, RESPONSE_ACK);
		return result;
	}

	private synchronized void deactivate() throws Exception {
		// Deactivate gripper
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x73, 0x30 };
		os.write(data);
		os.flush();
		if (!check()) {
			throw new RuntimeException("Deactivate error");
		}
	}

	private synchronized void activate() throws Exception {
		// Activate gripper
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x72,
				(byte) 0xE1 };
		os.write(data);
		os.flush();
		if (!check()) {
			throw new RuntimeException("Activate error");
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
		speed = (byte) clamp(speed & 0xFF, 0, 255);
		force = (byte) clamp(force & 0xFF, 0, 255);
		// 09 10 03 E8 00 03 06 09 00 00
		byte[] data = { 0x09, 0x10, 0x03, (byte) 0xE8, 0x00, 0x03, 0x06, 0x09, 0x00, 0x00, position, speed, force, 0x00, 0x00 };
		send(data);
	}

	private int clamp(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	protected synchronized void send(byte[] data) throws Exception {
		if (isConnected()) {
			int n = data.length;
			int[] crc = CRC16.calculateCRC(data, 0, n - 2);
			data[n - 2] = (byte) crc[0];
			data[n - 1] = (byte) crc[1];
			os.write(data);
			os.flush();
			if (!check()) {
				throw new RuntimeException("Send data error");
			}
		}
	}

	@Override
	public synchronized State getState() throws Exception {
		// 09 03 07 D0 00 03 04 0E
		byte[] data = { 0x09, 0x03, 0x07, (byte) 0xD0, 0x00, 0x03, 0x04, 0x0E };
		os.write(data);
		os.flush();

		data = new byte[11];
		readBytes(data);
		return new State(data);
	}

	public void readBytes(byte[] data) throws Exception {
		readBytes(data, 0, data.length);
	}

	public synchronized void readBytes(byte[] data, int offset, int len) throws Exception {
		int n = 0;
		while (n < len) {
			int count = is.read(data, offset + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
