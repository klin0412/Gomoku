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
		});
		this.setOnMouseExited(e ->
		{
			if(type == Stone.EMPTY.type())
				gc.clearRect(offset, offset, size, size);
		});
		this.setOnMouseClicked(e ->
		{
			if(type == Stone.EMPTY.type())
			{
				Move move = new Move(cell);
				System.out.println(move);
			}
			if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.BLACK_TURN)
			{
				gc.setGlobalAlpha(1.0); //draws solid image
				gc.drawImage(Stone.BLACK.image(), offset, offset);
				type = Stone.BLACK.type();
				cell.setType(Stone.BLACK.type());
				Board.getInstance().getBlackStones().add(cell);
				Board.getInstance().nextTurn(); //next turn, so AI work as white
				AIWorker solver = new AIWorker(Board.getInstance().toNode());
				//System.out.println(solver.getRootNode().calcValue());
				
				if(Board.getInstance().getDTurn() == 1)
				{
					int value = solver.iterativeDeepening();
					if(value > 0)
						for(Move move: solver.getThreatVariation())
							System.out.println("Play: "+move);
					//System.out.println(solver.bestMove());
					//System.out.println(solver.nodeExp);
				}
				
			}
			else if(type == Stone.EMPTY.type() && Board.getInstance().getTurn() == Board.WHITE_TURN)
			{
				gc.setGlobalAlpha(1.0);
				gc.drawImage(Stone.WHITE.image(), offset, offset);
				type = Stone.WHITE.type();
				cell.setType(Stone.WHITE.type());
				Board.getInstance().getWhiteStones().add(cell);
				Board.getInstance().nextTurn(); //next turn, so AI work as black
				AIWorker solver = new AIWorker(Board.getInstance().toNode());
				//System.out.println(solver.getRootNode().calcValue());
				
				if(Board.getInstance().getDTurn() == 1)
				{
					int value = solver.iterativeDeepening();
					if(value > 0)
						for(Move move: solver.getThreatVariation())
							System.out.println("Play: "+move);
					//System.out.println(solver.bestMove());
					//System.out.println(solver.nodeExp);
				}
			}
		});
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
	
	public void clear()
	{
		gc.clearRect(0, 0, gridSize, gridSize);
		type = Stone.EMPTY.type();
	}
}
