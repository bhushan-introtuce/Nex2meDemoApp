package co.introtuce.nex2me.test.helper;

import com.google.android.exoplayer2.ExoPlaybackException;

public interface ErrorListner {
    public void onExoError(ExoPlaybackException error, String source);
}
