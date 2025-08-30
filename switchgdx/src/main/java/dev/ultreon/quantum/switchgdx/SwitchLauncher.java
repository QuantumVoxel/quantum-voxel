package dev.ultreon.quantum.switchgdx;
import com.thelogicmaster.switchgdx.SwitchApplication;

/** Launches the switch (SwitchGDX) application. */
public class SwitchLauncher {
	public static void main(String[] args) {
		new SwitchApplication(new SafeLoadWrapper());
	}
}
