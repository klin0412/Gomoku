import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class AIWorker
{
	public static final int MAX_DEPTH = 5;
	
	private Node rootNode;
	private HashMap<Integer, MapEntry> transpositionTable;
	private HashMap<Integer, Integer> history;
	private ArrayList<Move> threatVariation;
	private LinkedList<Node> exactNodes;
	private HashMap<Integer, Integer> bestMoves;
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
		exactNodes = new LinkedList<Node>();
		bestMoves = new HashMap<Integer, Integer>();
		principalVariation = new Stack<Move>();
		maxTime = 10000;
	}

	public Node getRootNode()
	{
		return rootNode;
	}

	public Move bestMove(int value)
	{
		//TODO: correct - successfully found winning sequence without threat space but fail to retrieve
		if(principalVariation.size() == 1)
			return principalVariation.pop();
		for(int i = exactNodes.size()-1; i >= 0; i--)
			if(exactNodes.get(i).getValue() != value)
				exactNodes.remove(i);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(Node node: exactNodes)
		{
			principalVariation.clear();
			while(node.getParent() != null)
			{
				principalVariation.push(node.getMove());
				node = node.getParent();
			}
			Integer occurrence = bestMoves.get(principalVariation.peek().hashCode());
			if(occurrence == null)
			{
				bestMoves.put(principalVariation.peek().hashCode(), 1);
				nodes.add(rootNode.nextNode(principalVariation.peek()));
			}
			else
			{
				principalVariation.peek().setOccurrence(occurrence+1);
				bestMoves.put(principalVariation.peek().hashCode(), occurrence+1);
			}
		}
		nodes.sort((n1, n2) -> n1.compareTo(n2));
		ArrayList<Move> moves = new ArrayList<Move>();
		for(Node node: nodes)
			moves.add(node.getMove());
		System.out.println(Arrays.toString(moves.toArray()));
		return moves.get(0);
	}
	
	//Threat Space Search: http://vanilla47.com/PDFs/Gomoku%20Renju%20Pente/go-moku-and-threat.pdf
	//Comprehensive search techniques: http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
	//Gomoku specific: http://isites.harvard.edu/fs/docs/icb.topic707165.files/pdfs/Kulev_Wu.pdf
	public int iterativeDeepening()
	{
		startTime = (int)(System.nanoTime()/1000000);
		int firstGuess = threatSpace(rootNode);
		System.out.println("Score: "+firstGuess);
		if(firstGuess > 0)
		{
			principalVariation.push(threatVariation.get(0));
			return firstGuess;
		}
		for(int depth = 1; depth < MAX_DEPTH; depth++)
		{
			exactNodes.clear();
			firstGuess = MTDf(firstGuess, depth);
			if(System.nanoTime()/1000000-startTime > maxTime)
				break;
		}
		return firstGuess;
	}
	
	private int threatSpace(Node node)
	{
		threatVariation.clear();
		if(node.opponentThreat())
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
					if(node.opponentThreat())
						return 0;
					threatVariation.add(nextRespondedNode.getMove());
					nextRespondedNode.initGainSquares();
					for(Move nextMove: nextRespondedNode.getFiveSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return finalNode.calcValue();
					}
					for(Move nextMove: nextRespondedNode.getFourSquares())
					{
						Node finalNode = nextRespondedNode.nextNode(nextMove);
						threatVariation.add(finalNode.getMove());
						return finalNode.calcValue();
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
			exactNodes.add(node);
			return value;
		}
		int best = -Score.FIVE.value()-1;
		if(node.getChildren().size() == 0)
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
				Integer occurrence = null;
				if(node.getMove() != null)
					occurrence = history.get(node.getMove().hashCode());
				if(occurrence == null && node.getMove() != null)
					history.put(node.getMove().hashCode(), 0);
				else if(node.getMove() != null)
					history.put(node.getMove().hashCode(), occurrence+1);
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
