import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Node implements Comparable<Node>
{
	private static HashSet<Cell> cellToExamine = new HashSet<Cell>();
	private static int radius = 1;
	private Cell[][] grid;
	private ArrayList<Cell> blackStones, whiteStones;
	private int turn;
	private Move move;
	private ArrayList<Move> fiveSquares, fourSquares, gainSquares;
	private ArrayList<HashSet<Cell>> costSquares, restSquares;
	private int value;
	private Node parent;
	private ArrayList<Node> children;
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
		costSquares = new ArrayList<HashSet<Cell>>();
		restSquares = new ArrayList<HashSet<Cell>>();
		value = calcValue();
		parent = null;
		children = new ArrayList<Node>();
		zobristKey = (int)hash();
	}
	
	public static HashSet<Cell> getCellToExamine()
	{
		return cellToExamine;
	}

	public static int getRadius()
	{
		return radius;
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
	
	private int calcValue()
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
			size = stone.brokenThrees(null, null).size();
			score += Score.BROKEN_THREE.value()*size;
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
			size = stone.threes(null, null).size() + stone.brokenThrees(null, null).size();
			score -= Score.THREE.value()*size;
			size = stone.brokenThrees(null, null).size();
			score -= Score.BROKEN_THREE.value()*size;
			size = stone.doubles().size();
			score -= Score.DOUBLE.value()*size;
		}
		if(getTurn() == Board.BLACK_TURN)
			value = -score;
		else if(getTurn() == Board.WHITE_TURN)
			value = score;
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

	public ArrayList<Node> getChildren()
	{
		return children;
	}
	
	public void generateChildren()
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
				}
			}
		}
		if(fiveSquares.size() > 0)
		{
			Node child = nextNode(fiveSquares.get(0));
			children.add(child);
			child.setParent(this);
		}
		else if(fourThreat())
		{
			HashSet<Cell> forcedMoves = new HashSet<Cell>();
			for(HashSet<Cell> cost: costSquares)
				for(Cell costSquare: cost)
					forcedMoves.add(costSquare);
			for(Cell cell: forcedMoves)
			{
				Node child = nextNode(new Move(cell));
				children.add(child);
				child.setParent(this);
			}
		}
		else if(fourSquares.size() > 0)
		{
			Node child = nextNode(fourSquares.get(0));
			children.add(child);
			child.setParent(this);
		}
		else if(threeThreat())
		{
			HashSet<Cell> forcedMoves = new HashSet<Cell>();
			for(HashSet<Cell> cost: costSquares)
				for(Cell costSquare: cost)
					forcedMoves.add(costSquare);
			for(Cell cell: forcedMoves)
			{
				Node child = nextNode(new Move(cell));
				children.add(child);
				child.setParent(this);
			}
		}
		else
		{
			HashSet<Cell> cellToExamine = new HashSet<Cell>(Node.getCellToExamine());
			if(move != null)
			{
				for(int row = -radius; row <= radius; row++)
				{
					for(int col = -radius; col <= radius; col++)
					{
						if(row == 0 && col == 0)
							continue;
						try
						{
							Cell cell = grid[move.getCell().getRow()][move.getCell().getCol()];
							if(cell.getType() == Stone.EMPTY.type())
								cellToExamine.add(cell);
						}
						catch(ArrayIndexOutOfBoundsException e) {continue;}
					}
				}
			}
			for(Cell cell: cellToExamine)
			{
				Node child = nextNode(new Move(cell));
				children.add(child);
				child.setParent(this);
			}
		}
		children.sort((n1, n2) -> n1.compareTo(n2));
	}
	
	public Node getParent()
	{
		return parent;
	}

	public void setParent(Node parent)
	{
		this.parent = parent;
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
	
	public boolean opponentThreat()
	{
		return fourThreat() || threeThreat();
	}
	
	private boolean fourThreat()
	{
		int threatCount = 0;
		if(getTurn() == Board.BLACK_TURN)
		{
			for(Cell stone: whiteStones)
			{
				if(stone.five())
					threatCount++;
				if(stone.straightFours())
					threatCount++;
				threatCount += stone.fours(costSquares, restSquares).size();
			}
		}
		else
		{
			for(Cell stone: blackStones)
			{
				if(stone.five())
					threatCount++;
				if(stone.straightFours())
					threatCount++;
				threatCount += stone.fours(costSquares, restSquares).size();
			}
		}
		return threatCount > 0;
	}
	
	private boolean threeThreat()
	{
		int threatCount = 0;
		if(getTurn() == Board.BLACK_TURN)
		{
			for(Cell stone: whiteStones)
			{
				threatCount += stone.threes(costSquares, restSquares).size();
				threatCount += stone.brokenThrees(costSquares, restSquares).size();
			}
		}
		else
		{
			for(Cell stone: blackStones)
			{
				threatCount += stone.threes(costSquares, restSquares).size();
				threatCount += stone.brokenThrees(costSquares, restSquares).size();
			}
		}
		return threatCount > 0;
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
		//return move+": "+value;
	}

	@Override
	public int compareTo(Node o)
	{
		return o.getValue() - value;
	}
}
