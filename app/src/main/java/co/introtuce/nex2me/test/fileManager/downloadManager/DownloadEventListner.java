package co.introtuce.nex2me.test.fileManager.downloadManager;

public interface DownloadEventListner {
    public abstract void onStartDownload();
    public abstract void onEndDownload();
    public void updateUI(int unit);

}
