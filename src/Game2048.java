import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Game2048 extends JPanel {

    enum State {
        start, won, running, over
    }

    final Color[] colorTable = {
            new Color(0x701710), new Color(0xFFE4C3), new Color(0xfff4d3),
            new Color(0xffdac3), new Color(0xe7b08e), new Color(0xe7bf8e),
            new Color(0xffc4c3), new Color(0xE7948e), new Color(0xbe7e56),
            new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710)};

    final static int target = 16384; //the original target for our brute force :(

    static int highest;
    static int score;

    private Color gridColor = new Color(0xBBADA0);
    private Color emptyColor = new Color(0xCDC1B4);
    private Color startColor = new Color(0xFFEBCD);

    private Random rand = new Random();


    private Tile[][] tiles;
    private Tile[][] tilesTemp;
    private int side = 4;
    private int sideTemp = 4;

    private State gamestate = State.start;
    private boolean checkingAvailableMoves;

    public Game2048() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(0xFAF8EF));
        setFont(new Font("SansSerif", Font.BOLD, 48));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startGame();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Random r = new Random();
                int i = 0;
                switch (e.getKeyCode()) {
                    case (KeyEvent.VK_SPACE): //press to activate brute force for first 6 turns
                        for(int item: heuristic(tiles)){
                            if (item == 1)
                                moveLeft();
                            if (item == 2)
                                moveUp();
                            if (item == 3)
                                moveRight();
                            if (item == 4)
                                moveDown();
                    }
                        break;
                    case(KeyEvent.VK_ESCAPE):
                        if(true) {
                            repaint();
                            gamestate = State.start;
                            startGame();
                            repaint();
                        }
//                        if(n == 5){
//                            moveLeft();
//                        }
//                        else
//                            moveRight();
                        break;
                    case(KeyEvent.VK_UP):
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        break;
                }
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g);
    }



    int[] heuristic(Tile[][] n) { //brute force heuristic
        movesAvailable();
        int moveset[] = {0, 0, 0, 0, 0};
        int testmoveset[] = {0, 0, 0, 0, 0};
        int maxValue = 0;
        int bf = 0;
        // while(movesAvailable()) { //while you can move, check the 6 next moves for which is the highest
        for (int a = 0; a < 5; a++) {
            testmoveset[0] = a;
            for (int b = 0; b < 5; b++) {
                testmoveset[1] = b;
                for (int c = 0; c < 5; c++) {
                    testmoveset[2] = c;
                    for (int d = 0; d < 5; d++) {
                        testmoveset[3] = d;
                        for (int e = 0; e < 5; e++) {
                            testmoveset[4] = e;
                            for (int item : testmoveset) {
                                if (bf == 224) { //the final state of the deterministic heuristic
                                    break;
                                }
                                if (item == 1)
                                    moveLeftTest();
                                if (item == 2)
                                    moveUpTest();
                                if (item == 3)
                                    moveRightTest();
                                if (item == 4)
                                    moveDownTest();
                                tilesTemp = n;
                                bf++;
                                System.out.println(bf);
                            }
                            if (highest > maxValue) {
                                maxValue = highest;
                                moveset = testmoveset;
                            }
                        }
                    }
                }
            }
        }
    //}

        tiles = n;
        return moveset;
    }


    //will give a score on equal valued tiles that are next to each other, equal to the value of that tile
    private int weighting(Tile[][] tilesscore){
        int downScore = 0,upScore = 0,rightScore = 0,leftScore = 0;
        for (int i = 0;i <= 3;i++) {
            if (tilesscore[0][i].getValue() == tilesscore[1][i].getValue()) {
                downScore += tilesscore[0][i].getValue();
                return 1;
            }
            else if(tilesscore[0][i] == null){ //try to make it handle null
                return 0;
            }
        }
        for (int i = 0;i < 3;i++) {
            if (tilesscore[1][i].getValue() == tilesscore[2][i].getValue()) {
                downScore += tilesscore[1][i].getValue();
            }
        }

        for (int i = 0;i < 3;i++) {
            if (tilesscore[2][i].getValue() == tilesscore[3][i].getValue()) {
                downScore += tilesscore[2][i].getValue();
            }
        }
        for (int i = 0;i < 3;i++) {
            if (tilesscore[i][0].getValue() == tilesscore[i][1].getValue()) {
                rightScore += tilesscore[i][0].getValue();
            }
        }
        for (int i = 0;i < 3;i++) {
            if (tilesscore[i][1].getValue() == tilesscore[i][2].getValue()) {
                rightScore += tilesscore[i][1].getValue();
            }
        }
        for (int i = 0;i < 3;i++) {
            if (tilesscore[i][2].getValue() == tilesscore[i][3].getValue()) {
                rightScore += tilesscore[i][2].getValue();
            }
        }



        if (downScore>rightScore){
            return 1; //movedown
        }
        else if (downScore<rightScore){
            return 2; //moveright
        }
        else{
            return 0; //movedown moveright
        }
    }




    void startGame() {
        if (gamestate != State.running) {
            score = 0;
            highest = 0;
            gamestate = State.running;
            tiles = new Tile[side][side];
            tilesTemp = new Tile[sideTemp][sideTemp];

            addTile();
            tilesTemp = tiles;
        }
    }

    void drawGrid(Graphics2D g) {
        g.setColor(gridColor);
        g.fillRoundRect(200, 100, 499, 499, 15, 15);

        if (gamestate == State.running) {

            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    if (tiles[r][c] == null) {
                        g.setColor(emptyColor);
                        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
                    } else {
                        drawTile(g, r, c);
                    }
                }
            }
        } else {
            g.setColor(startColor);
            g.fillRoundRect(215, 115, 469, 469, 7, 7);

            g.setColor(gridColor.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, 128));
            g.drawString("2048", 310, 270);

            g.setFont(new Font("SansSerif", Font.BOLD, 20));

            if (gamestate == State.won) {
                g.drawString("it made it!", 390, 350);

            } else if (gamestate == State.over)
                g.drawString("game over", 400, 350);

            g.setColor(gridColor);
            g.drawString("click to start", 330, 470);
            g.drawString("arrow keys to move, press space to activate the brute force heuristic", 200, 670);

        }
    }

    void drawTile(Graphics2D g, int r, int c) {
        int value = tiles[r][c].getValue();

        g.setColor(colorTable[(int) (Math.log(value) / Math.log(2)) + 1]);
        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
        String s = String.valueOf(value);

        g.setColor(value < 128 ? colorTable[0] : colorTable[1]);

        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int dec = fm.getDescent();

        int x = 215 + c * 121 + (106 - fm.stringWidth(s)) / 2;
        int y = 115 + r * 121 + (asc + (106 - (asc + dec)) / 2);

        g.drawString(s, x, y);
    }


    private void addTile() {
        int pos =  -1;//rand.nextInt(side * side);
        int row, col;
        do {
            pos = (pos + 1) % (side * side);
            row = pos / side;
            col = pos % side;
        } while (tiles[row][col] != null);

        int val = 2;
        tiles[row][col] = new Tile(val);
    }
/* It's failing because the tilesscore is getting the value null from the graph. Need to find a way to handle null values.
* Also fails because the if statement is false. Otherwise it does move work under specific conditions.
*
*/
    private void skip(){
        int position = -1;
    }

    //The moves in the real 2048, what you see on screen
    private boolean move(int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;

        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);

            int r = j / side;
            int c = j % side;

            if (tiles[r][c] == null)
                continue;

            int nextR = r + yIncr;
            int nextC = c + xIncr;

            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {

                Tile next = tiles[nextR][nextC];
                Tile curr = tiles[r][c];

                if (next == null) {

                    if (checkingAvailableMoves)
                        return true;

                    tiles[nextR][nextC] = curr;
                    tiles[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;

                } else if (next.canMergeWith(curr)) {

                    if (checkingAvailableMoves)
                        return true;

                    int value = next.mergeWith(curr);
                    if (value > highest)
                        highest = value;
                    score += value;
                    tiles[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }

        if (moved) {
            if (highest < target) {
                clearMerged();
                addTile();
                if (!movesAvailable()) {
                   // gamestate = State.over;
                }
            } else if (highest == target)
                gamestate = State.won;
        }

        return moved;
    }
    //The moves in the fake 2048, what you don't see, and what is tracked in the brute force heuristic, supposdely
    //Doesn't se
    private boolean moveTemp(int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;

        for (int i = 0; i < sideTemp * sideTemp; i++) {
            int j = Math.abs(countDownFrom - i);

            int r = j / sideTemp;
            int c = j % sideTemp;

            if (tilesTemp[r][c] == null)
                continue;

            int nextR = r + yIncr;
            int nextC = c + xIncr;

            while (nextR >= 0 && nextR < sideTemp && nextC >= 0 && nextC < sideTemp) {

                Tile next = tilesTemp[nextR][nextC];
                Tile curr = tilesTemp[r][c];

                if (next == null) {

                    if (checkingAvailableMoves)
                        return true;

                    tilesTemp[nextR][nextC] = curr;
                    tilesTemp[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;

                } else if (next.canMergeWith(curr)) {

                    if (checkingAvailableMoves)
                        return true;

                    int value = next.mergeWith(curr);
                    if (value > highest)
                        highest = value;
                    score += value;
                    tilesTemp[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }

        if (highest < target) {
            clearMerged();
            addTile();
            if (!movesAvailable()) {
                 gamestate = State.over;
                 return moved;
            }
        }



        return moved;
    }



    boolean moveUp() {
        return move(0, -1, 0);
    }

    boolean moveDown() {
        return move(side * side - 1, 1, 0);
    }

    boolean moveLeft() {
        return move(0, 0, -1);
    }

    boolean moveRight() {
        return move(side * side - 1, 0, 1);
    }

    boolean moveUpTest() {
        return moveTemp(0, -1, 0);
    }

    boolean moveDownTest() {
        return moveTemp(side * side - 1, 1, 0);
    }

    boolean moveLeftTest() {
        return moveTemp(0, 0, -1);
    }

    boolean moveRightTest() {
        return moveTemp(side * side - 1, 0, 1);
    }

    void clearMerged() {
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile != null)
                    tile.setMerged(false);
    }

    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("2048");
            f.setResizable(true);
            f.add(new Game2048(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

        });
    }
}

class Tile {
    private boolean merged;
    private int value;

    Tile(int val) {
        value = val;
    }

    int getValue() {
        return value;
    }

    void setMerged(boolean m) {
        merged = m;
    }

    boolean canMergeWith(Tile other) {
        return !merged && other != null && !other.merged && value == other.getValue();
    }

    int mergeWith(Tile other) {
        if (canMergeWith(other)) {
            value *= 2;
            merged = true;
            return value;
        }
        return -1;
    }
}