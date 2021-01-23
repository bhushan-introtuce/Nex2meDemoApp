package co.introtuce.nex2me.test.fileManager;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import static co.introtuce.nex2me.test.MainActivity.TAG;

public class StaticFileStorage {

    public static final String UNKNOW_VALUES= "unknown";
    //Static videos urls
    public static String[] video_urls = {
            "https://boyvideo.s3.ap-south-1.amazonaws.com/Complex14.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/download.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/People+-+17351.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/Boy+-+21827.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/aanchal.gr_80111977_583855019046409_7144342298646601252_n+(1).mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/Trees+-+24540.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/Simple10.mp4",
            "https://boyvideo.s3.ap-south-1.amazonaws.com/Medium3.mp4"
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/Spiderman+and+Thanos+dancing+green+screen.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/Spiderman+and+Thanos+dancing+green+screen.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/Spiderman+and+Thanos+dancing+green+screen.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            //"https://mediafilnex.s3.ap-south-1.amazonaws.com/Spiderman+and+Thanos+dancing+green+screen.mp4",
    };
    //static videos thumbnails
    public static String[] thumbnails = {
            "https://tempmediako.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            "https://tempmediako.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            "https://tempmediako.s3.ap-south-1.amazonaws.com/sample_one.mp4",
            "https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.jpg",
            "https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.jpg",
            "https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.jpg",
            "https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.jpg",
            "https://mediafilnex.s3.ap-south-1.amazonaws.com/sample_one.jpg",
    };

    //Video name
    public static String fileName[] = {
            "dace_one1",
            "dance_one2",
            "Sir_videok",
            "mpvideo",
            "Sir_videofdgy",
            "superman_danceji",
            "Sir_videojkhgj",
            "superman_danceuu",
            "Sir_videolp",
            "superman_dance67",

    };
    //Local path of file
    public static String[] localPath = {
            UNKNOW_VALUES,
    };
    public static String getFilePath(int index){
        return getFilePath(fileName[index]);
    }
    public static String getFilePath(String url){
        String result = "";
        if(url==null){
            return result;
        }
        File root = Environment.getExternalStorageDirectory();
        if(root == null){
            Log.d(TAG, "Failed to get root");
            return result;
        }
        File saveDirectory = new File(Environment.getExternalStorageDirectory()+File.separator+ "nex2me/media/video" +File.separator);
        // create direcotory if it doesn't exists
        if(!saveDirectory.exists()){
            Log.d(TAG,"Directory does not exist");
            //saveDirectory.mkdirs();
            return result;
        }
        //String fileName=fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/"+1));
        if(url.indexOf(".")>0){
            url=url.substring(0,url.indexOf("."));
        }
        result = saveDirectory + url+""+"nex2me.mp4";
        return result;
    }
    public static boolean isFileExist(String path){
        Log.d(TAG,"Checking: "+path);
        File file = new File(path);
        return file.exists();
    }
    public static boolean isFileExist(int index){
        Log.d(TAG,"Checking: "+index);
        return  isFileExist(getFilePath(index));
    }

}
