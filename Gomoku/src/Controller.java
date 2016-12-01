
public class Controller
{
	public void compBlack()
	{
		AIWorker.setType(Stone.BLACK.type());
	}
	
	public void compWhite()
	{
		AIWorker.setType(Stone.WHITE.type());
	}
	
	public void setEasyDiff()
	{
		AIWorker.setDifficulty(GraphicRunner.EASY);
	}
	
	public void setMediumDiff()
	{
		AIWorker.setDifficulty(GraphicRunner.MEDIUM);
	}
	
	public void setHardDiff()
	{
		AIWorker.setDifficulty(GraphicRunner.HARD);
	}
	
	public void start()
	{
		GraphicRunner.start();
	}
}
