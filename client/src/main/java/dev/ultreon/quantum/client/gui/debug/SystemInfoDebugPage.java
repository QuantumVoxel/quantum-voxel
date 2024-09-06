package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.text.TextObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class SystemInfoDebugPage implements DebugPage {
    private final SystemInfo systemInfo = new SystemInfo();
    private Notification notification;

    @Override
    public void render(DebugPageContext context) {
        // Get current process's PID
        long pid = ProcessHandle.current().pid();
        CentralProcessor cpu = systemInfo.getHardware().getProcessor();
        context.left("System");
        context.left("Process PID", String.valueOf(pid));

        context.left();
        context.left("Processor");
        context.left("Name", cpu.getProcessorIdentifier().getName());
        context.left("Vendor", cpu.getProcessorIdentifier().getVendor());
        context.left("Family", cpu.getProcessorIdentifier().getFamily());
        context.left("Model", cpu.getProcessorIdentifier().getModel());
        context.left("Stepping", cpu.getProcessorIdentifier().getStepping());
        context.left("Physical cores", String.valueOf(cpu.getPhysicalProcessorCount()));
        context.left("Logical cores", String.valueOf(cpu.getLogicalProcessorCount()));

        context.left();
        context.left("Memory");
        context.left("Total", systemInfo.getHardware().getMemory().getTotal());
        context.left("Free", systemInfo.getHardware().getMemory().getAvailable());
        context.left("Used", systemInfo.getHardware().getMemory().getTotal() - systemInfo.getHardware().getMemory().getAvailable());

        context.left();
        context.left("Sensors");
        context.left("CPU Temperature", String.format("%.2f°C", systemInfo.getHardware().getSensors().getCpuTemperature()));
        context.left("GPU Temperature", String.format("%.2fV", systemInfo.getHardware().getSensors().getCpuVoltage()));

        context.left();
        context.left("Graphics");
        context.left("Display", Gdx.graphics.getDisplayMode().toString());
        context.left("GL Renderer", Gdx.gl.glGetString(GL20.GL_RENDERER));
        context.left("GL Version", Gdx.gl.glGetString(GL20.GL_VERSION));
        context.left("GL Vendor", Gdx.gl.glGetString(GL20.GL_VENDOR));
        context.left("GL Extensions", Gdx.gl.glGetString(GL20.GL_EXTENSIONS));

        context.right();
        context.right("OS");
        context.right("Name", System.getProperty("os.name"));
        context.right("Version", System.getProperty("os.version"));
        context.right("Architecture", System.getProperty("os.arch"));

        context.right();
        context.right("Java");
        context.right("Version", System.getProperty("java.version"));
        context.right("VM Name", System.getProperty("java.vm.name"));
        context.right("VM Vendor", System.getProperty("java.vm.vendor"));
        context.right("VM Version", System.getProperty("java.vm.version"));

        if (System.getProperty("java.vm.vendor").equals("GraalVM")) {
            context.right();
            context.right("GraalVM");
            context.right("Version", System.getProperty("graalvm.version"));
            context.right("VM Name", System.getProperty("graalvm.vm.name"));
            context.right("VM Vendor", System.getProperty("graalvm.vm.vendor"));
            context.right("VM Version", System.getProperty("graalvm.vm.version"));
        }

        if (Gdx.graphics.getFrameId() % 10 == 0)
            this.checkCpuTemp();
    }

    private void checkCpuTemp() {
        double cpuTemperature = systemInfo.getHardware().getSensors().getCpuTemperature();
        if (cpuTemperature == 0.0 || Double.isNaN(cpuTemperature)) {
            return;
        }
        QuantumClient client = QuantumClient.get();
        if (cpuTemperature > 90) {
            if (this.notification == null) {
                this.notification = Notification
                        .builder("CPU Temperature", String.format("The CPU temperature is %.2f°C!", cpuTemperature))
                        .sticky()
                        .subText("processor-watcher")
                        .build();

                client.notifications.add(this.notification);
            } else {
                this.notification.setSummary(TextObject.literal(String.format("The CPU temperature is %.2f°C!", cpuTemperature)));
            }
        } else if (this.notification != null) {
            this.notification.close();
            this.notification = null;
        }
    }
}
