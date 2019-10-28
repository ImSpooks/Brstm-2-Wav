package me.ImSpooks.brstm2wav.settings;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public enum AudioType {
    WAV("wav"),
    MP3("mp3", "libmp3lame"),
    OGG("ogg", "libvorbis"),
    ;

    public static AudioType[] CACHE = values();

    private final String encoding;
    private final String extension;
    private final String codec;

    AudioType(String encoding, String extension, String codec) {
        this.encoding = encoding;
        this.extension = extension;
        this.codec = codec;
    }

    AudioType(String encoding, String codec) {
        this(encoding, encoding, codec);
    }

    AudioType(String encoding) {
        this(encoding, encoding, "");
    }

    public String getEncoding() {
        return encoding;
    }

    public String getExtension() {
        return extension;
    }

    public String getCodec() {
        return codec;
    }
}
