package co.introtuce.nex2me.test;

import com.an.deviceinfo.device.model.Battery;
import com.an.deviceinfo.device.model.Device;
import com.an.deviceinfo.device.model.Memory;

import java.util.ArrayList;

import co.introtuce.nex2me.test.helper.MainLog;

public class SuperLog {


    String status;
    ArrayList<String> runs;
    Device device;
    Battery battery;
    Memory memory;
    String gpu_info;
    String cup_info;


    public String getGpu_info() {
        return gpu_info;
    }

    public void setGpu_info(String gpu_info) {
        this.gpu_info = gpu_info;
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

    public String getCup_info() {
        return cup_info;
    }

    public void setCup_info(String cup_info) {
        this.cup_info = cup_info;
    }

    long average_runtime,min_runtime,max_runtime,first_runtime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getRuns() {
        return runs;
    }

    public void setRuns(ArrayList<String> runs) {
        this.runs = runs;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public long getAverage_runtime() {
        return average_runtime;
    }

    public void setAverage_runtime(long average_runtime) {
        this.average_runtime = average_runtime;
    }

    public long getMin_runtime() {
        return min_runtime;
    }

    public void setMin_runtime(long min_runtime) {
        this.min_runtime = min_runtime;
    }

    public long getMax_runtime() {
        return max_runtime;
    }

    public void setMax_runtime(long max_runtime) {
        this.max_runtime = max_runtime;
    }

    public long getFirst_runtime() {
        return first_runtime;
    }

    public void setFirst_runtime(long first_runtime) {
        this.first_runtime = first_runtime;
    }
}
