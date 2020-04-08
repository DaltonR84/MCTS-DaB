import java.math.BigInteger;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it */

/* This is an implementation of an MCTSGame for the game of dots and boxes.*/

public class DotsAndBoxes extends MCTSGame{
	
	public int height;
	public int width;
	public int edges;
	public boolean scored;
	public boolean symmetry;
	
	public int[] fourthEdges = null;
	public int[] twoOrThreeEdges = null;
	
	public int[][] edgeSquares; //edgeSquares[i] is an array containing the squares that belong to edge i
	public int[][] squareEdges; //squareEdges[i] is an array containing all the edges of square i
	
	public static int[][] rotationMap = {
		{2,0,3,1},
		{4,9,1,6,11,3,8,0,5,10,2,7},
		{6,13,20,2,9,16,23,5,12,19,1,8,15,22,4,11,18,0,7,14,21,3,10,17},
		{8,17,26,35,3,12,21,30,39,7,16,23,34,2,11,20,29,38,6,15,24,33,1,10,19,28,37,5,14,23,32,0,9,18,27,36,4,13,22,31},
		{10,21,32,43,54,4,15,26,37,48,59,9,20,31,42,53,3,14,25,36,47,58,8,19,30,41,52,2,13,24,35,46,57,7,18,29,40,51,1,12,23,34,45,56,6,17,28,39,50,0,11,22,33,44,55,5,16,27,38,49},
		{12,25,38,51,64,77,5,18,31,44,57,70,83,11,24,37,50,63,76,4,17,30,43,56,69,82,10,23,36,49,62,75,3,16,29,42,55,68,81,9,22,35,48,61,74,2,15,28,41,54,67,80,8,21,34,47,60,73,1,14,27,40,53,66,79,7,20,33,46,59,72,0,13,26,39,52,65,78,6,19,32,45,58,71},
		{14,29,44,59,74,89,104,6,21,36,51,66,81,96,111,13,28,43,58,73,88,103,5,20,35,50,65,80,95,110,12,27,42,57,72,87,102,4,19,34,49,64,79,94,109,11,26,41,56,71,86,101,3,18,33,48,63,78,93,108,10,25,40,55,70,85,100,2,17,32,47,62,77,92,107,9,24,39,54,69,84,99,1,16,31,46,61,76,91,106,8,23,38,53,68,83,98,0,15,30,45,60,75,90,105,7,22,37,52,67,82,97}
	};
	
	public static int[][] reflectionMap = {
		{3,2,1,0},
		{10,11,7,8,9,5,6,2,3,4,0,1},
		{21,22,23,17,18,19,20,14,15,16,10,11,12,13,7,8,9,3,4,5,6,0,1,2},
		{39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0},
		{4,3,2,1,0,10,9,8,7,6,5,15,14,13,12,11,21,20,19,18,17,16,26,25,24,23,22,32,31,30,29,28,27,37,36,35,34,33,43,42,41,40,39,38,48,47,46,45,44,54,53,52,51,50,49,59,58,57,56,55},
		{5,4,3,2,1,0,12,11,10,9,8,7,6,18,17,16,15,14,13,25,24,23,22,21,20,19,31,30,29,28,27,26,38,37,36,35,34,33,32,44,43,42,41,40,39,51,50,49,48,47,46,45,57,56,55,54,53,52,64,63,62,61,60,59,58,70,69,68,67,66,65,77,76,75,74,73,72,71,83,82,81,80,79,78},
		{6,5,4,3,2,1,0,14,13,12,11,10,9,8,7,21,20,19,18,17,16,15,29,28,27,26,25,24,23,22,36,35,34,33,32,31,30,44,43,42,41,40,39,38,37,51,50,49,48,47,46,45,59,58,57,56,55,54,53,52,66,65,64,63,62,61,60,74,73,72,71,70,69,68,67,81,80,79,78,77,76,75,89,88,87,86,85,84,83,82,96,95,94,93,92,91,90,104,103,102,101,100,99,98,97,111,110,109,108,107,106,105}
	};
		                                           
	public DotsAndBoxes(int height, int width, boolean scored, boolean symmetry){
		this.height = height;
		this.width = width;
		this.scored = scored;
		this.symmetry = symmetry;
		this.scored = scored;
		
		if(height != width && symmetry){
			System.out.println("Cannot remove symmetries on a rectangular board.");
			symmetry = false;
		}
		
		edges = (height * (width + 1)) + (width * (height + 1));
		edgeSquares = new int[edges][2];
		squareEdges = new int[height * width][4];
		
		for(int i = 0; i < edgeSquares.length; i++){
			edgeSquares[i][0] = -1;
			edgeSquares[i][1] = -1;
		}
		
		//sets the edges for each square and the squares for each edge
		for(int i = 0; i < squareEdges.length; i++){
			int first = (((i / width) * ((2 * width) + 1)) + (i % width));
			int second = first + width;
			int third = second + 1;
			int fourth = third + width;
			
			
			int[] square = {first, second, third, fourth};
			squareEdges[i] = square;
			
			if(edgeSquares[first][0] == -1){
				edgeSquares[first][0] = i;
			}
			else{
				edgeSquares[first][1] = i;
			}
			
			if(edgeSquares[second][0] == -1){
				edgeSquares[second][0] = i;
			}
			else{
				edgeSquares[second][1] = i;
			}
			
			if(edgeSquares[third][0] == -1){
				edgeSquares[third][0] = i;
			}
			else{
				edgeSquares[third][1] = i;
			}
			
			if(edgeSquares[fourth][0] == -1){
				edgeSquares[fourth][0] = i;
			}
			else{
				edgeSquares[fourth][1] = i;
			}
		}
		
		//remove second array position if no square
		for(int i = 0; i < edgeSquares.length; i++){
			if(edgeSquares[i][1] == -1){
				int[] square = {edgeSquares[i][0]};
				edgeSquares[i] = square;
			}
		}
	}
	
	public int getNonCanonicalAction(int action, int rotation, boolean reflection){
		for(int i = 0; i < rotation; i++){
			action = rotationMap[height - 1][action];
		}
		
		if(reflection){
			action = reflectionMap[height - 1][action];
		}
		
		return action;
	}
	
	//returns the number of completed squares connected to edge in state, after edge is taken
	public int completedSquaresForEdge(int edge, GameState state){
		
		int taken = 0;
		String s = state.getBinaryString();
		
		//for each square attached to this edge
		for(int i = 0; i < edgeSquares[edge].length; i++){			
			int index = edgeSquares[edge][i];
			taken++;
			
			//for each edge of that square
			for(int b = 0; b < squareEdges[index].length; b++){
				if(squareEdges[index][b] == edge){
					continue;
				}
				
				if(s.length() < edges - squareEdges[index][b]){
					taken--;
					break;
				}
				
				if(s.charAt(squareEdges[index][b] - (edges - s.length())) == '0'){
					taken--;
					break;
				}
			}
		}
		
//		if(!symmetry){
//			//keep track of edges on almost completed squares
//			updateFourthEdges(edge, state);
//		}
		
		return taken;
	}
	
	// updates the array of fourth edges for almost completed squares
	public void updateFourthEdges(int edge, GameState state){
		fourthEdges = new int[edges];
		
		String s = state.getBinaryString();
		
		int index = 0;
		
		for(int i = 0; i < edgeSquares[edge].length; i++){
			int count = 0;
			int b;
			int missingEdgeIndex = 0;
			int j = edgeSquares[edge][i];
			
			
			for(b = 0; b < squareEdges[j].length; b++){
				if(s.length() < edges - squareEdges[j][b]){
					count++;
				
					missingEdgeIndex = b;
					
					if(count > 1){
						break;
					}
				}
				
				else if(s.charAt(squareEdges[j][b] - (edges - s.length())) == '0'){
					count++;
					
					missingEdgeIndex = b;
					
					if(count > 1){
						break;
					}
				}
			}
			
			if(count == 1){
				fourthEdges[index] = squareEdges[j][missingEdgeIndex];
				index++;
			}
		}
		
		if(index == 0){
			fourthEdges = null;
			return;
		}
		
		int[] temp = new int[index];
		
		for(int i = 0; i < temp.length; i++){
			temp[i] = fourthEdges[i];
		}
		
		fourthEdges = temp;
	}
	
	/* returns an array of ints representing all actions to be used
	 * each int refers to the edge that was taken*/
	public int[] getActions(GameState state) {
		
		if(symmetry){
			return getActionsSymmetrical(state);
		} else {
			return getAllActions(state, edges);
		}
	}
	
	/*return the array of all possible actions in state*/
	public static int[] getAllActions(GameState state, int edges){
		int[] temp = new int[edges];
		int index = 0;
		
		if(state.bigState != null){
			for(int i = 0; i < edges; i++){
				if(!state.bigState.testBit(edges - i - 1)){
					temp[index] = i;
					index++;
				}
			}
		}
		
		else {
			String binary = state.getBinaryString();
	
			// add extra leading zeros
			int b = binary.length();
			for (int i = 0; i < (edges - b); i++) {
				temp[index] = i;
				index++;
			}
	
			// for every character
			for (int i = 0; i < b; i++) {
				// if it is zero, add index to temp
				if (binary.charAt(i) == '0') {
					temp[index] = i + (edges - b);
					index++;
				}
			}
		}

		int[] actions = new int[index];

		// resize the array
		for (int i = 0; i < index; i++) {
			actions[i] = temp[i];
		}
		
		return actions;
	}
	
	public int[] getActionsSymmetrical(GameState state){
		int[] temp = new int[edges];
		GameState[] tempStates = new GameState[edges];
		GameState tempState;
		int tempStateIndex = 0;
		int index = 0;
		
		if(state.bigState != null){
			for(int i = 0; i < edges; i++){
				if(!state.bigState.testBit(edges - i - 1)){
					
					tempState = getSuccessorState(state, i);
					boolean used = false;
					
					for(int b = 0; b < tempStateIndex; b++){
						if(tempStates[b].equals(tempState)){
							used = true;
							break;
						}
					}
					
					if(!used){
						tempStates[tempStateIndex] = tempState;
						temp[index] = i;
						tempStateIndex++;
						index++;
					}
				}
			}
		}
		
		else {
			String binary = state.getBinaryString();
	
			// add extra leading zeros
			int b = binary.length();
			for (int i = 0; i < (edges - b); i++) {
				binary = "0" + binary;
			}
			
			// for every character
			for (int i = 0; i < edges; i++) {
				// if it is zero, add index to temp
				if (binary.charAt(i) == '0') {
					
					tempState = getSuccessorState(state, i);
					boolean used = false;
					
					for(int j = 0; j < tempStateIndex; j++){
						if(tempStates[j].equals(tempState)){
							used = true;
							break;
						}
					}
					
					if(!used){
						tempStates[tempStateIndex] = tempState;
						temp[index] = i;
						tempStateIndex++;
						index++;
					}
				}
			}
		}

		int[] actions = new int[index];

		// resize the array
		for (int i = 0; i < index; i++) {
			actions[i] = temp[i];
		}
		
		return actions;
	}
	
	/*returns successor state to be used on the tree*/
	public GameState getSuccessorState(GameState state, int action) {

		GameState returnState = getSimpleSuccessorState(state, action);
		
		if(symmetry){
			returnState = removeSymmetries(returnState);
		}
		
		return returnState;
	}
	
	/*return the simplest successor state*/
	public GameState getSimpleSuccessorState(GameState state, int action){
		GameState returnState = null;
		
		if(state.bigState != null){
			BigInteger newState = new BigInteger(state.bigState.toString());
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameState(newState);
		}
		
		else if(action > 62){
			BigInteger newState = new BigInteger(Long.toString(state.longState));
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameState(newState);
		}
		
		else{
			long newState = (long) (state.longState + ((long) Math.pow(2, edges - action - 1)));
			returnState = new GameState(newState);
		}
		
		return returnState;
	}
		
	public GameState removeSymmetries(GameState state){
		
		String stateString = state.getBinaryString();
		
		// add extra leading zeros
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		String returnState = stateString;
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);

			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		stateString = flip(stateString);
		
		if(first(stateString, returnState)){
			returnState = stateString;
		}

		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameState(returnState, true);
	}
	
	public GameStateScored removeSymmetries(GameStateScored state){
		
		String stateString = state.getBinaryString();
		
		// add extra leading zeros
		int b = stateString.length();
		for (int index = 0; index < (edges - b); index++) {
			stateString = "0" + stateString;
		}
		
		String returnState = stateString;
		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);

			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		stateString = flip(stateString);
		
		if(first(stateString, returnState)){
			returnState = stateString;
		}

		
		for(int j = 0; j < 3; j++){
			stateString = rotate(stateString);
			
			if(first(stateString, returnState)){
				returnState = stateString;
			}
		}
		
		return new GameStateScored(returnState, state.playerNetScore, true);
	}
	
	public boolean first(String state1, String state2){
		int length = state1.length();
		char c1;
		char c2;
		
		for(int i = 0; i < length; i++){
			c1 = state1.charAt(i);
			c2 = state2.charAt(i);
			
			if(c1 != c2){
				return c1 > c2 ? true : false;
			}
		}
		
		return false;
	}
	
	public String rotate(String state){
		
		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(rotationMap[height - 1][i]);
		}
		
		return newState;
	}
	
	public String flip(String state){

		String newState = "";
		
		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(reflectionMap[height - 1][i]);
		}
		
		return newState;
	}
	
	// returns true if state2 could be a successor of state1
	/*Deprecated with addition of Hashtable*/
	public boolean possibleChild(GameState state1, GameState state2) {
		
		if(!symmetry){
			String thisState = state1.getBinaryString();
			String pState = state2.getBinaryString();
			
			// for every character in thisState
			for (int i = 0; i < thisState.length(); i++) {
	
				// if the character is 1 (signifying a move)
				if (thisState.charAt(thisState.length() - 1 - i) == '1') {
	
					// if pState doesn't have a corresponding move, it can't be a
					// child of this node
					if (i >= pState.length()) {
						return false;
					}
	
					if (pState.charAt(pState.length() - 1 - i) != '1') {
						return false;
					}
				}
			}
		}
		
		else{
			//distinguish between symmetries
		}

		return true;
	}
	
	//gets the successor state for a scored game
	public GameStateScored getSuccessorState(GameStateScored state, int action){
		GameStateScored returnState = null;
		int z = completedSquaresForEdge(action, state);
		int score = state.playerNetScore;
		
		if(z > 0){
			score = score + z;
		} else {
			score = -score;
		}
		
		if(state.bigState != null){
			BigInteger newState = new BigInteger(state.bigState.toString());
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameStateScored(newState, score);
		}
		
		else if(action > 62){
			BigInteger newState = new BigInteger(Long.toString(state.longState));
			newState = newState.flipBit(edges - action - 1);
			returnState = new GameStateScored(newState, score);
		}
		
		else{
			long newState = (long) (state.longState + ((long) Math.pow(2, edges - action - 1)));
			returnState = new GameStateScored(newState, score);
		}
		
		if(symmetry){
			returnState = removeSymmetries(returnState);
		}
		
		return returnState;
	}
}
