package co.introtuce.nex2me.test;

public interface Nex2meTestEvent {
    public static final int ALL=0;
    void onStartTest(int modelMode,int runtime,int sleepTime);
    void onPauseTest();
    void onEndTest();
}
