# Inheritance of Chaos

A small turn-based dark fantasy RPG we made in Java for our course project.

You play as the last heir of a kingdom that burned down. You go out of your camp, explore places,
fight monsters, find loot, level up and try to beat two bosses. It's not a big game, about
30–40 fights from start to finish, but it has a beginning, a middle and an end.

The graphics are just text and a few buttons. We wanted to spend our time on the game logic,
not on drawing.

## How to run it

You need **Java 17 or newer** and **Maven**.

Build it once:

```powershell
mvn package
```

Then start the game:

```powershell
java -jar target/inheritance-of-chaos.jar
```

You can also just double-click `target/inheritance-of-chaos.jar` after building.

In **VS Code** (with the Extension Pack for Java) open the project folder, wait until it loads and press **F5**.

Your saves go into `<your user folder>/.inheritance-of-chaos/game.db`.

## How to play

1. Start a new game and type your hero's name.
2. Pick a location in the list and press **Explore**. Most of the time you meet a monster,
   but sometimes you find a chest, a shrine that heals you, or a trap.
3. In a fight you have 5 buttons:
   * **Attack** – a normal hit (sometimes a critical hit)
   * **Skill** – a special move that costs mana
   * **Item** – drink a potion or throw a bomb
   * **Defend** – take half damage this turn and get a bit of mana back
   * **Escape** – try to run away (does not work on bosses)
4. Winning gives you XP, gold and sometimes items. Every level gives you 2 stat points –
   spend them in **Character**.
5. Win enough fights in a location to open the next one. The last place of every world is a boss.
6. Back at the camp you can **Rest** and buy things from the **Merchant**.

If you die you don't lose your progress – you wake up in the camp and lose a quarter of your gold.

**Tip:** bosses warn you before their big attack (the orange line at the top says *Defend!*).
Pressing Defend at the right moment makes the boss fights a lot easier.

### The worlds

| World | Locations | Boss |
|---|---|---|
| Ashen Vale | Whispering Woods (Lv 1–3), Rotting Marsh (Lv 2–5) | Fallen Knight (Lv 6) |
| Chaos Wastes | Bone Desert (Lv 6–9), Crystal Caverns (Lv 8–11) | Herald of Chaos (Lv 13) |

Monster levels follow your level a bit, but they never go outside the range of the location.
Every monster has its own trick: the slime's acid goes through armor, the goblin steals health,
the skeleton gets back up once, the chaos hound gets angrier every turn, and so on.

## What is inside (course components)

Our project uses 4 components for 10 points:

| Component | Points | Where |
|---|---|---|
| GUI | 3 | Swing window in `ui/GameWindow.java`: `BorderLayout`, `CardLayout` to switch between normal and battle buttons, `JComboBox` for locations, `JOptionPane` dialogs for menus. |
| Database | 3 | SQLite through JDBC in the `db` package. 5 tables, foreign keys with `ON DELETE CASCADE`, a transaction when saving, UPSERT (`ON CONFLICT DO UPDATE`), `COUNT` and `GROUP BY` for the statistics screen ("Chronicle"). |
| Design patterns | 2 | See the list below. |
| Multithreading | 2 | Every battle runs in its own thread, and all database work runs in a separate background thread. |

### Design patterns we used

* **Factory** – `MonsterFactory` and `ItemFactory`. The game asks the factory for a monster or an item
  instead of calling `new` everywhere.
* **Strategy** – `Skill` and its subclasses. The battle just calls `skill.use(...)` and doesn't care what the skill does.
* **Observer** – `BattleListener`. The battle tells the window what happened, without knowing anything about Swing.
* **Template Method** – `Monster.takeTurn()` always calls `act()`, and every monster has its own `act()`.
* **Command** – `PlayerCommand`. A button press becomes an object that goes into the battle thread's queue.
* **Singleton** – `DatabaseManager`. There is only one, you get it with `getInstance()`.
* **DAO** – `SaveRepository` and `ChronicleRepository`. All the SQL is in these two classes.

### How the threads work

```
 Swing thread (window)              battle thread                  db-worker thread
 ---------------------              -------------                  ----------------
 click "Attack" --PlayerCommand-->  queue.take() wakes up
                                    hero's turn
                                    waits 0.7 sec
                                    monster's turn
 invokeLater(update) <--snapshot--  sendUpdate()

 SaveData.capture() --copy of the game---------------------------> save to SQLite
```

* The battle thread waits on a `BlockingQueue` until the player presses a button (producer / consumer).
* `submit()` is `synchronized`, so if you click very fast only one action counts per turn.
* The window never reads the player while the battle thread changes it – it only gets copies (`BattleSnapshot`).
* Saving works the same way: a copy of the game (`SaveData`) is made first and only the copy goes to the database thread,
  so the window never freezes.
* When you close the window, the game waits for the last save to finish.

## Project structure

```
src/main/java/com/inheritanceofchaos/
  Main.java       start of the program
  model/          the hero, monsters, items and skills
  world/          worlds, locations, monster levels
  factory/        MonsterFactory, ItemFactory
  battle/         the battle and everything it sends to the window
  game/           one playthrough (GameSession) and exploring
  db/             SQLite: saves and statistics
  ui/             the game window
  util/           random numbers and the database thread
src/test/java/    JUnit tests
```

Run the tests with `mvn test`.
