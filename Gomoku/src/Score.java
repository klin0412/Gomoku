
public enum Score
{
	DOUBLE(10),
	BROKEN_THREE(400),
	THREE(500),
	FOUR(2000),
	STRAIGHT_FOUR(6000),
	FIVE(1000000);
	
	private int value;
	
	private Score(int value)
	{
		this.value = value;
	}
	
	public int value()
	{
		return value;
	}
}
