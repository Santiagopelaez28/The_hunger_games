package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Mundo;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

/** Entre un día y otro: se compran ingredientes que se agregan al arreglo dinámico de disponibles. */
public class TiendaScreen extends PantallaBase {
    private static final float ANCHO_ITEM = 400, ALTO_ITEM = 55;

    public TiendaScreen(FoodFlowGame juego) { super(juego); }

    private static float tx(int i) { return 60 + (i % 2) * 440; }
    private static float ty(int i) { return 350 - (i / 2) * 70; }

    private void siguienteDia() {
        juego.mundo.siguienteDia();
        juego.setScreen(new JuegoScreen(juego));
    }

    @Override
    public void render(float dt) {
        Mundo m = juego.mundo;
        List<Ingrediente> venta = m.ingredientesPorComprar();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { siguienteDia(); return; }
        if (hayClick()) {
            if (dentro(toque, 330, 30, 300, 55)) { siguienteDia(); return; }
            for (int i = 0; i < venta.size(); i++) {
                if (dentro(toque, tx(i), ty(i), ANCHO_ITEM, ALTO_ITEM)) {
                    m.comprar(venta.get(i));
                    break;   // la lista cambió: se vuelve a leer en el próximo frame
                }
            }
        }
        venta = m.ingredientesPorComprar();

        empezarFrame();
        Pincel p = juego.pincel;
        p.textoCentrado("Dia " + m.getDia() + " terminado", 480, 555, 0xFFD166, 2f);
        p.textoCentrado("Entregados: " + m.getEntregados() + "     Perdidos: " + m.getPerdidos()
                + "     Vidas: " + m.getVidas(), 480, 505, 0xFFFFFF, 1f);
        p.textoCentrado("Monedas: $" + m.getMonedas(), 480, 472, 0xFFD166, 1.2f);

        StringBuilder tienes = new StringBuilder("Tienes: ");
        for (int i = 0; i < m.getDisponibles().size(); i++) {
            if (i > 0) tienes.append(", ");
            tienes.append(m.getDisponibles().get(i).getNombre());
        }
        p.textoCentrado(tienes.toString(), 480, 428, 0xB9BED0, 0.9f);

        if (venta.isEmpty()) {
            p.textoCentrado("Ya tienes todos los ingredientes!", 480, 280, 0x8EE39B, 1.3f);
        }
        for (int i = 0; i < venta.size(); i++) {
            Ingrediente ing = venta.get(i);
            String etiqueta = ing.getNombre() + "   $" + ing.getCosto() + "   (aporta $" + ing.getValor() + ")";
            Dibujos.botonIngrediente(p, etiqueta, tx(i), ty(i), ANCHO_ITEM, ALTO_ITEM, ing, m.getMonedas() >= ing.getCosto(), null);
        }
        p.boton("SIGUIENTE DIA (ENTER)", 330, 30, 300, 55, 0x4D96FF, true);
        terminarFrame();
    }
}
