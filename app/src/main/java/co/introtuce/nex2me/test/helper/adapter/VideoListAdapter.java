package co.introtuce.nex2me.test.helper.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.introtuce.nex2me.test.R;
import co.introtuce.nex2me.test.fileManager.StaticFileStorage;
import co.introtuce.nex2me.test.helper.PlayForListner;
import co.introtuce.nex2me.test.network.Nex2meBroadcast;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> implements ItemVisiblityListner{
    private Context context;
    private PlayForListner playForListner;
    private Map<Integer,ViewHolder> layouts= new HashMap<Integer, ViewHolder>();
    private SelectionListner itemSelectionListner;
    private List<Nex2meBroadcast> broadcasts;


    public List<Nex2meBroadcast> getBroadcasts() {
        return broadcasts;
    }

    public void setBroadcasts(List<Nex2meBroadcast> broadcasts) {
        this.broadcasts = broadcasts;
    }

    public VideoListAdapter(Context context){
        this.context = context;
    }

    public void setPlayForListner(PlayForListner playForListner) {
        this.playForListner = playForListner;
    }

    public void setItemSelectionListner(SelectionListner itemSelectionListner) {
        this.itemSelectionListner = itemSelectionListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from ( parent.getContext() );
        View view=layoutInflater.inflate (R.layout.video_row,parent,false );
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(position<StaticFileStorage.thumbnails.length){
            setThumbnail(StaticFileStorage.thumbnails[position],holder.imageView);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemSelectionListner!=null){
                    holder.button.setText("  Initializing  ");
                    itemSelectionListner.onItemSelect(position);
                }
            }
        });


        layouts.put(position,holder);

    }

    private void setThumbnail(String url, ImageView img){

        Picasso.get().load(url).into(img, new Callback() {
            @Override
            public void onSuccess() {
                // load success

            }

            @Override
            public void onError(Exception e) {
                //Fail to load set defualt thumbnail
            }
        });

    }

    public void setBack(int index){
        if(index<0 || index>=getItemCount()){
            return;
        }
        ViewHolder viewHolder = layouts.get(index);
        viewHolder.button.setText("I'm In");
    }

    @Override
    public int getItemCount() {
        return StaticFileStorage.video_urls.length+broadcasts.size();
    }

    private int current_index=-1;
    @Override
    public void onItemVisible(int index) {
        Log.d("SCROLLE_I",": "+index);
        if(index<0 || index>=getItemCount() || index == current_index){
            return;
        }
        if(playForListner!=null){
            try{

                ViewHolder viewHolder = layouts.get(index);
                if(viewHolder!=null){
                    playForListner.onItemVisible(viewHolder.getMainView(),index);
                    current_index = index;
                }

            }catch (Exception e){
                Log.d("EXCEPTION_IN",e.toString());
            }

        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            inflate(itemView);
        }

        private void inflate(@NonNull View itemView){
            imageView = itemView.findViewById(R.id.img);
            button = itemView.findViewById(R.id.imin);
        }

        public ImageView getMainView(){
            return imageView;
        }

    }




}
