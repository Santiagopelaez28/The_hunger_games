package com.foodflow;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
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
    public Texture nubes;    // fondo de la zona de clientes (assets/nubes.jpg)
    public Texture madera;   // fondo del mostrador (assets/madera.jpg)

    @Override
    public void create() {
        pincel = new Pincel();
        nubes = cargarTextura("nubes.jpg");
        madera = cargarTextura("madera.jpg");
        vista = new FitViewport(ANCHO, ALTO);   // el juego se dibuja en 960x600 y se escala a la ventana
        mundo = new Mundo();
        setScreen(new MenuScreen(this));
    }

    public void nuevaPartida() { mundo = new Mundo(); }

    /** Carga una imagen de assets/. Si falta, el juego sigue con un color liso en su lugar. */
    private Texture cargarTextura(String archivo) {
        try {
            Texture t = new Texture(Gdx.files.internal(archivo));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (RuntimeException e) {
            System.err.println("FoodFlow: no se pudo cargar assets/" + archivo + " (" + e.getMessage() + ")");
            return null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        pincel.dispose();
        if (nubes != null) nubes.dispose();
        if (madera != null) madera.dispose();
    }
}
