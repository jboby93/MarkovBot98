package bot;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import logging.Logger;
import sun.management.VMManagement;

public class Tools {
	// Random number generator -- range: [0, max)
	public static int rand(int max) {
		return (int) (Math.random() * max);
	}

	public static String getProcessID() {
		Integer pid = -1;

		/*
		 * When Java 9 is alpha, this whole process will be doable in one line:
		 * long pid = ProcessHandle.current().getPid(); As opposed to using
		 * hacky reflection
		 */
		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			VMManagement mgmt = (VMManagement) jvm.get(runtime);
			Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);

			pid = (Integer) pid_method.invoke(mgmt);
		} catch (Exception e) {
			Logger.error("getProcessID(): exception occurred: " + e.getMessage());
			Logger.logStackTrace(e);
			pid = -1;
		}

		return pid.toString();
	}
}
