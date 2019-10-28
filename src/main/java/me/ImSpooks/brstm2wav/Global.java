package me.ImSpooks.brstm2wav;

import java.io.File;

/**
 * Created by Nick on 28 okt. 2019.
 * Copyright © ImSpooks
 */
public class Global {

    public static final int WIDTH = 500 + 7;
    public static final int HEIGHT = 180 + 27;

    public static final File OUTPUT = new File(System.getProperty("user.dir") + File.separator + "output");
    public static final File TEMP_FOLDER = new File(System.getProperty("user.dir") + File.separator + "tmp");
}
