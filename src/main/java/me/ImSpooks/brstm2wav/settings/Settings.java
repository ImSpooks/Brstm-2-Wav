package me.ImSpooks.brstm2wav.settings;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Settings {

    private final AudioType audioType;
    private final int loopAmount;

    public Settings(AudioType audioType, int loopAmount) {
        this.audioType = audioType;
        this.loopAmount = loopAmount;
    }

    public AudioType getAudioType() {
        return audioType;
    }

    public int getLoopAmount() {
        return loopAmount;
    }
}
