import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it */

/* This is a Monte Carlo Tree Search */

public class MCTS {

	/* I split the search into iterations and simulations in order to record the
	 * progress incrementally
	 */

	static Random r = new Random();
	static int width; 
	static int height;
	static int edges;
	static double c; // uncertainty constant
	
	static DotsAndBoxes game = new DotsAndBoxes(2,2,false,false);
	static DotsAndBoxes game2;
	
	static int totalNodes = 1;

	static PolicyNodeTree tree;
	static PolicyNodeTree tree2;
	
	static long times[][];
	
	
	//------------------------------------
	static long[] boxEdges;
	static int looneyGames;
	static int totalGamesChecked;
	static long[] looneyStates;
	static int numStates;
	
	static boolean stopDefault; 
	static boolean stopSelection;
	//------------------------------------

	//args: width height c matches simulations_per_turn p1_scored p1_symmetry second_player(1 is an AI player, 2 is random) ... p2_scored p2_symmetry
	/*
	 *0)width 
	 *1)height
	 *2)c
	 *3)matches (number of games(
	 *4)simulations_per_turn (sim1- for both players sim2 is not specified, otherwise for player 1)
	 *5)p1_scored (True- use GameStateScored, False- used GameState
	 *6)p1_symmetry (True- uses non-symmetrical states, False- uses normal state)
	 *7)secondPlayer(1 is AI player, 2 is random)
	 *8)p2_scored (similar to p1 scored)
	 *9)p2_symmetry (similar to p1 symmetry)
	 *10)sim2
	 *11)stop default
	 */
	public static void main(String[] args) throws IOException {
		
		long start = System.currentTimeMillis();
		
		if(args.length > 0){
			width = Integer.parseInt(args[0]);
			height = Integer.parseInt(args[1]);
			
			edges = (height * (width + 1)) + (width * (height + 1));
			
			times = new long[edges][2];
			
			int sims1 = Integer.parseInt(args[4]);
			int sims2;
			
			
			//----------------------------------------------------------------------
			looneyGames = 0;
			totalGamesChecked = 0;
			looneyStates = new long[1000];
			numStates = 0;
			
			stopDefault = Boolean.parseBoolean(args[11]);
			stopSelection = Boolean.parseBoolean(args[12]);
			
			int[][] boxEdgesTemp = new int[width* height][4];
			
			for(int i = 0; i < boxEdgesTemp.length; i++){
				int first = (((i / width) * ((2 * width) + 1)) + (i % width));
				int second = first + width;
				int third = second + 1;
				int fourth = third + width;
				
				
				int[] square = {first, second, third, fourth};
				boxEdgesTemp[i] = square;	
			}
		
			boxEdges = LoonyEndgame.createBoxEdgesB(boxEdgesTemp, width);
			//-----------------------------------------------------------------------
			
			//Determines the number of simulations for player two if player two is a simulation
			if(args.length == 11){
				sims2 = Integer.parseInt(args[10]);
			} else {
				sims2 = sims1;
			}
			
			c = Double.parseDouble(args[2]);
			int matches = Integer.parseInt(args[3]);
			
			game = new DotsAndBoxes(height, width, Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
			
			
			//Determines whether player two is random or another simulation
			if(Integer.parseInt(args[7]) == 1){
				game2 = new DotsAndBoxes(height, width, Boolean.parseBoolean(args[8]), Boolean.parseBoolean(args[9]));
				competition(tree, game, tree2, game2, sims1, sims2, matches, Integer.parseInt(args[7]));
			} else {
				competition(tree, game, null, null, sims1, sims2, matches, Integer.parseInt(args[7]));
			}
			
			long end = System.currentTimeMillis();
			System.out.println("time in millis: " + (end - start));
			
			return;
		}
	}
	
	public static void competition(PolicyNodeTree tree, DotsAndBoxes game, PolicyNodeTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2, int matches, int policy){
		
		if(policy == 1){
			int wins = 0;
			int losses = 0;
			int draws = 0;
			
			double totalAveDepth = 0;
			long totalNodes = 0;
			
			for(int i = matches; i > 0; i--){
				double[] results = match(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
				int result = (int) results[0];
				totalAveDepth += results[1];
				totalNodes += results[2];
				
				if(result == 1)
					wins++;
				else if(result == 0){
					draws++;
				} else {
					losses++;
				}
			}
			
			System.out.println(height + "x" + width + " c=" + c + " matches=" + matches + " sims=" + simulationsPerTurn1 + "," + simulationsPerTurn2 + " p1=" + (game.scored ? "sc+" : "nsc+") + (game.symmetry ? "s" : "ns") + " p2=" + (game2.scored ? "sc+" : "nsc+") + (game2.symmetry ? "s" : "ns") + " w=" + wins + " l=" + losses + " d=" + draws + " looney games=" + looneyGames + " total games checked=" + totalGamesChecked);
			System.out.println("nodes: " + totalNodes / matches);
			System.out.println("average depth: " + (totalAveDepth / matches) + "\nAverage Time: ");
			
			for(int i = 0; i < times.length; i++){
				if(times[i][1] == 0){
					continue;
				}
				
				System.out.println("Move " + i + ": " + times[i][0] / times[i][1]);
			}
			
			
			/*
			//---------------------------------------------------
			for(int i = 0; i < numStates; i++) {
				System.out.println(i + ": " + Long.toBinaryString(looneyStates[i]));
			}
			//---------------------------------------------------
			*/
		}
	}
	
	//plays two games (each opponent is player one once)
	public static double[] match(PolicyNodeTree tree, DotsAndBoxes game, PolicyNodeTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2){
				
		tree = game.scored ? new PolicyNodeTree(game, new GameStateScored(0, 0)) : new PolicyNodeTree(game, new GameState(0));
		tree2 = game2.scored ? new PolicyNodeTree(game2, new GameStateScored(0, 0)) : new PolicyNodeTree(game2, new GameState(0));
		
		int match = -10;
		
		while(match == -10){
			match = testGame(tree, game, tree2, game2, simulationsPerTurn1, simulationsPerTurn2);
		}
		
		double results[] = new double[3];
		results[0] = match;
		results[1] = (double)tree.totalDepth / tree.numNodes;
		results[2] = tree.numNodes;
		
		return results;
	}
	
	/*play a single game and return the result for player one*/
	public static int testGame(PolicyNodeTree tree, DotsAndBoxes game, PolicyNodeTree tree2, DotsAndBoxes game2, int simulationsPerTurn1, int simulationsPerTurn2){
		
		GameState terminalState = null;
		
		if(edges > 60){
			terminalState = new GameState(new BigInteger("2").pow(edges).subtract(new BigInteger("1")));
		} 
		else{
			terminalState = new GameState((long) Math.pow(2, edges) - 1);
		}
		
		//the current node of each tree
		PolicyNode currentNode = tree.root;
		PolicyNode currentNode2 = tree2.root;
		
		//the game variables
		int action = 0;
		boolean playerOneTurn = true;
		int p1Score = 0;
		int p2Score = 0;
		
		//net boxes taken from looney game
		int looneyNet = 0;
		
		//for every turn
		while(!currentNode.state.equals(terminalState)){
			
			if(p1Score > (width*width) / 2 || p2Score > (width*width) / 2){
				break;
			}
			
			//------------------------------------------------------------------------------------------------
			if(stopSelection) {
				
				if(Long.bitCount(currentNode.state.getLongState()) >= edges/2) {
			
					long[] boxStates = LoonyEndgame.getBoxStates(boxEdges, currentNode.state.getLongState());
					
					if(LoonyEndgame.twoSides(boxStates, width)) {
						
						int[][] result = LoonyEndgame.getChainsLoops(currentNode.state.getBinaryString(),width);
					
						if(result[0].length != 0 && result[0][0] > 2) {
							
							System.out.println(Long.toBinaryString(currentNode.state.getLongState()));
							System.out.println(Arrays.toString(result[0])+Arrays.toString(result[1]));
							
							int[] zero = {0};
							if(result[0].length == 0 && result[1].length == 0) {
								looneyNet = 0;
							} else if(result[0].length == 0) {
								looneyNet = FindWinner.getValue(zero,result[1]);
							} else {
								looneyNet = FindWinner.getValue(result[0],result[1]);
							}
							
							if(!playerOneTurn) {
								looneyNet = looneyNet * -1;
							}
							
							looneyGames++;
							break;
						}
					}
				}
			}
			//------------------------------------------------------------------------------------------------------
			
			int sims = playerOneTurn ? simulationsPerTurn1 : simulationsPerTurn2;
			
			//get the action based on the current player
			if(playerOneTurn){
				long start = System.currentTimeMillis();
				
				//perform the simulations for this move
				while(sims > 0){
					//give player one's game, tree, node, and score
					simulate(currentNode.state, p1Score - p2Score, currentNode, terminalState, tree, game);
					sims--;
				}
				
				long end = System.currentTimeMillis();
				
				try{
					times[currentNode.moves][1]++;
					times[currentNode.moves][0] = times[currentNode.moves][0] + (end - start);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Array Index Error");
					return -10;
				}
				
				action = currentNode.getNextAction(0);
			} else {
				//perform the simulations for this move
				while(sims > 0){
					//give player two's game, tree, node, and score
					simulate(currentNode2.state, p2Score - p1Score, currentNode2, terminalState, tree2, game2);
					sims--;
				}
				
				action = currentNode2.getNextAction(0);
			}
			
			//get the point for this move
			int taken = game.completedSquaresForEdge(action, currentNode.state);
			
			//function to rotate unsym board to canon and return rotation and reversal
			
			//update the currentNodes
			currentNode = currentNode.getNode(action, tree.root, true);
			currentNode2 = currentNode2.getNode(action, tree2.root, true);
			
			/*possibly circumvent the null pointer*/
			if(currentNode == null || currentNode2 == null){
				System.out.println("Null Error: " + currentNode == null ? "Player 1" : "Player 2");
				return -10;
			}
			
			//System.out.println(currentNode.state.getBinaryString() + " " + action);
			
			if(playerOneTurn){
				p1Score += taken;
			} else {
				p2Score += taken;
			}
			
			playerOneTurn = taken > 0 ? playerOneTurn : !playerOneTurn;

		}
		
		System.out.println(p1Score + " " + p2Score + " " + looneyNet);
		System.out.println(playerOneTurn);
		
		int p1Net = p1Score - p2Score + looneyNet;
		
		System.out.println(p1Net);
		
		return p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
	}

	/*updates the given nodes with the result (win, tie, or loss) for player one
	* player is an array of booleans where player[i] is true if move i was made by player one
	* actions is the array of actions made */
	public static void backup(PolicyNode[] nodes, boolean[] player, int[] actions, int result) {
		for (int i = 0; i < nodes.length; i++) {			
			if (nodes[i] == null) {
				break;
			}

			// switch result if this was player two's move
			if (!player[i]) {
				result = -result;
			}

			// add a win, loss, or tie, to the node given the action taken
			nodes[i].addValue(actions[i], result, c);

			// switch back
			if (!player[i]) {
				result = -result;
			}
		}
	}

	/* 	simulates the portion of the games that is not part of the tree
	 	returns 1 for p1 win, -1 for p2 win, 0 for tie */
	public static int simulateDefault(GameState state, boolean playerOne, int p1Net, GameState terminalState) {
		
		for (int i = 0; i < edges; i++) {
			
			int action = randomPolicy(state);
			state = game.getSimpleSuccessorState(state, action);
			
			int taken = game.completedSquaresForEdge(action, state);
			
			if(taken > 0){
				p1Net += playerOne ? taken : -taken;
			}
			
			else{
				playerOne = !playerOne;
			}

			// break if final state
			if (state.equals(terminalState)) {
				break;
			}
			
			if(stopDefault) {
				
				if(Long.bitCount(state.getLongState()) >= edges/2) {
					
					long[] boxStates = LoonyEndgame.getBoxStates(boxEdges, state.getLongState());
					
					if(LoonyEndgame.twoSides(boxStates, width)) {
						
						int[][] result = LoonyEndgame.getChainsLoops(state.getBinaryString(),width);
					
						if(result[0].length != 0 && result[0][0] > 2) {
							
							//System.out.println(Long.toBinaryString(state.getLongState()));
							//System.out.println(Arrays.toString(result[0])+Arrays.toString(result[1]));
							
							int[] zero = {0};
							if(result[0].length == 0 && result[1].length == 0) {
								p1Net += 0;
							} else if(result[0].length == 0) {
								p1Net += FindWinner.getValue(zero,result[1]);
							} else {
								p1Net += FindWinner.getValue(result[0],result[1]);
							}
							
							p1Net = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
							
							looneyGames++;
							return p1Net;
						}
					}
				}
			}
		}
		
		p1Net = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
		
		return p1Net;
	}

	/* run a single simulation and update the policy */
	public static void simulate(GameState state, int p1Net, PolicyNode pastNode, GameState terminalState, PolicyNodeTree tree, DotsAndBoxes game) {
		boolean playerOne = true;
		
		int action = 0;
		boolean[] turns = new boolean[edges];
		int[] actionsTaken = new int[edges + 1];
		
		boolean gameFound = false;
		
		boolean looneyExists = false;

		// keep the nodes already existing to update
		PolicyNode[] playedNodes = new PolicyNode[edges];
		PolicyNode currentNode = pastNode;

		playedNodes[0] = currentNode;
		
		// for every move in the game
		for (int i = 0; !state.equals(terminalState); i++) {
			
//			System.out.println(state.getString());
			
			turns[i] = playerOne ? true : false;
			
			// get the next node, given c
			action = currentNode.getNextAction(c);
			
			currentNode = currentNode.getNode(action, tree.root, false);
			
			// add the new action
			actionsTaken[i] = action;
			
			//if someone has more than half the squares, quit
			if(p1Net > (height * width) / 2 || p1Net < (-height * width) / 2){
				state = terminalState;
				break;
			}

			int taken = game.completedSquaresForEdge(action, state);
			
			//update the state
			if(currentNode != null){
				state = currentNode.state;
			}
			
			else{
				//this turns a scored state to unscored, but since it just feeds int simulateDefault, it doesn't matter
				state = game.getSuccessorState(state, action);
			}
			
			
			//-------------------------------------------------------------------------------------------------------------
			if(stopSelection) {
				
				if(Long.bitCount(state.getLongState()) >= edges/2) {
			
					long[] boxStates = LoonyEndgame.getBoxStates(boxEdges, state.getLongState());
					
					if(LoonyEndgame.twoSides(boxStates, width)) {
						
						int[][] result = LoonyEndgame.getChainsLoops(state.getBinaryString(),width);
					
						if(result[0].length != 0 && result[0][0] > 2) {
							
							//System.out.println(Long.toBinaryString(state.getLongState()));
							//System.out.println(Arrays.toString(result[0])+Arrays.toString(result[1]));
							
							int[] zero = {0};
							if(result[0].length == 0 && result[1].length == 0) {
								p1Net += 0;
							} else if(result[0].length == 0) {
								p1Net += FindWinner.getValue(zero,result[1]);
							} else {
								p1Net += FindWinner.getValue(result[0],result[1]);
							}
							
							looneyGames++;
							looneyExists = true;
							break;
						}
					}
				}
			}
			
			/*
			 if(numStates != 1000 && (numStates == 0 || looneyStates[numStates-1] != state.getLongState())) {
				looneyStates[numStates] = state.getLongState();
				numStates++;
			 }
			*/
			
			//----------------------------------------------------------------------------------------------------------------
			
			//don't add the terminal node (no need)
			if(!state.equals(terminalState)){
				// add to set
				playedNodes[i + 1] = currentNode;
			}
			
			if(taken > 0){
				p1Net += playerOne ? taken : -taken;
			}
			
			else{
				playerOne = !playerOne;
			}
			
			//break if null
			if(currentNode == null){
				break;
			}
		}
		
//		System.out.println(state.getString());
		
		int z; //the result
		
		//playout if not at terminal state
		if(!state.equals(terminalState) && !looneyExists){
			z = simulateDefault(state, playerOne, p1Net, terminalState);
		}		
		else{
			z = p1Net > 0 ? 1 : p1Net < 0 ? -1 : 0;
		}
				
		backup(playedNodes, turns, actionsTaken, z);
	}

	// returns a random successor state action
	public static int randomPolicy(GameState state) {		
		int[] actions = DotsAndBoxes.getAllActions(state, edges);

		int next = r.nextInt(actions.length);

		return actions[next];
	}
	
	// returns a state by taking a square if possible
	public static int boxTakingPolicy(GameState state, DotsAndBoxes game){
		int[] actions = game.fourthEdges;
		
		if(actions == null){
			return randomPolicy(state);
		}
		
		int next = r.nextInt(actions.length);
		
		return actions[next];
	}
	
	/* play a test game using the policy developed and return true if a win */
	public static boolean testPolicy(boolean random) {
		int p1Net = 0;
		GameState state = new GameState(0);
		int action = 0;
		boolean playerOne = true;

		// set the current node to root and add to set
		PolicyNode currentNode = tree.root;

		// for every move in the game
		for (int i = 0; i < edges; i++) {

			//for a random player or when off the tree
			if((random && !playerOne) || currentNode == null){
				//get a random action
				action = randomPolicy(state);
				
				//if on the tree, update the current node
				if(currentNode != null){
					currentNode = currentNode.getNode(action, tree.root, false);
				}
			}
			
			else{
				// get the next node, given c
				action = currentNode.getNextAction(0);
				currentNode = currentNode.getNode(action, tree.root, false);
			}
			
			//set the state
			if(currentNode != null){
				state = currentNode.state;
			}
			
			else{
				state = game.getSuccessorState(state, action);
			}
			
			int taken = game.completedSquaresForEdge(action, state);
			
			if(taken > 0){
				p1Net += playerOne ? taken : -taken;
			}
			
			else{
				playerOne = !playerOne;
			}
		}
		
		if(p1Net > 0){
			return true;
		}
		
		return false;
	}
}
