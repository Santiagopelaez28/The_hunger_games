package com.foodflow.ui;

import com.foodflow.logica.Catalogo;
import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.NivelCliente;
import com.foodflow.modelo.TipoPlato;

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
        if (ing == Catalogo.PIMIENTO) return 8f;
        if (ing == Catalogo.HUEVO) return 9f;
        if (ing == Catalogo.CHAMPINON) return 7f;
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

    /**
     * Dibuja el plato completo segun su tipo. La lista de capas siempre va en el orden de la receta.
     */
    public static void plato(Pincel p, TipoPlato tipo, float cx, float yBase, float ancho, float altoMax,
                             List<Ingrediente> capas) {
        if (capas == null || capas.isEmpty()) return;
        if (tipo == TipoPlato.PIZZA) pizza(p, cx, yBase, ancho, altoMax, capas);
        else if (tipo == TipoPlato.HOTDOG) hotdog(p, cx, yBase, ancho, altoMax, capas);
        else hamburguesa(p, cx, yBase, ancho, altoMax, capas);
    }

    /** Pizza vista desde arriba: masa, salsa, queso y los ingredientes repartidos por la superficie. */
    private static void pizza(Pincel p, float cx, float yBase, float ancho, float altoMax,
                              List<Ingrediente> capas) {
        boolean masa = false, salsa = false, queso = false;
        for (Ingrediente ing : capas) {
            if (ing == Catalogo.MASA) masa = true;
            else if (ing == Catalogo.SALSA) salsa = true;
            else if (ing == Catalogo.QUESO) queso = true;
        }
        if (!masa) return;

        float d = Math.min(ancho, altoMax * 2.6f);
        float rx = d * 0.50f;
        float ry = d * 0.17f;
        float cy = yBase + ry * 1.05f;

        p.elipse(cx, cy, rx, ry, 0xD9A441);
        p.elipse(cx, cy + ry * 0.10f, rx * 0.93f, ry * 0.84f, 0xE8B85C);
        if (salsa) p.elipse(cx, cy + ry * 0.16f, rx * 0.80f, ry * 0.68f, 0xC0392B);
        if (queso) {
            p.elipse(cx, cy + ry * 0.19f, rx * 0.69f, ry * 0.56f, 0xF4D35E);
            p.circulo(cx - rx * 0.24f, cy + ry * 0.15f, d * 0.025f, 0xFFE89A, 0.8f);
            p.circulo(cx + rx * 0.18f, cy + ry * 0.23f, d * 0.022f, 0xFFE89A, 0.75f);
            p.circulo(cx + rx * 0.30f, cy - ry * 0.02f, d * 0.018f, 0xFFE89A, 0.7f);
        }

        int indice = 0;
        for (Ingrediente ing : capas) {
            if (ing == Catalogo.MASA || ing == Catalogo.SALSA || ing == Catalogo.QUESO) continue;
            float ang = 0.45f + indice * 2.1f;
            float tx = cx + (float) Math.cos(ang) * rx * 0.46f;
            float ty = cy + (float) Math.sin(ang) * ry * 0.46f;
            if (ing == Catalogo.PEPPERONI) {
                float r = Math.max(1.8f, d * 0.035f);
                p.circulo(tx, ty, r, 0xB8322A);
                p.circulo(tx - r * 0.28f, ty + r * 0.20f, r * 0.20f, 0x7A201C, 0.8f);
                p.circulo(tx + r * 0.30f, ty - r * 0.15f, r * 0.16f, 0x7A201C, 0.8f);
            } else {
                icono(p, ing, tx, ty, Math.max(9f, d * 0.22f));
            }
            indice++;
        }
    }

    /** Hotdog de lado: pan inferior, salchicha, ingredientes y pan superior si la receta lo pide. */
    private static void hotdog(Pincel p, float cx, float yBase, float ancho, float altoMax,
                               List<Ingrediente> capas) {
        if (capas.get(0) != Catalogo.PAN) return;

        float d = Math.min(ancho, altoMax * 2.0f);
        float len = d * 0.92f;
        float h = d * 0.30f;
        float x = cx - len / 2f;
        boolean tapa = capas.size() > 1 && capas.get(capas.size() - 1) == Catalogo.PAN;

        p.redondeado(x, yBase, len, h * 0.62f, h * 0.30f, 0xD9A441);
        p.redondeado(x + len * 0.04f, yBase + h * 0.08f, len * 0.92f, h * 0.20f,
                h * 0.10f, 0xF0C878, 0.82f);

        int salchichas = 0;
        for (Ingrediente ing : capas) if (ing == Catalogo.SALCHICHA) salchichas++;
        for (int k = 0; k < salchichas; k++) {
            float sy = yBase + h * 0.37f + k * h * 0.20f;
            p.redondeado(x + len * 0.05f, sy, len * 0.90f, h * 0.34f, h * 0.17f, 0xB5452F);
            p.redondeado(x + len * 0.12f, sy + h * 0.08f, len * 0.54f, h * 0.08f,
                    h * 0.04f, 0xD96B52, 0.68f);
            for (int g = -1; g <= 1; g++) {
                p.rectRotado(cx + g * len * 0.20f, sy + h * 0.16f, len * 0.015f,
                        h * 0.24f, 24f, 0x7A2E20, 0.55f);
            }
        }

        int indice = 0;
        for (Ingrediente ing : capas) {
            if (ing == Catalogo.PAN || ing == Catalogo.SALCHICHA) continue;
            float tx = cx - len * 0.26f + (indice % 3) * len * 0.26f;
            float ty = yBase + h * 0.68f;
            icono(p, ing, tx, ty, Math.max(9f, d * 0.17f));
            indice++;
        }

        if (tapa) {
            p.redondeado(x, yBase + h * 1.00f, len, h * 0.34f, h * 0.17f, 0xE0B25A);
            p.redondeado(x + len * 0.05f, yBase + h * 1.07f, len * 0.90f, h * 0.12f,
                    h * 0.06f, 0xF0C878, 0.78f);
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
        } else if (ing == Catalogo.PIMIENTO) {
            p.circulo(cx - w * 0.14f, cy - h * 0.08f, w * 0.25f, 0xD9382F);
            p.circulo(cx + w * 0.14f, cy - h * 0.08f, w * 0.25f, 0xE5484D);
            p.circulo(cx, cy - h * 0.04f, w * 0.29f, 0xF05A3C);
            p.circulo(cx - w * 0.10f, cy + h * 0.03f, w * 0.11f, 0xFF8A70, 0.65f);
            p.rect(cx - w * 0.025f, y + h * 0.66f, w * 0.05f, h * 0.18f, 0x3F9B46);
            p.elipse(cx - w * 0.11f, y + h * 0.82f, w * 0.12f, h * 0.05f, 0x4CAF50);
            p.elipse(cx + w * 0.11f, y + h * 0.82f, w * 0.12f, h * 0.05f, 0x6BD66B);
        } else if (ing == Catalogo.CHAMPINON) {
            p.redondeado(cx - w * 0.12f, y + h * 0.05f, w * 0.24f, h * 0.38f, w * 0.06f, 0xF0E2C8);
            p.rect(cx - w * 0.09f, y + h * 0.09f, w * 0.06f, h * 0.22f, 0xDCCBAA, 0.85f);
            cupula(p, cx, y + h * 0.38f, w * 0.82f, h * 0.46f, 0xB98A5E);
            p.rect(cx - w * 0.41f, y + h * 0.36f, w * 0.82f, h * 0.05f, 0x9A6E48);
            p.circulo(cx - w * 0.20f, y + h * 0.60f, w * 0.045f, 0xE8D6B8, 0.85f);
            p.circulo(cx + w * 0.13f, y + h * 0.68f, w * 0.04f, 0xE8D6B8, 0.75f);
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
        } else if (ing == Catalogo.AGUACATE) {
            p.elipse(cx, cy, w * 0.38f, h * 0.46f, 0x3F6B2A);
            p.elipse(cx, cy - h * 0.02f, w * 0.30f, h * 0.38f, 0xA9D38B);
            p.circulo(cx, cy - h * 0.14f, w * 0.13f, 0x6B4226);
            p.circulo(cx - w * 0.02f, cy + h * 0.12f, w * 0.09f, 0xB9DDA0, 0.6f);
        } else if (ing == Catalogo.MASA) {
            p.elipse(cx, cy - h * 0.04f, w * 0.43f, h * 0.32f, 0xE0C084);
            p.elipse(cx, cy + h * 0.05f, w * 0.34f, h * 0.23f, 0xF1DCA8);
            p.circulo(cx - w * 0.18f, cy + h * 0.05f, w * 0.035f, 0xFFF3D6, 0.8f);
            p.circulo(cx + w * 0.12f, cy - h * 0.06f, w * 0.03f, 0xFFF3D6, 0.7f);
        } else if (ing == Catalogo.SALSA) {
            // Bote de salsa: tapa, cuello, cuerpo y salsa rebosando por arriba
            float cuelloW = w * 0.45f;
            float cuelloH = h * 0.22f;
            float cuerpoW = w * 0.78f;
            float cuerpoH = h * 0.62f;

            // Tapa del bote
            p.redondeado(cx - cuelloW / 2f, cy - h * 0.42f, cuelloW, cuelloH * 0.5f, cuelloH * 0.10f, 0x7A5200);
            p.redondeado(cx - cuelloW / 2f + cuelloW * 0.06f, cy - h * 0.38f, cuelloW * 0.88f, cuelloH * 0.45f, cuelloH * 0.08f, 0x5A3A1E);
            // Cuello
            p.rect(cx - cuelloW / 2f, cy - h * 0.20f, cuelloW, cuelloH * 0.55f, 0xA98A5A);
            // Cuerpo del bote (cristal traslúcido)
            p.redondeado(cx - cuerpoW / 2f, cy - h * 0.20f - cuerpoH, cuerpoW, cuerpoH, cuerpoH * 0.10f, 0xFDF6E3, 0.95f);
            p.redondeado(cx - cuerpoW / 2f + 2f, cy - h * 0.20f - cuerpoH + 2f, cuerpoW - 4f, cuerpoH - 4f, cuerpoH * 0.08f, 0xF5E7C8, 0.7f);
            // Salsa dentro del bote (relleno rojo)
            float salsaY = cy - h * 0.20f - cuerpoH + cuerpoH * 0.78f;
            p.redondeado(cx - cuerpoW / 2f + 4f, salsaY, cuerpoW - 8f, cuerpoH * 0.62f, 3f, 0xC0392B);
            // Burbujas de la salsa
            p.circulo(cx - cuerpoW * 0.18f, salsaY + cuerpoH * 0.20f, w * 0.05f, 0xF5A9A9, 0.8f);
            p.circulo(cx + cuerpoW * 0.12f, salsaY + cuerpoH * 0.35f, w * 0.04f, 0xF5A9A9, 0.75f);
            // Salpicadura de salsa saliendo por la tapa
            p.circulo(cx, cy - h * 0.45f, w * 0.08f, 0xC0392B);
            p.circulo(cx - w * 0.05f, cy - h * 0.40f, w * 0.06f, 0xE5484D, 0.8f);
        } else if (ing == Catalogo.SALCHICHA) {
            p.redondeado(cx - w * 0.45f, cy - h * 0.16f, w * 0.90f, h * 0.32f,
                    h * 0.16f, 0xA8432F);
            p.redondeado(cx - w * 0.32f, cy + h * 0.03f, w * 0.46f, h * 0.07f,
                    h * 0.035f, 0xD96B52, 0.68f);
            for (int k = -1; k <= 1; k++) {
                p.rectRotado(cx + k * w * 0.20f, cy + h * 0.02f, w * 0.025f,
                        h * 0.24f, 24f, 0x7A2E20, 0.55f);
            }
        } else if (ing == Catalogo.PEPPERONI) {
            p.circulo(cx, cy, w * 0.36f, 0xB8322A);
            p.circulo(cx - w * 0.10f, cy + h * 0.08f, w * 0.07f, 0xD66A58, 0.8f);
            p.circulo(cx + w * 0.12f, cy - h * 0.05f, w * 0.045f, 0x7A201C, 0.75f);
            p.circulo(cx - w * 0.14f, cy - h * 0.10f, w * 0.035f, 0x7A201C, 0.7f);
        } else {
            p.circulo(cx, cy, w * 0.36f, ing.getColor());
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
        } else if (ing == Catalogo.PIMIENTO) {
            float aw = w * 0.94f;
            pildora(p, cx - aw / 2f, y, aw, h, 0xD9382F);
            for (int k = -2; k <= 2; k++) {
                p.rect(cx + k * w * 0.17f, y + h * 0.08f, Math.max(1f, w * 0.025f), h * 0.84f, 0xF07A5A, 0.75f);
            }
        } else if (ing == Catalogo.CHAMPINON) {
            float aw = w * 0.94f;
            pildora(p, cx - aw / 2f, y, aw, h, 0xE8D6B8);
            for (int k = -2; k <= 2; k++) {
                float sx = cx + k * w * 0.17f;
                p.circulo(sx, y + h * 0.52f, h * 0.34f, 0xC7A97B);
                p.circulo(sx, y + h * 0.52f, h * 0.16f, 0x9A7A50, 0.8f);
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
        p.rect(cx - 0.17f * s, cy - 0.08f * s, 0.62f * s, 0.16f * s, rgb, alfa);
        p.rectRotado(cx - 0.275f * s, cy + 0.15f * s, 0.54f * s, 0.15f * s, 33.7f, rgb, alfa);
        p.rectRotado(cx - 0.275f * s, cy - 0.15f * s, 0.54f * s, 0.15f * s, -33.7f, rgb, alfa);
    }

    public static void flechaDerecha(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        p.rect(cx - 0.45f * s, cy - 0.08f * s, 0.62f * s, 0.16f * s, rgb, alfa);
        p.rectRotado(cx + 0.275f * s, cy + 0.15f * s, 0.54f * s, 0.15f * s, -33.7f, rgb, alfa);
        p.rectRotado(cx + 0.275f * s, cy - 0.15f * s, 0.54f * s, 0.15f * s, 33.7f, rgb, alfa);
    }

    /** Libro abierto, para el boton que abre el recetario. */
    public static void libro(Pincel p, float cx, float cy, float s, int rgb, float alfa) {
        float w = 0.90f * s, h = 0.68f * s;
        p.redondeado(cx - w / 2f, cy - h / 2f, w * 0.47f, h, s * 0.07f, rgb, alfa);
        p.redondeado(cx + w * 0.03f, cy - h / 2f, w * 0.47f, h, s * 0.07f, rgb, alfa);
        p.rect(cx - s * 0.015f, cy - h / 2f, s * 0.03f, h, rgb, alfa);
        for (int lado = -1; lado <= 1; lado += 2) {
            for (int k = 0; k < 2; k++) {
                p.rect(cx + lado * w * 0.27f - w * 0.16f, cy - h * 0.05f + k * h * 0.24f,
                        w * 0.32f, Math.max(1f, s * 0.045f), 0xFFFFFF, 0.35f * alfa);
            }
        }
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

        int peinado = a.getPeinado();
        int accesorio = a.getAccesorio();

        animo = Math.max(0f, Math.min(1f, animo));

        // =====================================================
        // 1. CUERPO Y CUELLO
        // =====================================================

        // Cuerpo con bordes redondeados
        p.redondeado(
            cx - 1.35f * s,
            cy - 3.10f * s,
            2.70f * s,
            1.85f * s,
            0.42f * s,
            camisa
        );

        // Cuello
        p.redondeado(
            cx - 0.30f * s,
            cy - 1.60f * s,
            0.60f * s,
            0.72f * s,
            0.15f * s,
            mezclar(piel, 0.92f)
        );

        // =====================================================
        // 2. CABELLO DE ATRÁS
        // =====================================================

        // Cabello largo
        if (peinado == 1 || peinado == 4) {

            p.redondeado(
                cx - 0.98f * s,
                cy - 1.15f * s,
                0.42f * s,
                1.75f * s,
                0.18f * s,
                pelo
            );

            p.redondeado(
                cx + 0.56f * s,
                cy - 1.15f * s,
                0.42f * s,
                1.75f * s,
                0.18f * s,
                pelo
            );
        }

        // Moño detrás de la cabeza
        if (peinado == 4) {
            p.circulo(
                cx + 0.48f * s,
                cy + 0.92f * s,
                0.36f * s,
                pelo
            );
        }

        // =====================================================
        // 3. OREJAS Y CABEZA
        // =====================================================

        p.circulo(
            cx - 0.84f * s,
            cy - 0.05f * s,
            0.19f * s,
            mezclar(piel, 0.90f)
        );

        p.circulo(
            cx + 0.84f * s,
            cy - 0.05f * s,
            0.19f * s,
            mezclar(piel, 0.90f)
        );

        // Cabeza ligeramente ovalada
        p.elipse(
            cx,
            cy,
            0.84f * s,
            1.00f * s,
            piel
        );

        // =====================================================
        // 4. CABELLO DE ADELANTE
        // =====================================================

        // Corto, largo y moño
        if (peinado == 0 || peinado == 1 || peinado == 4) {

            cupula(
                p,
                cx,
                cy + 0.32f * s,
                1.86f * s,
                0.68f * s,
                pelo
            );
        }

        // Rizado: rizos pequeños y conectados
        else if (peinado == 2) {

            for (int k = -2; k <= 2; k++) {

                p.circulo(
                    cx + k * 0.32f * s,
                    cy + 0.73f * s,
                    0.29f * s,
                    pelo
                );
            }

            // Rizos laterales
            p.circulo(
                cx - 0.77f * s,
                cy + 0.35f * s,
                0.24f * s,
                pelo
            );

            p.circulo(
                cx + 0.77f * s,
                cy + 0.35f * s,
                0.24f * s,
                pelo
            );
        }

        // Calvo: pequeño brillo en la cabeza
        else if (peinado == 3) {

            p.elipse(
                    cx - 0.30f * s,
                    cy + 0.48f * s,
                    0.12f * s,
                    0.07f * s,
                    mezclar(piel, 1.10f)
            );
        }

        // =====================================================
        // 5. OJOS Y GAFAS
        // =====================================================

        float ojoY = cy + 0.12f * s;

        // Gafas
        if (accesorio == 2) {

            for (int lado = -1; lado <= 1; lado += 2) {

                float gx = cx + lado * 0.34f * s;

                // Marco exterior
                p.redondeado(
                        gx - 0.31f * s,
                        ojoY - 0.21f * s,
                        0.62f * s,
                        0.42f * s,
                        0.10f * s,
                        0x292B35
                );

                // Cristal
                p.redondeado(
                        gx - 0.26f * s,
                        ojoY - 0.16f * s,
                        0.52f * s,
                        0.32f * s,
                        0.08f * s,
                        0xCFE8FF,
                        0.85f
                );
            }

            // Unión de las gafas
            p.rect(
                    cx - 0.10f * s,
                    ojoY - 0.03f * s,
                    0.20f * s,
                    0.07f * s,
                    0x292B35
            );
        }

        // Ojos
        for (int lado = -1; lado <= 1; lado += 2) {

            float ojoX = cx + lado * 0.34f * s;

            p.circulo(
                    ojoX,
                    ojoY,
                    0.12f * s,
                    0xFFF9ED
            );

            p.circulo(
                    ojoX + 0.02f * s,
                    ojoY,
                    0.065f * s,
                    OSCURO
            );
        }

        // =====================================================
        // 6. CEJAS
        // =====================================================

        int ceja = mezclar(pelo, 0.80f);

        if (animo < 0.25f) {

            // Expresión de enojo
            p.rectRotado(
                    cx - 0.37f * s,
                    cy + 0.40f * s,
                    0.34f * s,
                    0.075f * s,
                    -18f,
                    ceja,
                    1f
            );

            p.rectRotado(
                    cx + 0.37f * s,
                    cy + 0.40f * s,
                    0.34f * s,
                    0.075f * s,
                    18f,
                    ceja,
                    1f

            );

        } else {

            // Expresión tranquila
            p.rectRotado(
                    cx - 0.36f * s,
                    cy + 0.43f * s,
                    0.32f * s,
                    0.065f * s,
                    5f,
                    ceja,
                    1f
            );

            p.rectRotado(
                    cx + 0.36f * s,
                    cy + 0.43f * s,
                    0.32f * s,
                    0.065f * s,
                    -5f,
                    ceja,
                    1f
            );
        }

        // =====================================================
        // 7. NARIZ
        // =====================================================

        p.elipse(
                cx,
                cy - 0.13f * s,
                0.055f * s,
                0.075f * s,
                mezclar(piel, 0.82f)
        );

        // =====================================================
        // 8. BOCA Y EXPRESIÓN
        // =====================================================

        int boca = 0x7A2E2E;

        if (animo > 0.55f) {

            // Sonrisa
            p.circulo(
                    cx,
                    cy - 0.42f * s,
                    0.23f * s,
                    boca
            );

            p.circulo(
                    cx,
                    cy - 0.29f * s,
                    0.23f * s,
                    piel
            );

            // Mejillas
            p.circulo(
                    cx - 0.58f * s,
                    cy - 0.23f * s,
                    0.12f * s,
                    0xFF7A7A,
                    0.45f
            );

            p.circulo(
                    cx + 0.58f * s,
                    cy - 0.23f * s,
                    0.12f * s,
                    0xFF7A7A,
                    0.45f
            );

        } else if (animo > 0.25f) {

            // Boca neutral
            p.redondeado(
                    cx - 0.19f * s,
                    cy - 0.45f * s,
                    0.38f * s,
                    0.07f * s,
                    0.035f * s,
                    boca
            );

        } else {

            // Boca de enojo
            p.circulo(
                    cx,
                    cy - 0.48f * s,
                    0.22f * s,
                    boca
            );

            p.circulo(
                    cx,
                    cy - 0.61f * s,
                    0.22f * s,
                    piel
            );
        }

        // =====================================================
        // 9. ACCESORIOS
        // =====================================================

        // Bigote
        if (accesorio == 3) {

            p.circulo(
                    cx - 0.15f * s,
                    cy - 0.29f * s,
                    0.15f * s,
                    mezclar(pelo, 0.70f)
            );

            p.circulo(
                    cx + 0.15f * s,
                    cy - 0.29f * s,
                    0.15f * s,
                    mezclar(pelo, 0.70f)
            );
        }

        // Gorra
        else if (accesorio == 1) {

            int col = a.getColorAccesorio();

            cupula(
                    p,
                    cx,
                    cy + 0.40f * s,
                    1.95f * s,
                    0.63f * s,
                    col
            );

            // Visera
            p.redondeado(
                    cx - 1.00f * s,
                    cy + 0.35f * s,
                    2.00f * s,
                    0.14f * s,
                    0.07f * s,
                    mezclar(col, 0.80f)
            );

            p.redondeado(
                    cx + 0.05f * s,
                    cy + 0.25f * s,
                    1.05f * s,
                    0.14f * s,
                    0.07f * s,
                    mezclar(col, 0.70f)
            );
        }
    }

    /**
     * Cliente de cuerpo completo apoyado en yPies. Reutiliza cliente() para el busto
     * y agrega piernas, brazos, zapatos y sombra para la escena del restaurante.
     */
    public static void clienteCompleto(Pincel p, float cx, float yPies, float s, Aspecto a, float animo) {
        clienteCompleto(p, cx, yPies, s, a, animo, true, true);
    }

    private static void clienteCompleto(Pincel p, float cx, float yPies, float s, Aspecto a, float animo,
                                        boolean brazoIzquierdo, boolean brazoDerecho) {
        int pantalon = Pincel.mezclarRgb(a.getCamisa(), 0x1B1D26, 0.62f);
        int zapato = 0x2A2320;

        p.elipse(cx, yPies + s * 0.06f, s * 1.18f, s * 0.28f, 0x090502, 0.28f);

        // Piernas y zapatos
        p.redondeado(cx - s * 0.62f, yPies + s * 0.12f, s * 0.44f, s * 1.05f, s * 0.18f, pantalon);
        p.redondeado(cx + s * 0.18f, yPies + s * 0.12f, s * 0.44f, s * 1.05f, s * 0.18f, pantalon);
        p.redondeado(cx - s * 0.70f, yPies, s * 0.58f, s * 0.30f, s * 0.13f, zapato);
        p.redondeado(cx + s * 0.12f, yPies, s * 0.58f, s * 0.30f, s * 0.13f, zapato);

        cliente(p, cx, yPies + s * 4.10f, s, a, animo);

        // Brazos a los costados, por fuera del torso para que se vean completos.
        if (brazoIzquierdo) {
            p.redondeado(cx - s * 1.78f, yPies + s * 1.30f, s * 0.36f, s * 1.18f, s * 0.17f, a.getCamisa());
            p.circulo(cx - s * 1.60f, yPies + s * 1.24f, s * 0.18f, a.getPiel());
        }
        if (brazoDerecho) {
            p.redondeado(cx + s * 1.42f, yPies + s * 1.30f, s * 0.36f, s * 1.18f, s * 0.17f, a.getCamisa());
            p.circulo(cx + s * 1.60f, yPies + s * 1.24f, s * 0.18f, a.getPiel());
        }
    }

    /**
     * Corona VIP sobre la cabeza del cliente. Mas grande y brillante para Gold,
     * un poco mas sobria para Silver. Se dibuja encima del busto del cliente.
     */
    public static void coronaVIP(Pincel p, float cx, float cy, float s, NivelCliente nivel) {
        int color = (nivel == NivelCliente.GOLD) ? DORADO : 0xBFC9D1;
        float w = 1.7f * s;
        float h = 0.75f * s;
        float y = cy - 1.05f * s - h;   // sobre la cabeza

        // Base de la corona
        p.redondeado(cx - w / 2f, y, w, h * 0.45f, h * 0.10f, color);
        // Tres picos
        p.redondeado(cx - w * 0.42f, y - h * 0.45f, w * 0.34f, h * 0.75f, h * 0.08f, color);
        p.redondeado(cx - w * 0.08f, y - h * 0.70f, w * 0.26f, h * 0.95f, h * 0.08f, color);
        p.redondeado(cx + w * 0.26f, y - h * 0.45f, w * 0.34f, h * 0.75f, h * 0.08f, color);
        // Joyas en los picos
        p.circulo(cx, y - h * 0.55f + h * 0.95f, h * 0.10f, 0xFFFFFF, 0.95f);
        p.circulo(cx - w * 0.33f, y - h * 0.20f, h * 0.08f, 0xFFFFFF, 0.85f);
        p.circulo(cx + w * 0.33f, y - h * 0.20f, h * 0.08f, 0xFFFFFF, 0.85f);
        // Destello para que sea mas llamativo
        Dibujos.destello(p, cx, y - h * 0.30f, h * 0.50f, color, 0.9f);
    }

    private static final Aspecto CHEF = new Aspecto(0xF1C27D, 0x3B2416, 0xFFF7E8, 0, 3, 0xFFFFFF);

    /**
     * Personaje fijo del jugador: chef con gorro, delantal y pañuelo.
     * 'atendiendo' extiende el brazo hacia el cliente; 'servir' distinto de 0 muestra la bandeja
     * (positivo con visto bueno, negativo con cruz).
     */
    public static void cocinero(Pincel p, float cx, float yPies, float s, boolean atendiendo, float servir) {
        // Al atender, el brazo derecho se reemplaza por el brazo extendido de abajo.
        clienteCompleto(p, cx, yPies, s, CHEF, 1f, true, !(atendiendo || servir != 0f));
        float cy = yPies + s * 4.10f;

        int delantal = 0x2A9D8F;
        p.redondeado(cx - s * 0.88f, yPies + s * 1.05f, s * 1.76f, s * 1.75f, s * 0.18f, delantal, 1f);
        p.redondeado(cx - s * 0.42f, yPies + s * 1.45f, s * 0.84f, s * 0.36f, s * 0.08f, 0x247F76, 1f);
        p.rectRotado(cx - s * 0.34f, cy - s * 1.05f, s * 0.68f, s * 0.10f, 12f, delantal, 1f);
        p.rectRotado(cx + s * 0.34f, cy - s * 1.05f, s * 0.68f, s * 0.10f, -12f, delantal, 1f);
        p.circulo(cx, cy - s * 1.48f, s * 0.22f, 0xE5484D, 1f);

        // Gorro de chef (toca el techo: silueta mas alta, estilo logo)
        p.redondeado(cx - s * 0.92f, cy + s * 0.72f, s * 1.84f, s * 0.34f, s * 0.12f, 0xFFFFFF, 1f);
        p.circulo(cx - s * 0.52f, cy + s * 1.16f, s * 0.42f, 0xFFFFFF, 1f);
        p.circulo(cx, cy + s * 1.32f, s * 0.52f, 0xFFFFFF, 1f);
        p.circulo(cx + s * 0.52f, cy + s * 1.16f, s * 0.42f, 0xFFFFFF, 1f);
        p.redondeado(cx - s * 0.20f, cy + s * 0.84f, s * 0.40f, s * 0.12f, s * 0.05f,
                0xE8E2D2, 0.90f);

        if (atendiendo || servir != 0f) {
            p.redondeado(cx + s * 1.00f, yPies + s * 2.35f, s * 1.15f, s * 0.34f, s * 0.17f,
                    0xFFF7E8, 1f);
            p.circulo(cx + s * 2.18f, yPies + s * 2.52f, s * 0.20f, CHEF.getPiel(), 1f);
            p.elipse(cx + s * 2.18f, yPies + s * 2.76f, s * 0.34f, s * 0.08f, 0xE8E4D8, 1f);
            if (servir != 0f) {
                p.circulo(cx + s * 2.18f, yPies + s * 2.88f, s * 0.17f, 0xF2B33D, 0.95f);
                if (servir > 0f) check(p, cx + s * 2.18f, yPies + s * 2.88f, s * 0.24f, 0x3A2708, 1f);
                else cruz(p, cx + s * 2.18f, yPies + s * 2.88f, s * 0.20f, 0x7A1D1D, 1f);
            }
        }
    }
}
