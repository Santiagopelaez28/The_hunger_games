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
import java.util.Random;

public class MenuScreen extends PantallaBase {
    private static final List<Ingrediente> DEMO = List.of(Catalogo.PAN, Catalogo.CARNE, Catalogo.QUESO,
            Catalogo.TOCINETA, Catalogo.LECHUGA, Catalogo.TOMATE, Catalogo.PAN);

    private final Aspecto[] clientes = new Aspecto[3];

    public MenuScreen(FoodFlowGame juego) {
        super(juego);
        Random azar = new Random();
        for (int i = 0; i < clientes.length; i++) clientes[i] = Aspecto.aleatorio(azar);
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
        p.rect(95, 452, 190, 8, 0xE8E8E8);
        Dibujos.hamburguesa(p, 190, 460, 170, 110, DEMO);
        for (int i = 0; i < clientes.length; i++) {
            Dibujos.cliente(p, 720 + i * 85, 490, 20, clientes[i], i == 1 ? 0.4f : 1f);
        }
        p.textoCentrado("FOODFLOW", 480, 480, 0xFFD166, 3.2f);
        p.textoCentrado("Atiende a tus clientes antes de que se cansen de esperar", 480, 410, 0xFFFFFF, 1f);

        p.textoCentrado("TIENDA: entre dias compra ingredientes nuevos para pedidos mas variados.", 480, 290, 0x8EE39B, 0.95f);

        p.textoCentrado("Mouse: click en los botones   |   Teclado: ESPACIO atender/entregar,", 480, 160, 0xB9BED0, 0.85f);
        p.textoCentrado("RETROCESO deshacer, 1-9 y 0 ingredientes, P pausa", 480, 138, 0xB9BED0, 0.85f);
        p.textoCentrado("Click o ENTER para empezar", 480, 80, 0xFFD166, 1.3f);
        terminarFrame();
    }
}
