package co.introtuce.nex2me.test.helper;

public class MyMenory {

    private boolean hasExternalSDCard;
    private long totalRAM;
    private long availableInternalMemorySize;
    private long totalInternalMemorySize;
    private long availableExternalMemorySize;
    private long totalExternalMemorySize;

    public boolean isHasExternalSDCard() {
        return hasExternalSDCard;
    }

    public void setHasExternalSDCard(boolean hasExternalSDCard) {
        this.hasExternalSDCard = hasExternalSDCard;
    }

    public long getTotalRAM() {
        return totalRAM;
    }

    public void setTotalRAM(long totalRAM) {
        this.totalRAM = totalRAM;
    }

    public long getAvailableInternalMemorySize() {
        return availableInternalMemorySize;
    }

    public void setAvailableInternalMemorySize(long availableInternalMemorySize) {
        this.availableInternalMemorySize = availableInternalMemorySize;
    }

    public long getTotalInternalMemorySize() {
        return totalInternalMemorySize;
    }

    public void setTotalInternalMemorySize(long totalInternalMemorySize) {
        this.totalInternalMemorySize = totalInternalMemorySize;
    }

    public long getAvailableExternalMemorySize() {
        return availableExternalMemorySize;
    }

    public void setAvailableExternalMemorySize(long availableExternalMemorySize) {
        this.availableExternalMemorySize = availableExternalMemorySize;
    }

    public long getTotalExternalMemorySize() {
        return totalExternalMemorySize;
    }

    public void setTotalExternalMemorySize(long totalExternalMemorySize) {
        this.totalExternalMemorySize = totalExternalMemorySize;
    }
}
