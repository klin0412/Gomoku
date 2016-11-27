
public enum Direction
{
	E(1, 0),
	SE(1, -1),
	S(0, -1),
	SW(-1, -1);
	
	private int dRow, dCol;
	
	private Direction(int dRow, int dCol)
	{
		this.dRow = dRow;
		this.dCol = dCol;
	}
	
	public int dRow()
	{
		return dRow;
	}
	
	public int dCol()
	{
		return dCol;
	}
}
