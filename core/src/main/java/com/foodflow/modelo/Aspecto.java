package com.foodflow.modelo;

import java.util.Random;

/** Cómo se ve un cliente. Son solo datos (colores RGB y números), el dibujo está en ui/Dibujos. */
public final class Aspecto {
    public static final int[] PIEL = {0xFFDBB4, 0xF1C27D, 0xE0AC69, 0xC68642, 0x8D5524, 0x6B4226};
    public static final int[] PELO = {0x2B1B12, 0x5A3825, 0x9C5B2E, 0xD8B25A, 0xB8B8B8, 0xC0392B};
    public static final int[] CAMISA = {0x6FB1FC, 0xF08BA4, 0x9BE08F, 0xF2B66D, 0xB69CF2, 0x7FD6C9, 0xE76F51};
    public static final int[] COLOR_ACCESORIO = {0xE63946, 0x2A9D8F, 0x264653, 0xF4A261};

    /** Peinados: 0 corto, 1 largo, 2 rizado, 3 calvo, 4 moño. */
    public static final int PEINADOS = 5;
    /** Accesorios: 0 ninguno, 1 gorra, 2 gafas, 3 bigote. */
    public static final int ACCESORIOS = 4;

    private final int piel, pelo, camisa, peinado, accesorio, colorAccesorio;

    public Aspecto(int piel, int pelo, int camisa, int peinado, int accesorio, int colorAccesorio) {
        this.piel = piel;
        this.pelo = pelo;
        this.camisa = camisa;
        this.peinado = peinado;
        this.accesorio = accesorio;
        this.colorAccesorio = colorAccesorio;
    }

    public static Aspecto aleatorio(Random rnd) {
        int peinado = rnd.nextInt(PEINADOS);
        int accesorio = rnd.nextInt(ACCESORIOS);
        if (accesorio == 1 && (peinado == 2 || peinado == 4)) accesorio = 0;   // la gorra no cabe con rizos ni moño
        return new Aspecto(
                PIEL[rnd.nextInt(PIEL.length)],
                PELO[rnd.nextInt(PELO.length)],
                CAMISA[rnd.nextInt(CAMISA.length)],
                peinado,
                accesorio,
                COLOR_ACCESORIO[rnd.nextInt(COLOR_ACCESORIO.length)]);
    }

    /**
     * Crea un aspecto de cliente VIP mas llamativo: colores vivos y brillantes
     * segun el nivel (dorado para Gold, plata para Silver).
     */
    public static Aspecto vip(Random rnd, NivelCliente nivel) {
        int piel = PIEL[rnd.nextInt(PIEL.length)];
        int pelo = PELO[rnd.nextInt(PELO.length)];
        int camisa = (nivel == NivelCliente.GOLD) ? 0xF2B33D : 0xBFC9D1;   // dorado o plata
        int peinado = rnd.nextInt(PEINADOS);
        int accesorio = rnd.nextInt(ACCESORIOS);
        if (accesorio == 1 && (peinado == 2 || peinado == 4)) accesorio = 0;
        int colorAccesorio = (nivel == NivelCliente.GOLD) ? 0xF2B33D : 0xBFC9D1;
        return new Aspecto(piel, pelo, camisa, peinado, accesorio, colorAccesorio);
    }

    public int getPiel() { return piel; }
    public int getPelo() { return pelo; }
    public int getCamisa() { return camisa; }
    public int getPeinado() { return peinado; }
    public int getAccesorio() { return accesorio; }
    public int getColorAccesorio() { return colorAccesorio; }

    @Override
    public String toString() {
        return piel + "/" + pelo + "/" + camisa + "/" + peinado + "/" + accesorio + "/" + colorAccesorio;
    }
}
