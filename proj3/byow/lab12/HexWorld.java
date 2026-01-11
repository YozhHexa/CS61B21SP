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

    public static void drawHexagons (TETile[][] tiles, int length, int xPosition, int yPosition) {
        for (int col = 0; col < (2 * length - 1) * 5; col += 2 * length - 1) {
            int x = xPosition + col;
            int i = col < (2 * length - 1) * 3 ? col / (2 * length - 1) : 4 - col / (2 * length - 1);
            int number = 3 + i;
            drawALineHexagons(tiles, length, x, yPosition - length * i, number);
            if (x <= xPosition + (2 * length - 1) * 2) {
                drawALineHexagons(tiles, length, x, yPosition - length * ((x - xPosition) / (2 * length - 1)), 1);
            }
        }
    }

    public static void drawALineHexagons(TETile[][] tiles, int length, int xPosition, int yPosition, int number) {
        for (int i = 0; i < number; i++) {
            TETile ts = randomTile();
            drawASingleHexagon(tiles, length, ts, xPosition, yPosition + 2 * length * i);
        }
    }


    public static void drawASingleHexagon(TETile[][] tiles, int length, TETile ts, int xPosition, int yPosition){
        for (int row = 0; row < 2 * length; row++) {
            int y = row + yPosition;
            int effectiveRow = row < length ? row : 2 * length - 1 - row;
            drawALine(xPosition - effectiveRow, length + 2 * effectiveRow, y, tiles, ts);
        }
    }

    private static void drawALine(int startPoint, int width, int yPosition, TETile[][] tiles, TETile ts){
        // draw ts into tiles from startPoint with width in yPosition
        for (int i = startPoint; i < startPoint + width; i++) {
            tiles[i][yPosition] = ts;
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

    public static void main(String[] args){
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] randomTiles = new TETile[WIDTH][HEIGHT];
        int length = 3;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                randomTiles[x][y] = Tileset.NOTHING;
            }
        }
        drawHexagons(randomTiles, length, 10, 10);

        ter.renderFrame(randomTiles);
    }
}
