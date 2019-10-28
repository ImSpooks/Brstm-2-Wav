package me.ImSpooks.brstm2wav.gui;

import org.hackyourlife.gcn.dsp.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Layout {
    public JPanel mainPanel;
    private JButton convert;
    private JButton selectFile;
    private JList<String> fileList;
    private JLabel fileLabel;

    private JFileChooser fc = new JFileChooser();

    public Layout(JFrame frame) {
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

        DefaultListModel<String> files = new DefaultListModel<>();
        fileList.setModel(files);

        selectFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int returnVal = fc.showOpenDialog(mainPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    for (File selectedFile : fc.getSelectedFiles()) {
                        files.addElement(selectedFile.getAbsolutePath());
                    }

                    int entrySize = Math.min(files.getSize() - 2, 0);
                }
            }
        });

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int index = fileList.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    files.remove(index);
                }
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    files.set(index, JOptionPane.showInputDialog("New value: ", files.get(index)));

                }
            }
        });

        convert.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                List<Throwable> exceptions = new ArrayList<>();
                int converted = 0;
                for (int i = 0; i < files.size(); i++) {
                    try {
                        if (convert(files.get(i))) {
                            converted++;
                        }
                    } catch (Exception ex) {
                        exceptions.add(ex);
                    }
                }
                if (!exceptions.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, exceptions, "Error", JOptionPane.ERROR_MESSAGE);
                }
                if (converted > 0) {
                    JOptionPane.showMessageDialog(mainPanel, String.format("%s files converted.", converted));
                }
            }
        });
    }

    private boolean convert(String fileName) throws Exception {
        File originalFile = new File(fileName);
        Stream stream = null;

        if (true) {
            throw new RuntimeException("test");
        }

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
        field.setInt(stream, 0);

        List<Byte> data = new ArrayList<>();

        while (stream.hasMoreData()) {
            byte[] buffer = stream.decode();
            for (byte b : sum(buffer, stream.getChannels())) {
                data.add(b);
            }
        }

        byte[] result = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i);
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(result);

        AudioInputStream audioStream = new AudioInputStream(byteStream, format,
                result.length);
        File file = new File(originalFile.getParentFile().getAbsolutePath(), this.getFileNameWithoutExtension(originalFile) + ".wav");
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
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
}
