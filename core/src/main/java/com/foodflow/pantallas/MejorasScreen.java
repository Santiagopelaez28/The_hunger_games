package com.foodflow.pantallas;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Mundo;
import com.foodflow.modelo.Mejora;
import com.foodflow.ui.Pincel;

/** Pantalla intermedia entre el final del dia y la tienda. */
public class MejorasScreen extends PantallaBase {
    private static final float TARJETA_Y = 218f;
    private static final float TARJETA_W = 278f;
    private static final float TARJETA_H = 238f;
    private static final float BOTON_Y = 236f;
    private static final float BOTON_W = 218f;
    private static final float BOTON_H = 42f;
    private static final float OMITIR_X = 360f;
    private static final float OMITIR_Y = 112f;
    private static final float OMITIR_W = 240f;
    private static final float OMITIR_H = 52f;

    private String aviso = "";
    private float tiempoAviso = 0f;

    public MejorasScreen(FoodFlowGame juego) {
        super(juego);
    }

    private static float tarjetaX(int indice) {
        return 32f + indice * 310f;
    }

    private void irATienda() {
        juego.setScreen(new TiendaScreen(juego));
    }

    private void omitir() {
        juego.mundo.omitirMejoras();
        irATienda();
    }

    private void comprar(Mejora mejora) {
        if (mejora == null) return;
        if (juego.mundo.comprarMejora(mejora.getId())) {
            irATienda();
        } else if (juego.mundo.getMonedas() < mejora.getCosto()) {
            aviso = "No tienes suficientes monedas para esa mejora";
            tiempoAviso = 2.5f;
        } else {
            aviso = "No puedes comprar esa mejora ahora";
            tiempoAviso = 2.5f;
        }
    }

    @Override
    public void render(float dt) {
        Mundo m = juego.mundo;
        List<Mejora> ofertas = m.getOfertasMejoras();
        if (tiempoAviso > 0f) tiempoAviso -= dt;

        // Atajos: 1, 2 o 3 compran la mejora correspondiente; ESC omite.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            omitir();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && ofertas.size() > 0) {
            comprar(ofertas.get(0));
            if (m.getEstado() != Mundo.Estado.MEJORAS) return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && ofertas.size() > 1) {
            comprar(ofertas.get(1));
            if (m.getEstado() != Mundo.Estado.MEJORAS) return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && ofertas.size() > 2) {
            comprar(ofertas.get(2));
            if (m.getEstado() != Mundo.Estado.MEJORAS) return;
        }

        if (hayClick()) {
            if (dentro(toque, OMITIR_X, OMITIR_Y, OMITIR_W, OMITIR_H)) {
                omitir();
                return;
            }
            for (int i = 0; i < ofertas.size(); i++) {
                float x = tarjetaX(i) + (TARJETA_W - BOTON_W) / 2f;
                if (dentro(toque, x, BOTON_Y, BOTON_W, BOTON_H)) {
                    comprar(ofertas.get(i));
                    if (m.getEstado() != Mundo.Estado.MEJORAS) return;
                    break;
                }
            }
        }

        empezarFrame();
        Pincel p = juego.pincel;
        float t = p.getTiempo();

        // Fondo con el mismo estilo visual de la tienda.
        p.imagenCubriendo(juego.fondoTienda, 0, 0, 960, 600, 0.5f, 0.35f, 0x30213A);
        p.rect(0, 0, 960, 600, 0x171020, 0.22f);
        p.degradadoAlpha(0, 0, 960, 150, 0x100A18, 0.72f, 0f);
        p.halo(480, 520, 240, 0xB69CF2, 0.17f + 0.04f * (float) Math.sin(t * 1.8f));

        p.textoCentradoContorno("MEJORAS PARA TU RESTAURANTE", 480, 555, 0xE6D7FF, 1.65f, 0x241633);
        p.textoCentradoSombra("Elige una mejora para hacer crecer tu negocio", 480, 520, 0xFFF3D6, 0.82f);
        p.textoCentrado("DIA " + m.getDia() + " COMPLETADO", 480, 488, 0xC9B4F5, 0.72f);
        p.texto("MONEDAS: $" + m.getMonedas(), 42, 474, 0xFFD166, 0.85f);
        p.texto("MEJORAS ADQUIRIDAS: " + m.getCantidadMejorasCompradas() + "/10", 690, 474, 0xD8C9F2, 0.72f);

        if (ofertas.isEmpty()) {
            p.panel(200, 255, 560, 150, 20, 0x242338, 0xA78BFA, 0.98f);
            p.textoCentrado("Ya adquiriste todas las mejoras disponibles", 480, 342, 0x8EE39B, 1.0f);
            p.textoCentrado("Puedes continuar a la tienda para comprar ingredientes o vidas", 480, 304,
                    0xE2D9F5, 0.68f);
        } else {
            for (int i = 0; i < ofertas.size(); i++) {
                dibujarTarjeta(p, m, ofertas.get(i), i);
            }
        }

        p.boton(ofertas.isEmpty() ? "CONTINUAR A LA TIENDA" : "OMITIR MEJORAS", OMITIR_X, OMITIR_Y,
                OMITIR_W, OMITIR_H, 0x8B74B8, true, false);
        p.textoCentrado("Pulsa ESC o S para omitir", 480, 91, 0xB9BED0, 0.62f);

        if (tiempoAviso > 0f && !aviso.isEmpty()) {
            float ancho = Math.max(300f, p.anchoTexto(aviso, 0.76f) + 38f);
            p.panel(480f - ancho / 2f, 166, ancho, 30, 14, 0x17131F, 0xFFC24A, 0.98f);
            p.textoCentrado(aviso, 480, 181, 0xFFF3D6, 0.76f);
        } else if (!ofertas.isEmpty()) {
            p.textoCentrado("Cada dia puedes comprar una mejora. Las demas pueden aparecer en dias posteriores.",
                    480, 185, 0xC5BDD4, 0.63f);
        }

        terminarFrame();
    }

    private void dibujarTarjeta(Pincel p, Mundo m, Mejora mejora, int indice) {
        float x = tarjetaX(indice);
        boolean puede = m.getMonedas() >= mejora.getCosto();
        boolean costosa = mejora.getTipo() == Mejora.Tipo.COSTOSA;
        int borde = costosa ? 0xE6B95C : 0xA78BFA;
        int fondo = costosa ? 0x30291D : 0x252238;

        p.halo(x + TARJETA_W / 2f, TARJETA_Y + TARJETA_H / 2f, 130,
                costosa ? 0xFFC24A : 0xB69CF2, 0.11f);
        p.panel(x, TARJETA_Y, TARJETA_W, TARJETA_H, 18, fondo, borde, 0.98f);
        p.textoCentrado(costosa ? "MEJORA COSTOSA" : "RIESGO Y RECOMPENSA",
                x + TARJETA_W / 2f, TARJETA_Y + TARJETA_H - 25,
                costosa ? 0xFFD166 : 0xCDBBFA, 0.62f);
        p.textoCentrado(mejora.getNombre(), x + TARJETA_W / 2f, TARJETA_Y + TARJETA_H - 57,
                0xFFF3D6, 0.96f);

        String[] lineas = mejora.getDescripcion().split("\\n");
        float yTexto = TARJETA_Y + TARJETA_H - 94;
        for (int i = 0; i < lineas.length; i++) {
            p.textoCentrado(lineas[i], x + TARJETA_W / 2f, yTexto - i * 22f, 0xD8D2E7, 0.66f);
        }

        p.circulo(x + 35, TARJETA_Y + 72, 13, 0xFFC24A, 0.16f);
        p.textoCentrado("$" + mejora.getCosto(), x + TARJETA_W / 2f, TARJETA_Y + 72,
                0xFFD166, 1.05f);
        p.boton(puede ? "COMPRAR" : "FALTAN MONEDAS",
                x + (TARJETA_W - BOTON_W) / 2f, BOTON_Y, BOTON_W, BOTON_H,
                puede ? (costosa ? 0xC99A42 : 0x8B74B8) : 0x646477, puede, false);
        p.tecla(String.valueOf(indice + 1), x + TARJETA_W - 24, BOTON_Y + BOTON_H / 2f);
    }
}
