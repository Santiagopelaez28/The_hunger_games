package com.foodflow.ui;

import com.foodflow.logica.Catalogo;
import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Ingrediente;

import java.util.List;

/**
 * Dibujos hechos solo con rectángulos y círculos del Pincel: clientes, hamburguesas e iconos de ingredientes.
 * Todo se escala con el tamaño que se pida, así sirve igual para la cola, el mostrador y los botones.
 */
public final class Dibujos {
    private static final float ANCHO_NOMINAL = 190f;
    private static final int OSCURO = 0x1B1D26;

    private Dibujos() { }

    // =====================================================================  HAMBURGUESA

    /** Alto "natural" de una capa cuando la hamburguesa mide ANCHO_NOMINAL de ancho. */
    private static float altoCapa(Ingrediente ing, int indice) {
        if (ing == Catalogo.PAN) return indice == 0 ? 22f : 42f;   // base plana o tapa abombada
        if (ing == Catalogo.CARNE) return 22f;
        if (ing == Catalogo.QUESO) return 7f;
        if (ing == Catalogo.LECHUGA) return 9f;
        if (ing == Catalogo.TOMATE) return 10f;
        if (ing == Catalogo.CEBOLLA) return 7f;
        if (ing == Catalogo.HUEVO) return 9f;
        if (ing == Catalogo.TOCINETA) return 8f;
        if (ing == Catalogo.PINA) return 9f;
        if (ing == Catalogo.AGUACATE) return 10f;
        return 12f;
    }

    /**
     * Dibuja las capas (de abajo hacia arriba) apiladas como una hamburguesa, centrada en cx y apoyada en yBase.
     * Si no cabe en ancho x altoMax, se reduce todo por igual.
     * El primer Pan es la base; cualquier otro Pan se dibuja como tapa con ajonjolí.
     */
    public static void hamburguesa(Pincel p, float cx, float yBase, float ancho, float altoMax, List<Ingrediente> capas) {
        float total = 0;
        for (int i = 0; i < capas.size(); i++) total += altoCapa(capas.get(i), i);
        if (total <= 0) return;
        float f = Math.min(ancho / ANCHO_NOMINAL, altoMax / total);
        float y = yBase;
        for (int i = 0; i < capas.size(); i++) {
            Ingrediente ing = capas.get(i);
            float h = altoCapa(ing, i) * f;
            capa(p, ing, i == 0, true, cx, y, ANCHO_NOMINAL * f, h);
            y += h;
        }
    }

    /** Icono pequeño de un ingrediente (una sola capa), centrado en (cx, cy). */
    public static void icono(Pincel p, Ingrediente ing, float cx, float cy, float ancho) {
        float h = ancho * (ing == Catalogo.PAN ? 0.42f : 0.30f);
        capa(p, ing, false, false, cx, cy - h / 2f, ancho, h);
    }

    private static void capa(Pincel p, Ingrediente ing, boolean base, boolean goteo, float cx, float y, float w, float h) {
        float x0 = cx - w / 2f;
        if (ing == Catalogo.PAN) {
            if (base) {
                pildora(p, x0, y, w, h, 0xD9A441);
                p.rect(x0 + h / 2f, y, w - h, h * 0.28f, 0xB8862F);
            } else {
                cupula(p, cx, y, w, h, 0xD9A441);
                float[][] semillas = {{-0.28f, 0.55f}, {0.02f, 0.74f}, {0.30f, 0.55f}, {-0.48f, 0.28f}, {0.46f, 0.28f}, {-0.10f, 0.30f}};
                float r = Math.max(1.2f, h * 0.045f);
                for (float[] s : semillas) p.circulo(cx + s[0] * w, y + s[1] * h, r, 0xFFF1C9);
            }
        } else if (ing == Catalogo.CARNE) {
            float aw = w * 0.96f;
            pildora(p, cx - aw / 2f, y, aw, h, 0x6B3322);
            p.rect(cx - aw / 2f + h / 2f, y + h * 0.78f, aw - h, h * 0.12f, 0x8A4A33);
            for (int k = 0; k < 4; k++) {
                p.rect(cx - aw * 0.3f + k * aw * 0.2f, y + h * 0.12f, Math.max(1f, aw * 0.035f), h * 0.62f, 0x3F1D12);
            }
        } else if (ing == Catalogo.QUESO) {
            float aw = w * 1.04f;
            p.rect(cx - aw / 2f, y + h * 0.25f, aw, h * 0.75f, 0xF5D142);
            if (goteo) {   // el queso se derrite sobre la carne
                p.rect(cx - aw / 2f + aw * 0.06f, y - h * 0.9f, aw * 0.12f, h * 1.2f, 0xF5D142);
                p.rect(cx + aw / 2f - aw * 0.22f, y - h * 0.6f, aw * 0.14f, h * 0.9f, 0xF5D142);
            }
        } else if (ing == Catalogo.LECHUGA) {
            float r = Math.max(h * 0.42f, 1f);
            p.rect(x0, y + h * 0.15f, w, h * 0.55f, 0x4CAF50);
            int n = Math.max(6, (int) (w / (r * 1.1f)));
            for (int k = 0; k < n; k++) {
                float px = x0 + w * k / (n - 1);
                float py = y + h * 0.55f + (k % 2 == 0 ? h * 0.1f : -h * 0.05f);
                p.circulo(px, py, r, k % 2 == 0 ? 0x6BD66B : 0x4CAF50);
            }
        } else if (ing == Catalogo.TOMATE) {
            pildora(p, x0 + w * 0.03f, y, w * 0.94f, h, 0xE5484D);
            for (int k = -1; k <= 1; k++) p.circulo(cx + k * w * 0.22f, y + h * 0.5f, h * 0.2f, 0xFF9AA0);
        } else if (ing == Catalogo.CEBOLLA) {
            float aw = w * 0.92f;
            pildora(p, cx - aw / 2f, y, aw, h, 0xEBD8F3);
            for (int k = 0; k < 4; k++) {
                p.rect(cx - aw * 0.3f + k * aw * 0.2f, y + h * 0.15f, Math.max(1f, aw * 0.03f), h * 0.7f, 0xB78AD0);
            }
        } else if (ing == Catalogo.HUEVO) {
            float aw = w * 0.95f;
            pildora(p, cx - aw / 2f, y, aw, h * 0.75f, 0xF7F3EA);
            p.circulo(cx + w * 0.08f, y + h * 0.55f, h * 0.55f, 0xFFB703);
            p.circulo(cx + w * 0.08f - h * 0.15f, y + h * 0.72f, h * 0.14f, 0xFFE08A);
        } else if (ing == Catalogo.TOCINETA) {
            int n = 8;
            float sw = w / n;
            for (int k = 0; k < n; k++) {
                boolean par = k % 2 == 0;
                p.rect(x0 + k * sw, y + (par ? h * 0.25f : 0f), sw + 0.5f, h * 0.75f, par ? 0xB5452F : 0xE07A5F);
            }
        } else if (ing == Catalogo.PINA) {
            pildora(p, x0 + w * 0.03f, y, w * 0.94f, h, 0xFFB84D);
            for (int k = -2; k <= 2; k++) p.circulo(cx + k * w * 0.17f, y + h * 0.5f, h * 0.14f, 0xFFE29A);
        } else if (ing == Catalogo.AGUACATE) {
            pildora(p, x0 + w * 0.03f, y, w * 0.94f, h, 0x3F6B2A);
            pildora(p, cx - w * 0.43f, y + h * 0.2f, w * 0.86f, h * 0.6f, 0xA9D38B);
        } else {
            pildora(p, x0, y, w, h, ing.getColor());
        }
    }

    /** Rectángulo con los extremos redondeados. */
    private static void pildora(Pincel p, float x, float y, float w, float h, int rgb) {
        float r = h / 2f;
        p.circulo(x + r, y + r, r, rgb);
        p.circulo(x + w - r, y + r, r, rgb);
        p.rect(x + r, y, Math.max(0f, w - 2f * r), h, rgb);
    }

    /** Media elipse apoyada en y (la parte de arriba de un pan, una gorra). Se arma con franjas horizontales. */
    private static void cupula(Pincel p, float cx, float y, float w, float h, int rgb) {
        int n = Math.max(8, Math.min(40, (int) (h / 1.1f)));
        float paso = h / n;
        for (int k = 0; k < n; k++) {
            float t = (k + 0.5f) / n;
            float ancho = w * (float) Math.sqrt(1f - t * t);
            p.rect(cx - ancho / 2f, y + k * paso, ancho, paso + 0.6f, rgb);
        }
    }

    // =====================================================================  BOTÓN CON ICONO

    /** Botón con el icono del ingrediente a la izquierda. 'tecla' puede ser null. */
    public static void botonIngrediente(Pincel p, String texto, float x, float y, float w, float h,
                                        Ingrediente ing, boolean activo, String tecla) {
        p.setAlfa(activo ? 1f : 0.4f);
        p.rect(x, y, w, h, 0x343849);
        icono(p, ing, x + h * 0.72f, y + h / 2f, h * 0.9f);
        p.textoCentrado(texto, x + (h * 1.45f + w) / 2f, y + h / 2f, 0xFFFFFF, 0.9f);
        if (tecla != null) p.texto(tecla, x + 4, y + h - 3, 0xB9BED0, 0.6f);
        p.setAlfa(1f);
    }

    // =====================================================================  CLIENTE

    private static int mezclar(int rgb, float f) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * f));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * f));
        int b = Math.min(255, Math.round((rgb & 0xFF) * f));
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Cliente de busto: (cx, cy) es el centro de la cabeza y s su radio.
     * 'animo' va de 1 (recién llegó, sonríe) a 0 (a punto de irse, enojado).
     */
    public static void cliente(Pincel p, float cx, float cy, float s, Aspecto a, float animo) {
        int piel = a.getPiel();
        int pelo = a.getPelo();
        int camisa = a.getCamisa();

        // cuerpo
        p.circulo(cx - 0.9f * s, cy - 2.0f * s, 0.9f * s, camisa);
        p.circulo(cx + 0.9f * s, cy - 2.0f * s, 0.9f * s, camisa);
        p.rect(cx - 0.9f * s, cy - 2.9f * s, 1.8f * s, 1.8f * s, camisa);
        p.rect(cx - 1.8f * s, cy - 2.9f * s, 3.6f * s, 0.9f * s, camisa);
        p.rect(cx - 0.32f * s, cy - 1.3f * s, 0.64f * s, 0.5f * s, mezclar(piel, 0.88f));   // cuello

        // pelo de atrás
        if (a.getPeinado() == 1) {
            p.circulo(cx, cy + 0.05f * s, 1.12f * s, pelo);
            p.rect(cx - 1.12f * s, cy - 1.7f * s, 2.24f * s, 1.75f * s, pelo);
        } else if (a.getPeinado() == 2) {
            p.circulo(cx, cy + 0.25f * s, 1.38f * s, pelo);
        } else if (a.getPeinado() == 3) {
            p.circulo(cx - 0.9f * s, cy + 0.05f * s, 0.3f * s, pelo);
            p.circulo(cx + 0.9f * s, cy + 0.05f * s, 0.3f * s, pelo);
        }

        // orejas y cabeza
        p.circulo(cx - 1.0f * s, cy - 0.1f * s, 0.2f * s, mezclar(piel, 0.9f));
        p.circulo(cx + 1.0f * s, cy - 0.1f * s, 0.2f * s, mezclar(piel, 0.9f));
        p.circulo(cx, cy, s, piel);

        // pelo de adelante: un círculo de pelo y encima la cara "recortada" con piel
        switch (a.getPeinado()) {
            case 0:
            case 1:
            case 4:
                p.circulo(cx, cy + 0.42f * s, 1.03f * s, pelo);
                p.circulo(cx, cy - 0.07f * s, 0.93f * s, piel);
                if (a.getPeinado() == 4) p.circulo(cx, cy + 1.3f * s, 0.45f * s, pelo);   // moño
                break;
            case 2:
                p.circulo(cx, cy + 0.55f * s, 1.12f * s, pelo);
                p.circulo(cx, cy - 0.1f * s, 0.9f * s, piel);
                p.circulo(cx - 0.7f * s, cy + 1.35f * s, 0.36f * s, pelo);
                p.circulo(cx + 0.7f * s, cy + 1.35f * s, 0.36f * s, pelo);
                p.circulo(cx, cy + 1.55f * s, 0.36f * s, pelo);
                break;
            default:   // calvo: solo un brillo en la cabeza (los pelitos de los lados van atras)
                p.circulo(cx - 0.35f * s, cy + 0.6f * s, 0.18f * s, mezclar(piel, 1.12f));
                break;
        }

        // gafas (antes que los ojos)
        float ojoY = cy + 0.12f * s;
        if (a.getAccesorio() == 2) {
            float gw = 0.62f * s, gh = 0.46f * s;
            for (int lado = -1; lado <= 1; lado += 2) {
                float gx = cx + lado * 0.38f * s;
                p.rect(gx - gw / 2f - 0.05f * s, ojoY - gh / 2f - 0.05f * s, gw + 0.1f * s, gh + 0.1f * s, 0x2B2B2B);
                p.rect(gx - gw / 2f, ojoY - gh / 2f, gw, gh, 0xCFE8FF);
            }
            p.rect(cx - 0.1f * s, ojoY - 0.03f * s, 0.2f * s, 0.06f * s, 0x2B2B2B);
        }

        // ojos
        p.circulo(cx - 0.38f * s, ojoY, 0.11f * s, OSCURO);
        p.circulo(cx + 0.38f * s, ojoY, 0.11f * s, OSCURO);

        // cejas: enojado = inclinadas hacia el centro
        int ceja = mezclar(pelo, 0.8f);
        if (animo < 0.25f) {
            p.rect(cx - 0.62f * s, cy + 0.38f * s, 0.22f * s, 0.1f * s, ceja);
            p.rect(cx - 0.42f * s, cy + 0.32f * s, 0.22f * s, 0.1f * s, ceja);
            p.rect(cx + 0.40f * s, cy + 0.38f * s, 0.22f * s, 0.1f * s, ceja);
            p.rect(cx + 0.20f * s, cy + 0.32f * s, 0.22f * s, 0.1f * s, ceja);
        } else {
            float alto = animo > 0.55f ? 0.44f : 0.38f;
            p.rect(cx - 0.65f * s, cy + alto * s, 0.45f * s, 0.09f * s, ceja);
            p.rect(cx + 0.20f * s, cy + alto * s, 0.45f * s, 0.09f * s, ceja);
        }

        // boca: sonrisa, neutral o ceño fruncido (un círculo oscuro "recortado" con otro de piel)
        int boca = 0x7A2E2E;
        if (animo > 0.55f) {
            p.circulo(cx, cy - 0.38f * s, 0.3f * s, boca);
            p.circulo(cx, cy - 0.21f * s, 0.3f * s, piel);
            p.setAlfa(0.45f);
            p.circulo(cx - 0.62f * s, cy - 0.2f * s, 0.15f * s, 0xFF7A7A);
            p.circulo(cx + 0.62f * s, cy - 0.2f * s, 0.15f * s, 0xFF7A7A);
            p.setAlfa(1f);
        } else if (animo > 0.25f) {
            p.rect(cx - 0.22f * s, cy - 0.42f * s, 0.44f * s, 0.08f * s, boca);
        } else {
            p.circulo(cx, cy - 0.55f * s, 0.3f * s, boca);
            p.circulo(cx, cy - 0.72f * s, 0.3f * s, piel);
        }

        // bigote (después de la boca) y gorra
        if (a.getAccesorio() == 3) {
            pildora(p, cx - 0.5f * s, cy - 0.22f * s, 1.0f * s, 0.2f * s, mezclar(pelo, 0.7f));
        } else if (a.getAccesorio() == 1) {
            int col = a.getColorAccesorio();
            cupula(p, cx, cy + 0.42f * s, 2.1f * s, 0.85f * s, col);
            p.rect(cx - 1.05f * s, cy + 0.38f * s, 2.1f * s, 0.14f * s, mezclar(col, 0.8f));
            p.rect(cx + 0.1f * s, cy + 0.34f * s, 1.35f * s, 0.15f * s, mezclar(col, 0.7f));
        }
    }
}
