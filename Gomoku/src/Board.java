import java.util.ArrayList;
import java.util.Random;

import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class Board extends BorderPane
{
	public static final int WIDTH = 535;
	public static final int HEIGHT = 535; //image of board 535x535 pixels
	public static final int BORDER = 21; //border of image 21 pixels
	public static final int GRID_SIZE = 35; //grid of image 35 pixels
	public static final int BOARD_SIZE = 15; //15x15 board
	public static final int BLACK_TURN = 0;
	public static final int WHITE_TURN = 1;
	
	private static final Board instance = new Board();
	private Tile[][] board; //tile: visual, displayed board
	private Cell[][] grid; //cell: board used to do work
	private ArrayList<Cell> blackStones, whiteStones; //ArrayList storing their respective stones
	private long[][][] zobrist; //pseudorandom bitstrings for zobrist hashing
	private int turn;
	private int dTurn; //increment, 1 is normal, 2 is only black or only white
	
	private Image goBoard = new Image("goBoard.jpg");
	
	public static Board getInstance()
	{
		return instance;
	}
	
	private Board()
	{
		initBoard();
	}
	
	public void initBoard()
	{
		Pane pane = new Pane(); //pane for the goban image
		this.setCenter(pane);
		
		Canvas goban = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = goban.getGraphicsContext2D();
		gc.drawImage(goBoard, 0, 0);
		pane.getChildren().add(goban); //adds canvas to pane which was added to center of this
		
		board = new Tile[BOARD_SIZE][BOARD_SIZE];
		grid = new Cell[BOARD_SIZE][BOARD_SIZE];
		for(int row = 0; row < board.length; row++)
		{
			for(int col = 0; col < board[row].length; col++)
			{
				Tile tile = new Tile(row, col); //tile for each row x col
				board[row][col] = tile;
				pane.getChildren().add(tile);
				Cell cell = new Cell(row, col); //cell for each row x col
				grid[row][col] = cell;
				tile.setCell(cell); //set tile's reference to cell
				cell.setTile(tile); //set cell's reference to tile
			}
		}
		blackStones = new ArrayList<Cell>();
		whiteStones = new ArrayList<Cell>();
		
		//https://en.wikipedia.org/wiki/Zobrist_hashing
		Random random = new Random();
		zobrist = new long[BOARD_SIZE][BOARD_SIZE][Stone.values().length];
		for(int row = 0; row < zobrist.length; row++)
			for(int col = 0; col < zobrist[row].length; col++)
				for(int depth = 0; depth < zobrist[row][col].length; depth++)
					zobrist[row][col][depth] = random.nextLong();
		
		turn = 0;
		dTurn = 1;
	}
	
	public Cell[][] getGrid()
	{
		return grid;
	}
	
	public ArrayList<Cell> getBlackStones()
	{
		return blackStones;
	}
	
	public ArrayList<Cell> getWhiteStones()
	{
		return whiteStones;
	}
	
	public long[][][] getZobrist()
	{
		return zobrist;
	}
	
	public int getTurn()
	{
		return turn%2;
	}
	
	public void setTurn(int turn)
	{
		this.turn = turn;
	}
	
	public void nextTurn()
	{
		turn += dTurn;
	}
	
	public void lastTurn()
	{
		turn -= dTurn;
	}
	
	public int getDTurn()
	{
		return dTurn;
	}
	
	public void setDTurn(int dTurn)
	{
		this.dTurn = dTurn;
	}
	
	public Node toNode()
	{
		Cell[][] gridCopy = new Cell[BOARD_SIZE][BOARD_SIZE];
		for(int row = 0; row < grid.length; row++)
			for(int col = 0; col < grid[row].length; col++)
				gridCopy[row][col] = grid[row][col].copy(); //copy over each cell from grid
		ArrayList<Cell> blackStonesCopy = new ArrayList<Cell>();
		ArrayList<Cell> whiteStonesCopy = new ArrayList<Cell>();
		for(Cell cell: blackStones)
			blackStonesCopy.add(gridCopy[cell.getRow()][cell.getCol()]); //copy over each cell from blackStones
		for(Cell cell: whiteStones)
			whiteStonesCopy.add(gridCopy[cell.getRow()][cell.getCol()]); //copy over each cell from whiteStones
		return new Node(gridCopy, blackStonesCopy, whiteStonesCopy, turn, null); //return a node with copies as parameters
	}
	
	public void back()
	{
		Cell lastCell;
		if(getTurn() == BLACK_TURN)
		{
			if(whiteStones.size() == 0)
				return;
			lastCell = whiteStones.remove(whiteStones.size()-1);
		}
		else
			lastCell = blackStones.remove(blackStones.size()-1);
		lastCell.getTile().clear();
		lastCell.setType(Stone.EMPTY.type());
		lastTurn();
		Move move = new Move(lastCell);
		System.out.println(move+" removed");
	}
}
