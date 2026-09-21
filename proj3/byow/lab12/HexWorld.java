package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random();
    public void addHexagon(int length){
        return;
    }

    private static class Position {
        int x;
        int y;

        public Position (int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift (int x, int y) {
            return new Position(this.x + x, this.y + y);
        }
    }

    public static void drawWorld (TETile[][] t, int length, Position p) {
        for (int col = 0; col < 5; col ++) {
            int i = col <  3 ? col : 4 - col;
            int number = 3 + i;
            Position p0 = p.shift((2 * length - 1) * col, -length * i);
            drawRow(t, length, p0, number);
        }
    }

    public static void drawRow(TETile[][] t, int length, Position p, int number) {
        for (int i = 0; i < number; i++) {
            TETile ts = randomTile();
            Position p0 = p.shift(0, 2 * i * length);
            addHexagon(t, length, ts, p0);
        }
    }


    public static void addHexagon (TETile[][] t, int length, TETile ts, Position p) {
        for (int row = 0; row < 2 * length; row++) {
            int effectiveRow = row < length ? row : 2 * length - 1 - row;
            Position startRowPosition = p.shift(-effectiveRow, row);
            drawALine(t, startRowPosition, length + 2 * effectiveRow, ts);
        }
    }

    private static void drawALine(TETile[][] t, Position p, int width, TETile ts){
        // draw ts into tiles from startPoint with width in yPosition
        for (int i = p.x; i < p.x + width; i++) {
            t[i][p.y] = ts;
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            default: return Tileset.NOTHING;
        }
    }

    private static void fillWorldWithBlank(TETile[][] t) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT + 50; y += 1) {
                t[x][y] = Tileset.NOTHING;
            }
        }
    }

    public static void main(String[] args){
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        fillWorldWithBlank(tiles);
        int length = 4;
        Position p = new Position(10, 15);
        //addHexagon(randomTiles, 3, Tileset.WALL, p);
        //drawRow(tiles, length, p, 3);
        drawWorld(tiles, length, p);

        ter.renderFrame(tiles);
    }
}
