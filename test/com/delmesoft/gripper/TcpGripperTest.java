package com.delmesoft.gripper;

public class TcpGripperTest {
	
	public static void main(String[] args) throws Exception {
		
		Gripper gripper = new TcpGripper("192.168.10.114");
		gripper.connect();
		while(true) {
			System.out.println("open");
			gripper.pose(1.0, 1.0, 0.0);
			
			System.out.println(gripper.getState());
			Thread.sleep(500);
			
			System.out.println("close");
			gripper.pose(0.0, 1.0, 0.0);
			System.out.println(gripper.getState());
			Thread.sleep(500);
		}

		// gripper.disconnect();
	}

}
