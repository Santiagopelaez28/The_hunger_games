package com.foodflow;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.foodflow.logica.Mundo;
import com.foodflow.pantallas.MenuScreen;
import com.foodflow.ui.Pincel;

/** Punto de entrada del juego: guarda lo que comparten todas las pantallas. */
public class FoodFlowGame extends Game {
    public static final float ANCHO = 960f;
    public static final float ALTO = 600f;

    public Pincel pincel;
    public FitViewport vista;
    public Mundo mundo;

    @Override
    public void create() {
        pincel = new Pincel();
        vista = new FitViewport(ANCHO, ALTO);   // el juego se dibuja en 960x600 y se escala a la ventana
        mundo = new Mundo();
        setScreen(new MenuScreen(this));
    }

    public void nuevaPartida() { mundo = new Mundo(); }

    @Override
    public void dispose() {
        super.dispose();
        pincel.dispose();
    }
}
