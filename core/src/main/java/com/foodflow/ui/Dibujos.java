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
    public static final int OSCURO = 0x1B1D26;
    public static final int CREMA = 0xFFF3D6;
    public static final int DORADO = 0xF2B33D;
    public static final int ROJO = 0xE5484D;
    public static final int VERDE = 0x6BD66B;
    public static final int CIELO = 0x8EDBF9;
    public static final int MADERA = 0x6B4A1E;

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

    /** Icono de un ingrediente (silueta propia, no una capa de hamburguesa), centrado en (cx, cy). */
    public static void icono(Pincel p, Ingrediente ing, float cx, float cy, float ancho) {
        float w = ancho, h = ancho * 0.86f;
        float x = cx - w / 2f, y = cy - h / 2f;

        if (ing == Catalogo.PAN) {
            cupula(p, cx, y + h * 0.17f, w * 0.92f, h * 0.72f, 0xD9A441);
            pildora(p, x + w * 0.06f, y + h * 0.03f, w * 0.88f, h * 0.24f, 0xC98C35);
            for (int k = -2; k <= 2; k++) {
                p.circulo(cx + k * w * 0.17f, y + h * 0.58f - Math.abs(k) * h * 0.045f,
                        Math.max(1f, w * 0.035f), 0xFFF1C9);
            }
        } else if (ing == Catalogo.CARNE) {
            pildora(p, x + w * 0.03f, y + h * 0.15f, w * 0.94f, h * 0.60f, 0x6B3322);
            p.rect(x + w * 0.15f, y + h * 0.60f, w * 0.70f, h * 0.10f, 0x8A4A33, 0.9f);
            for (int k = 0; k < 3; k++) {
                p.rectRotado(x + w * 0.29f + k * w * 0.21f, cy, w * 0.05f, h * 0.44f,
                        18f, 0x3F1D12, 0.9f);
            }
        } else if (ing == Catalogo.QUESO) {
            int n = 10;
            for (int i = 0; i < n; i++) {
                float t = (i + 0.5f) / n;
                float ww = w * (1f - t) * 0.86f;
                p.rect(cx - ww / 2f, y + h * 0.05f + i * (h * 0.84f / n), ww,
                        h * 0.84f / n + 0.8f, i % 2 == 0 ? 0xF5D142 : 0xEAC43A);
            }
            p.circulo(cx - w * 0.12f, y + h * 0.30f, w * 0.055f, 0xD9B22F);
            p.circulo(cx + w * 0.10f, y + h * 0.45f, w * 0.045f, 0xD9B22F);
            p.circulo(cx - w * 0.03f, y + h * 0.20f, w * 0.040f, 0xD9B22F);
        } else if (ing == Catalogo.LECHUGA) {
            p.circulo(cx, cy, w * 0.40f, 0x2F7A36);
            p.circulo(cx - w * 0.16f, cy + h * 0.05f, w * 0.27f, 0x6BD66B);
            p.circulo(cx + w * 0.17f, cy + h * 0.02f, w * 0.26f, 0x4CAF50);
            p.circulo(cx, cy + h * 0.19f, w * 0.22f, 0x8FE39A);
            p.circulo(cx + w * 0.02f, cy - h * 0.10f, w * 0.14f, 0x3F9B46);
        } else if (ing == Catalogo.TOMATE) {
            p.circulo(cx, cy - h * 0.10f, w * 0.40f, 0xE5484D);
            p.circulo(cx - w * 0.12f, cy + h * 0.03f, w * 0.10f, 0xFF7A7A, 0.55f);
            p.elipse(cx, y + h * 0.87f, w * 0.10f, h * 0.05f, 0x3F9B46);
            p.elipse(cx - w * 0.11f, y + h * 0.82f, w * 0.10f, h * 0.045f, 0x4CAF50);
            p.elipse(cx + w * 0.11f, y + h * 0.82f, w * 0.10f, h * 0.045f, 0x4CAF50);
            p.rect(cx - w * 0.012f, y + h * 0.82f, w * 0.024f, h * 0.15f, 0x2F7A36);
        } else if (ing == Catalogo.CEBOLLA) {
            p.circulo(cx, cy - h * 0.05f, w * 0.38f, 0xC9A0DC);
            p.circulo(cx, cy - h * 0.05f, w * 0.30f, 0xDCBDE9);
            for (int k = -1; k <= 1; k++) {
                p.rect(cx + k * w * 0.12f - w * 0.015f, cy - h * 0.30f, w * 0.03f,
                        h * 0.52f, 0xB78AD0, 0.85f);
            }
            p.rectRotado(cx, y + h * 0.88f, w * 0.06f, h * 0.22f, 18f, 0x4CAF50, 1f);
            p.rectRotado(cx - w * 0.07f, y + h * 0.86f, w * 0.06f, h * 0.19f, -20f, 0x6BD66B, 1f);
        } else if (ing == Catalogo.HUEVO) {
            p.elipse(cx, cy - h * 0.05f, w * 0.48f, h * 0.38f, 0xF7F3EA);
            p.elipse(cx - w * 0.17f, cy + h * 0.14f, w * 0.28f, h * 0.22f, 0xFDFBF5);
            p.circulo(cx + w * 0.06f, cy - h * 0.02f, w * 0.21f, 0xFFB703);
            p.circulo(cx + w * 0.01f, cy + h * 0.04f, w * 0.08f, 0xFFE08A, 0.9f);
        } else if (ing == Catalogo.TOCINETA) {
            for (int i = 0; i < 2; i++) {
                float sy = i == 0 ? cy + h * 0.11f : cy - h * 0.17f;
                pildora(p, x + w * 0.05f, sy - h * 0.10f, w * 0.90f, h * 0.22f,
                        i == 0 ? 0xE07A5F : 0xD06A50);
                for (int k = 0; k < 3; k++) {
                    p.rect(x + w * 0.24f + k * w * 0.22f, sy - h * 0.10f, w * 0.075f,
                            h * 0.22f, 0xB5452F, 0.85f);
                }
            }
        } else if (ing == Catalogo.PINA) {
            p.elipse(cx, cy - h * 0.10f, w * 0.34f, h * 0.38f, 0xFFB84D);
            for (int fila = -1; fila <= 1; fila++) {
                for (int col = -1; col <= 1; col++) {
                    p.circulo(cx + col * w * 0.16f, cy - h * 0.10f + fila * h * 0.19f,
                            w * 0.035f, 0xD98E2B, 0.85f);
                }
            }
            for (int k = -2; k <= 2; k++) {
                p.rectRotado(cx + k * w * 0.09f, cy + h * 0.36f, w * 0.07f, h * 0.28f,
                        k * 18f, k % 2 == 0 ? 0x4CAF50 : 0x3F9B46, 1f);
            }
        } else {   // aguacate
            p.elipse(cx, cy, w * 0.38f, h * 0.46f, 0x3F6B2A);
            p.elipse(cx, cy - h * 0.02f, w * 0.30f, h * 0.38f, 0xA9D38B);
            p.circulo(cx, cy - h * 0.14f, w * 0.13f, 0x6B4226);
            p.circulo(cx - w * 0.02f, cy + h * 0.12f, w * 0.09f, 0xB9DDA0, 0.6f);
        }
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
        boolean hover = activo && p.ratonDentro(x, y, w, h);
        float dy = hover ? 2f : 0f;
        float alfa = activo ? 1f : 0.45f;

        p.redondeado(x, y - 4f + dy, w, h, 11f, 0x05060A, 0.45f * alfa);
        p.redondeado(x, y + dy - 1f, w, h + 2f, 12f, 0x171A26, alfa);
        p.redondeado(x, y + dy, w, h, 11f, hover ? 0x454C66 : 0x373C51, alfa);
        p.redondeado(x + 3f, y + dy + h * 0.48f, w - 6f, h * 0.45f, 8f, 0xFFFFFF,
                (hover ? 0.14f : 0.07f) * alfa);
        p.redondeado(x + 3f, y + dy + 2f, w - 6f, h * 0.26f, 8f, 0x0B0D15, 0.22f * alfa);
        p.rect(x + 11f, y + dy + h - 2f, Math.max(0f, w - 22f), 1.2f, 0xFFFFFF, 0.18f * alfa);

        p.circulo(x + 24f, y + dy + h / 2f, 16f, 0x20232F, alfa);
        p.circulo(x + 24f, y + dy + h / 2f, 14f, ing.getColor(), 0.20f * alfa);
        p.setAlfa(alfa);
        icono(p, ing, x + 24f, y + dy + h / 2f, 29f);
        p.setAlfa(1f);

        p.textoCentrado(texto, x + (w + 48f) / 2f, y + dy + h / 2f - 0.5f, 0xFFFFFF, 0.85f, alfa);
        if (tecla != null) {
            p.redondeado(x + w - 18f, y + dy + 3f, 14f, 11f, 3f, 0x11131B, 0.9f * alfa);
            p.textoCentrado(tecla, x + w - 11f, y + dy + 8.5f, 0xD8DCEA, 0.52f, alfa);
        }
    }

    // =====================================================================  ICONOS DE INTERFAZ

    /** Barra de progreso redondeada con brillo. */
    public static void barra(Pincel p, float x, float y, float w, float h, float fraccion) {
        float f = Math.max(0f, Math.min(1f, fraccion));
        float r = h / 2f;
        p.redondeado(x, y, w, h, r, 0x161821, 0.85f);
        p.redondeado(x + 1f, y + 1f, w - 2f, h - 2f, Math.max(1f, r - 1f), 0x2E3142, 0.95f);
        if (f <= 0.002f) return;
        int color = f > 0.5f ? 0x6BD66B : f > 0.25f ? 0xF2C230 : 0xFF6B6B;
        float fw = Math.min(w - 2f, Math.max(h - 2f, (w - 2f) * f));
        p.redondeado(x + 1f, y + 1f, fw, h - 2f, Math.max(1f, r - 1f), color, 1f);
        p.redondeado(x + 3f, y + h * 0.45f, Math.max(1f, fw - 5f), Math.max(1f, h * 0.28f),
                Math.max(1f, h * 0.12f), 0xFFFFFF, 0.28f);
    }

    public static void moneda(Pincel p, float cx, float cy, float r) {
        p.circulo(cx, cy + 1.6f, r, 0x7A5200, 0.85f);
        p.circulo(cx, cy, r, 0xFFC94A);
        p.circulo(cx, cy, r * 0.72f, 0xFFE7A3);
        p.circulo(cx - r * 0.22f, cy + r * 0.26f, r * 0.20f, 0xFFFFFF, 0.9f);
        p.rect(cx - r * 0.09f, cy - r * 0.42f, r * 0.18f, r * 0.84f, 0xC08A12, 0.9f);
    }

    /** Calendario pequeno; 'fondo' debe ser el color de la tarjeta donde se dibuja. */
    public static void calendario(Pincel p, float cx, float cy, float s, int rgb, int fondo) {
        p.redondeado(cx - s * 0.55f, cy - s * 0.50f, s * 1.10f, s * 1.00f, s * 0.14f, rgb, 1f);
        p.redondeado(cx - s * 0.42f, cy - s * 0.40f, s * 0.84f, s * 0.74f, s * 0.08f, fondo, 1f);
        p.rect(cx - s * 0.42f, cy + s * 0.23f, s * 0.84f, s * 0.11f, rgb, 1f);
        p.rect(cx - s * 0.28f, cy + s * 0.40f, s * 0.09f, s * 0.24f, rgb, 1f);
        p.rect(cx + s * 0.19f, cy + s * 0.40f, s * 0.09f, s * 0.24f, rgb, 1f);
        p.circulo(cx - s * 0.20f, cy - s * 0.09f, s * 0.08f, rgb, 1f);
        p.circulo(cx + s * 0.14f, cy - s * 0.09f, s * 0.08f, rgb, 1f);
    }

    public static void corazon(Pincel p, float cx, float cy, float r, int rgb, float alfa) {
        p.circulo(cx - 0.30f * r, cy + 0.15f * r, 0.44f * r, rgb, alfa);
        p.circulo(cx + 0.30f * r, cy + 0.15f * r, 0.44f * r, rgb, alfa);
        p.rectRotado(cx, cy - 0.18f * r, 0.66f * r, 0.66f * r, 45f, rgb, alfa);
        p.circulo(cx - 0.36f * r, cy + 0.24f * r, 0.11f * r, 0xFFFFFF, 0.35f * alfa);
    }

    /** Reloj sencillo; 'fondo' debe ser el color de la superficie donde se dibuja. */
    public static void reloj(Pincel p, float cx, float cy, float r, int rgb, int fondo) {
        p.circulo(cx, cy, r, rgb);
        p.circulo(cx, cy, r * 0.76f, fondo);
        p.rectRotado(cx, cy + r * 0.20f, r * 0.12f, r * 0.46f, 0f, rgb, 1f);
        p.rectRotado(cx + r * 0.20f, cy, r * 0.46f, r * 0.12f, 0f, rgb, 1f);
        p.circulo(cx, cy, r * 0.13f, rgb);
    }

    public static void check(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.rectRotado(cx - 0.25f * s, cy - 0.02f * s, 0.50f * s, 0.18f * s, -45f, rgb, alfa);
        p.rectRotado(cx + 0.14f * s, cy + 0.10f * s, 0.75f * s, 0.18f * s, 48f, rgb, alfa);
    }

    public static void cruz(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.rectRotado(cx, cy, s * 0.82f, s * 0.20f, 45f, rgb, alfa);
        p.rectRotado(cx, cy, s * 0.82f, s * 0.20f, -45f, rgb, alfa);
    }

    public static void flechaIzquierda(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.rect(cx - 0.28f * s, cy - 0.08f * s, 0.78f * s, 0.16f * s, rgb, alfa);
        p.rectRotado(cx - 0.28f * s, cy + 0.15f * s, 0.34f * s, 0.14f * s, 45f, rgb, alfa);
        p.rectRotado(cx - 0.28f * s, cy - 0.15f * s, 0.34f * s, 0.14f * s, -45f, rgb, alfa);
    }

    public static void campana(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.circulo(cx, cy + 0.06f * s, 0.42f * s, rgb, alfa);
        p.rect(cx - 0.42f * s, cy - 0.30f * s, 0.84f * s, 0.38f * s, rgb, alfa);
        p.redondeado(cx - 0.55f * s, cy - 0.38f * s, 1.10f * s, 0.14f * s, 0.07f * s, rgb, alfa);
        p.circulo(cx, cy - 0.46f * s, 0.10f * s, rgb, alfa);
        p.circulo(cx, cy + 0.52f * s, 0.08f * s, rgb, alfa);
    }

    public static void destello(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.rectRotado(cx, cy, s, s * 0.20f, 0f, rgb, alfa);
        p.rectRotado(cx, cy, s * 0.20f, s, 0f, rgb, alfa);
        p.circulo(cx, cy, s * 0.15f, rgb, Math.min(1f, alfa * 1.4f));
    }

    public static void vapor(Pincel p, float cx, float y, float ancho, float t, int rgb, float alfa) {
        for (int i = 0; i < 4; i++) {
            float fase = t * 1.1f + i * 1.6f;
            float x = cx + (float) Math.sin(fase) * ancho * 0.20f + (i - 1.5f) * ancho * 0.17f;
            float alto = (t * 22f + i * 14f) % 52f;
            float r = 5.5f + (i % 2) * 2f;
            float a = Math.max(0f, 1f - alto / 52f) * alfa;
            p.circulo(x, y + alto, r, rgb, a);
        }
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
