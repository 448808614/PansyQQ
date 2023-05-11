package com.pansy.robot.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.io.IOException;


public class SoundUtil {
    /**
     * 播放声音 不能同时播放多种音频
     * 消耗资源较大
     *
     * @param rawId
     */
    private static MediaPlayer mMediaPlayer;
    private static SoundPool soundPool;

    public static void playSound(Context context,int rawId) {
        try {
            if(mMediaPlayer!=null){
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
            mMediaPlayer=new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(beepListener);
            try {
                AssetFileDescriptor file = context.getResources().openRawResourceFd(
                        rawId);
                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                //mMediaPlayer.setVolume(0.50f, 0.50f);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            e.printStackTrace();
        }

    }

    private static final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    /**
     * 适合播放声音短，文件小
     * 可以同时播放多种音频
     * 消耗资源较小
     */
    public static void playSoundPool(Context context,int rawId) {
        if(soundPool!=null){
            soundPool.release();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频的数量
            builder.setMaxStreams(1);
            //AudioAttributes是一个封装音频各种属性的类
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            //第一个参数是可以支持的声音数量，第二个是声音类型，第三个是声音品质
            soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        }
        //第一个参数Context,第二个参数资源Id，第三个参数优先级
        soundPool.load(context, rawId, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(1, 1, 1, 0, 0, 1);
            }
        });

        //第一个参数id，即传入池中的顺序，第二个和第三个参数为左右声道，第四个参数为优先级，第五个是否循环播放，0不循环，-1循环
        //最后一个参数播放比率，范围0.5到2，通常为1表示正常播放
//        soundPool.play(1, 1, 1, 0, 0, 1);
        //回收Pool中的资源
        //soundPool.release();
    }


}
