# The hunger games

Atiendes clientes que llegan a una cola, armas cada pedido apilando
ingredientes y compras ingredientes nuevos entre un día y otro.

## Cómo ejecutarlo

Necesitas JDK 17 o superior (el proyecto compila con `release 17`).

    ./gradlew lwjgl3:run          (Windows: gradlew.bat lwjgl3:run)

La primera vez Gradle descarga LibGDX, así que necesita internet.
En IntelliJ IDEA: *Open* sobre esta carpeta y ejecuta `Lwjgl3Launcher`.

## Controles

| Acción            | Mouse             | Teclado        |
|-------------------|-------------------|----------------|
| Atender / Entregar| botones           | ESPACIO        |
| Deshacer          | botón DESHACER    | RETROCESO      |
| Ingrediente       | botón del ingrediente | 1-9 y 0    |
| Pausa             |                   | P              |

## Dónde está cada estructura de datos (`core/.../logica/Mundo.java`)

| Estructura                  | Uso en el juego                                                            |
|-----------------------------|----------------------------------------------------------------------------|
| `Queue<Cliente>` (LinkedList)   | Clientes en orden de llegada (FIFO). Cola llena o paciencia agotada: se van. |
| `Stack<Ingrediente>`            | Plato en construcción (LIFO). `deshacer()` quita el último ingrediente.      |
| `ArrayList<Ingrediente>`        | Ingredientes disponibles; crece al comprar en la tienda.                     |

Son las mismas estructuras de `java.util` que usaba el proyecto original.

## Organización

- `modelo/` y `logica/`: reglas del juego. No usan LibGDX, se pueden probar sin abrir ventana.
- `pantallas/`: Menú, Juego, Tienda y Fin. `ui/Pincel`: dibuja rectángulos, círculos y texto.
- `ui/Dibujos`: dibuja los clientes, la hamburguesa (capa por capa) y los iconos de ingredientes.
- `modelo/Aspecto`: cómo se ve cada cliente (piel, pelo, peinado, camisa, accesorio). Se sortea al llegar.
- `Mundo.java`: constantes de dificultad (`CAPACIDAD_COLA`, `DURACION_DIA`, `intervaloLlegada()`, `paciencia()`).
- `Catalogo.java`: ingredientes, precios y colores.

## Imágenes (carpeta `assets/`)

- `nubes.jpg`: cielo que se ve detrás de los clientes.
- `madera.jpg`: fondo del mostrador.

Para cambiarlas, reemplaza el archivo con el mismo nombre (en minúsculas) o edita los nombres en `FoodFlowGame.create()`.
Se ajustan al área sin deformarse; `RECORTE_CIELO` en `JuegoScreen` elige qué parte del cielo se ve (0 a 1).
Si una imagen falta, el juego usa un color liso y sigue funcionando.

## Notas

- Los textos en pantalla van sin tildes ni eñe porque la fuente que trae LibGDX no garantiza esos caracteres.
  Para usarlos, agrega una fuente `.fnt` a `assets/` y cárgala en `Pincel`.
- Todo se dibuja con formas (no hay imágenes). La cara de cada cliente cambia con su paciencia: sonríe, se pone serio y se enoja.
- El primer Pan del plato es la base; cualquier otro Pan se dibuja como tapa con ajonjolí.
- Para ingredientes nuevos: agrégalos en `Catalogo` y dibújalos en `Dibujos.capa` (si no, salen como una barra de su color).
- Para usar imágenes en vez de formas, ponlas en `assets/` y cárgalas con `Texture`.
