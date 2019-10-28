package me.ImSpooks.brstm2wav;

import me.ImSpooks.brstm2wav.conversion.Converter;
import me.ImSpooks.brstm2wav.gui.Layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Main {

    private Converter converter;
    private JFrame frame;

    public Main() {
        this.converter = new Converter();

        JFrame frame = new JFrame("Brstm 2 Wav converter");

        Layout layout = new Layout(this);
        frame.setContentPane(layout.mainPanel);

        frame.setPreferredSize(new Dimension(Global.WIDTH, Global.HEIGHT));

        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Converter getConverter() {
        return converter;
    }

    public JFrame getFrame() {
        return frame;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // look and feel from system's os
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new Main();
    }
}
