package com.foodflow.logica;

import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.Receta;
import com.foodflow.modelo.TipoPlato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diccionario de recetas: el nombre es la clave y la receta (tipo + ingredientes) el valor.
 * Los clientes solo piden recetas que se puedan preparar con la despensa del jugador.
 * Los ingredientes se leen en el orden en que se arman, igual que la pila del plato.
 */
public final class Recetario {
    /** Receta de respaldo por si la despensa no alcanza para ninguna otra. */
    public static final String RECETA_BASICA = "Sencilla";

    private static final Map<String, Receta> RECETAS = new LinkedHashMap<>();

    static {
        // ---------------------------------------------------------------- recetas del primer dia
        receta("Sencilla", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.PAN);
        receta("Clasica", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.QUESO,
                Catalogo.LECHUGA, Catalogo.TOMATE, Catalogo.PAN);
        receta("Doble carne", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.CARNE,
                Catalogo.QUESO, Catalogo.PAN);
        receta("Vegetariana", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.QUESO, Catalogo.LECHUGA,
                Catalogo.TOMATE, Catalogo.PAN);
        receta("Fresca", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.LECHUGA, Catalogo.TOMATE,
                Catalogo.PAN);
        receta("Queso loco", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.QUESO,
                Catalogo.QUESO, Catalogo.PAN);

        receta("Margarita", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.TOMATE);
        receta("Cuatro queso", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.QUESO, Catalogo.QUESO);
        receta("Pizza carne", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.CARNE);

        receta("Hotdog clasico", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA, Catalogo.QUESO,
                Catalogo.PAN);
        receta("Hotdog completo", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA, Catalogo.TOMATE,
                Catalogo.LECHUGA, Catalogo.PAN);
        receta("Hotdog doble", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA, Catalogo.SALCHICHA,
                Catalogo.QUESO, Catalogo.PAN);

        // ---------------------------------------------------------------- recetas que se desbloquean
        receta("Criolla", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.CEBOLLA,
                Catalogo.TOMATE, Catalogo.PAN);
        receta("Campesina", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.HUEVO,
                Catalogo.CEBOLLA, Catalogo.PAN);
        receta("Tocineta", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.TOCINETA,
                Catalogo.QUESO, Catalogo.PAN);
        receta("Hawaiana", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.PINA,
                Catalogo.TOCINETA, Catalogo.QUESO, Catalogo.PAN);
        receta("Rancho", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.HUEVO,
                Catalogo.TOCINETA, Catalogo.PAN);
        receta("Aguacate", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.AGUACATE,
                Catalogo.TOMATE, Catalogo.PAN);
        receta("Verde", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.AGUACATE, Catalogo.LECHUGA,
                Catalogo.QUESO, Catalogo.PAN);
        receta("Picante", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.PIMIENTO,
                Catalogo.QUESO, Catalogo.TOMATE, Catalogo.PAN);
        receta("Champinon", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.CHAMPINON,
                Catalogo.CEBOLLA, Catalogo.PAN);
        receta("Suprema", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.TOCINETA,
                Catalogo.HUEVO, Catalogo.QUESO, Catalogo.PAN);
        receta("Tropical", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.CARNE, Catalogo.PINA,
                Catalogo.AGUACATE, Catalogo.PAN);
        receta("Huerto", TipoPlato.HAMBURGUESA, Catalogo.PAN, Catalogo.PIMIENTO, Catalogo.CHAMPINON,
                Catalogo.LECHUGA, Catalogo.PAN);

        receta("Peperoni", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.PEPPERONI);
        receta("Jardin", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.PIMIENTO, Catalogo.CHAMPINON, Catalogo.CEBOLLA);
        receta("Aloha", TipoPlato.PIZZA, Catalogo.MASA, Catalogo.SALSA, Catalogo.QUESO,
                Catalogo.PINA, Catalogo.TOCINETA);

        receta("Hotdog criollo", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA,
                Catalogo.CEBOLLA, Catalogo.PIMIENTO, Catalogo.PAN);
        receta("Hotdog hawaiano", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA,
                Catalogo.PINA, Catalogo.TOCINETA, Catalogo.PAN);
        receta("Hotdog verde", TipoPlato.HOTDOG, Catalogo.PAN, Catalogo.SALCHICHA,
                Catalogo.AGUACATE, Catalogo.TOMATE, Catalogo.PAN);
    }

    private Recetario() { }

    private static void receta(String nombre, TipoPlato tipo, Ingrediente... ingredientes) {
        RECETAS.put(nombre, new Receta(nombre, tipo, List.of(ingredientes)));
    }

    /** Diccionario completo, en el orden en que se muestra en el recetario. */
    public static Map<String, Receta> todas() {
        return Collections.unmodifiableMap(RECETAS);
    }

    /** Nombres de todas las recetas, en orden. */
    public static List<String> nombres() {
        return new ArrayList<>(RECETAS.keySet());
    }

    /** Receta completa; null si el nombre no existe. */
    public static Receta receta(String nombre) {
        return RECETAS.get(nombre);
    }

    /** Ingredientes de una receta; lista vacia si el nombre no existe. */
    public static List<Ingrediente> ingredientes(String nombre) {
        Receta receta = RECETAS.get(nombre);
        return receta == null ? List.of() : receta.getIngredientes();
    }

    /** Tipo de plato de una receta. */
    public static TipoPlato tipo(String nombre) {
        Receta receta = RECETAS.get(nombre);
        return receta == null ? TipoPlato.HAMBURGUESA : receta.getTipo();
    }

    /** Recetas que se pueden preparar con la despensa indicada. */
    public static List<Receta> disponibles(List<Ingrediente> despensa) {
        List<Receta> lista = new ArrayList<>();
        for (Receta receta : RECETAS.values()) {
            if (despensa.containsAll(receta.getIngredientes())) lista.add(receta);
        }
        return lista;
    }

    /** Ingredientes que faltan para una receta, sin repetirlos. */
    public static List<Ingrediente> faltantes(String nombre, List<Ingrediente> despensa) {
        List<Ingrediente> faltan = new ArrayList<>();
        for (Ingrediente ingrediente : ingredientes(nombre)) {
            if (!despensa.contains(ingrediente) && !faltan.contains(ingrediente)) {
                faltan.add(ingrediente);
            }
        }
        return faltan;
    }
}
