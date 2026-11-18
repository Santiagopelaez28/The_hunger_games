package com.foodflow.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Dibuja todo con un solo SpriteBatch: rectangulos, circulos, paneles redondeados,
 * degradados, brillos y texto. No necesita imagenes en disco (las formas salen de
 * dos texturas blancas generadas al iniciar).
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
    private float ratonX = -9999f, ratonY = -9999f;
    private float tiempo = 0f;

    public Pincel() {
        Pixmap cuadro = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        cuadro.setColor(Color.WHITE);
        cuadro.fill();
        blanco = new Texture(cuadro);
        cuadro.dispose();

        // disco con borde suave (supersampling) para que los redondeados no salgan dentados.
        // Se escriben los bytes directo para que el color quede blanco y solo cambie el alfa.
        Pixmap disco = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        java.nio.ByteBuffer pixeles = disco.getPixels();
        for (int py = 0; py < 64; py++) {
            for (int px = 0; px < 64; px++) {
                int cubiertos = 0;
                for (int sy = 0; sy < 4; sy++) {
                    for (int sx = 0; sx < 4; sx++) {
                        float dx = px + (sx + 0.5f) / 4f - 32f;
                        float dy = py + (sy + 0.5f) / 4f - 32f;
                        if (dx * dx + dy * dy <= 30f * 30f) cubiertos++;
                    }
                }
                int i = (py * 64 + px) * 4;
                pixeles.put(i, (byte) 0xFF);
                pixeles.put(i + 1, (byte) 0xFF);
                pixeles.put(i + 2, (byte) 0xFF);
                pixeles.put(i + 3, (byte) (cubiertos * 255 / 16));
            }
        }
        circulo = new Texture(disco);
        disco.dispose();
        circulo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // la fuente incluida se ve mas suave con filtrado lineal al escalarla
        fuente.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void iniciar(Matrix4 proyeccion) {
        tiempo += Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        batch.setProjectionMatrix(proyeccion);
        batch.begin();
    }

    public void terminar() { batch.end(); }

    /** Segundos desde que arranco el juego (para animaciones). */
    public float getTiempo() { return tiempo; }

    /** Posicion del mouse en coordenadas del juego (la actualiza PantallaBase cada frame). */
    public void setRaton(float x, float y) { ratonX = x; ratonY = y; }

    public boolean ratonDentro(float x, float y, float w, float h) {
        return ratonX >= x && ratonX <= x + w && ratonY >= y && ratonY <= y + h;
    }

    private void color(int rgb, float alfa) {
        Color.rgb888ToColor(tmp, rgb);
        tmp.a = alfa * alfaGlobal;
    }

    /** Multiplica la transparencia de todo lo que se dibuje despues (1 = normal). */
    public void setAlfa(float alfa) { alfaGlobal = alfa; }

    // ------------------------------------------------------------------ formas basicas

    public void rect(float x, float y, float w, float h, int rgb) { rect(x, y, w, h, rgb, 1f); }

    public void rect(float x, float y, float w, float h, int rgb, float alfa) {
        if (w <= 0 || h <= 0) return;
        color(rgb, alfa);
        batch.setColor(tmp);
        batch.draw(blanco, x, y, w, h);
    }

    public void elipse(float cx, float cy, float rx, float ry, int rgb) {
        elipse(cx, cy, rx, ry, rgb, 1f);
    }

    public void elipse(float cx, float cy, float rx, float ry, int rgb, float alfa) {
        if (rx <= 0 || ry <= 0) return;
        color(rgb, alfa);
        batch.setColor(tmp);
        float dw = rx * 2f * 32f / 30f, dh = ry * 2f * 32f / 30f;
        batch.draw(circulo, cx - dw / 2f, cy - dh / 2f, dw, dh);
    }

    public void circulo(float cx, float cy, float radio, int rgb) {
        circulo(cx, cy, radio, rgb, 1f);
    }

    public void circulo(float cx, float cy, float radio, int rgb, float alfa) {
        if (radio <= 0) return;
        color(rgb, alfa);
        batch.setColor(tmp);
        dibujarDisco(cx, cy, radio);
    }

    private void dibujarDisco(float cx, float cy, float radio) {
        float d = radio * 64f / 60f;   // la textura es un disco de radio 30 dentro de 32
        batch.draw(circulo, cx - d / 2f, cy - d / 2f, d, d);
    }

    /** Rectangulo con las esquinas redondeadas. */
    public void redondeado(float x, float y, float w, float h, float radio, int rgb) {
        redondeado(x, y, w, h, radio, rgb, 1f);
    }

    public void redondeado(float x, float y, float w, float h, float radio, int rgb, float alfa) {
        if (w <= 0 || h <= 0) return;
        float r = Math.max(0f, Math.min(radio, Math.min(w, h) / 2f));
        color(rgb, alfa);
        batch.setColor(tmp);
        if (r < 0.6f) {
            batch.draw(blanco, x, y, w, h);
            return;
        }
        // Esquinas: se dibuja solo un cuadrante del disco, sin superponerlo con los rectangulos.
        // Asi el color no se mezcla dos veces cuando se usa transparencia.
        float d = r * 32f / 30f;
        batch.draw(circulo, x + r - d, y + h - r, d, d, 0, 0, 32, 32, false, false);
        batch.draw(circulo, x + w - r, y + h - r, d, d, 32, 0, 32, 32, false, false);
        batch.draw(circulo, x + r - d, y, d, d, 0, 32, 32, 32, false, false);
        batch.draw(circulo, x + w - r, y, d, d, 32, 32, 32, 32, false, false);

        float cw = w - 2f * r, ch = h - 2f * r;
        if (cw > 0) {
            if (ch > 0) batch.draw(blanco, x + r, y + r, cw, ch);
            batch.draw(blanco, x + r, y, cw, r);
            batch.draw(blanco, x + r, y + h - r, cw, r);
        }
        if (ch > 0) {
            batch.draw(blanco, x, y + r, r, ch);
            batch.draw(blanco, x + w - r, y + r, r, ch);
        }
    }

    /** Rectangulo girado 'grados' alrededor de su centro. */
    public void rectRotado(float cx, float cy, float w, float h, float grados, int rgb, float alfa) {
        color(rgb, alfa);
        batch.setColor(tmp);
        batch.draw(blanco, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h,
                1f, 1f, grados, 0, 0, 1, 1, false, false);
    }

    /** Sombra suave debajo de una tarjeta. */
    public void sombra(float x, float y, float w, float h, float radio, float alfa) {
        redondeado(x, y - 4f, w, h, radio, 0x000000, alfa);
    }

    /** Tarjeta con borde, sombra y un brillo sutil arriba. */
    public void panel(float x, float y, float w, float h, float radio, int relleno, int borde) {
        panel(x, y, w, h, radio, relleno, borde, 1f);
    }

    public void panel(float x, float y, float w, float h, float radio, int relleno, int borde, float alfa) {
        sombra(x, y, w, h, radio, 0.30f * alfa);
        redondeado(x, y, w, h, radio, borde, alfa);
        redondeado(x + 1.5f, y + 1.5f, w - 3f, h - 3f, Math.max(1f, radio - 1.5f), relleno, alfa);
        redondeado(x + 5f, y + h * 0.52f, w - 10f, h * 0.42f, Math.max(1f, radio - 4f), 0xFFFFFF, 0.05f * alfa);
    }

    // ------------------------------------------------------------------ degradados y brillos

    /** Degradado vertical opaco: 'colorAbajo' en la base y 'colorArriba' en el tope. */
    public void degradado(float x, float y, float w, float h, int colorAbajo, int colorArriba) {
        degradado(x, y, w, h, colorAbajo, colorArriba, 1f);
    }

    public void degradado(float x, float y, float w, float h, int colorAbajo, int colorArriba, float alfa) {
        int pasos = Math.max(2, Math.min(48, (int) (h / 3f)));
        float alto = h / pasos;
        for (int i = 0; i < pasos; i++) {
            float t = (i + 0.5f) / pasos;
            rect(x, y + i * alto, w, alto + 0.65f, mezclarRgb(colorAbajo, colorArriba, t), alfa);
        }
    }

    /** Degradado de transparencia (util para oscurecer bordes de una foto). */
    public void degradadoAlpha(float x, float y, float w, float h, int rgb, float alfaAbajo, float alfaArriba) {
        int pasos = Math.max(2, Math.min(40, (int) (h / 3f)));
        float alto = h / pasos;
        for (int i = 0; i < pasos; i++) {
            float t = (i + 0.5f) / pasos;
            rect(x, y + i * alto, w, alto + 0.65f, rgb, alfaAbajo + (alfaArriba - alfaAbajo) * t);
        }
    }

    /** Resplandor circular suave, para luces y botones importantes. */
    public void halo(float cx, float cy, float radio, int rgb, float alfa) {
        int pasos = 10;
        for (int i = pasos; i >= 1; i--) {
            circulo(cx, cy, radio * i / pasos, rgb, alfa * 0.065f);
        }
    }

    // ------------------------------------------------------------------ imagenes

    /**
     * Dibuja la imagen cubriendo el rectangulo (x, y, w, h) sin deformarla; lo que sobra se recorta.
     * fx y fy (0 a 1) eligen que parte se ve cuando sobra: 0 = arriba/izquierda, 1 = abajo/derecha.
     * Si la textura es null (no se pudo cargar), pinta el color de respaldo.
     */
    public void imagenCubriendo(Texture t, float x, float y, float w, float h, float fx, float fy, int rgbRespaldo) {
        if (t == null) {
            rect(x, y, w, h, rgbRespaldo);
            return;
        }
        int tw = t.getWidth(), th = t.getHeight();
        float escala = Math.max(w / tw, h / th);
        int sw = Math.min(tw, Math.round(w / escala));
        int sh = Math.min(th, Math.round(h / escala));
        int sx = Math.round(fx * (tw - sw));
        int sy = Math.round(fy * (th - sh));
        batch.setColor(1f, 1f, 1f, alfaGlobal);
        batch.draw(t, x, y, w, h, sx, sy, sw, sh, false, false);
    }

    // ------------------------------------------------------------------ texto

    /** Texto con la linea base en y. */
    public void texto(String s, float x, float y, int rgb) { texto(s, x, y, rgb, 1f); }

    public void texto(String s, float x, float y, int rgb, float escala) {
        texto(s, x, y, rgb, escala, 1f);
    }

    public void texto(String s, float x, float y, int rgb, float escala, float alfa) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        color(rgb, alfa);
        fuente.setColor(tmp);
        fuente.draw(batch, s, x, y);
    }

    /** Texto centrado en (cx, cy). */
    public void textoCentrado(String s, float cx, float cy, int rgb, float escala) {
        textoCentrado(s, cx, cy, rgb, escala, 1f);
    }

    public void textoCentrado(String s, float cx, float cy, int rgb, float escala, float alfa) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        // El color debe fijarse ANTES de medir: GlyphLayout copia el color de la fuente.
        color(rgb, alfa);
        fuente.setColor(tmp);
        medida.setText(fuente, s);
        fuente.draw(batch, medida, cx - medida.width / 2f, cy + medida.height / 2f);
    }

    /** Texto con un contorno grueso, ideal para titulos sobre imagenes. */
    public void textoContorno(String s, float x, float y, int rgb, float escala, int contorno) {
        float o = Math.max(1f, escala * 1.15f);
        texto(s, x - o, y, contorno, escala);
        texto(s, x + o, y, contorno, escala);
        texto(s, x, y - o, contorno, escala);
        texto(s, x, y + o, contorno, escala);
        texto(s, x - o * 0.7f, y - o * 0.7f, contorno, escala);
        texto(s, x + o * 0.7f, y - o * 0.7f, contorno, escala);
        texto(s, x - o * 0.7f, y + o * 0.7f, contorno, escala);
        texto(s, x + o * 0.7f, y + o * 0.7f, contorno, escala);
        texto(s, x, y, rgb, escala);
    }

    public void textoCentradoContorno(String s, float cx, float cy, int rgb, float escala, int contorno) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        medida.setText(fuente, s);
        textoContorno(s, cx - medida.width / 2f, cy + medida.height / 2f, rgb, escala, contorno);
    }

    /** Texto centrado con una sombra suave debajo. */
    public void textoCentradoSombra(String s, float cx, float cy, int rgb, float escala) {
        textoCentrado(s, cx + 1.5f, cy - 2f, 0x000000, escala, 0.45f);
        textoCentrado(s, cx, cy, rgb, escala);
    }

    public float anchoTexto(String s, float escala) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        medida.setText(fuente, s);
        return medida.width;
    }

    public float altoTexto(String s, float escala) {
        fuente.getData().setScale(ESCALA_BASE * escala);
        medida.setText(fuente, s);
        return medida.height;
    }

    // ------------------------------------------------------------------ controles

    /** Tecla dibujada como una tecla de teclado, centrada en (cx, cy). */
    public void tecla(String tecla, float cx, float cy) {
        float w = Math.max(18f, anchoTexto(tecla, 0.62f) + 10f);
        redondeado(cx - w / 2f, cy - 8f, w, 16f, 4f, 0x11131B, 0.9f);
        redondeado(cx - w / 2f + 0.8f, cy - 7.2f, w - 1.6f, 13.4f, 3.2f, 0x30344A, 0.95f);
        textoCentrado(tecla, cx, cy + 0.5f, 0xDCE0EE, 0.62f);
    }

    public void boton(String texto, float x, float y, float w, float h, int rgb, boolean activo) {
        boton(texto, x, y, w, h, rgb, activo, false);
    }

    public void boton(String texto, float x, float y, float w, float h, int rgb, boolean activo, boolean resaltado) {
        float alfa = activo ? 1f : 0.42f;
        boolean hover = activo && ratonDentro(x, y, w, h);
        float radio = Math.min(16f, h * 0.32f);
        float dy = hover ? 2f : 0f;
        if (resaltado) {
            float pulso = 0.5f + 0.5f * (float) Math.sin(tiempo * 4.5f);
            halo(x + w / 2f, y + h / 2f, w * 0.62f, rgb, 0.22f + 0.16f * pulso);
        }
        redondeado(x, y - 4f + dy, w, h, radio, 0x05060A, 0.45f * alfa);
        redondeado(x, y + dy - 1f, w, h + 2f, radio + 1f, mezclarRgb(rgb, 0x000000, 0.45f), alfa);
        redondeado(x, y + dy, w, h, radio, rgb, alfa);
        redondeado(x + 2f, y + dy + h * 0.48f, w - 4f, h * 0.46f, Math.max(1f, radio - 2f),
                0xFFFFFF, (hover ? 0.14f : 0.08f) * alfa);
        redondeado(x + 2f, y + dy + 2f, w - 4f, h * 0.28f, Math.max(1f, radio - 2f),
                mezclarRgb(rgb, 0x000000, 0.25f), 0.30f * alfa);
        rect(x + radio, y + dy + h - 2f, Math.max(0f, w - 2f * radio), 1.4f, 0xFFFFFF, 0.30f * alfa);
        textoCentrado(texto, x + w / 2f, y + dy + h / 2f + 1f, tinta(rgb), 1f, alfa);
    }

    /** Negro o blanco, segun cual se lea mejor sobre el color de fondo. */
    public static int tinta(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        return (0.299f * r + 0.587f * g + 0.114f * b) > 0.6f ? 0x1B1D26 : 0xFFFFFF;
    }

    public static int mezclarRgb(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fuente.dispose();
        blanco.dispose();
        circulo.dispose();
    }
}
