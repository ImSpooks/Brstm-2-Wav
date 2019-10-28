package me.ImSpooks.brstm2wav;

import me.ImSpooks.brstm2wav.gui.Layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Main {

    private static final int WIDTH = 350;
    private static final int HEIGHT = 100;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Brstm 2 Wav converter");
        frame.setContentPane(new Layout().mainPanel);

        frame.setSize(WIDTH, HEIGHT);
        frame.setMinimumSize(new Dimension(WIDTH,  HEIGHT));
        frame.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
