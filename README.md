# The hunger games

Atiendes clientes que llegan a una cola, buscas la receta del pedido en RECETAS
para prepararla y compras ingredientes nuevos entre un día y otro.

## Cómo ejecutarlo

Requiere JDK 17 (Necesario para Gradle).

    ./gradlew lwjgl3:run
La primera vez Gradle descarga LibGDX, así que necesita internet.

## Controles

| Acción            | Mouse             | Teclado        |
|-------------------|-------------------|----------------|
| Atender / Entregar| botones           | ESPACIO        |
| Deshacer          | botón DESHACER    | RETROCESO      |
| Ingrediente       | botón del ingrediente | 1-9, 0, Q, E |
| Paginar ingredientes | flechas junto a INGREDIENTES | IZQ / DER |
| Recetario         | botón RECETAS (juego y tienda) | R  |
| Pausa             |                   | P              |

## Dónde está cada estructura de datos (`core/.../logica/`)

| Estructura                  | Uso en el juego                                                            |
|-----------------------------|----------------------------------------------------------------------------|
| `Queue<Cliente>` (LinkedList)   | Clientes en orden de llegada (FIFO). Cola llena o paciencia agotada: se van. |
| `Stack<Ingrediente>`            | Plato en construcción (LIFO). `deshacer()` quita el último ingrediente.      |
| `ListaDoble<Ingrediente>`       | Ingredientes disponibles; cada nodo enlaza al anterior y al siguiente. Crece al comprar y permite paginar los botones. |
| `Map<String, Receta>` (LinkedHashMap en `Recetario`) | Diccionario de recetas: el nombre es la clave y la `Receta` (tipo de plato + ingredientes en orden) es el valor. El pedido del cliente muestra el nombre de la receta y una imagen del plato; hay que buscar la receta en RECETAS para prepararla. |

`ListaDoble` es propia (`logica/ListaDoble.java`); las demás son estructuras de `java.util`.

## Organización

- `modelo/` y `logica/`: reglas del juego. No usan LibGDX, se pueden probar sin abrir ventana.
- `pantallas/`: Menú, Juego, Tienda, Recetas y Fin. `ui/Pincel`: dibuja rectángulos, círculos y texto.
- `ui/Dibujos`: dibuja los clientes, las hamburguesas (capa por capa), las pizzas, los hotdogs y los iconos de ingredientes.
- `modelo/Aspecto`: cómo se ve cada cliente (piel, pelo, peinado, camisa, accesorio). Se sortea al llegar.
- `modelo/Receta` y `modelo/TipoPlato`: nombre, tipo de plato e ingredientes en orden.
- `Mundo.java`: constantes de dificultad (`CAPACIDAD_COLA`, `DURACION_DIA`, `intervaloLlegada()`, `paciencia()`).
- `Catalogo.java`: ingredientes, precios, colores y los básicos del primer día.
- `Recetario.java`: diccionario de recetas y filtro de las que se pueden preparar con la despensa actual.

## Recetas

`Recetario.java` guarda las recetas en un `LinkedHashMap<String, Receta>`: el nombre es la clave y
la `Receta` (tipo de plato + lista de ingredientes en orden) es el valor. Los clientes solo piden
recetas que se puedan preparar con los ingredientes que ya tienes y nunca repiten la misma receta
dos veces seguidas.

Cada receta tiene un `TipoPlato` (`HAMBURGUESA`, `PIZZA` o `HOTDOG`) que decide cómo se dibuja en la
cola, el plato y el recetario. El pedido del cliente solo dice qué receta se sirve (nombre y una
imagen del plato); busca el nombre en RECETAS y respeta el orden de los ingredientes al armar el
plato. La pizza se ve desde arriba (masa, salsa, queso y toppings repartidos) y el hotdog
de lado (pan, salchicha e ingredientes).

El primer día empiezas con los básicos: Pan, Carne, Queso, Lechuga, Tomate, Masa, Salsa y Salchicha.
Con ellos ya hay hamburguesas, pizzas y hotdogs posibles. En la tienda se desbloquean Cebolla,
Pimiento, Huevo, Champinón, Tocineta, Pepperoni, Piña y Aguacate, que habilitan las recetas más
elaboradas.

La despensa es una `ListaDoble<Ingrediente>`. Cuando pasas de 12 ingredientes, los botones muestran
una página y las flechas junto a INGREDIENTES (o las teclas IZQ/DER) avanzan y retroceden caminando
por los nodos. Las teclas 1-9, 0, Q y E corresponden siempre a los botones visibles en esa página.

El botón RECETAS (o la tecla R) abre el recetario desde el juego o desde la tienda. Muestra
todas las recetas, marca las que están listas y dice qué ingredientes faltan. El juego queda en
pausa mientras lo consultas; las flechas del teclado cambian de página.

Para agregar una receta, añade una línea `receta("Nombre", TipoPlato.PIZZA, Catalogo.MASA, ...)` en
el bloque estático de `Recetario`. Para agregar un ingrediente nuevo, agrégalo en `Catalogo` y
dibújalo en `Dibujos.icono` (y en `Dibujos.capa` si también puede ir en una hamburguesa). Para un
tipo de plato nuevo, agrégalo a `TipoPlato` y dibújalo en `Dibujos.plato`.

## Imágenes (carpeta `assets/`)

- `nubes.jpg`: cielo que se ve detrás de los clientes.
- `madera.jpg`: madera del mostrador y de la zona de preparación.
- `fondo_menu.png`: ilustración del menú principal (diner al atardecer).
- `fondo_tienda.png`: despensa de la tienda entre días.
- `fondo_fin.png`: calle de noche para la pantalla final.

Para cambiarlas, reemplaza el archivo con el mismo nombre (en minúsculas) o edita los nombres en `FoodFlowGame.create()`.
Se ajustan al área sin deformarse; `RECORTE_CIELO` en `JuegoScreen` elige qué parte del cielo se ve (0 a 1).
Si una imagen falta, el juego usa un color liso y sigue funcionando.

## Notas

- Los textos en pantalla van sin tildes ni eñe porque la fuente que trae LibGDX no garantiza esos caracteres.
  Para usarlos, agrega una fuente `.fnt` a `assets/` y cárgala en `Pincel`.
- La interfaz, los clientes y la comida se dibujan con formas; los fondos son imágenes. La cara de cada cliente cambia con su paciencia: sonríe, se pone serio y se enoja.
- El primer Pan del plato es la base; cualquier otro Pan se dibuja como tapa con ajonjolí.
  En la pizza la base es la Masa y en el hotdog el Pan de los extremos es el pan de abajo y la tapa.
- Para ingredientes nuevos: agrégalos en `Catalogo` y dibújalos en `Dibujos.icono` (icono suelto de la tienda, botones y recetas); si pueden ir en una hamburguesa, también en `Dibujos.capa`.
- Para usar imágenes en vez de formas, ponlas en `assets/` y cárgalas con `Texture`.
