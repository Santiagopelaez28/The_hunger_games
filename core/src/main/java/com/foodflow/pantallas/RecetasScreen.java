package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Recetario;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.TipoPlato;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

/**
 * Recetario: muestra el diccionario de recetas y marca cuales se pueden preparar
 * con la despensa actual. Se abre desde el juego o desde la tienda y el tiempo queda en pausa.
 */
public class RecetasScreen extends PantallaBase {
    public enum Origen { JUEGO, TIENDA }

    private static final int POR_PAGINA = 8;
    private static final float TARJETA_W = 430, TARJETA_H = 78;
    private static final float COLUMNA_1 = 40, COLUMNA_2 = 490;
    private static final float FILA_BASE = 430, FILA_PASO = 86;

    private static final float VOLVER_X = 30, VOLVER_Y = 82, VOLVER_W = 170, VOLVER_H = 46;
    private static final float PAGER_Y = 82, PAGER_W = 150, PAGER_H = 46;
    private static final float ANTERIOR_X = 300, SIGUIENTE_X = 510;

    private final Origen origen;
    private final List<String> nombres = Recetario.nombres();
    private final int totalPaginas = Math.max(1, (nombres.size() + POR_PAGINA - 1) / POR_PAGINA);
    private int pagina = 0;

    public RecetasScreen(FoodFlowGame juego, Origen origen) {
        super(juego);
        this.origen = origen;
    }

    @Override
    public void render(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            volver();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) anteriorPagina();
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) siguientePagina();

        if (hayClick()) {
            if (dentro(toque, VOLVER_X, VOLVER_Y, VOLVER_W, VOLVER_H)) {
                volver();
                return;
            }
            if (dentro(toque, ANTERIOR_X, PAGER_Y, PAGER_W, PAGER_H)) anteriorPagina();
            if (dentro(toque, SIGUIENTE_X, PAGER_Y, PAGER_W, PAGER_H)) siguientePagina();
        }

        List<Ingrediente> despensa = juego.mundo.getDisponibles();
        empezarFrame();
        Pincel p = juego.pincel;
        float t = p.getTiempo();

        // ---------------------------------------------------------- fondo
        p.imagenCubriendo(juego.fondoMenu, 0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0.5f, 0.30f, 0x2C2233);
        p.rect(0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0x140E1E, 0.34f);
        p.degradadoAlpha(0, 300, FoodFlowGame.ANCHO, 300, 0x120E18, 0f, 0.82f);
        p.degradadoAlpha(0, 0, FoodFlowGame.ANCHO, 130, 0x120E18, 0.72f, 0f);
        p.halo(480, 545, 220, 0xFFB347, 0.15f + 0.03f * (float) Math.sin(t * 1.5f));

        // ---------------------------------------------------------- cabecera
        int listas = 0;
        for (String nombre : nombres) {
            if (Recetario.faltantes(nombre, despensa).isEmpty()) listas++;
        }
        p.textoCentradoContorno("RECETARIO", 480, 574, 0xFFD166, 1.95f, 0x3A2410);
        p.textoCentradoSombra("Cada cliente pide una receta completa: sigue el orden de la comanda",
                480, 546, 0xFFF3D6, 0.78f);
        p.textoCentradoSombra("Con tu despensa puedes preparar " + listas + " de " + nombres.size() + " recetas",
                480, 524, 0xE8D7A8, 0.72f);

        // ---------------------------------------------------------- recetas de la pagina
        int inicio = pagina * POR_PAGINA;
        for (int i = 0; i < POR_PAGINA && inicio + i < nombres.size(); i++) {
            String nombre = nombres.get(inicio + i);
            float x = i % 2 == 0 ? COLUMNA_1 : COLUMNA_2;
            float y = FILA_BASE - (i / 2) * FILA_PASO;
            dibujarReceta(p, nombre, x, y, despensa);
        }

        // ---------------------------------------------------------- leyenda
        Dibujos.check(p, 330, 152, 12, 0x8EE39B, 1f);
        p.texto("lista con tu despensa", 344, 156, 0x8EE39B, 0.56f);
        Dibujos.cruz(p, 550, 152, 10, 0xFF9A9A, 1f);
        p.texto("faltan ingredientes", 562, 156, 0xFF9A9A, 0.56f);

        // ---------------------------------------------------------- navegacion
        p.boton("VOLVER", VOLVER_X, VOLVER_Y, VOLVER_W, VOLVER_H, 0xFFC24A, true, false);
        p.tecla("R", VOLVER_X + VOLVER_W - 32, VOLVER_Y + VOLVER_H / 2f);
        p.boton("ANTERIOR", ANTERIOR_X, PAGER_Y, PAGER_W, PAGER_H, 0x7C88B8, totalPaginas > 1, false);
        p.boton("SIGUIENTE", SIGUIENTE_X, PAGER_Y, PAGER_W, PAGER_H, 0x7C88B8, totalPaginas > 1, false);
        p.textoCentrado((pagina + 1) + " / " + totalPaginas, 480, PAGER_Y + PAGER_H / 2f + 1,
                0xFFF3D6, 0.9f);
        p.textoCentrado("Usa las flechas del teclado", 800, PAGER_Y + PAGER_H / 2f + 1,
                0x9AA0B4, 0.62f);

        terminarFrame();
    }

    private void dibujarReceta(Pincel p, String nombre, float x, float y, List<Ingrediente> despensa) {
        List<Ingrediente> ingredientes = Recetario.ingredientes(nombre);
        TipoPlato tipo = Recetario.tipo(nombre);
        List<Ingrediente> faltan = Recetario.faltantes(nombre, despensa);
        boolean lista = faltan.isEmpty();

        int relleno = lista ? 0xFFF5E0 : 0xE9E2D4;
        int borde = lista ? 0x8BC34A : 0xC8B89A;
        int tinta = lista ? 0x4A3212 : 0x6E6354;
        p.panel(x, y, TARJETA_W, TARJETA_H, 13, relleno, borde, 1f);

        p.texto(nombre, x + 16, y + 66, tinta, 0.92f);

        String tipoTexto = tipo.getEtiqueta().toUpperCase();
        p.texto(tipoTexto, x + TARJETA_W - 86 - p.anchoTexto(tipoTexto, 0.48f), y + 66,
                0x8A7A60, 0.48f);

        String etiqueta = lista ? "LISTA" : "FALTA";
        int colorEtiqueta = lista ? 0x2E7D32 : 0xA67C2E;
        p.redondeado(x + TARJETA_W - 78, y + TARJETA_H - 25, 64, 18, 8, colorEtiqueta, 1f);
        p.textoCentrado(etiqueta, x + TARJETA_W - 46, y + TARJETA_H - 16, 0xFFFFFF, 0.55f);

        p.elipse(x + 54, y + 10, 36, 5, 0x1A1208, 0.16f);
        p.setAlfa(lista ? 1f : 0.55f);
        Dibujos.plato(p, tipo, x + 54, y + 12, 72, 36, ingredientes);
        p.setAlfa(1f);

        StringBuilder resumen = new StringBuilder();
        for (int i = 0; i < ingredientes.size(); i++) {
            if (i > 0) resumen.append(" + ");
            resumen.append(ingredientes.get(i).getNombre());
        }
        p.texto(resumen.toString(), x + 100, y + 47, tinta, 0.54f);

        String estado;
        int colorEstado;
        if (lista) {
            estado = "Lista para pedir";
            colorEstado = 0x2E7D32;
        } else {
            StringBuilder falta = new StringBuilder(faltan.size() == 1 ? "Falta: " : "Faltan: ");
            for (int i = 0; i < faltan.size(); i++) {
                if (i > 0) falta.append(", ");
                falta.append(faltan.get(i).getNombre());
            }
            estado = falta.toString();
            colorEstado = 0xA33B2E;
        }
        p.texto(estado, x + 100, y + 26, colorEstado, 0.54f);
    }

    private void anteriorPagina() {
        pagina = (pagina + totalPaginas - 1) % totalPaginas;
    }

    private void siguientePagina() {
        pagina = (pagina + 1) % totalPaginas;
    }

    private void volver() {
        if (origen == Origen.TIENDA) juego.setScreen(new TiendaScreen(juego));
        else juego.setScreen(new JuegoScreen(juego));
    }
}
