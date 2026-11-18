package com.foodflow.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.foodflow.FoodFlowGame;

/** Lanzador de escritorio. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("FoodFlow");
        config.setWindowedMode(960, 600);
        // ventana de 960x600 pixeles reales: evita que Windows la agrande y meta barras negras
        config.setHdpiMode(com.badlogic.gdx.graphics.glutils.HdpiMode.Pixels);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new FoodFlowGame(), config);
    }
}
