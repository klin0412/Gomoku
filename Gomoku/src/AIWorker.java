import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class AIWorker
{
	public static final int MAX_DEPTH = 7;
	
	private Node rootNode;
	private HashMap<Integer, MapEntry> transpositionTable;
	private ArrayList<Move> threatVariation;
	private Stack<Move> principalVariation;
	private int startTime, maxTime;
	public int nodeExp;

	public AIWorker(Node rootNode)
	{
		this.rootNode = rootNode;
		rootNode.initGainSquares();
		transpositionTable = new HashMap<Integer, MapEntry>();
		threatVariation = new ArrayList<Move>();
		principalVariation = new Stack<Move>();
		maxTime = 10000;
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
		if(worker.threatSpace(node) != 0)
			return worker.getThreatVariation().get(0);
		else
			return rootNode.getChildren().get(0).getMove();
	}
	
	//Threat Space Search: http://vanilla47.com/PDFs/Gomoku%20Renju%20Pente/go-moku-and-threat.pdf
	//Comprehensive search techniques: http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
	//Gomoku specific: http://isites.harvard.edu/fs/docs/icb.topic707165.files/pdfs/Kulev_Wu.pdf
	public int iterativeDeepening()
	{
		startTime = (int)(System.nanoTime()/1000000);
		int firstGuess = threatSpace(rootNode);
		if(firstGuess > 0)
		{
			principalVariation.push(threatVariation.get(0));
			return firstGuess;
		}
		for(int depth = 1; depth < MAX_DEPTH; depth++)
		{
			firstGuess = MTDf(firstGuess, depth);
			if(System.nanoTime()/1000000-startTime > maxTime)
				break;
		}
		return firstGuess;
	}
	
	public int threatSpace(Node node)
	{
		threatVariation.clear();
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
			return threatSpace(node, null);
		else
			return 0;
	}
	
	private int threatSpace(Node node, Move prevMove)
	{
		for(Move move: node.getGainSquares())
		{
			for(HashSet<Cell> rest: move.getRestSquares())
			{
				if(prevMove == null || rest.contains(prevMove.getCell()))
				{
					Node nextRespondedNode = node.nextRespondedNode(move);
					if(node.opponentThreat())
						return 0;
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
					int result = threatSpace(nextRespondedNode, move);
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
			nodeExp++;
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
}
