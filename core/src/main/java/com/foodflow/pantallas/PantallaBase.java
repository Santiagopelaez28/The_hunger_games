package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.foodflow.FoodFlowGame;

/** Lo común de todas las pantallas: ventana escalable, clicks en coordenadas del juego y limpiar/dibujar. */
public abstract class PantallaBase extends ScreenAdapter {
    protected final FoodFlowGame juego;
    protected final Vector2 toque = new Vector2();

    protected PantallaBase(FoodFlowGame juego) { this.juego = juego; }

    @Override
    public void resize(int ancho, int alto) { juego.vista.update(ancho, alto, true); }

    /** true si hubo un click este frame; la posición queda en 'toque' (coordenadas 960x600). */
    protected boolean hayClick() {
        if (!Gdx.input.justTouched()) return false;
        toque.set(Gdx.input.getX(), Gdx.input.getY());
        juego.vista.unproject(toque);
        return true;
    }

    protected static boolean dentro(Vector2 p, float x, float y, float w, float h) {
        return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
    }

    protected void empezarFrame() {
        ScreenUtils.clear(0.10f, 0.11f, 0.15f, 1f);
        juego.vista.apply();
        juego.pincel.iniciar(juego.vista.getCamera().combined);
    }

    protected void terminarFrame() { juego.pincel.terminar(); }
}
