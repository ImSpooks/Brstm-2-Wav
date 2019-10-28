package me.ImSpooks.brstm2wav.gui;

import me.ImSpooks.brstm2wav.Global;
import me.ImSpooks.brstm2wav.Main;
import me.ImSpooks.brstm2wav.gui.init.IntegerFilter;
import me.ImSpooks.brstm2wav.settings.AudioType;
import me.ImSpooks.brstm2wav.settings.Settings;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Layout {
    public JPanel mainPanel;
    private JButton btnSelect;
    private JButton btnConvert;
    private JList<String> fileList;
    private JComboBox<AudioType> cbAudioTypes;
    private JPanel settings;
    private JTextField tbLoopAmount;

    private JFileChooser fc = new JFileChooser();

    private void initialize(Main main) {
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

        for (AudioType audioType : AudioType.CACHE) {
            cbAudioTypes.addItem(audioType);
        }

        ((PlainDocument) tbLoopAmount.getDocument()).setDocumentFilter(new IntegerFilter());
    }

    public Layout(Main main) {
        DefaultListModel<String> files = new DefaultListModel<>();
        fileList.setModel(files);

        btnSelect.addMouseListener(new MouseAdapter() {
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

        btnConvert.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!Global.TEMP_FOLDER.exists())
                    Global.TEMP_FOLDER.mkdir();

                List<Throwable> exceptions = new ArrayList<>();
                int converted = 0;
                for (int i = 0; i < files.size(); i++) {
                    try {
                        if (main.getConverter().convert(files.get(i), new Settings((AudioType) cbAudioTypes.getSelectedItem(), Integer.parseInt(tbLoopAmount.getText())))) {
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

                if (Global.TEMP_FOLDER.exists())
                    deleteFolder(Global.TEMP_FOLDER);
            }
        });

        this.initialize(main);
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}