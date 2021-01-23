package co.introtuce.nex2me.test.fileManager.downloadManager;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoDownloader extends DownloadManager {

    public static final String TAG= "VideoDownloader";
    private String fileName;



    public VideoDownloader(Context context, String fileUrl, String fileName) {
        super(context, fileUrl);
        this.fileName=fileName;
    }

    public VideoDownloader(Context context, String fileUrl, DownloadEventListner downloadEventListner, String fileName) {
        super(context, fileUrl, downloadEventListner);
        this.fileName=fileName;
    }

    private void setMaxLength(int maxLength){
        if(progressBar!=null){
            progressBar.setMax(maxLength);
        }
    }

    @Override
    public void download() {

        OutputStream outputStream = null;

        try {

            File root = Environment.getExternalStorageDirectory();
            if(root == null){
                Log.d(TAG, "Failed to get root");
            }


            URL url = new URL(fileUrl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            //c.setDoOutput(true);
            c.connect();
            Log.d(TAG,"Connected...");
            // create a directory
            File saveDirectory = new File(Environment.getExternalStorageDirectory()+File.separator+ "nex2me/media/video" +File.separator);
            // create direcotory if it doesn't exists
            if(!saveDirectory.exists()){
                Log.d(TAG,"Directory does not exist");
                saveDirectory.mkdirs();
            }

            //String fileName=fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/"+1));
            if(fileName.indexOf(".")>0){
                fileName=fileName.substring(0,fileName.indexOf("."));
            }



            outputStream = new FileOutputStream( saveDirectory + fileName+""+"nex2me.mp4"); // filename.png, .mp3, .mp4 ...
            if(outputStream != null){
                Log.e( "SAVE_PHONE", "Output Stream Opened successfully");
            }

            InputStream in = c.getInputStream();
            int length = c.getContentLength();
            Log.d(TAG,"Max: "+length);

            setMaxLength(length);

            byte[] buffer = new byte[1024];
            int len1 = 0;
            int total = 0;
            while ((len1 = in.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len1);
                total = total+len1;
                updateUI(total);
            }
            outputStream.close();
            outfileUri = saveDirectory + fileName+""+"nex2me.mp4";

        }catch (Exception e) {
            Log.d(TAG, "Exception in download " + e.toString());
            e.printStackTrace();

        }
    }

    @Override
    public void startUI() {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startProgressBar();
                startProgressDialog();
            }
        });


    }

    private void startProgressBar(){
        if(progressBar!=null){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }
    }

    private void startProgressDialog(){
        if(progressDialog!=null){
            //progressDialog= ProgressDialog.show(context,null, "Please Wait ...", true);
            progressDialog.setCancelable(false);
        }
    }



    private void updateProgressBar(int unit){
        if(progressBar!=null){
            progressBar.setProgress(unit);
        }
    }
    private void stopProgressbar(){
        if (progressBar!=null){
            progressBar.setVisibility(View.GONE);
        }
    }

    private void stopProgressDiaglog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void hideUI() {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopProgressbar();
                stopProgressDiaglog();
            }
        });


    }

    @Override
    public void updateUI(final int unit) {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(downloadListner!=null){
                    downloadListner.updateUI(unit);
                }

                updateProgressBar(unit);
            }
        });

    }

    @Override
    public void onStart() {
        if(downloadListner!=null){
            downloadListner.onStartDownload();
        }
        startUI();
    }

    @Override
    public void onEnd() {

        if(downloadListner!=null){
            downloadListner.onEndDownload();
        }
        hideUI();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onEnd();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        download();
        return null;
    }
}
