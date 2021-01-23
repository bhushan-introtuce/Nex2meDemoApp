package co.introtuce.nex2me.test.analytics;

import com.google.mediapipe.framework.Graph;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketCreator;
import com.google.mediapipe.framework.TextureReleaseCallback;

public class CustomPacketCreator extends PacketCreator {
    public CustomPacketCreator(Graph context) {
        super(context);
    }

    //Calling super class Method as it is.
    @Override
    public Packet createGpuBuffer(int name, int width, int height, TextureReleaseCallback releaseCallback) {
        return super.createGpuBuffer(name, width, height, releaseCallback);
    }
}
