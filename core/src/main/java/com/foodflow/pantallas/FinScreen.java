package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.ArbolAVL;
import com.foodflow.logica.Mundo;
import com.foodflow.logica.RegistroPartida;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

public class FinScreen extends PantallaBase {
    private static final float BOTON_X = 320, BOTON_Y = 140, BOTON_W = 320, BOTON_H = 58;

    private float espera = 1.0f;   // evita saltarse esta pantalla con un click apurado

    public FinScreen(FoodFlowGame juego) { super(juego); }

    @Override
    public void render(float dt) {
        if (espera > 0) {
            espera -= dt;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || (hayClick() && dentro(toque, BOTON_X, BOTON_Y, BOTON_W, BOTON_H))) {
            juego.setScreen(new MenuScreen(juego));
            return;
        }

        Mundo m = juego.mundo;
        empezarFrame();
        Pincel p = juego.pincel;
        float t = p.getTiempo();

        // ---------------------------------------------------------- fondo
        p.imagenCubriendo(juego.fondoFin, 0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0.5f, 0.35f, 0x201A2B);
        p.rect(0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0x140E1E, 0.16f);
        p.degradadoAlpha(0, 300, FoodFlowGame.ANCHO, 300, 0x0E0A14, 0f, 0.82f);
        p.degradadoAlpha(0, 0, FoodFlowGame.ANCHO, 120, 0x0E0A14, 0.72f, 0f);
        p.halo(480, 500, 230, 0xE5484D, 0.16f + 0.04f * (float) Math.sin(t * 1.4f));

        // chispas flotando
        for (int i = 0; i < 7; i++) {
            float ang = t * 0.55f + i * 1.4f;
            float x = 480 + (float) Math.cos(ang) * (230 + (i % 3) * 55);
            float y = 330 + (float) Math.sin(ang * 1.3f) * 150;
            Dibujos.destello(p, x, y, 9 + (i % 2) * 4, 0xFFC24A,
                    0.12f + 0.14f * (0.5f + 0.5f * (float) Math.sin(t * 2.2f + i)));
        }

        // ---------------------------------------------------------- resultado
        p.textoCentradoContorno("FIN DEL JUEGO", 480, 540, 0xFF8A7A, 2.45f, 0x2A0E0E);
        p.textoCentradoSombra("Llegaste al dia " + m.getDia(), 480, 500, 0xFFF3D6, 1.05f);

        String frase;
        if (m.getPerdidos() == 0 && m.getEntregados() > 0) {
            frase = "Servicio perfecto: ningun cliente se fue enojado";
        } else if (m.getEntregados() >= 10) {
            frase = "Chef de leyenda: la ciudad vuelve por mas";
        } else if (m.getEntregados() >= 5) {
            frase = "Buen servicio, la cocina va tomando ritmo";
        } else {
            frase = "La cocina te espera para otra ronda";
        }
        p.textoCentrado(frase, 480, 470, 0xFFD166, 0.85f);

        // ---------------------------------------------------------- estadisticas de la partida actual
        dibujarTarjeta(p, 50, 360, "DIAS", String.valueOf(m.getDia()), 0, 0xFFD166);
        dibujarTarjeta(p, 270, 360, "ENTREGADOS", String.valueOf(m.getEntregados()), 1, 0x8EE39B);
        dibujarTarjeta(p, 490, 360, "PERDIDOS", String.valueOf(m.getPerdidos()), 2, 0xFF9A9A);
        dibujarTarjeta(p, 710, 360, "MONEDAS", "$" + m.getMonedas(), 3, 0xFFD166);

        // ---------------------------------------------------------- top 5 mejores partidas (árbol AVL)
        ArbolAVL arbol = juego.arbolPartidas;
        List<RegistroPartida> top5 = arbol != null ? arbol.top(5) : List.of();

        float topX = 50f;
        float topY = 252f;
        float topW = 860f;
        float topH = 90f;
        p.panel(topX, topY, topW, topH, 14, 0x1B1F2E, 0x3E4560, 0.95f);
        p.texto("TOP 5 MEJORES PARTIDAS", topX + 16, topY + topH - 22, 0xFFD166, 0.6f);
        p.rect(topX + 16, topY + topH - 20, topW - 32, 1, 0x3E4560, 0.4f);

        if (top5.isEmpty()) {
            p.textoCentrado("Aún no hay registros de partidas", topX + topW / 2f, topY + topH / 2f, 0x9AA0B4, 0.65f);
        } else {
            float filaH = (topH - 34f) / 5f;
            for (int i = 0; i < top5.size(); i++) {
                RegistroPartida r = top5.get(i);
                float fy = topY + topH - 34f - i * filaH - filaH * 0.5f;
                int colorRank = i == 0 ? 0xF2B33D : (i == 1 ? 0xC0C0C0 : (i == 2 ? 0xCD7F32 : 0xE8E2D2));
                String medal = i == 0 ? "1." : (i == 1 ? "2." : (i == 2 ? "3." : String.valueOf(i + 1) + "."));
                p.texto(medal, topX + 22, fy, colorRank, 0.65f);
                p.texto("Día " + r.dia, topX + 52, fy, 0xFFF3D6, 0.55f);
                p.texto("Entregados: " + r.entregados, topX + 150, fy, 0x8EE39B, 0.55f);
                p.texto("Perdidos: " + r.perdidos, topX + 280, fy, 0xFF9A9A, 0.55f);
                p.texto("Puntaje: " + r.puntaje, topX + 400, fy, 0xFFD166, 0.65f);
            }
        }

        // ---------------------------------------------------------- volver
        float pulso = 0.5f + 0.5f * (float) Math.sin(t * 3f);
        p.halo(480, BOTON_Y + BOTON_H / 2f, 160 + pulso * 8f, 0xFFC24A, 0.16f + 0.10f * pulso);
        p.boton("VOLVER AL MENU", BOTON_X, BOTON_Y, BOTON_W, BOTON_H, 0xFFC24A, espera <= 0, false);
        p.tecla("ENTER", BOTON_X + BOTON_W - 42, BOTON_Y + BOTON_H / 2f);
        p.panel(480 - 155, 108, 310, 26, 13, 0x120E18, 0x3E4560, 0.72f);
        p.textoCentrado("Click en el boton o pulsa ENTER", 480, 122, 0xD8DCEA, 0.75f);

        terminarFrame();
    }

    private void dibujarTarjeta(Pincel p, float x, float y, String etiqueta, String valor, int icono, int color) {
        p.panel(x, y, 200, 80, 16, 0x1B1F2E, 0x3E4560, 0.95f);
        float cx = x + 34, cy = y + 44;
        p.circulo(cx, cy, 20, color, 0.14f);
        if (icono == 0) Dibujos.calendario(p, cx, cy, 26, color, 0x1B1F2E);
        else if (icono == 1) Dibujos.check(p, cx, cy, 22, color, 1f);
        else if (icono == 2) Dibujos.cruz(p, cx, cy, 17, color, 1f);
        else Dibujos.moneda(p, cx, cy, 13);

        p.texto(etiqueta, x + 64, y + 52, 0x9AA0B4, 0.55f);
        p.texto(valor, x + 64, y + 27, color, 1.15f);
    }
}
