import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Move implements Comparable<Move>
{
	private Cell cell;
	private boolean five, straightFour;
	private ArrayList<HashSet<Cell>> costSquares, restSquares;
	private int occurrence;
	
	public Move(Cell cell)
	{
		this.cell = cell;
		five = false;
		straightFour = false;
		costSquares = new ArrayList<HashSet<Cell>>();
		restSquares = new ArrayList<HashSet<Cell>>();
		occurrence = 0;
	}
	
	public void generateCostRest()
	{
		if(cell.getNode().getTurn() == Board.BLACK_TURN)
		{
			cell.setType(Stone.BLACK.type());
			cell.getNode().getBlackStones().add(cell);
			for(Cell stone: cell.getNode().getBlackStones())
			{
				if(!five)
					five = stone.five();
				if(!straightFour)
					straightFour = stone.straightFours();
				if(!five && !straightFour)
				{
					stone.fours(costSquares, restSquares);
					stone.threes(costSquares, restSquares);
					stone.brokenThrees(costSquares, restSquares);
				}
			}
			for(HashSet<Cell> cost: restSquares)
				cost.remove(cell);
			cell.getNode().getBlackStones().remove(cell);
			cell.setType(Stone.EMPTY.type());
		}
		else if(cell.getNode().getTurn() == Board.WHITE_TURN)
		{
			cell.setType(Stone.WHITE.type());
			cell.getNode().getWhiteStones().add(cell);
			for(Cell stone: cell.getNode().getWhiteStones())
			{
				if(!five)
					five = stone.five();
				if(!straightFour)
					straightFour = stone.straightFours();
				if(!five && !straightFour)
				{
					stone.fours(costSquares, restSquares);
					stone.threes(costSquares, restSquares);
					stone.brokenThrees(costSquares, restSquares);
				}
			}
			for(HashSet<Cell> cost: restSquares)
				cost.remove(cell);
			cell.getNode().getWhiteStones().remove(cell);
			cell.setType(Stone.EMPTY.type());
		}
	}
	
	public Cell getCell()
	{
		return cell;
	}
	
	public boolean isFive()
	{
		return five;
	}
	
	public boolean isStraightFour()
	{
		return straightFour;
	}

	public ArrayList<HashSet<Cell>> getCostSquares()
	{
		return costSquares;
	}
	
	public ArrayList<HashSet<Cell>> getRestSquares()
	{
		return restSquares;
	}
	
	public int getOccurrence()
	{
		return occurrence;
	}

	public void setOccurrence(int occurrence)
	{
		this.occurrence = occurrence;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(cell.getRow(), cell.getCol());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Move other = (Move)obj;
		return cell.equals(other.getCell());
	}
	
	@Override
	public String toString()
	{
		return cell.getRow()+"x"+cell.getCol();
	}

	@Override
	public int compareTo(Move o)
	{
		return o.getOccurrence() - occurrence;
	}
}
