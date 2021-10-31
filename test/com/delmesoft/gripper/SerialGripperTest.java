package com.delmesoft.gripper;

public class SerialGripperTest {
	
	public static void main(String[] args) throws Exception {
		
		Gripper gripper = new SerialGripper("COM16");
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
