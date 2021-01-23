package co.introtuce.nex2me.test.helper;

public class MyBattery {

    private int batteryPercent;
    private boolean isPhoneCharging;
    private String batteryHealth;
    private String batteryTechnology;
    private float batteryTemperature;
    private int batteryVoltage;
    private String chargingSource;
    private boolean isBatteryPresent;

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public boolean isPhoneCharging() {
        return isPhoneCharging;
    }

    public void setPhoneCharging(boolean phoneCharging) {
        isPhoneCharging = phoneCharging;
    }

    public String getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(String batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public String getBatteryTechnology() {
        return batteryTechnology;
    }

    public void setBatteryTechnology(String batteryTechnology) {
        this.batteryTechnology = batteryTechnology;
    }

    public float getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(float batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    public int getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(int batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public String getChargingSource() {
        return chargingSource;
    }

    public void setChargingSource(String chargingSource) {
        this.chargingSource = chargingSource;
    }

    public boolean isBatteryPresent() {
        return isBatteryPresent;
    }

    public void setBatteryPresent(boolean batteryPresent) {
        isBatteryPresent = batteryPresent;
    }
}
