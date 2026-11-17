package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Mundo;
import com.foodflow.ui.Pincel;

public class FinScreen extends PantallaBase {
    private float espera = 1.2f;   // evita saltarse esta pantalla con un click apurado

    public FinScreen(FoodFlowGame juego) { super(juego); }

    @Override
    public void render(float dt) {
        if (espera > 0) {
            espera -= dt;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || hayClick()) {
            juego.setScreen(new MenuScreen(juego));
            return;
        }

        Mundo m = juego.mundo;
        empezarFrame();
        Pincel p = juego.pincel;
        p.textoCentrado("FIN DEL JUEGO", 480, 450, 0xFF6B6B, 3f);
        p.textoCentrado("Llegaste al dia " + m.getDia(), 480, 370, 0xFFFFFF, 1.3f);
        p.textoCentrado("Pedidos entregados: " + m.getEntregados(), 480, 320, 0x8EE39B, 1.1f);
        p.textoCentrado("Clientes perdidos: " + m.getPerdidos(), 480, 285, 0xFFB86B, 1.1f);
        p.textoCentrado("Monedas: $" + m.getMonedas(), 480, 250, 0xFFD166, 1.1f);
        if (espera <= 0) p.textoCentrado("Click o ENTER para volver al menu", 480, 140, 0xFFD166, 1.1f);
        terminarFrame();
    }
}
