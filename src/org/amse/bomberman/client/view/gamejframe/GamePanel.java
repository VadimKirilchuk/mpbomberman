package org.amse.bomberman.client.view.gamejframe;

import org.amse.bomberman.client.models.gamemodel.GameMap;
import org.amse.bomberman.client.models.gamemodel.Cell;
import org.amse.bomberman.client.models.gamemodel.impl.GameMapModel;
import org.amse.bomberman.util.Constants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.amse.bomberman.client.models.gamemodel.impl.PlayerModel;
import org.amse.bomberman.util.ImageUtilities;


/**
 *
 * @author Mikhail Korovkin
 */
@SuppressWarnings("serial")
public class GamePanel  extends JPanel{
    private GameMap map;
    private List<Cell> changes;

    public static final int CELL_SIZE = 48;
    // amount of cells at the one line on the Screen
    private int range;
    public static final int DEFAULT_RANGE = 10;

    private int height;
    private int width;
    // amount of cells at one line in the Full map
    private int size;
    // Cell that is the most Left and Up at the screen
    private Cell LUCell = new Cell(0, 0);
    // Cell that is the most Right and Down at the screen
    private Cell RDCell = new Cell(DEFAULT_RANGE-1, DEFAULT_RANGE-1);
    private Cell myCoord;

    private final int step = 4;
    private boolean isFirst = true;
    private BufferedImage buffer
            = new BufferedImage(CELL_SIZE, CELL_SIZE, BufferedImage.TYPE_INT_ARGB);

    private static final Color EMPTY_COLOR   = Color.LIGHT_GRAY;
    private static final Color PL_EXPL_COLOR = Color.RED;

    private static final String iconPathPrefix = "org/amse/bomberman/client/icons/";
    private static final String BOMB_ICON_PATH = iconPathPrefix + "bomb-48.png";
    private static final String WALL_ICON_PATH = iconPathPrefix + "wall-wood-48.png";
    private static final String PL1_ICON_PATH  = iconPathPrefix + "superman-48-1.png";
    private static final String PL2_ICON_PATH  = iconPathPrefix + "superman-48-2.png";
    private static final String PL3_ICON_PATH  = iconPathPrefix + "superman-48-3.png";
    private static final String PL4_ICON_PATH  = iconPathPrefix + "superman-48-4.png";
    private static final String BURN_ICON_PATH = iconPathPrefix + "burn-48.png";
    private static final String LIFE_ICON_PATH = iconPathPrefix + "heart-48.png";

    private static final String B_RADIUS_ICON_PATH = iconPathPrefix + "b_radius-48.png";
    private static final String B_COUNT_ICON_PATH = iconPathPrefix + "b_count-48.png";

    private static ImageIcon ICON_PL1  = ImageUtilities.loadIcon(PL1_ICON_PATH);
    private static ImageIcon ICON_PL2  = ImageUtilities.loadIcon(PL2_ICON_PATH);
    private static ImageIcon ICON_PL3  = ImageUtilities.loadIcon(PL3_ICON_PATH);
    private static ImageIcon ICON_PL4  = ImageUtilities.loadIcon(PL4_ICON_PATH);

    private static ImageIcon ICON_WALL = ImageUtilities.loadIcon(WALL_ICON_PATH);
    private static ImageIcon ICON_BOMB = ImageUtilities.loadIcon(BOMB_ICON_PATH);
    private static ImageIcon ICON_BURN = ImageUtilities.loadIcon(BURN_ICON_PATH);

    private static ImageIcon ICON_BONUS_LIFE     = ImageUtilities.loadIcon(LIFE_ICON_PATH);
    private static ImageIcon ICON_BONUS_B_RADIUS = ImageUtilities.loadIcon(B_RADIUS_ICON_PATH);
    private static ImageIcon ICON_BONUS_B_COUNT  = ImageUtilities.loadIcon(B_COUNT_ICON_PATH);

    public GamePanel() {
        range = DEFAULT_RANGE;
        width = range * CELL_SIZE;
        height = range * CELL_SIZE;
        setPreferredSize(new Dimension(width, height));
    }

    public void updateGameMap(GameMapModel model) {
        GameMap newMap = model.getMap();
        // if map was not received yet.
        if (newMap == null) {
            return;
        }
        // if this is first invocation after receiving of map
        if (map == null) {
            map = newMap;
            changes = new ArrayList<Cell>();
            size = map.getSize();
            if (size < DEFAULT_RANGE) {
                range = size;
            } else {
                range = DEFAULT_RANGE;
            }
            width = range * CELL_SIZE;
            height = range * CELL_SIZE;
            setPreferredSize(new Dimension(width, height));            
        }
        changes = model.getChanges();
        this.repaint();
    }

    public void updatePlayer(PlayerModel model) {
        myCoord = model.getPlayer().getCoord();
        int x = myCoord.getX();
        int y = myCoord.getY();
        // bad hack with scroll sizes
        if ((x - step < LUCell.getX() && LUCell.getX() > 0)
            || (x+1+step > RDCell.getX() && RDCell.getX() < size-1)
            || (y-step < LUCell.getY() && LUCell.getY() > 0)
            || (y+1+step > RDCell.getY() && RDCell.getY() < size-1))  {
            this.findEyeShot();
        }
        this.repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        if (isFirst) {
            if (map == null) {
                return;
            }
            List<Cell> expl = map.getExplosions();
            for (int i = LUCell.getX(); i <= RDCell.getX(); i++) {
                for (int j = LUCell.getY(); j <= RDCell.getY(); j++) {
                    Cell c = parseToMyCoord(new Cell(i,j));
                    graphics.drawImage(drawNotExpl(buffer,new Cell(i,j))
                            , c.getY()*CELL_SIZE, c.getX()*CELL_SIZE, this);
                }
            }
            for (Cell cell:expl) {
                Cell myCell = parseToMyCoord(cell);
                graphics.drawImage(drawExplosion(buffer,cell), CELL_SIZE*myCell.getY()
                                , CELL_SIZE * myCell.getX(), this);
            }
            //isFirst = false;
            return;
        }
        /*if (changes != null) {
            List<Cell> expl = map.getExplosions();
            for (Cell cell : changes) {
                Cell c = parseToMyCoord(cell);
                if (isInEyeShot(cell) && expl.contains(cell)) {
                    graphics.drawImage(drawExplosion(buffer,cell), c.getY()*cellSize
                                , c.getX()*cellSize, this);
                } else {
                    if (isInEyeShot(cell) && !expl.contains(cell)) {
                        graphics.drawImage(drawNotExpl(buffer,cell), c.getY()*cellSize
                                , c.getX()*cellSize, this);
                    }
                }
            }
        }*/
    }

    private Image getImage(Cell cell) {
        int key = map.getValue(cell);
        Image icon = null;
        switch (key) {
            case Constants.MAP_EMPTY: {
                icon = null;
                break;
            }
            case Constants.MAP_BOMB: {
                icon = ICON_BOMB.getImage();
                break;
            }
            case Constants.MAP_DETONATED_BOMB: {
                icon = ICON_BURN.getImage();
                break;
            }
            case Constants.MAP_BONUS_LIFE: {
                icon = ICON_BONUS_LIFE.getImage();
                break;
            }
            case Constants.MAP_BONUS_BOMB_COUNT: {
                icon = ICON_BONUS_B_COUNT.getImage();
                break;
            }
            case Constants.MAP_BONUS_BOMB_RADIUS: {
                icon = ICON_BONUS_B_RADIUS.getImage();
                break;
            }
        }
        if (key < Constants.MAP_EMPTY && key >= Constants.MAP_PROOF_WALL) {
            icon = ICON_WALL.getImage();
        } else {
            if (key > Constants.MAP_EMPTY && key <= Constants.MAX_PLAYERS) {
                icon = this.getPlayerIcon(key);
            }
        }
        return icon;
    }

    private Cell parseToRealCoord(Cell myCell) {
        Cell result;
        int x = myCell.getX() + LUCell.getX();
        int y = myCell.getY() + LUCell.getY();
        result = new Cell(x,y);
        return result;
    }

    private Cell parseToMyCoord(Cell realCell) {
        Cell result;
        int x = realCell.getX() - LUCell.getX();
        int y = realCell.getY() - LUCell.getY();
        result = new Cell(x,y);
        return result;
    }

    private void findEyeShot() {
        if (range >= size) {
            LUCell = new Cell(0,0);
            RDCell = new Cell(size-1, size-1);
        } else {
            int x1;
            int x2;
            int y1;
            int y2;
            // if player is on the left border of map
            if (myCoord.getX() <= (range-1)/2) {
                x1 = 0;
            } else {
                // if player is on the right border of map
                if (myCoord.getX() >=(size-1)-(range-1)/2) {
                    x1 = size - range;
                } else {
                    x1 = myCoord.getX() - (range-1)/2;
                }
            }
            if (myCoord.getY() <= (range-1)/2) {
                y1 = 0;
            } else {
                if (myCoord.getY() >= (size-1)-(range-1)/2) {
                    y1 = size - range;
                } else {
                    y1 = myCoord.getY() - (range-1)/2;
                }
            }
            x2 = x1 + range - 1;
            y2 = y1 + range - 1;
            LUCell = new Cell(x1,y1);
            RDCell = new Cell(x2, y2);
        }
    }

    private boolean isInEyeShot(Cell cell) {
        return ((cell.getX() >= LUCell.getX()) && (cell.getX() <= RDCell.getX())
                && (cell.getY() >= LUCell.getY()) && (cell.getY() <= RDCell.getY()));
    }

    private BufferedImage drawExplosion(BufferedImage image, Cell cell) {
        Image icon = null;
        Graphics2D g = image.createGraphics();
        g.setBackground(EMPTY_COLOR);
        g.clearRect(0, 0, CELL_SIZE, CELL_SIZE);
        int mapValue = map.getValue(cell);
        // if it is wall
        if (mapValue < Constants.MAP_EMPTY && mapValue
                >= Constants.MAP_PROOF_WALL) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 10 * 0.1f));
            g.drawImage(ICON_WALL.getImage(), 0, 0, this);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 9 * 0.1f));
            g.drawImage(ICON_BURN.getImage(), 0, 0, this);
        } else {
            // if it is player
            if (mapValue > Constants.MAP_EMPTY && mapValue
                    <= Constants.MAX_PLAYERS) {
                icon = getPlayerIcon(mapValue);
                g.setBackground(PL_EXPL_COLOR);
                g.clearRect(0, 0, CELL_SIZE, CELL_SIZE);
                g.drawImage(icon, 0, 0, this);
            } else {
                // others
                icon = ICON_BURN.getImage();
                g.drawImage(icon, 0, 0, this);
            }
        }
        return image;
    }

    private BufferedImage drawNotExpl(BufferedImage image, Cell cell) {
        Graphics2D g = image.createGraphics();
        g.setBackground(EMPTY_COLOR);
        g.clearRect(0, 0, CELL_SIZE, CELL_SIZE);
        // if it is player on the bomb
        if (map.getValue(cell) >= Constants.MAP_BOMB + 100 + 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 10 * 0.1f));
            int playerNum = map.getValue(cell)-100 - Constants.MAP_BOMB;
            g.drawImage(getPlayerIcon(playerNum), 0, 0, this);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 9 * 0.1f));
            g.drawImage(ICON_BOMB.getImage(), 0, 0, this);
        } else {
            Image icon = getImage(cell);
            if (icon != null) {
                g.drawImage(icon, 0, 0, this);
            } 
        }
        return image;
    }

    private Image getPlayerIcon(int mapValue) {
        switch (mapValue) {
            case 1:
                return ICON_PL1.getImage();
            case 2:
                return ICON_PL2.getImage();
            case 3:
                return ICON_PL3.getImage();
            case 4:
                return ICON_PL4.getImage();
            default:
                return ICON_PL1.getImage();
        }
    }
}
