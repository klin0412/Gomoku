import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class AIWorker
{
	public static final int MAX_DEPTH = 7;
	
	private Node rootNode;
	private HashMap<Integer, MapEntry> transpositionTable;
	private HashMap<Integer, Integer> history;
	private ArrayList<Move> threatVariation;
	private Stack<Move> principalVariation;
	private int startTime, maxTime;
	public int nodeExp;

	public AIWorker(Node rootNode)
	{
		this.rootNode = rootNode;
		rootNode.initGainSquares();
		transpositionTable = new HashMap<Integer, MapEntry>();
		history = new HashMap<Integer, Integer>();
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

	public Move bestMove()
	{
		return principalVariation.pop();
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
			principalVariation.add(threatVariation.get(0));
			return firstGuess;
		}
		/*for(int depth = 1; depth < MAX_DEPTH; depth++)
		{
			TODO: speed up search algorithm and return correct move
			firstGuess = MTDf(firstGuess, depth);
			if(System.nanoTime()/1000000-startTime > maxTime)
				break;
		}*/
		return firstGuess;
	}
	
	private int threatSpace(Node node)
	{
		threatVariation.clear();
		if(opponentThreat(node))
			return 0;
		if(node.getFiveSquares().size() > 0)
		{
			Node finalNode = node.nextNode(node.getFiveSquares().get(0));
			threatVariation.add(finalNode.getMove());
			return -finalNode.calcValue();
		}
		else if(node.getFourSquares().size() > 0)
		{
			Node finalNode = node.nextNode(node.getFourSquares().get(0));
			threatVariation.add(finalNode.getMove());
			return -finalNode.calcValue();
		}
		else if(node.getGainSquares().size() > 0)
			return threatSpace(rootNode, null);
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
					if(opponentThreat(node))
						return 0;
					threatVariation.add(nextRespondedNode.getMove());
					nextRespondedNode.initGainSquares();
					for(Move nextMove: nextRespondedNode.getFiveSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return -finalNode.calcValue();
					}
					for(Move nextMove: nextRespondedNode.getFourSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return -finalNode.calcValue();
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
	
	private boolean opponentThreat(Node node)
	{
		int threatCount = 0;
		if(node.getTurn() == Board.BLACK_TURN)
		{
			for(Cell stone: node.getWhiteStones())
			{
				if(stone.five())
					threatCount++;
				if(stone.straightFours())
					threatCount++;
				threatCount += stone.fours(null, null).size();
				threatCount += stone.threes(null, null).size();
				threatCount += stone.brokenThrees(null, null).size();
			}
		}
		else
		{
			for(Cell stone: node.getBlackStones())
			{
				if(stone.five())
					threatCount++;
				if(stone.straightFours())
					threatCount++;
				threatCount += stone.fours(null, null).size();
				threatCount += stone.threes(null, null).size();
				threatCount += stone.brokenThrees(null, null).size();
			}
		}
		return threatCount > 0;
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
			value = node.calcValue();
			transpositionTable.put(node.getZobristKey(), new MapEntry(node.getZobristKey(), depth, value, MapEntry.EXACT));
			principalVariation.clear();
			return value;
		}
		int best = -Score.FIVE.value()-1;
		node.generateChildren(history);
		for(Node child: node.getChildren())
		{
			nodeExp++;
			value = -alphaBetaTT(child, -beta, -alpha, depth-1);
			if(value > best)
				best = value;
			if(best > alpha)
			{
				alpha = best;
				Integer occurrence = history.get(node.getZobristKey());
				if(occurrence == null)
					history.put(node.getZobristKey(), 0);
				else
					history.put(node.getZobristKey(), occurrence+1);
				principalVariation.push(node.getMove());
			}
			if(best >= beta)
				break;
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
