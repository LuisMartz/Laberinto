# Laberinto

## Estructura

- `src/game`: codigo fuente Java.
- `assets/backgrounds`: fondos de menu y combate.
- `assets/ui`: botones e iconos de interfaz.
- `assets/tiles`: tiles del laberinto.
- `assets/player`: sprites del jugador.
- `assets/enemies`: sprites de enemigos.
- `assets/effects`: efectos y sprites auxiliares.
- `out`: clases compiladas.

## Ejecutar

```bat
run.bat
```

## Compilar

```bat
build.bat
```

El punto de entrada principal es `game.MainMenu`.

## Arquitectura progresiva

El proyecto mantiene por ahora el paquete `game` para evitar un movimiento masivo de imports. La estructura objetivo, cuando el codigo este mas desacoplado, es:

- `game.app`: arranque y host de pantallas (`MainMenu`, `GameHost`).
- `game.maze`: estado, generacion, control y render del laberinto.
- `game.combat`: estado de combate, combatientes, acciones, turnos y resolucion.
- `game.player`: datos del jugador, stats y progreso.
- `game.items`: inventario, equipo, consumibles, rarezas y loot.
- `game.skills`: arbol de habilidades, nodos y progresion.
- `game.world`: mundos, areas conectadas, pisos y progreso.
- `game.ui`: paneles Swing, menus y renderizadores.
- `game.assets`: utilidades de carga de recursos.

## Notas de refactor

- `LaberintoPanel` sigue siendo la clase mas mezclada: contiene renderizado Swing, input, guardado, estado de menu, triggers de combate y parte de la logica de exploracion. El siguiente paso seguro es seguir moviendo responsabilidades hacia `MazeState`, `MazeController` y `MazeRenderer`.
- `PanelTriangulo.java` sigue siendo la UI del combate, pero la logica de combatientes, estado, turnos y resolucion ya tiene clases puras separadas. `TurnBasedFightGameGUI.java` queda como launcher de prueba.
- `GameMenuController.java` y `GameMenuRenderer.java` separan el estado, navegacion y dibujo del menu de pausa/inventario/habilidades que antes vivia dentro de `LaberintoPanel`.
- `MazeScreenController.java` coordina exploracion, transiciones de combate, cambio de nivel y loot. `LaberintoPanel` conserva responsabilidades Swing como timers, foco, mensajes y cambio de paneles.
- Las clases de combate nuevas no deben importar Swing, AWT, `Graphics`, `KeyEvent` ni `JOptionPane`.

## Stats y armas

- `STR` escala armas pesadas como espadas.
- `DEX` escala armas ligeras como dagas.
- `INT` aumenta el dano de hechizos y reduce el dano magico recibido.
- `MIN` se mantiene como stat de reserva/mana.

## Guardado pendiente

El guardado actual ya usa propiedades en `LaberintoPanel`, pero todavia necesita consolidarse en una capa de progreso completa. Datos a revisar antes de ampliar mundos y areas:

- nivel del jugador, XP, puntos de stats y puntos de habilidad.
- stats base y stats derivados por equipo.
- HP/MP actuales.
- inventario, consumibles y equipo equipado.
- habilidades desbloqueadas y progreso de nodos del arbol.
- mundo, area, piso actual y areas completadas.
- estado de mazmorra si se quiere reanudar una run a mitad de piso.
