import java.util.ArrayList;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }
}

class FloodItWorld extends World {
  // size of board
  int boardSize;

  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;

  ArrayList<Cell> cells;

  // are cells being flooded now?
  boolean flood;

  // is the first cell being flooded?
  boolean firstFlood;

  // Color of current flood
  Color color;

  // Number of times player has clicked
  int clicks;

  // Maximum number of allowed clicks
  int max;

  // List of all colors that could be used in the game
  ArrayList<Color> colors;

  FloodItWorld(int boardSize, ArrayList<Color> colors) {
    this.boardSize = boardSize;
    this.colors = colors;
    this.board = new ArrayList<ArrayList<Cell>>();
    this.cells = new ArrayList<Cell>();
    this.flood = false;
    this.firstFlood = true;
    this.clicks = 0;
    this.max = (int) (1.5 * this.boardSize);
    this.initialize();
    this.connectCells();
  }

  FloodItWorld(ArrayList<ArrayList<Cell>> board) {
    this.board = board;
    this.boardSize = board.size();
    this.colors = new ArrayList<Color>();
    this.cells = new ArrayList<Cell>();
    this.flood = false;
    this.firstFlood = true;
    this.clicks = 0;
    this.max = (int) (1.5 * this.boardSize);
  }

  // draws the world
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.boardSize * 15, (this.boardSize * 15) + 50);
    for (ArrayList<Cell> a : board) {
      for (Cell c : a) {
        scene.placeImageXY(new RectangleImage(15, 15, OutlineMode.SOLID, c.color), 8 + ((c.x) * 15),
            8 + ((c.y) * 15));
      }
    }
    scene.placeImageXY(new TextImage(String.valueOf(this.clicks) + "/" + String.valueOf(this.max),
        30, Color.BLACK), (this.boardSize * 15) / 2, (this.boardSize * 15) + 25);
    return scene;
  }

  // updates the board on each click
  public void onMouseReleased(Posn pos) {
    if (!this.flood) {
      if (pos.x < this.boardSize * 15 && pos.y < this.boardSize * 15) {
        Cell c = this.board.get((int) Math.ceil(pos.y / 15)).get((int) Math.ceil(pos.x / 15));
        this.color = c.color;
        this.firstFlood = true;
        this.flood();
        this.flood = true;
        this.clicks++;
      }
    }
  }

  // updates the board on each tick
  public void onTick() {
    if (this.flood) {
      if (this.firstFlood) {
        cells.add(this.board.get(0).get(0));
        this.firstFlood = false;
      }
      for (Cell cell : cells) {
        cell.color = this.color;
      }
      ArrayList<Cell> cells2 = new ArrayList<Cell>();
      for (Cell cell : cells) {
        cells2.add(cell.top);
        cells2.add(cell.bottom);
        cells2.add(cell.left);
        cells2.add(cell.right);
      }
      cells.clear();
      for (Cell cell2 : cells2) {
        if (cell2 != null && cell2.flooded && !cell2.color.equals(this.color)) {
          cells.add(cell2);
        }
      }
      cells2.clear();
      if (cells.size() == 0) {
        this.flood = false;
      }
    }
  }

  // initializes the board
  void initialize() {
    for (int i = 0; i < boardSize; i++) {
      this.board.add(new ArrayList<Cell>());
      for (int j = 0; j < boardSize; j++) {
        this.board.get(i).add(new Cell(j, i, this.randomColor(), (i == 0 && j == 0)));
      }
    }
  }

  // returns the cell that should be in the given location relative to a cell
  void connectCells() {
    for (int i1 = 0; i1 < boardSize; i1++) {
      for (int j1 = 0; j1 < boardSize - 1; j1++) {
        this.board.get(i1).get(j1).right = this.board.get(i1).get(j1 + 1);
        this.board.get(i1).get(j1 + 1).left = this.board.get(i1).get(j1);
      }
    }
    for (int i1 = 0; i1 < boardSize - 1; i1++) {
      for (int j1 = 0; j1 < boardSize; j1++) {
        this.board.get(i1).get(j1).bottom = this.board.get(i1 + 1).get(j1);
        this.board.get(i1 + 1).get(j1).top = this.board.get(i1).get(j1);
      }
    }
  }

  // chooses a random color from the given list of colors
  Color randomColor() {
    return this.colors.get(new Random().nextInt(this.colors.size()));
  }

  // changes the flooded status of each of the given cells of the given color to
  // true
  void flood() {
    ArrayList<Cell> cells = new ArrayList<Cell>();
    for (int i2 = 0; i2 < boardSize - 1; i2++) {
      for (int j2 = 0; j2 < boardSize; j2++) {
        Cell cell = this.board.get(i2).get(j2);
        if (cell.flooded) {
          cells.add(cell.top);
          cells.add(cell.bottom);
          cells.add(cell.left);
          cells.add(cell.right);
          for (Cell cell2 : cells) {
            if (cell2 != null && !cell2.flooded && this.color.equals(cell2.color)) {
              cell2.flooded = true;
            }
          }
        }
      }
    }
  }
}

// Examples and tests for FloodItWorld
class ExamplesFloodItWorld {
  ArrayList<Color> colors1 = new ArrayList<Color>();

  void testBigBang(Tester t) {
    colors1.add(Color.BLUE);
    colors1.add(Color.GREEN);
    colors1.add(Color.RED);
    colors1.add(Color.YELLOW);
    colors1.add(Color.WHITE);
    colors1.add(Color.BLACK);
    FloodItWorld f = new FloodItWorld(10, colors1);
    World w = f;
    int worldWidth = 1000;
    int worldHeight = 1000;
    double tickRate = 0.05;
    w.bigBang(worldWidth, worldHeight, tickRate);
  }

  // tests for makeScene()
  boolean testMakeScene(Tester t) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    ArrayList<Cell> row1 = new ArrayList<Cell>();
    ArrayList<Cell> row2 = new ArrayList<Cell>();
    row1.add(new Cell(0, 0, Color.BLUE, false));
    row1.add(new Cell(1, 0, Color.RED, false));
    row2.add(new Cell(0, 1, Color.YELLOW, false));
    row2.add(new Cell(1, 1, Color.GREEN, false));
    board.add(row1);
    board.add(row2);
    WorldScene scene = new WorldScene(30, 80);
    scene.placeImageXY(new RectangleImage(15, 15, OutlineMode.SOLID, Color.BLUE), 8, 8);
    scene.placeImageXY(new RectangleImage(15, 15, OutlineMode.SOLID, Color.RED), 23, 8);
    scene.placeImageXY(new RectangleImage(15, 15, OutlineMode.SOLID, Color.YELLOW), 8, 23);
    scene.placeImageXY(new RectangleImage(15, 15, OutlineMode.SOLID, Color.GREEN), 23, 23);
    scene.placeImageXY(new TextImage("0/3", 30, Color.BLACK), 15, 55);
    FloodItWorld f = new FloodItWorld(board);
    f.colors.add(Color.BLUE);
    f.colors.add(Color.RED);
    f.colors.add(Color.YELLOW);
    f.colors.add(Color.GREEN);
    return t.checkExpect(f.makeScene(), scene);
  }

  // tests for initialize()
  boolean testInitialize(Tester t) {
    colors1.add(Color.BLUE);
    colors1.add(Color.GREEN);
    colors1.add(Color.RED);
    colors1.add(Color.YELLOW);
    colors1.add(Color.WHITE);
    colors1.add(Color.BLACK);
    FloodItWorld f = new FloodItWorld(10, colors1);
    return t.checkExpect(f.board.size(), 10);
  }

  // tests for connectCell()
  boolean testConnectCells(Tester t) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    ArrayList<Cell> row1 = new ArrayList<Cell>();
    ArrayList<Cell> row2 = new ArrayList<Cell>();
    row1.add(new Cell(0, 0, Color.BLUE, false));
    row1.add(new Cell(1, 0, Color.RED, false));
    row2.add(new Cell(0, 1, Color.YELLOW, false));
    row2.add(new Cell(1, 1, Color.GREEN, false));
    board.add(row1);
    board.add(row2);
    FloodItWorld f = new FloodItWorld(board);
    f.colors.add(Color.BLUE);
    f.colors.add(Color.RED);
    f.colors.add(Color.YELLOW);
    f.colors.add(Color.GREEN);
    f.connectCells();
    return t.checkExpect(board.get(0).get(0).right, board.get(0).get(1))
        && t.checkExpect(board.get(0).get(1).left, board.get(0).get(0))
        && t.checkExpect(board.get(0).get(0).bottom, board.get(1).get(0))
        && t.checkExpect(board.get(1).get(0).top, board.get(0).get(0))
        && t.checkExpect(board.get(1).get(0).right, board.get(1).get(1))
        && t.checkExpect(board.get(1).get(1).left, board.get(1).get(0))
        && t.checkExpect(board.get(0).get(1).bottom, board.get(1).get(1))
        && t.checkExpect(board.get(1).get(1).top, board.get(0).get(1));
  }

  // tests for randomColor()
  boolean testRandomColor(Tester t) {
    colors1.add(Color.BLUE);
    colors1.add(Color.GREEN);
    colors1.add(Color.RED);
    colors1.add(Color.YELLOW);
    colors1.add(Color.WHITE);
    colors1.add(Color.BLACK);
    FloodItWorld f = new FloodItWorld(10, colors1);
    return t.checkExpect(colors1.contains(f.randomColor()), true);
  }

  // tests for onMouseReleased() {
  boolean testOnMouseReleased(Tester t) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    ArrayList<Cell> row1 = new ArrayList<Cell>();
    ArrayList<Cell> row2 = new ArrayList<Cell>();
    row1.add(new Cell(0, 0, Color.BLUE, false));
    row1.add(new Cell(1, 0, Color.RED, false));
    row2.add(new Cell(0, 1, Color.YELLOW, false));
    row2.add(new Cell(1, 1, Color.GREEN, false));
    board.add(row1);
    board.add(row2);
    FloodItWorld f = new FloodItWorld(board);
    f.colors.add(Color.BLUE);
    f.colors.add(Color.RED);
    f.colors.add(Color.YELLOW);
    f.colors.add(Color.GREEN);
    f.onMouseReleased(new Posn(5, 5));
    return t.checkExpect(f.color, Color.BLUE) && t.checkExpect(f.firstFlood, true)
        && t.checkExpect(f.flood, true);
  }

  // tests for onTick()
  boolean testOnTick(Tester t) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    ArrayList<Cell> row1 = new ArrayList<Cell>();
    ArrayList<Cell> row2 = new ArrayList<Cell>();
    row1.add(new Cell(0, 0, Color.BLUE, true));
    row1.add(new Cell(1, 0, Color.RED, false));
    row2.add(new Cell(0, 1, Color.YELLOW, false));
    row2.add(new Cell(1, 1, Color.GREEN, false));
    board.add(row1);
    board.add(row2);
    FloodItWorld f = new FloodItWorld(board);
    f.colors.add(Color.BLUE);
    f.colors.add(Color.RED);
    f.colors.add(Color.YELLOW);
    f.colors.add(Color.GREEN);
    f.color = Color.RED;
    f.flood = true;
    f.firstFlood = true;
    f.onTick();
    return t.checkExpect(f.board.get(0).get(0).color, Color.RED);
  }

  // tests for flood()
  boolean testFlood(Tester t) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    ArrayList<Cell> row1 = new ArrayList<Cell>();
    ArrayList<Cell> row2 = new ArrayList<Cell>();
    row1.add(new Cell(0, 0, Color.BLUE, true));
    row1.add(new Cell(1, 0, Color.RED, false));
    row2.add(new Cell(0, 1, Color.YELLOW, false));
    row2.add(new Cell(1, 1, Color.GREEN, false));
    board.add(row1);
    board.add(row2);
    FloodItWorld f = new FloodItWorld(board);
    f.colors.add(Color.BLUE);
    f.colors.add(Color.RED);
    f.colors.add(Color.YELLOW);
    f.colors.add(Color.GREEN);
    f.color = Color.RED;
    f.flood();
    return t.checkExpect(f.board.get(1).get(0).flooded, true);
  }
}
