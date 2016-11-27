import javafx.scene.image.Image;

public enum Stone
{
	EMPTY(0, null),
	BLACK(1, "blackStone.png"),
	WHITE(2, "whiteStone.png");
	
	private int type;
	private Image image;
	
	private Stone(int type, String filename)
	{
		this.type = type;
		if(filename != null)
			image = new Image(filename);
	}
	
	public int type()
	{
		return type;
	}
	
	public Image image()
	{
		return image;
	}
}
