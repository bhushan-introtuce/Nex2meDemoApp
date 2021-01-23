package co.introtuce.nex2me.test.fileManager.downloadManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

abstract public class DownloadManager extends AsyncTask<Void,Void,Void> {
    protected String fileUrl;
    protected ProgressBar progressBar;
    protected ProgressDialog progressDialog;
    protected Context context;
    protected String outfileUri;
    protected DownloadEventListner downloadListner;

    public DownloadManager(Context context,String fileUrl){
        this.context = context;
        this.fileUrl = fileUrl;
    }

    public DownloadManager(Context context,String fileUrl, DownloadEventListner downloadEventListner){
        this(context,fileUrl);
        downloadListner=downloadEventListner;
    }

    public DownloadEventListner getDownloadListner() {
        return downloadListner;
    }

    public void setDownloadListner(DownloadEventListner downloadListner) {
        this.downloadListner = downloadListner;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public String getOutfileUri() {
        return outfileUri;
    }

    protected abstract void download();
    protected abstract void startUI();
    protected abstract void hideUI();
    protected abstract void updateUI(int unit);
    protected abstract void onStart();
    protected abstract void onEnd();

    public void start(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        download();
        return null;
    }

}
