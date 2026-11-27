# The hunger games

The hunger games es un videojuego / simulador interactivo en el que el jugador tendrá la posibilidad de preparar las recetas solicitadas por los clientes, el jugador tiene la posibilidad de buscar las diferentes recetas en el recetario del juego y prepararlas acorde a sus ingredientes, cada cliente tiene un limite de paciencia, si el jugador realiza mal una receta o el cliente se va, pierde una de las cinco vidas disponibles; el juego permite al jugador incorporar mejoras al restaurante y comprar más ingredientes para ampliar su catalogo y desbloquear más posibles recetas.

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

Sin un cliente en el mostrador la cocina permanece cerrada y los botones de
ingredientes no se muestran. Primero pulsa ATENDER (o ESPACIO) para que el
cliente pase al mostrador y revele su pedido.

## Dónde está cada estructura de datos (`core/.../logica/`)

| Estructura                  | Uso en el juego                                                            |
|-----------------------------|----------------------------------------------------------------------------|
| `ColaPrioridad<Cliente>` (min-heap) | Cola de clientes ordenada por prioridad: VIP Gold (1), VIP Silver (2), Común (3). El montículo compara prioridades y el cliente con mayor prioridad (valor más bajo) se atiende primero. Capacidad 10. |
| `Stack<Ingrediente>`            | Plato en construcción (LIFO). `deshacer()` quita el último ingrediente.      |
| `ListaDoble<Ingrediente>`       | Ingredientes disponibles; cada nodo enlaza al anterior y al siguiente. Crece al comprar y permite paginar los botones. |
| `ArbolAVL<RegistroPartida>`     | Historial de partidas finalizadas. Inserta registros ordenados por puntaje descendente y devuelve el Top 5 (mayor puntaje primero, desempate por día mayor). Se muestra en `FinScreen`. |
| `Map<String, Receta>` (LinkedHashMap en `Recetario`) | Diccionario de recetas: el nombre es la clave y la `Receta` (tipo de plato + ingredientes en orden) es el valor. El globo del cliente en el mostrador muestra solo el nombre y la imagen de la receta; los ingredientes se consultan en el recetario. |

`ColaPrioridad`, `ArbolAVL` y `ListaDoble` son propias (`logica/`); las demás son estructuras de `java.util`.

## Organización

- `modelo/` y `logica/`: reglas del juego. No usan LibGDX, se pueden probar sin abrir ventana.
  - `modelo/Cliente`, `modelo/NivelCliente`: datos del cliente y su nivel VIP.
  - `modelo/Aspecto`: cómo se ve cada cliente (piel, pelo, peinado, camisa, accesorio). Incluye `Aspecto.vip()` para clientes VIP.
  - `logica/Mundo`: constantes de dificultad, lógica de llegada de clientes (VIP y comunes) y entrega de pedidos.
  - `logica/ColaPrioridad`: montículo min-heap para la cola de clientes por prioridad VIP.
  - `logica/RegistroPartida` y `logica/ArbolAVL`: historial y Top 5 de mejores partidas.
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

Cada receta tiene un `TipoPlato` (`HAMBURGUESA`, `PIZZA` o `HOTDOG`) que decide cómo se dibuja en el
globo del pedido, el plato y el recetario. En la cola solo se ve al cliente y su paciencia: el
pedido se revela al pulsar ATENDER, cuando el cliente pasa al mostrador y se acerca a él. El globo muestra solo el nombre y la imagen de la receta: para ver los ingredientes hay que buscarlos en el recetario. La pizza se ve desde
arriba (masa, salsa, queso y toppings repartidos) y el hotdog de lado (pan, salchicha e
ingredientes).

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

## Sistema VIP y colas separadas

Cuando un cliente llega, puede ser:
- **Común** (prioridad 3): aspecto aleatorio normal, globo blanco.
- **VIP Silver** (prioridad 2): aspecto llamativo en tonos plateados, globo con borde plata.
- **VIP Gold** (prioridad 1): aspecto llamativo en tonos dorados, globo con borde dorado y corona sobre la cabeza.

Reglas de los clientes VIP:
- Son **menos frecuentes** que los comunes (15 % de probabilidad base).
- Se garantiza **al menos 2 VIPs por día** (subiendo la probabilidad mientras no se alcance el mínimo).
- No aparecen hasta pasados **10 segundos** de iniciado el día.
- Nunca llegan dos VIPs uno tras otro (el siguiente siempre es común).
- Tienen **25 % más de paciencia** que los comunes.
- En la zona de espera se dibujan en **dos filas separadas**: comunes arriba y VIPs abajo. La prioridad solo se usa para decidir a quién atender siguiente, no para el orden visual de dibujo.
- Si un VIP recibe un pedido **incorrecto** y el jugador tiene monedas suficientes, se hace un **reembolso de la mitad del valor de la receta** y el cliente no se va enfadado.

## Top 5 mejores partidas (árbol AVL)

Al terminar una partida (pantalla `FIN_JUEGO`), las estadísticas se guardan en un **árbol AVL** (`logica/ArbolAVL.java`) como un `RegistroPartida` con:
- Día alcanzado
- Entregados, perdidos y monedas
- Puntaje = `100 × entregados + 300 × (día − 1)`

El árbol se ordena por **puntaje descendente** (mayor primero); si hay empate, gana el que llegó a un día mayor. En `FinScreen` se muestra el **Top 5** recorriendo el árbol en orden (derecha, nodo, izquierda) y deteniéndose en el quinto registro. Cada registro muestra día, entregados, perdidos y puntaje, con medalla de posición (1.º dorado, 2.º plateado, 3.º bronce).

## Imágenes (carpeta `assets/`)

- `nubes.jpg`: paisaje que se ve por la ventana del restaurante.
- `madera.jpg`: suelo del restaurante y mostrador.
- `fondo_menu.png`: ilustración del menú principal (diner al atardecer).
- `fondo_tienda.png`: despensa de la tienda entre días.
- `fondo_fin.png`: calle de noche para la pantalla final.
- `restaurante_logo.*`: logo del restaurante que se muestra en el letrero del mostrador (opcional; si falta, se pinta un color liso).

Para cambiarlas, reemplaza el archivo con el mismo nombre (en minúsculas) o edita los nombres en `FoodFlowGame.create()`.
Se ajustan al área sin deformarse y se recortan para cubrir su espacio.
Si una imagen falta, el juego usa un color liso y sigue funcionando.

- `campana.wav` (opcional): sonido que suena al pulsar ATENDER. Si falta, el juego sigue sin sonido.

La ventana arranca en modo `960 x 540` y el juego se dibuja en `960 x 600` (3:2), escalado a la ventana.

## Notas

- Los textos se renderizan con la fuente incluida en LibGDX (filtro cercano, sin alisado borroso). Cubre caracteres latinos básicos, incluida la eñe y las tildes del español; si se quiere una fuente distinta, agrega una `.ttf` a `assets/` y cárgala en `Pincel`.
- La interfaz, los clientes, el chef y la comida se dibujan con formas; los fondos son imágenes. La cara de cada cliente cambia con su paciencia: sonríe, se pone serio y se enoja.
- El chef ocupa un puesto fijo detras del mostrador y no se mueve horizontalmente; su silueta (toque alto) se asemeja al del logo del restaurante. Al pulsar ATENDER suena una campana y el cliente se acerca al mostrador. Los clientes de la cola se reparten en dos filas para que quepan los 10. Un mostrador vertical blanco separa al chef de los clientes.
- El primer Pan del plato es la base; cualquier otro Pan se dibuja como tapa con ajonjolí.
  En la pizza la base es la Masa y en el hotdog el Pan de los extremos es el pan de abajo y la tapa.
- Para ingredientes nuevos: agrégalos en `Catalogo` y dibújalos en `Dibujos.icono` (icono suelto de la tienda, botones y recetas); si pueden ir en una hamburguesa, también en `Dibujos.capa`.
- Para usar imágenes en vez de formas, ponlas en `assets/` y cárgalas con `Texture`.
