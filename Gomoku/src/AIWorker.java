import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class AIWorker
{
	private static final int SAFE = 0;
	private static final int RISK = 1;
	
	private static int maxDepth;
	private static int type = Stone.EMPTY.type();
	private Node rootNode;
	private HashMap<Integer, MapEntry> transpositionTable;
	private ArrayList<Move> threatVariation;
	private Stack<Move> principalVariation;
	private int startTime, maxTime;

	public AIWorker(Node rootNode)
	{
		this.rootNode = rootNode;
		rootNode.initGainSquares();
		transpositionTable = new HashMap<Integer, MapEntry>();
		threatVariation = new ArrayList<Move>();
		principalVariation = new Stack<Move>();
		maxTime = 10000;
	}
	
	public static void setDifficulty(int difficulty)
	{
		maxDepth = difficulty;
	}

	public static int getType()
	{
		return type;
	}

	public static void setType(int type)
	{
		AIWorker.type = type;
	}

	public Node getRootNode()
	{
		return rootNode;
	}
	
	public ArrayList<Move> getThreatVariation()
	{
		return threatVariation;
	}

	public Move bestMove(int value)
	{
		if(principalVariation.size() == 1)
			return principalVariation.pop();
		for(Node child: rootNode.getChildren())
			if(child.getValue() == value)
				return child.getMove();
		Board.getInstance().nextTurn();
		Node node = Board.getInstance().toNode();
		Board.getInstance().lastTurn();
		AIWorker worker = new AIWorker(node);
		if(worker.threatSpace(node, RISK) != 0)
			return worker.getThreatVariation().get(0);
		else
		{
			worker.getRootNode().generateChildren();
			return worker.getRootNode().getChildren().get(0).getMove();
		}
	}
	
	//Threat Space Search: http://vanilla47.com/PDFs/Gomoku%20Renju%20Pente/go-moku-and-threat.pdf
	//Comprehensive search techniques: http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
	//Gomoku specific: http://isites.harvard.edu/fs/docs/icb.topic707165.files/pdfs/Kulev_Wu.pdf
	public int iterativeDeepening()
	{
		startTime = (int)(System.nanoTime()/1000000);
		int firstGuess = threatSpace(rootNode, SAFE);
		if(firstGuess > 0)
		{
			principalVariation.push(threatVariation.get(0));
			return firstGuess;
		}
		for(int depth = 1; depth < maxDepth; depth++)
		{
			firstGuess = MTDf(firstGuess, depth);
			if(System.nanoTime()/1000000-startTime > maxTime)
				break;
		}
		return firstGuess;
	}
	
	public int threatSpace(Node node, int type)
	{
		threatVariation.clear();
		if(type == SAFE)
			if(node.opponentThreat())
				return 0;
		if(node.getFiveSquares().size() > 0)
		{
			Node finalNode = node.nextNode(node.getFiveSquares().get(0));
			threatVariation.add(finalNode.getMove());
			return -finalNode.getValue();
		}
		else if(node.getFourSquares().size() > 0)
		{
			Node finalNode = node.nextNode(node.getFourSquares().get(0));
			threatVariation.add(finalNode.getMove());
			return -finalNode.getValue();
		}
		else if(node.getGainSquares().size() > 0)
			return threatSpace(node, null, type);
		else
			return 0;
	}
	
	private int threatSpace(Node node, Move prevMove, int type)
	{
		for(Move move: node.getGainSquares())
		{
			for(HashSet<Cell> rest: move.getRestSquares())
			{
				if(prevMove == null || rest.contains(prevMove.getCell()))
				{
					Node nextRespondedNode = node.nextRespondedNode(move);
					if(type == SAFE)
						if(node.opponentThreat())
							continue;
					threatVariation.add(nextRespondedNode.getMove());
					nextRespondedNode.initGainSquares();
					for(Move nextMove: nextRespondedNode.getFiveSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return finalNode.getValue();
					}
					for(Move nextMove: nextRespondedNode.getFourSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return finalNode.getValue();
					}
					int result = threatSpace(nextRespondedNode, move, type);
					if(result == 0)
					{
						threatVariation.remove(threatVariation.size()-1);
						continue;
					}
					else
						return result;
				}
			}
		}
		return 0;
	}
	
	private int MTDf(int firstGuess, int depth)
	{
		int score = firstGuess;
		int upperBound = Score.FIVE.value();
		int lowerBound = -Score.FIVE.value();
		while(upperBound > lowerBound)
		{
			int beta;
			if(score == lowerBound)
				beta = score+1;
			else
				beta = score;
			score = alphaBetaTT(rootNode, beta-1, beta, depth);
			if(score < beta)
				upperBound = score;
			else
				lowerBound = score;
		}
		return score;
	}
	
	private int alphaBetaTT(Node node, int alpha, int beta, int depth)
	{
		MapEntry entry = transpositionTable.get(node.getZobristKey());
		if(entry != null && entry.getDepth() >= depth)
		{
			if(entry.getNodeType() == MapEntry.EXACT)
				return entry.getValue();
			if(entry.getNodeType() == MapEntry.LOWERBOUND && entry.getValue() > alpha)
				alpha = entry.getValue();
			else if(entry.getNodeType() == MapEntry.UPPERBOUND && entry.getValue() < beta)
				beta = entry.getValue();
			if(alpha >= beta)
				return entry.getValue();
		}
		
		int value;
		if(depth == 0)
		{
			value = node.getValue();
			transpositionTable.put(node.getZobristKey(), new MapEntry(node.getZobristKey(), depth, value, MapEntry.EXACT));
			return value;
		}
		int best = -Score.FIVE.value()-1;
		if(node.getChildren().size() == 0)
			node.generateChildren();
		for(Node child: node.getChildren())
		{
			if(System.nanoTime()/1000000-startTime > maxTime)
				break;
			value = -alphaBetaTT(child, -beta, -alpha, depth-1);
			if(value > best)
				best = value;
			if(best > alpha)
				alpha = best;
			if(best >= beta)
				break;
			child.setValue(best);
		}
		
		if(best <= alpha)
			transpositionTable.put(node.getZobristKey(), new MapEntry(node.getZobristKey(), depth, best, MapEntry.LOWERBOUND));
		else if(best >= beta)
			transpositionTable.put(node.getZobristKey(), new MapEntry(node.getZobristKey(), depth, best, MapEntry.UPPERBOUND));
		else
			transpositionTable.put(node.getZobristKey(), new MapEntry(node.getZobristKey(), depth, best, MapEntry.EXACT));
		return best;
	}
	
	public void run()
	{
		new Thread(new RunnableAI()).start();
	}
	
	public class RunnableAI implements Runnable
	{
		public void run()
		{
			int value = iterativeDeepening();
			if(type == Stone.BLACK.type())
				bestMove(value).getCell().getTile().setBlack();
			else if(type == Stone.WHITE.type())
				bestMove(value).getCell().getTile().setWhite();
			int five = Board.getInstance().toNode().five();
			if(five != Stone.EMPTY.type())
			{
				Board.getInstance().setWon(true);
				System.out.println("Computer won!");
			}
		}
	}
}
