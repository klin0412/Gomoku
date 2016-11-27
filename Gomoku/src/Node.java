import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Node implements Comparable<Node>
{
	private Cell[][] grid;
	private ArrayList<Cell> blackStones, whiteStones;
	private int turn;
	private Move move;
	private ArrayList<Move> fiveSquares, fourSquares, gainSquares;
	private int value;
	private int occurrence;
	private ArrayList<Node> children;
	private int radius = 1;
	private int zobristKey;
	
	public Node(Cell[][] grid, ArrayList<Cell> blackStones, ArrayList<Cell> whiteStones, int turn, Move move)
	{
		this.grid = grid;
		for(Cell[] row: grid)
			for(Cell cell: row)
				cell.setNode(this);
		this.blackStones = blackStones;
		this.whiteStones = whiteStones;
		this.turn = turn;
		this.move = move;
		fiveSquares = new ArrayList<Move>();
		fourSquares = new ArrayList<Move>();
		gainSquares = new ArrayList<Move>();
		value = 0;
		occurrence = 0;
		children = new ArrayList<Node>();
		zobristKey = (int)hash();
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
	
	public int getTurn()
	{
		return turn%2;
	}
	
	public void setTurn(int turn)
	{
		this.turn = turn;
	}

	public Move getMove()
	{
		return move;
	}

	public ArrayList<Move> getFiveSquares()
	{
		return fiveSquares;
	}

	public ArrayList<Move> getFourSquares()
	{
		return fourSquares;
	}

	public ArrayList<Move> getGainSquares()
	{
		return gainSquares;
	}
	
	public int calcValue()
	{
		int score = 0;
		for(Cell stone: blackStones)
		{
			int size = 0;
			if(stone.five())
				score += Score.FIVE.value();
			if(stone.straightFours())
				score += Score.STRAIGHT_FOUR.value();
			size = stone.fours(null, null).size();
			score += Score.FOUR.value()*size;
			size = stone.threes(null, null).size();
			score += Score.THREE.value()*size;
			size = stone.doubles().size();
			score += Score.DOUBLE.value()*size;
		}
		for(Cell stone: whiteStones)
		{
			int size = 0;
			if(stone.five())
				score -= Score.FIVE.value();
			if(stone.straightFours())
				score -= Score.STRAIGHT_FOUR.value();
			size = stone.fours(null, null).size();
			score -= Score.FOUR.value()*size;
			size = stone.threes(null, null).size();
			score -= Score.THREE.value()*size;
			size = stone.doubles().size();
			score -= Score.DOUBLE.value()*size;
		}
		if(getTurn() == Board.BLACK_TURN)
			value = score;
		else if(getTurn() == Board.WHITE_TURN)
			value = -score;
		return value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public void setValue(int value)
	{
		this.value = value;
	}

	public int getOccurrence()
	{
		return occurrence;
	}
	
	public void setOccurrence(int occurrence)
	{
		this.occurrence = occurrence;
	}

	public ArrayList<Node> getChildren()
	{
		return children;
	}
	
	public void generateChildren(HashMap<Integer, Integer> history)
	{
		int topBound = -1;
		int bottomBound = Board.BOARD_SIZE;
		int leftBound = -1;
		int rightBound = Board.BOARD_SIZE;
		for(int row = 0; row < Board.BOARD_SIZE; row++)
		{
			for(int col = 0; col < Board.BOARD_SIZE; col++)
				if(topBound == -1 && grid[row][col].getType() != Stone.EMPTY.type())
					topBound = row;
			for(int col = Board.BOARD_SIZE-1; col >= 0; col--)
				if(bottomBound == Board.BOARD_SIZE && grid[row][col].getType() != Stone.EMPTY.type())
					bottomBound = row;
			if(topBound != -1 && bottomBound != Board.BOARD_SIZE)
				break;
		}
		for(int col = 0; col < Board.BOARD_SIZE; col++)
		{
			for(int row = 0; row < Board.BOARD_SIZE; row++)
				if(leftBound == -1 && grid[row][col].getType() != Stone.EMPTY.type())
					leftBound = col;
			for(int row = Board.BOARD_SIZE-1; row >= 0; row--)
				if(rightBound == Board.BOARD_SIZE && grid[row][col].getType() != Stone.EMPTY.type())
					rightBound = col;
			if(leftBound != -1 && rightBound != Board.BOARD_SIZE)
				break;
		}
		for(int row = topBound-radius; row <= bottomBound+radius; row++)
		{
			for(int col = leftBound-radius; col <= rightBound+radius; col++)
			{
				try
				{
					if(grid[row][col].getType() == Stone.EMPTY.type())
					{
						Node child = nextNode(new Move(grid[row][col]));
						children.add(child);
						Integer occurrence = history.get(child.getZobristKey());
						if(occurrence != null)
							child.setOccurrence(occurrence);
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {continue;}
			}
		}
		children.sort((n1, n2) -> n1.compareTo(n2));
	}
	
	public int getZobristKey()
	{
		return zobristKey;
	}

	private long hash()
	{
		long h = 0;
		long[][][] zobrist = Board.getInstance().getZobrist();
		for(int row = 0; row < zobrist.length; row++)
		{
			for(int col = 0; col < zobrist[row].length; col++)
			{
				int depth = grid[row][col].getType();
				h = h ^ zobrist[row][col][depth];
			}
		}
		return h;
	}

	public void initGainSquares()
	{
		for(int row = 0; row < Board.BOARD_SIZE; row++)
		{
			for(int col = 0; col < Board.BOARD_SIZE; col++)
			{
				if(grid[row][col].getType() == Stone.EMPTY.type())
				{
					Move move = new Move(grid[row][col]);
					move.generateCostRest();
					if(move.isFive())
						fiveSquares.add(move);
					else if(move.isStraightFour())
						fourSquares.add(move);
					else if(move.getCostSquares().size() != 0)
						gainSquares.add(move);
				}
			}
		}
	}
	
	public Node nextNode(Move move)
	{
		Cell[][] gridCopy = new Cell[grid.length][grid[0].length];
		for(int row = 0; row < grid.length; row++)
			for(int col = 0; col < grid[row].length; col++)
				gridCopy[row][col] = grid[row][col].copy();
		ArrayList<Cell> blackStonesCopy = new ArrayList<Cell>();
		ArrayList<Cell> whiteStonesCopy = new ArrayList<Cell>();
		for(Cell cell: blackStones)
			blackStonesCopy.add(gridCopy[cell.getRow()][cell.getCol()]);
		for(Cell cell: whiteStones)
			whiteStonesCopy.add(gridCopy[cell.getRow()][cell.getCol()]);
		if(getTurn() == Board.BLACK_TURN)
		{
			gridCopy[move.getCell().getRow()][move.getCell().getCol()].setType(Stone.BLACK.type());
			blackStonesCopy.add(gridCopy[move.getCell().getRow()][move.getCell().getCol()]);
		}
		else if(getTurn() == Board.WHITE_TURN)
		{
			gridCopy[move.getCell().getRow()][move.getCell().getCol()].setType(Stone.WHITE.type());
			whiteStonesCopy.add(gridCopy[move.getCell().getRow()][move.getCell().getCol()]);
		}
		return new Node(gridCopy, blackStonesCopy, whiteStonesCopy, turn+1, move);
	}
	
	public Node nextRespondedNode(Move move)
	{
		Node nextNode = nextNode(move);
		if(nextNode.getTurn() == Board.BLACK_TURN)
		{
			for(Cell costSquare: move.getCostSquares().get(0))
			{
				nextNode.getGrid()[costSquare.getRow()][costSquare.getCol()].setType(Stone.BLACK.type());
				nextNode.getBlackStones().add(nextNode.getGrid()[costSquare.getRow()][costSquare.getCol()]);
			}
		}
		else if(nextNode.getTurn() == Board.WHITE_TURN)
		{
			for(Cell costSquare: move.getCostSquares().get(0))
			{
				nextNode.getGrid()[costSquare.getRow()][costSquare.getCol()].setType(Stone.WHITE.type());
				nextNode.getWhiteStones().add(nextNode.getGrid()[costSquare.getRow()][costSquare.getCol()]);
			}
		}
		nextNode.setTurn(turn+2);
		return nextNode;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Node other = (Node)obj;
		return toString().equals(other.toString());
	}
	
	@Override
	public String toString()
	{
		String str = "";
		for(Cell[] row: grid)
		{
			str += Arrays.toString(row);
			str += "\n";
		}
		return str;
	}

	@Override
	public int compareTo(Node o)
	{
		return o.getOccurrence() - occurrence;
	}
}
