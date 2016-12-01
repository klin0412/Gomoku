import javafx.scene.canvas.*;

public class Tile extends Canvas
{
	private GraphicsContext gc = this.getGraphicsContext2D(); //gc to draw things, e.g. images, on canvas
	private int border = Board.BORDER;
	private int gridSize = Board.GRID_SIZE;
	private int size = 32;
	private int offset = gridSize-size; //looks fancy but this value kinda just worked :P
	private int row, col;
	private int x, y;
	private Cell cell;
	private int type;
	
	public Tile(int row, int col)
	{
		this.row = row;
		this.col = col;
		x = border-gridSize/2 + gridSize*col;
		y = border-gridSize/2 + gridSize*row; //center at, instead of start at, intersection
		this.setWidth(gridSize);
		this.setHeight(gridSize);
		this.setTranslateX(x);
		this.setTranslateY(y);
		type = Stone.EMPTY.type();
		
		this.setOnMouseEntered(e ->
		{
			if(Board.getInstance().isWon())
				return;
			if((Board.getInstance().getTurn() == Board.BLACK_TURN && AIWorker.getType() != Stone.BLACK.type()) ||
			   (Board.getInstance().getTurn() == Board.WHITE_TURN && AIWorker.getType() != Stone.WHITE.type()))
			{
				if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.BLACK_TURN)
				{
					gc.setGlobalAlpha(0.5); //draws translucent image
					gc.drawImage(Stone.BLACK.image(), offset, offset);
				}
				else if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.WHITE_TURN)
				{
					gc.setGlobalAlpha(0.5);
					gc.drawImage(Stone.WHITE.image(), offset, offset);
				}
			}
		});
		this.setOnMouseExited(e ->
		{
			if(type == Stone.EMPTY.type())
				gc.clearRect(offset, offset, size, size);
		});
		this.setOnMouseClicked(e ->
		{
			if(Board.getInstance().isWon())
				return;
			if((Board.getInstance().getTurn() == Board.BLACK_TURN && AIWorker.getType() != Stone.BLACK.type()) ||
			   (Board.getInstance().getTurn() == Board.WHITE_TURN && AIWorker.getType() != Stone.WHITE.type()))
			{
				if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.BLACK_TURN)
					setBlack();
				else if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.WHITE_TURN)
					setWhite();
			}
		});
	}
	
	public void setBlack()
	{
		gc.setGlobalAlpha(1.0); //draws solid image
		gc.drawImage(Stone.BLACK.image(), offset, offset);
		type = Stone.BLACK.type();
		cell.setType(Stone.BLACK.type());
		Board.getInstance().getBlackStones().add(cell);
		Cell[][] grid = Board.getInstance().getGrid();
		for(int i = -Node.getRadius(); i <= Node.getRadius(); i++)
		{
			for(int j = -Node.getRadius(); j <= Node.getRadius(); j++)
			{
				try
				{
					Cell cell = grid[row+i][col+j];
					if(cell.getType() == Stone.EMPTY.type())
						Node.getCellToExamine().add(cell);
				}
				catch(ArrayIndexOutOfBoundsException ex) {continue;}
			}
		}
		Node.getCellToExamine().remove(cell);
		Board.getInstance().nextTurn(); //next turn, so AI work as white
		int five = Board.getInstance().toNode().five();
		if(five != Stone.EMPTY.type())
		{
			Board.getInstance().setWon(true);
			System.out.println("Player won!");
		}
		
		if(AIWorker.getType() == Stone.WHITE.type())
		{
			AIWorker worker = new AIWorker(Board.getInstance().toNode());
			worker.run();
		}
	}
	
	public void setWhite()
	{
		gc.setGlobalAlpha(1.0);
		gc.drawImage(Stone.WHITE.image(), offset, offset);
		type = Stone.WHITE.type();
		cell.setType(Stone.WHITE.type());
		Board.getInstance().getWhiteStones().add(cell);
		Cell[][] grid = Board.getInstance().getGrid();
		for(int i = -Node.getRadius(); i <= Node.getRadius(); i++)
		{
			for(int j = -Node.getRadius(); j <= Node.getRadius(); j++)
			{
				try
				{
					Cell cell = grid[row+i][col+j];
					if(cell.getType() == Stone.EMPTY.type())
						Node.getCellToExamine().add(cell);
				}
				catch(ArrayIndexOutOfBoundsException ex) {continue;}
			}
		}
		Node.getCellToExamine().remove(cell);
		Board.getInstance().nextTurn(); //next turn, so AI work as black
		int five = Board.getInstance().toNode().five();
		if(five != Stone.EMPTY.type())
		{
			Board.getInstance().setWon(true);
			System.out.println("Player won!");
		}
		
		if(AIWorker.getType() == Stone.BLACK.type())
		{
			AIWorker worker = new AIWorker(Board.getInstance().toNode());
			worker.run();
		}
	}
	
	public GraphicsContext getGC()
	{
		return gc;
	}
	
	public int getRow()
	{
		return row;
	}
	
	public int getCol()
	{
		return col;
	}
	
	public Cell getCell()
	{
		return cell;
	}

	public void setCell(Cell cell)
	{
		this.cell = cell;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void clear()
	{
		gc.clearRect(0, 0, gridSize, gridSize);
		type = Stone.EMPTY.type();
	}
}
