package co.introtuce.nex2me.test.analytics;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import co.introtuce.nex2me.test.ui.videoviews.AutoFitTextureView;

public class FrameEncoder {

    private Thread thread;
    boolean isStart=false;
    private File tempDirectory;
    int counter=0;
    Bitmap bitmap;
    Context context;
    private AutoFitTextureView textureView;
    public FrameEncoder(AutoFitTextureView textureView, Context context){
        this.textureView=textureView;
        this.context=context;
    }
    public void toggle(){
        if(isStart){
            stopEncoding();
        }
        else {
            startEncoding();
        }

    }
    public void startEncoding(){
        if(textureView==null){
            return;
        }
        thread = getNewThread();
        isStart=true;
        thread.start();

    }
    public void stopEncoding(){
        isStart=false;
    }

    private Thread getNewThread(){
       return new Thread(new Runnable() {
            @Override
            public void run() {
                counter=0;
                while (isStart){
                    try {
                        prepRootFolder();
                        Log.d("COMPRESS_TIME","Starting");
                        if(textureView!=null && context!=null){
                            long oldTime = System.currentTimeMillis();
                            bitmap = textureView.getBitmap();
                            counter=counter+1;
                            String fileName = System.currentTimeMillis()+""+counter+".png";
                            try (FileOutputStream out = new FileOutputStream(tempDirectory.getAbsolutePath()+"/"+fileName)) {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            }catch (IOException e){
                                e.printStackTrace();
                                Log.d("COMPRESS_TIME",e.toString());
                            }
                            long newTime = System.currentTimeMillis();
                            Log.d("COMPRESS_TIME","Time "+(newTime-oldTime));
                            Thread.sleep(1000/20);
                        }
                    }catch (Exception ee){
                        ee.printStackTrace();
                        Log.d("COMPRESS_TIME","Error "+ ee.toString());
                    }

                }
                if(tempDirectory!=null){
                    deleteTempFiles(tempDirectory);
                }

            }
        });

    }

    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if(f!=null){
                        if (f.isDirectory()) {
                            //deleteTempFiles(f);
                        } else {
                            f.delete();
                        }
                    }
                }
            }
        }
        return file.delete();
    }

    private void prepRootFolder(){
        String root = Environment.getExternalStorageDirectory().toString()+"/nex2me/media/temp";

        File myDir = new File(root);
        if(!myDir.exists())
            myDir.mkdirs();
        //deleteTempFiles(tempDirectory);
        this.tempDirectory=myDir;
    }


}
