package co.introtuce.nex2me.test.helper;


import com.an.deviceinfo.device.model.Battery;
import com.an.deviceinfo.device.model.Device;
import com.an.deviceinfo.device.model.Memory;

public class MainLog {

    String runTime;
    Device device;
    Battery battery;
    Memory memory;
    String CPU_Info;
    String GPU_info;

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public String getCPU_Info() {
        return CPU_Info;
    }

    public void setCPU_Info(String CPU_Info) {
        this.CPU_Info = CPU_Info;
    }

    public String getGPU_info() {
        return GPU_info;
    }

    public void setGPU_info(String GPU_info) {
        this.GPU_info = GPU_info;
    }
}
