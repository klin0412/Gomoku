import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Cell
{
	private Node node;
	private int row, col;
	private Tile tile;
	private int type;

	public Cell(int row, int col)
	{
		this.row = row;
		this.col = col;
	}
	
	public Node getNode()
	{
		return node;
	}
	
	public void setNode(Node node)
	{
		this.node = node;
	}
	
	public int getRow()
	{
		return row;
	}

	public void setRow(int row)
	{
		this.row = row;
	}
	
	public int getCol()
	{
		return col;
	}

	public void setCol(int col)
	{
		this.col = col;
	}
	
	public Tile getTile()
	{
		return tile;
	}

	public void setTile(Tile tile)
	{
		this.tile = tile;
	}
	
	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
	
	public boolean five()
	{
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean formed = true;
				for(int i = 1; i < 5; i++)
				{
					if(grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != type)
					{
						formed = false;
						break;
					}
				}
				if(formed)
					return formed;
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return false;
	}
	
	public boolean straightFours()
	{
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean formed = true;
				boolean clearHeadTail = grid[row-direction.dRow()][col-direction.dCol()].getType() == Stone.EMPTY.type() &&
										grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() == Stone.EMPTY.type();
				if(clearHeadTail)
				{
					for(int i = 1; i < 4; i++)
					{
						if(grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != type)
						{
							formed = false;
							break;
						}
					}
				}
				else
					formed = false;
				if(formed)
					return formed;
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return false;
	}
	
	public ArrayList<Direction> fours(ArrayList<HashSet<Cell>> costSquares, ArrayList<HashSet<Cell>> restSquares)
	{
		ArrayList<Direction> fours = new ArrayList<Direction>();
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean formed = true;
				boolean headAtBorder = outOfBounds(row-direction.dRow()) || outOfBounds(col-direction.dCol());
				boolean tailAtBorder = outOfBounds(row+4*direction.dRow()) || outOfBounds(col+4*direction.dCol());
				boolean headBlocked = (headAtBorder ||
									  (grid[row-direction.dRow()][col-direction.dCol()].getType() != Stone.EMPTY.type() &&
									  grid[row-direction.dRow()][col-direction.dCol()].getType() != type)) &&
									  grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() == Stone.EMPTY.type();
				boolean tailBlocked = (tailAtBorder ||
									  (grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() != Stone.EMPTY.type() &&
									  grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() != type)) &&
									  grid[row-direction.dRow()][col-direction.dCol()].getType() == Stone.EMPTY.type();
				if(headBlocked || tailBlocked)
				{
					for(int i = 1; i < 4; i++)
					{
						if(grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != type)
						{
							formed = false;
							break;
						}
					}
				}
				else
					formed = false;
				if(formed)
				{
					fours.add(direction);
					if(costSquares != null && restSquares != null)
					{
						HashSet<Cell> cost = new HashSet<Cell>();
						HashSet<Cell> rest = new HashSet<Cell>();
						if(headBlocked)
							cost.add(grid[row+4*direction.dRow()][col+4*direction.dCol()]);
						else if(tailBlocked)
							cost.add(grid[row-direction.dRow()][col-direction.dCol()]);
						for(int i = 0; i < 4; i++)
							rest.add(grid[row+i*direction.dRow()][col+i*direction.dCol()]);
						costSquares.add(cost);
						restSquares.add(rest);
					}
				}
				
				boolean brokeOnTwo = true;
				boolean brokeOnThree = true;
				boolean brokeOnFour = true;
				if(grid[row+direction.dRow()][col+direction.dCol()].getType() != Stone.EMPTY.type())
					brokeOnTwo = false;
				if(grid[row+2*direction.dRow()][col+2*direction.dCol()].getType() != Stone.EMPTY.type())
					brokeOnThree = false;
				if(grid[row+3*direction.dRow()][col+3*direction.dCol()].getType() != Stone.EMPTY.type())
					brokeOnFour = false;
				if(brokeOnTwo || brokeOnThree || brokeOnFour)
				{
					for(int i = 1; i < 5; i++)
					{
						if(grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != type)
						{
							if(brokeOnTwo && i != 1)
								brokeOnTwo = false;
							if(brokeOnThree && i != 2)
								brokeOnThree = false;
							if(brokeOnFour && i != 3)
								brokeOnFour = false;
						}
					}
				}
				if(brokeOnTwo || brokeOnThree || brokeOnFour)
				{
					fours.add(direction);
					if(costSquares != null && restSquares != null)
					{
						HashSet<Cell> cost = new HashSet<Cell>();
						HashSet<Cell> rest = new HashSet<Cell>();
						for(int i = 0; i < 5; i++)
							rest.add(grid[row+i*direction.dRow()][col+i*direction.dCol()]);
						if(brokeOnTwo)
						{
							cost.add(grid[row+direction.dRow()][col+direction.dCol()]);
							rest.remove(grid[row+direction.dRow()][col+direction.dCol()]);
						}
						else if(brokeOnThree)
						{
							cost.add(grid[row+2*direction.dRow()][col+2*direction.dCol()]);
							rest.remove(grid[row+2*direction.dRow()][col+2*direction.dCol()]);
						}
						else if(brokeOnFour)
						{
							cost.add(grid[row+3*direction.dRow()][col+3*direction.dCol()]);
							rest.remove(grid[row+3*direction.dRow()][col+3*direction.dCol()]);
						}
						costSquares.add(cost);
						restSquares.add(rest);
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return fours;
	}
	
	public ArrayList<Direction> threes(ArrayList<HashSet<Cell>> costSquares, ArrayList<HashSet<Cell>> restSquares)
	{
		ArrayList<Direction> threes = new ArrayList<Direction>();
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean formed = true;
				boolean clearHeadTail = grid[row-2*direction.dRow()][col-2*direction.dCol()].getType() == Stone.EMPTY.type() ||
   										grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() == Stone.EMPTY.type();
				if(clearHeadTail &&
				   grid[row-direction.dRow()][col-direction.dCol()].getType() == Stone.EMPTY.type() &&
				   grid[row+3*direction.dRow()][col+3*direction.dCol()].getType() == Stone.EMPTY.type())
				{
					for(int i = 1; i < 3; i++)
					{
						if(grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != type)
						{
							formed = false;
							break;
						}
					}
				}
				else
					formed = false;
				if(formed)
				{
					threes.add(direction);
					if(costSquares != null && restSquares != null)
					{
						HashSet<Cell> cost = new HashSet<Cell>();
						HashSet<Cell> rest = new HashSet<Cell>();
						cost.add(grid[row-direction.dRow()][col-direction.dCol()]);
						cost.add(grid[row+3*direction.dRow()][col+3*direction.dCol()]);
						for(int i = 0; i < 3; i++)
							rest.add(grid[row+i*direction.dRow()][col+i*direction.dCol()]);
						costSquares.add(cost);
						restSquares.add(rest);
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return threes;
	}
	
	public ArrayList<Direction> brokenThrees(ArrayList<HashSet<Cell>> costSquares, ArrayList<HashSet<Cell>> restSquares)
	{
		ArrayList<Direction> brokenThrees = new ArrayList<Direction>();
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean clearHeadTail = grid[row-direction.dRow()][col-direction.dCol()].getType() == Stone.EMPTY.type() &&
										grid[row+3*direction.dRow()][col+3*direction.dCol()].getType() == type &&
										grid[row+4*direction.dRow()][col+4*direction.dCol()].getType() == Stone.EMPTY.type();
				boolean brokeOnTwo = clearHeadTail && grid[row+direction.dRow()][col+direction.dCol()].getType() == Stone.EMPTY.type() &&
									 grid[row+2*direction.dRow()][col+2*direction.dCol()].getType() == type;
				boolean brokeOnThree = clearHeadTail && grid[row+2*direction.dRow()][col+2*direction.dCol()].getType() == Stone.EMPTY.type() &&
									   grid[row+direction.dRow()][col+direction.dCol()].getType() == type;
				if(brokeOnTwo || brokeOnThree)
				{
					brokenThrees.add(direction);
					if(costSquares != null && restSquares != null)
					{
						HashSet<Cell> cost = new HashSet<Cell>();
						HashSet<Cell> rest = new HashSet<Cell>();
						cost.add(grid[row-direction.dRow()][col-direction.dCol()]);
						cost.add(grid[row+4*direction.dRow()][col+4*direction.dCol()]);
						for(int i = 0; i < 4; i++)
							rest.add(grid[row+i*direction.dRow()][col+i*direction.dCol()]);
						if(brokeOnTwo)
						{
							cost.add(grid[row+direction.dRow()][col+direction.dCol()]);
							rest.remove(grid[row+direction.dRow()][col+direction.dCol()]);
						}
						else if(brokeOnThree)
						{
							cost.add(grid[row+2*direction.dRow()][col+2*direction.dCol()]);
							rest.remove(grid[row+2*direction.dRow()][col+2*direction.dCol()]);
						}
						costSquares.add(cost);
						restSquares.add(rest);
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return brokenThrees;
	}
	
	public ArrayList<Direction> doubles()
	{
		ArrayList<Direction> doubles = new ArrayList<Direction>();
		Cell[][] grid = node.getGrid();
		for(Direction direction: Direction.values())
		{
			try
			{
				boolean formed = true;
				
				for(int i = -2; i <= 3; i++)
				{
					if(i == 0)
						continue;
					if(i == 1 && grid[row+direction.dRow()][col+direction.dCol()].getType() != type)
					{
						formed = false;
						break;
					}
					else if(i != 1 && grid[row+i*direction.dRow()][col+i*direction.dCol()].getType() != Stone.EMPTY.type())
					{
						formed = false;
						break;
					}
				}
				
				if(formed)
					doubles.add(direction);
			}
			catch(ArrayIndexOutOfBoundsException e) {continue;}
		}
		return doubles;
	}
	
	private boolean outOfBounds(int i)
	{
		if(i == -1 || i == Board.BOARD_SIZE)
			return true;
		else
			return false;
	}
	
	public Cell copy()
	{
		Cell cell = new Cell(row, col);
		cell.setTile(tile);
		cell.setType(type);
		return cell;
	}

	@Override
	public int hashCode()
	{
		int hashCode = Objects.hash(row, col);
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		Cell other = (Cell)obj;
		if(other.getRow() == row && other.getCol() == col)
			return true;
		else
			return false;
	}

	@Override
	public String toString()
	{
		if(type == Stone.BLACK.type())
			return "œ";
		else if(type == Stone.WHITE.type())
			return "›";
		else
			return " ";
	}
}
