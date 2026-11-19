package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Catalogo;
import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

public class MenuScreen extends PantallaBase {
    private static final List<Ingrediente> DEMO = List.of(Catalogo.PAN, Catalogo.CARNE, Catalogo.QUESO,
            Catalogo.TOCINETA, Catalogo.LECHUGA, Catalogo.TOMATE, Catalogo.PAN);

    private final Aspecto[] clientes = new Aspecto[2];

    public MenuScreen(FoodFlowGame juego) {
        super(juego);
        // clientes fijos: el menu se ve igual todas las veces y sin combinaciones raras
        clientes[0] = new Aspecto(0xF1C27D, 0x2B1B12, 0x6FB1FC, 0, 2, 0xE63946);
        clientes[1] = new Aspecto(0xFFDBB4, 0xC0392B, 0xF08BA4, 4, 0, 0xE63946);
    }

    @Override
    public void render(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || hayClick()) {
            juego.nuevaPartida();
            juego.setScreen(new JuegoScreen(juego));
            return;
        }

        empezarFrame();
        Pincel p = juego.pincel;
        float t = p.getTiempo();

        // ---------------------------------------------------------- fondo
        p.imagenCubriendo(juego.fondoMenu, 0, 0, 960, 600, 0.5f, 0.32f, 0x2C2233);
        p.rect(0, 0, 960, 600, 0x2A1620, 0.10f);
        p.degradadoAlpha(0, 330, 960, 270, 0x120E18, 0f, 0.78f);
        p.degradadoAlpha(0, 0, 960, 90, 0x120E18, 0.72f, 0f);
        p.halo(480, 522, 200, 0xFFB347, 0.28f + 0.05f * (float) Math.sin(t * 1.5f));

        // ---------------------------------------------------------- hamburguesa estrella
        float flotar = (float) Math.sin(t * 1.7f) * 2.5f;
        p.halo(480, 215, 175, 0xFFB347, 0.20f);
        p.elipse(480, 124, 150, 23, 0x090502, 0.45f);
        p.elipse(480, 132, 150, 22, 0xE9E4D6);
        p.elipse(480, 136, 126, 15, 0xCFC8B6);
        Dibujos.hamburguesa(p, 480, 140 + flotar, 250, 190, DEMO);
        Dibujos.vapor(p, 480, 332, 120, t, 0xFFFFFF, 0.30f);
        for (int i = 0; i < 4; i++) {
            float ang = t * 0.8f + i * 1.7f;
            float sx = 480 + (float) Math.cos(ang) * (150 + i * 8);
            float sy = 240 + (float) Math.sin(ang) * 95;
            Dibujos.destello(p, sx, sy, 10 + (i % 2) * 4, 0xFFE7A3, 0.20f + 0.18f * (float) Math.sin(t * 3 + i));
        }

        // ---------------------------------------------------------- clientes con globos de pedido
        float bob0 = (float) Math.sin(t * 2.4f) * 3f;
        float bob1 = (float) Math.sin(t * 2.4f + 1.4f) * 3f;
        dibujarCliente(p, 150, 214 + bob0, 40, clientes[0], Catalogo.QUESO, t, 1f);
        dibujarCliente(p, 810, 214 + bob1, 40, clientes[1], Catalogo.TOMATE, t, 0.55f);

        // ---------------------------------------------------------- titulo
        p.textoCentradoContorno("THE", 480, 559, 0xFFE7A3, 0.95f, 0x3A2410);
        p.textoCentradoContorno("HUNGER GAMES", 480, 513, 0xFFD166, 2.85f, 0x3A2410);
        p.redondeado(340, 483, 280, 5, 2.5f, 0xF2B33D, 0.92f);
        p.rectRotado(480, 485.5f, 12, 12, 45f, 0xFFE7A3, 1f);
        p.textoCentradoContorno("Atiende, cocina y entrega antes de que se acabe la paciencia",
                480, 458, 0xFFF3D6, 0.90f, 0x2A1620);

        // ---------------------------------------------------------- boton JUGAR
        float bx = 366, by = 382, bw = 228, bh = 60;
        boolean hover = p.ratonDentro(bx, by, bw, bh);
        float pulso = 0.5f + 0.5f * (float) Math.sin(t * 3.2f);
        p.halo(480, by + bh / 2f, 148 + pulso * 10f, 0xFFC94A, 0.18f + 0.14f * pulso);
        p.redondeado(bx, by - 6, bw, bh, 20, 0x080502, 0.50f);
        p.redondeado(bx, by - 2, bw, bh, 20, 0xB97712, 1f);
        p.redondeado(bx, by, bw, bh, 20, hover ? 0xFFD36B : 0xFFC24A, 1f);
        p.redondeado(bx + 4, by + bh * 0.46f, bw - 8, bh * 0.48f, 16, 0xFFFFFF, hover ? 0.20f : 0.13f);
        p.redondeado(bx + 4, by + 3, bw - 8, bh * 0.26f, 16, 0xB97712, 0.32f);
        p.rect(bx + 22, by + bh - 3, bw - 44, 2f, 0xFFFFFF, 0.42f);
        float icx = bx + 52, icy = by + bh / 2f;
        for (int i = 0; i < 5; i++) {
            p.rect(icx - 8 + i * 4, icy - (10 - i * 2), 4, 20 - i * 4, 0x3A2708, 0.92f);
        }
        p.textoCentrado("JUGAR", bx + bw / 2f + 14, by + bh / 2f + 1, 0x3A2708, 1.55f);
        p.textoCentradoSombra("Click o ENTER para empezar", 480, 364, 0xFFF3D6, 0.78f);
        p.textoCentradoSombra("Compra ingredientes nuevos entre un dia y otro", 480, 340, 0xE8D7A8, 0.74f);

        // ---------------------------------------------------------- barra de controles
        p.rect(0, 0, 960, 46, 0x0E0B12, 0.76f);
        p.rect(0, 46, 960, 1.5f, 0xF2B33D, 0.35f);
        pista(p, "ESPACIO", "atender / entregar", 230, 23);
        pista(p, "1-0, Q, E", "ingredientes", 500, 23);
        pista(p, "P", "pausa", 730, 23);

        terminarFrame();
    }

    private void dibujarCliente(Pincel p, float cx, float cy, float s, Aspecto aspecto,
                                Ingrediente gusto, float t, float animo) {
        p.halo(cx, cy - 6, s * 3.1f, aspecto.getCamisa(), 0.28f);
        float bob = (float) Math.sin(t * 2.2f + cx * 0.01f) * 1.5f;
        Dibujos.cliente(p, cx, cy + bob, s, aspecto, animo);

        // globo de pedido justo encima de la cabeza, con una punta que senala al cliente
        float bw = 54f, bh = 34f;
        float bxc = cx;
        float byc = cy + s * 2.25f;
        p.sombra(bxc - bw / 2f, byc - bh / 2f, bw, bh, 16f, 0.22f);
        p.rectRotado(bxc, byc - bh / 2f - 4f, 11f, 11f, 45f, 0xFFFFFF, 0.94f);
        p.redondeado(bxc - bw / 2f, byc - bh / 2f, bw, bh, 16f, 0xFFFFFF, 0.94f);
        p.redondeado(bxc - bw / 2f + 4f, byc + bh / 2f - 13f, bw - 8f, 9f, 4f, 0xFFFFFF, 0.55f);
        Dibujos.icono(p, gusto, bxc, byc + 1f, 31f);
    }

    private static void pista(Pincel p, String tecla, String texto, float cx, float cy) {
        float kw = Math.max(18f, p.anchoTexto(tecla, 0.62f) + 10f);
        float tw = p.anchoTexto(texto, 0.72f);
        float total = kw + 8f + tw;
        float x = cx - total / 2f;
        p.tecla(tecla, x + kw / 2f, cy);
        p.texto(texto, x + kw + 8f, cy + 4f, 0xB9BED0, 0.72f);
    }
}
