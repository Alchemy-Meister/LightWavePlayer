package com.naman14.timber;

import com.naman14.timber.helpers.MusicPlaybackTrack;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

interface ITimberService
{
    void openFile(String path);
    void open(in long [] list, int position, long sourceId, int sourceType);
    void stop();
    void pause();
    void play();
    void prev(boolean forcePrevious);
    void next();
    void enqueue(in long [] list, int action, long sourceId, int sourceType);
    void setQueuePosition(int index);
    void setShuffleMode(int shufflemode);
    void setRepeatMode(int repeatmode);
    void moveQueueItem(int from, int to);
    void refresh();
    void playlistChanged();
    boolean isPlaying();
    long [] getQueue();
    long getQueueItemAtPosition(int position);
    int getQueueSize();
    int getQueuePosition();
    int getQueueHistoryPosition(int position);
    int getQueueHistorySize();
    int[] getQueueHistoryList();
    long duration();
    long position();
    long seek(long pos);
    void seekRelative(long deltaInMs);
    long getAudioId();
    MusicPlaybackTrack getCurrentTrack();
    MusicPlaybackTrack getTrack(int index);
    long getNextAudioId();
    long getPreviousAudioId();
    long getArtistId();
    long getAlbumId();
    String getArtistName();
    String getTrackName();
    String getAlbumName();
    String getPath();
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(long id);
    boolean removeTrackAtPosition(long id, int position);
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();
    void setLockscreenAlbumArt(boolean enabled);
    boolean isDeviceConnected(String address);
    boolean connectToDevice(in BluetoothDevice device, boolean secure, in List<ParcelUuid> candidates);
    void initializeVisualizer();
    void enableVisualizer(boolean enable);
    String getConnectedDeviceName();
    void enableReconnect(boolean enable);
    void startBluetoothConnectionChecker();
    void interruptReconnect(boolean cancel);
}

