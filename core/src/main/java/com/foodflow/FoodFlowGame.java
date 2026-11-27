package com.foodflow;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.foodflow.logica.Mundo;
import com.foodflow.pantallas.MenuScreen;
import com.foodflow.ui.Pincel;

/** Punto de entrada del juego: guarda lo que comparten todas las pantallas. */
public class FoodFlowGame extends Game {
    public static final float ANCHO = 960f;
    public static final float ALTO = 600f;
    public static final String NOMBRE_RESTAURANTE = "The Flavor Rush";

    public Pincel pincel;
    public FitViewport vista;
    public Mundo mundo;
    public Texture nubes;        // fondo de la zona de clientes (assets/nubes.jpg)
    public Texture madera;       // fondo del mostrador (assets/madera.jpg)
    public Texture fondoMenu;    // ilustracion del menu principal
    public Texture fondoTienda;  // ilustracion de la despensa
    public Texture fondoFin;     // ilustracion del final de la partida

    public Sound campana;        // sonido al atender (assets/campana.wav; opcional)

    public Texture logoRestaurante; // logo del restaurante para el letrero (assets/restaurante_logo.*; opcional)

    @Override
    public void create() {
        pincel = new Pincel();
        nubes = cargarFondo("nubes");
        madera = cargarFondo("madera");
        fondoMenu = cargarFondo("fondo_menu");
        fondoTienda = cargarFondo("fondo_tienda");
        fondoFin = cargarFondo("fondo_fin");
        if (Gdx.files.internal("campana.wav").exists()) campana = Gdx.audio.newSound(Gdx.files.internal("campana.wav"));
        logoRestaurante = cargarFondo("restaurante_logo");
        if (logoRestaurante == null && Gdx.files.internal("Restaurante_logo.png").exists()) {
            logoRestaurante = cargarTextura("Restaurante_logo.png");
        }
        vista = new FitViewport(ANCHO, ALTO);   // el juego se dibuja en 960x600 y se escala a la ventana
        mundo = new Mundo();
        setScreen(new MenuScreen(this));
    }

    public void nuevaPartida() { mundo = new Mundo(); }

    /** Carga assets/nombre.jpg, .png o .webp. Si falta, el juego sigue con un color liso en su lugar. */
    private Texture cargarFondo(String nombre) {
        String[] extensiones = {".jpg", ".jpeg", ".png", ".webp"};
        for (String ext : extensiones) {
            if (Gdx.files.internal(nombre + ext).exists()) return cargarTextura(nombre + ext);
        }
        System.err.println("FoodFlow: no se encontro assets/" + nombre + " (se usara un color liso)");
        return null;
    }

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
        if (fondoMenu != null) fondoMenu.dispose();
        if (fondoTienda != null) fondoTienda.dispose();
        if (fondoFin != null) fondoFin.dispose();
        if (campana != null) campana.dispose();
        if (logoRestaurante != null) logoRestaurante.dispose();
    }
}
