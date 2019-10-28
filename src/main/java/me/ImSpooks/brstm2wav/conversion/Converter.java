package me.ImSpooks.brstm2wav.conversion;

import me.ImSpooks.brstm2wav.Global;
import me.ImSpooks.brstm2wav.settings.AudioType;
import me.ImSpooks.brstm2wav.settings.Settings;
import org.hackyourlife.gcn.dsp.*;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Converter {

    public Converter() {
        if (!Global.OUTPUT.exists())
            Global.OUTPUT.mkdir();
    }

    public boolean convert(String fileName, Settings settings) throws Exception {
        File originalFile = new File(fileName);
        Stream stream;

        try {
            String filenameLeft = null;
            String filenameRight = null;
            int lext = fileName.lastIndexOf('.');
            if (lext > 1) {
                char[] data = fileName.toCharArray();
                char c = data[lext - 1];
                if (c == 'L') {
                    data[lext - 1] = 'R';
                    filenameLeft = fileName;
                    filenameRight = new String(data);
                }
                else if (c == 'R') {
                    data[lext - 1] = 'L';
                    filenameLeft = new String(data);
                    filenameRight = fileName;
                }
            }

            RandomAccessFile file = new RandomAccessFile(fileName, "r");
            try {
                stream = new BRSTM(file);
            } catch (FileFormatException ex) {
                try {
                    stream = new BFSTM(file);
                } catch (FileFormatException exc) {
                    try {
                        stream = new RS03(file);
                    } catch (FileFormatException exce) {
                        if (filenameLeft != null
                                && new File(filenameLeft).exists()
                                && new File(filenameRight).exists()) {
                            file.close();
                            RandomAccessFile left = new RandomAccessFile(filenameLeft, "r");
                            RandomAccessFile right = new RandomAccessFile(filenameRight, "r");
                            try {
                                stream = new DSP(left, right);
                            } catch (FileFormatException excec) {
                                left.close();
                                right.close();
                                file = new RandomAccessFile(fileName, "r");
                                stream = new DSP(file);
                            }
                        }
                        else
                            stream = new DSP(file);
                    }
                }
            }

        } catch (Exception ex) {
            throw new IllegalStateException("Something went wrong while getting the brstm stream.");
        }

        if (stream == null) {
            throw new IllegalStateException("File must be an brstm file.");
        }

        int channels = stream.getChannels();
        if (channels > 2) {
            channels = 2;
        }
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,    // encoding
                stream.getSampleRate(),            // sample rate
                16,                    // bit/sample
                channels,                // channels
                2 * channels,
                stream.getSampleRate(),
                true                    // big-endian
        );

        // set loop to false
        Field field = stream.getClass().getDeclaredField("loop_flag");
        field.setAccessible(true);
        boolean isLoopable = field.getInt(stream) != 1;

        List<Byte> data = new ArrayList<>();

        int amount = Math.max(settings.getLoopAmount(), 0) + 1;

        while (stream.hasMoreData()) {
            if (stream.getLoopTimes() < amount) {
                byte[] buffer = stream.decode();
                for (byte b : sum(buffer, stream.getChannels())) {
                    data.add(b);
                }
            }
            else break;
        }

        byte[] result = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i);
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(result);

        AudioInputStream audioStream = new AudioInputStream(byteStream, format,
                result.length);
        File source = new File(Global.TEMP_FOLDER, this.getFileNameWithoutExtension(originalFile) + ".wav");
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, source);

        File output = new File(Global.OUTPUT, this.getFileNameWithoutExtension(source) + "." + settings.getAudioType().getExtension());
        if (!output.exists())
            output.createNewFile();

        if (settings.getAudioType() == AudioType.WAV) {
            copyFile(source, output);
        }
        else {
            try {
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec(settings.getAudioType().getCodec());
                audio.setBitRate(128000);
                audio.setChannels(2);
                audio.setSamplingRate(44100);
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat(settings.getAudioType().getEncoding());
                attrs.setAudioAttributes(audio);
                Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(source), output, attrs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private byte[] sum(byte[] data, int channels) {
        if (channels == 1 || channels == 2) {
            return data;
        }
        int samples = data.length / (channels * 2);
        byte[] result = new byte[samples * 4]; // 2 channels, 16bit
        for (int i = 0; i < samples; i++) {
            int l = 0;
            int r = 0;
            for (int ch = 0; ch < channels; ch++) {
                int idx = (i * channels + ch) * 2;
                short val = (short) (Byte.toUnsignedInt(data[idx]) << 8 | Byte.toUnsignedInt(data[idx + 1]));
                if ((ch & 1) == 0) {
                    l += val;
                }
                else {
                    r += val;
                }
            }
            // clamp
            if (l < -32768) {
                l = -32768;
            }
            else if (l > 32767) {
                l = 32767;
            }
            if (r < -32768) {
                r = -32768;
            }
            else if (r > 32767) {
                r = 32767;
            }
            // write back
            result[i * 4] = (byte) (l >> 8);
            result[i * 4 + 1] = (byte) l;
            result[i * 4 + 2] = (byte) (r >> 8);
            result[i * 4 + 3] = (byte) r;
        }
        return result;
    }

    private final Pattern ext = Pattern.compile("(?<=.)\\.[^.]+$");

    private String getFileNameWithoutExtension(File file) {
        return ext.matcher(file.getName()).replaceAll("");
    }

    private void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}
