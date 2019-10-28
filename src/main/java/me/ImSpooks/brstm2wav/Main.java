package me.ImSpooks.brstm2wav;

import me.ImSpooks.brstm2wav.gui.Layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Main {

    public static final int WIDTH = 500 + 7;
    public static final int HEIGHT = 180 + 27;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // look and feel from system's os
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Brstm 2 Wav converter");
        frame.setContentPane(new Layout(frame).mainPanel);

        frame.setSize(WIDTH, HEIGHT);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
