
public class MapEntry
{
	//http://www.chessbin.com/post/Transposition-Table-and-Zobrist-Hashing
	
	public static final int EXACT = 0;
	public static final int LOWERBOUND = 1;
	public static final int UPPERBOUND = 2;
	
	private int zobristKey;
	private int depth;
	private int value;
	private int nodeType;
	
	public MapEntry(int zobristKey, int depth, int value, int nodeType)
	{
		this.zobristKey = zobristKey;
		this.depth = depth;
		this.value = value;
		this.nodeType = nodeType;
	}

	public int getZobristKey()
	{
		return zobristKey;
	}

	public int getDepth() {
		return depth;
	}

	public int getValue()
	{
		return value;
	}

	public int getNodeType()
	{
		return nodeType;
	}
}
