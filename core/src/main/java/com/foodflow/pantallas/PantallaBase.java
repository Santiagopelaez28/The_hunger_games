package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.foodflow.FoodFlowGame;

/** Lo comun de todas las pantallas: ventana escalable, clicks en coordenadas del juego y limpiar/dibujar. */
public abstract class PantallaBase extends ScreenAdapter {
    protected final FoodFlowGame juego;
    protected final Vector2 toque = new Vector2();
    protected final Vector2 raton = new Vector2(-5000f, -5000f);
    protected float tiempo = 0f;

    protected PantallaBase(FoodFlowGame juego) { this.juego = juego; }

    @Override
    public void resize(int ancho, int alto) { juego.vista.update(ancho, alto, true); }

    /** true si hubo un click este frame; la posicion queda en 'toque' (coordenadas 960x600). */
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
        tiempo += Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        // el mouse tambien se sigue sin hacer click, para resaltar botones
        raton.set(Gdx.input.getX(), Gdx.input.getY());
        juego.vista.unproject(raton);
        juego.pincel.setRaton(raton.x, raton.y);

        ScreenUtils.clear(0.055f, 0.062f, 0.09f, 1f);
        juego.vista.apply();
        juego.pincel.iniciar(juego.vista.getCamera().combined);
    }

    protected void terminarFrame() {
        // pequeno fundido al entrar a cada pantalla
        float f = Math.min(1f, tiempo / 0.30f);
        if (f < 1f) juego.pincel.rect(0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0x0B0D13, 1f - f);
        juego.pincel.terminar();
    }
}
