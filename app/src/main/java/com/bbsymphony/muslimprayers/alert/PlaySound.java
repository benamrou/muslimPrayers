package com.bbsymphony.muslimprayers.alert;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Ahmed on 8/14/2015.
 */
public final class PlaySound {
    private static HashSet<MediaPlayer> mpSet = new HashSet<MediaPlayer>();
    private static String LOG ="PLAYSOUND";

     public static void play(Context context, int id) {
        try {
             MediaPlayer mp = new MediaPlayer();
             mp.setDataSource (context, Uri.parse("android.resource://" + context.getPackageName() + "/" + id));
             mp.setAudioStreamType(AudioManager.STREAM_RING);
             mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                 @Override
                 public void onCompletion(MediaPlayer mp) {
                     mpSet.remove(mp);
                     mp.stop();
                     mp.release();
                 }
             });
             mp.prepare();
             mp.setLooping(false);
             mpSet.add(mp);
             mp.start();
         } catch (IOException e) {
             Log.d(LOG, "Error playing audio resource", e);
         }
     }

    public static boolean isPlaying() {
        for (MediaPlayer mp : mpSet) {
            if (mp != null) {
                return true;
            }
        }
        return false;
    }

    public static void stop() {
        for (MediaPlayer mp : mpSet) {
            if (mp != null) {
            mp.stop();
            mp.release();
            }
        }
        mpSet.clear();
     }
}
