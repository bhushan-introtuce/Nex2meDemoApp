package co.introtuce.nex2me.test;

import java.util.ArrayList;

public class MegaSuperLog {

    SuperLog small_fp16,small_fp32,medium_fp16,medium_fp32,large_fp16,large_fp32;
    String testId;
    boolean from_all = false;


    public String getTestId() {
        return testId;
    }

    public MegaSuperLog() {
    }
    public boolean isFrom_all() {
        return from_all;
    }

    public void setFrom_all(boolean from_all) {
        this.from_all = from_all;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public SuperLog getSmall_fp16() {
        return small_fp16;
    }

    public void setSmall_fp16(SuperLog small_fp16) {
        this.small_fp16 = small_fp16;
    }

    public SuperLog getSmall_fp32() {
        return small_fp32;
    }

    public void setSmall_fp32(SuperLog small_fp32) {
        this.small_fp32 = small_fp32;
    }

    public SuperLog getMedium_fp16() {
        return medium_fp16;
    }

    public void setMedium_fp16(SuperLog medium_fp16) {
        this.medium_fp16 = medium_fp16;
    }

    public SuperLog getMedium_fp32() {
        return medium_fp32;
    }

    public void setMedium_fp32(SuperLog medium_fp32) {
        this.medium_fp32 = medium_fp32;
    }

    public SuperLog getLarge_fp16() {
        return large_fp16;
    }

    public void setLarge_fp16(SuperLog large_fp16) {
        this.large_fp16 = large_fp16;
    }

    public SuperLog getLarge_fp32() {
        return large_fp32;
    }

    public void setLarge_fp32(SuperLog large_fp32) {
        this.large_fp32 = large_fp32;
    }
}
