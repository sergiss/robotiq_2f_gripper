package com.delmesoft.gripper.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SyncSerialPort {
	
	/**
	 * Note for Linux users: Serial port access is limited to certain users and groups in Linux. To enable user access, you must open a terminal and enter the following commands before jSerialComm will be able to access the ports on your system. Don't worry if some of the commands fail. All of these groups may not exist on every Linux distro. (Note, this process must only be done once for each user):
	 * sudo usermod -a -G uucp <username>
	 * sudo usermod -a -G dialout <username>
	 * sudo usermod -a -G lock <username>
	 * sudo usermod -a -G tty <username>
	 */

	private int timeout;

	private String port;
	private int baudRate;
	private int numDataBits;
	private int numStopBits;
	private int parity;
	private int flowControl;

	private com.fazecast.jSerialComm.SerialPort serialPort;
	private OutputStream os;
	private InputStream is;

	public SyncSerialPort() { // Default initialization
		baudRate = 9600;
		numDataBits = 8;
		numStopBits = 1;
		parity = 0;
		flowControl = 0;	
		timeout = 5_000;
	}

	/**
	 *  Opens this serial port for reading and writing
	 */
	public synchronized void connect() {
		if(serialPort == null) {
			// create port instance
			serialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(port); // (i.e: "/dev/ttyS0" or "COM3")
			// Sets the desired baud rate for this serial port.
			serialPort.setBaudRate(baudRate);
			// Sets the desired number of data bits per word.
			serialPort.setNumDataBits(numDataBits);
			// Sets the desired number of stop bits per word.
			serialPort.setNumStopBits(numStopBits);
			// Sets the desired parity error-detection scheme to be used.
			serialPort.setParity(parity);
			// Specifies what kind of flow control to enable for this serial port.
			// See: Flow Control constants
			serialPort.setFlowControl(flowControl);
			// mode specifies that a corresponding read call will block until either newReadTimeout milliseconds 
			// of inactivity have elapsed or at least 1 byte of data can be read.
			// serialPort.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING, readTimeout, 0); // 30 ms. timeout
			// serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, timeout, timeout);
			
			// In this mode, a call to any of the read() or readBytes() methods will block
			// until the number of milliseconds specified by the newReadTimeout parameter
			// has elapsed or at least 1 byte of data can be read.
			serialPort.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING | com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING, timeout, timeout);

			if (serialPort.openPort()) { // open port
				os = serialPort.getOutputStream();
				is = serialPort.getInputStream();
			} else {
				disconnect();
				throw new RuntimeException("Port '" + port + "' could not be opened.");
			}
		}
	}

	/**
	 * Check if serial port is connected
	 * @return
	 */
	public synchronized boolean isConnected() {
		return serialPort != null;
	}

	/**
	 *  Closes this serial port
	 */
	public synchronized void disconnect() {
		if (serialPort != null) {
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception ignore) {
			} finally {
				os = null;
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception ignore) {
			} finally {
				is = null;
			}
			try {
				serialPort.closePort();
			} catch (Exception ignore) {
			} finally {
				serialPort = null;
			}
		}
	}

	/**
	 * Writes len bytes from the specified byte array to this serial port.
	 * @param data
	 * @throws Exception 
	 */
	public void writeBytes(byte[] data) throws IOException {
		writeBytes(data, 0, data.length, true);
	}

	/**
	 * Writes len bytes from the specified byte array to this serial port.
	 * @param data
	 * @param flush
	 * @throws Exception 
	 */
	public void writeBytes(byte[] data, boolean flush) throws IOException {
		writeBytes(data, 0, data.length, flush);
	}

	/**
	 * Writes len bytes from the specified byte array starting at offset <code>offset</code> to this serial port
	 * @param data
	 * @param offset
	 * @param len
	 * @throws Exception 
	 */
	public void writeBytes(byte[] data, int offset, int len) throws IOException {
		writeBytes(data, offset, len, true);
	}
	
	/**
	 * Writes len bytes from the specified byte array starting at offset <code>offset</code> to this serial port
	 * @param data
	 * @param offset
	 * @param len
	 * @throws Exception 
	 */
	public synchronized void writeBytes(byte[] data, int offset, int len, boolean flush) throws IOException {
		if (serialPort != null) {
			os.write(data, offset, len);
			if (flush)
				os.flush();
		}
	}

	public synchronized void writeLine(String line) throws IOException {
		if (serialPort != null) {
			line += "\r\n";
			writeBytes(line.getBytes());
		}
	}
	
	public int available() throws IOException {
		return is.available();
	}

	public synchronized void readBytes(byte[] data) throws IOException {
		readBytes(data, 0, data.length);
	}
	
	public synchronized void readBytes(byte[] data, int offset, int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = is.read(data, offset + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}
	
	public int read(byte[] data) throws IOException {
		return read(data, 0, data.length);
	}

	public int read(byte[] data, int offset, int len) throws IOException {
		return is.read(data, offset, len);
	}
	
	public int read() throws IOException {
		return is.read();
	}
	
	public void flush() throws IOException {
		os.flush();
	}

	/**
	 * Get desired serial port to use.
	 * @return
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Set desired serial port to use.
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the current baud rate of the serial port.
	 * @return
	 */
	public int getBaudRate() {
		return baudRate;
	}

	/**
	 * Sets the desired baud rate for this serial port.
	 * @param baudRate
	 */
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	/**
	 * Gets the current number of data bits per word.
	 * @return
	 */
	public int getNumDataBits() {
		return numDataBits;
	}

	/**
	 * Sets the desired number of data bits per word.
	 * @param numDataBits
	 */
	public void setNumDataBits(int numDataBits) {
		this.numDataBits = numDataBits;
	}

	/**
	 * Gets the current number of stop bits per word.
	 * @return
	 */
	public int getNumStopBits() {
		return numStopBits;
	}

	/**
	 * Set desired number of stop bits per word.
	 * @param numStopBits
	 */
	public void setNumStopBits(int numStopBits) {
		this.numStopBits = numStopBits;
	}

	/**
	 * Gets the current parity error-checking scheme.
	 * @return
	 */
	public int getParity() {
		return parity;
	}

	/**
	 * Sets the desired parity error-detection scheme to be used.
	 * @param parity
	 */
	public void setParity(int parity) {
		this.parity = parity;
	}

	/**
	 * Gets flow control settings enabled on this serial port.
	 * @return
	 */
	public int getFlowControl() {
		return flowControl;
	}

	/**
	 * Set desired type of flow control to enable for this serial port.
	 * @param flowControl
	 */
	public void setFlowControl(int flowControl) {
		this.flowControl = flowControl;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SerialPort [readTimeout=");
		builder.append(timeout);
		builder.append(", port=");
		builder.append(port);
		builder.append(", baudRate=");
		builder.append(baudRate);
		builder.append(", numDataBits=");
		builder.append(numDataBits);
		builder.append(", numStopBits=");
		builder.append(numStopBits);
		builder.append(", parity=");
		builder.append(parity);
		builder.append(", flowControl=");
		builder.append(flowControl);
		builder.append("]");
		return builder.toString();
	}

}
