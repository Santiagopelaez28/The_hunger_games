package com.foodflow.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Dibuja todo con un solo SpriteBatch: rectángulos, círculos y texto.
 * No necesita imágenes en disco (las formas salen de dos texturas blancas generadas al iniciar).
 * Los colores son enteros RGB (0xRRGGBB).
 */
public class Pincel implements Disposable {
    private static final float ESCALA_BASE = 1.2f;

    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fuente = new BitmapFont();   // Arial incluida en LibGDX
    private final GlyphLayout medida = new GlyphLayout();
    private final Texture blanco;
    private final Texture circulo;
    private final Color tmp = new Color();
    private float alfaGlobal = 1f;

    public Pincel() {
        Pixmap cuadro = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        cuadro.setColor(Color.WHITE);
        cuadro.fill();
        blanco = new Texture(cuadro);
        cuadro.dispose();

        Pixmap disco = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        disco.setColor(Color.WHITE);
        disco.fillCircle(32, 32, 30);
        circulo = new Texture(disco);
        disco.dispose();
        circulo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void iniciar(Matrix4 proyeccion) {
        batch.setProjectionMatrix(proyeccion);
        batch.begin();
    }

    public void terminar() { batch.end(); }

    private void color(int rgb, float alfa) {
        Color.rgb888ToColor(tmp, rgb);
        tmp.a = alfa * alfaGlobal;
    }

    /** Multiplica la transparencia de todo lo que se dibuje después (1 = normal). */
    public void setAlfa(float alfa) { alfaGlobal = alfa; }

    public void rect(float x, float y, float w, float h, int rgb) { rect(x, y, w, h, rgb, 1f); }

    public void rect(float x, float y, float w, float h, int rgb, float alfa) {
        color(rgb, alfa);
        batch.setColor(tmp);
        batch.draw(blanco, x, y, w, h);
    }

    public void circulo(float cx, float cy, float radio, int rgb) {
        color(rgb, 1f);
        batch.setColor(tmp);
        float d = radio * 2f * 32f / 30f;   // la textura es un disco de radio 30 dentro de 32
        batch.draw(circulo, cx - d / 2f, cy - d / 2f, d, d);
    }

    /** Texto con la esquina superior izquierda en (x, y). */
    public void texto(String s, float x, float y, int rgb) { texto(s, x, y, rgb, 1f); }

    public void texto(String s, float x, float y, int rgb, float escala) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        color(rgb, 1f);
        fuente.setColor(tmp);
        fuente.draw(batch, s, x, y);
    }

    /** Texto centrado en (cx, cy). */
    public void textoCentrado(String s, float cx, float cy, int rgb, float escala) {
        textoCentrado(s, cx, cy, rgb, escala, 1f);
    }

    public void textoCentrado(String s, float cx, float cy, int rgb, float escala, float alfa) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        medida.setText(fuente, s);
        color(rgb, alfa);
        fuente.setColor(tmp);
        fuente.draw(batch, medida, cx - medida.width / 2f, cy + medida.height / 2f);
    }

    public void boton(String texto, float x, float y, float w, float h, int rgb, boolean activo) {
        float alfa = activo ? 1f : 0.35f;
        rect(x, y, w, h, rgb, alfa);
        textoCentrado(texto, x + w / 2f, y + h / 2f, tinta(rgb), 1f, activo ? 1f : 0.6f);
    }

    /** Negro o blanco, según cuál se lea mejor sobre el color de fondo. */
    public static int tinta(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        return (0.299f * r + 0.587f * g + 0.114f * b) > 0.6f ? 0x1B1D26 : 0xFFFFFF;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fuente.dispose();
        blanco.dispose();
        circulo.dispose();
    }
}
