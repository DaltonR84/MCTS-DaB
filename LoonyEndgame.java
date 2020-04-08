import java.util.Arrays;

public class LoonyEndgame {

	public static void main(String[] args) {
		//variables about board taken in
		int size = 5;
		//String binStr = "111100001111";
		//String binStr = "110101100010111100101001";
		//String binStr = "111111111111111111111111";
		//String binStr = "0111110110001110100110110110000010101111";
		String binStr = "111111101011000001011001101110100000111111010000011011101100";
		
		long start = System.currentTimeMillis();
		
		int[][] result = getChainsLoops(binStr, size);
		
		//--------------------------------------------------------------------------------------
		
		for(int cnt = 0; cnt < result[0].length; cnt++) {
			System.out.println("Chain: " + result[0][cnt]);
		}
		for(int cnt = 0; cnt < result[1].length; cnt++) {
			System.out.println("Loop: " + result[1][cnt]);
		}
		
		
		
		int[] zero = {0};
		if(result[0].length == 0 && result[1].length == 0) {
			System.out.println(0);
		} else if(result[1].length == 0) {
			System.out.println("Game Value: " + FindWinner.getValue(result[0],zero));
		} else if(result[0].length == 0) {
			System.out.println("Game Value: " + FindWinner.getValue(zero,result[1]));
		} else {
			System.out.println("Game Value: " + FindWinner.getValue(result[0],result[1]));
		}
		
		long end = System.currentTimeMillis();
		System.out.println("time in millis: " + (end - start));
	}
	
	
	// takes in the board and the size of the board and outputs the chains and loops as a 2d array
	// Parameter @ boardStr is the board as a string of the binary representation of the board
	// Parameters @ size is the size of the board
	// Return @ an 2d array with [0] being the length of the chain on board and [1] being the loops (sorted)
	public static int[][] getChainsLoops(String boardStr, int size){
		
		long board = Long.parseLong(boardStr, 2);
		
		//holds the edges as ints
		int[][] boxEdgesTemp = new int[size * size][4];
		
		for(int i = 0; i < boxEdgesTemp.length; i++){
			int first = (((i / size) * ((2 * size) + 1)) + (i % size));
			int second = first + size;
			int third = second + 1;
			int fourth = third + size;
			
			
			int[] square = {first, second, third, fourth};
			boxEdgesTemp[i] = square;	
		}
	
		//converts the edges from being stored as ints to being stored as longs
		long[] boxEdges = createBoxEdgesB(boxEdgesTemp, size);
		
		//gets the states of the boxes in board using the edges
		long[] boxStates = getBoxStates(boxEdges, board);
		
		//find all of the boxes that are the beginning/end of chains
		long endBoxes = findEndBoxes(findEndBoxEdges(board, size), boxEdges, size);
		
		//gets all of the boxes remaining on the board after all boxes taken are taken out
		long boxesRemaining = getBoxesRemaining(boxStates, boxEdges, size);
		
		//stores the number of chains and loops
		int chainNum = 0;
		int loopNum = 0;
		
		//get all chain lengths
		int[] chains = new int[size*size]; //stores all of the chains
		int posInArr = 0; //stores the position to put the next chain in chains
		int location = size*size - 1; //bit it is checking in tempEnd
		long tempEnd = endBoxes; //tempEnd is used to find the beginning of chains
		while(tempEnd != 0) {
			
			if(tempEnd%2 == 1) { //if there is the beginning of a chain at the bit being checked
				
				//makes a new chain at the location of the box
				int[] chain = followChain(location, boxStates, boxEdges, size);
				
				//gets the length of the chain
				int length = 0;
				for(int cnt = 0; cnt < chain.length && chain[cnt] != -1; cnt++) {
					length++;
					//System.out.println("Chain: " + chain[cnt]);
				}
				
				//adds the chain to chains
				chains[posInArr] = length;
				posInArr++; 
				chainNum++;
				
				//makes a new boxesReamining without the newly found chain
				boxesRemaining = takeOutBoxes(chain, boxesRemaining, size);
				//System.out.println(Long.toBinaryString(boxesRemaining));
				//System.out.println("");
				
				//take the new chain's end and beginning out of end boxes
				endBoxes = endBoxes&boxesRemaining;
				tempEnd = endBoxes;	//restarts the process again with a new temp end
				location = size*size-1; //restats the location
				
			} else {
				
				location--; //move to next bit in tempEnd/check next box
				tempEnd = tempEnd >> 1;
				
			}
		}
		
		chains[posInArr] = -1; //add -1 to end of chains signifying the end
		
		//-------------------------------------------------
		//loops works pretty much the same as the chains does
		//-------------------------------------------------
		
		int[] loops = new int[size*size];
		long tempBoxes = boxesRemaining;
		posInArr = 0;
		location = size*size - 1;
		
		while(boxesRemaining != 0) {
			
			if(tempBoxes%2==1) {
				
				int[] loop = followLoop(location, boxStates, boxEdges, size);
				
				int length = 0;
				for(int cnt = 0; cnt < loop.length && loop[cnt] != -1; cnt++) {
					length++;
					//System.out.println("Loop: " + loop[cnt]);
				}
				loops[posInArr] = length;
				posInArr++;
				loopNum++;
				
				boxesRemaining = takeOutBoxes(loop, boxesRemaining, size);
				//System.out.println(Long.toBinaryString(boxesRemaining));
				//System.out.println("");
				
				tempBoxes = boxesRemaining;	
				location = size*size-1;
				
			} else { 
				location--;
				tempBoxes = tempBoxes >> 1;	
			}
			
		}
		
		loops[posInArr] = -1;	
	
		
		//make a final chain for the chains and the loops
		
		int[] finalChain = new int[chainNum];
		for(int cnt = 0; cnt < chainNum; cnt++) {
			finalChain[cnt] = chains[cnt];
		}
		int[] finalLoop = new int[loopNum];
		for(int cnt = 0; cnt < loopNum; cnt++) {
			finalLoop[cnt] = loops[cnt];
		}
		
		//sort the arrays
		Arrays.sort(finalChain);
		Arrays.sort(finalLoop);
		
		int[][] result = {finalChain, finalLoop};
		
		//returns the chains and the loops
		return result;
	}

	
	// take an array of int that represent the edges of the boxes
	// Parameter @ boxEdges is the array of edges for each box [boxNumber][sideNumber]
	// Parameter @ size is the size of the board
	// Return @ long[] that represents the box edges in binary
	public static long[] createBoxEdgesB(int[][] boxEdgesParam, int size) {
			
		long[] boxEdgesB = new long[size * size];
		int edges = (size * (size + 1)) + (size * (size + 1));
		long bitLoc = 1;
			
		for(int box = 0; box < boxEdgesParam.length; box++) {
			boxEdgesB[box] = 0;
			for(int side = 0; side < 4; side++) { 
				boxEdgesB[box] = (long)(boxEdgesB[box] + (bitLoc << (edges-boxEdgesParam[box][side]-1)));
			}
		}
		
		return boxEdgesB;
	}
	
	
	//using a board (in binary) and an array of the boxEdges, it will return the states of each box in binary longs
	//Parameters @ board is binary board -- boxEdgesB is an array of binary longs that contains the edges for each board. Obtain this from createBoxEdgesB
	//Returns the boxStates as an array of binary long values
	public static long[] getBoxStates(long[] boxEdges, long board) {
		
		//create an array to hold the box states
		long[] boxStates = new long[boxEdges.length];
		
		//compare the board with the boxEdges to get each boxes state
		for(int box = 0; box < boxEdges.length; box++) {
			boxStates[box] = board & boxEdges[box];
		}
		
		//return the box states
		return boxStates;
	}
	
	
	//returns true if all boxes have at least two edges taken
	//Parameters @ boxStates is the box states retrieved from getBoxStates() and size is size of board
	public static boolean twoSides(long[] boxStates, int size) {
		
		boolean complete = true;
		
		//go through all box states
		for(int box = 0; box < boxStates.length; box++) {
			
			if(Long.bitCount(boxStates[box]) < 2 || Long.bitCount(boxStates[box]) == 3) { //return false if a box has less than 2 edges taken
				return false;
			}	
			if(Long.bitCount(boxStates[box]) != 4) {
				complete = false;
			}
		}
		//returns true if all boxes have at least 2 edges taken
		
		if(complete == false)
			return true;
		return false;
	}
	
	
	//finds all edges on the edge of board
	//Parameters @ board is the board as a binary long and size is size of board
	//Returns @ a binary long representing all of the edges on the edge of the board, a 1 means the edge has not been taken 
	public static long findEndBoxEdges(long board, int size) {
		long edges = 0; // stores every edge for a board of size
		int numOfEdges = (size * (size + 1)) + (size * (size + 1));
		long bitLoc= 1;
		
		//go through each outside edge and adds it to edges
		for(int cnt = 0; cnt < size; cnt++) {
			//top
			edges = (long)(edges + (bitLoc << (numOfEdges-cnt-1)));
			//left
			edges = (long)(edges + (bitLoc << (numOfEdges-(size + cnt * (size*2+1))-1)));
			//right
			edges = (long)(edges + (bitLoc << (numOfEdges-(size*2 + cnt * (size*2+1))-1)));
			//bottom
			edges = (long)(edges + (bitLoc << (numOfEdges-(numOfEdges-cnt-1)-1)));
		}
		
		//returns all edges that are on the current board
		return edges&(~board);
	}
	
	
	//using the edges, it will find the boxes that are the beginnings(or ends) of chains
	//Parameters @ endEdges is the return from findEndBoxEdges()
	//Parameters @ boxEdges is all of the edges for each box as binary longs
	//Return @ a binary long of size*size. A 1 represents a box that is the beginning or end of a chain
	//Returns as binary long starting at top left box and ending a bottom right
	public static long findEndBoxes(long endEdges, long[] boxEdges, int size) {
		long endBoxes = 0; //store the answer
		long bitLoc = 1;
		
		for(int cnt = 0; cnt < size*size; cnt++) {
			
			//find which boxes contain the edges from endEdges
			if((endEdges&boxEdges[cnt]) >= 1) {
				
				//this adds a box to the appropriate location of the long in binary 
				endBoxes = endBoxes + (bitLoc << ((size*size)-cnt-1));
			}
		}
		
		return endBoxes;
	}
	
	
	//gets the next box in a chain 
	//Parameter @ box is the current box in the chain 
	//Parameter @ previousBox is the previous box in the chain, should be -1 if the is no previous box
	//Parameter @ boxStates is from getBoxStates
	//Parameter @ boxEdges is from createBoxEdgesB
	//Return @ int that is the next box in the chain, will be -1 if there is no next box (box is the end of a chain)
	public static int nextBox(int box, int previousBox, long[] boxStates, long[] boxEdges, int size) {
		//the state and edges of box
		long boxState = boxStates[box];
		long boxEdge = boxEdges[box];
	
		//holds the possible answer
		int possibleBox = 0;
		
		int length = Long.toBinaryString(boxEdge).length();
		
		//direction contains the current possible direction that the next box would be in
		int direction = 1;
		
		//go through each bit of boxEdge
		for(int cnt = 0; cnt < length; cnt++) {
			
			//if the current bit is a 1
			if(boxEdge % 2 == 1) {
				
				//if the current bit is a 0
				if(boxState % 2 == 0) {
					
					//if it gets to here, the box is missing an edge and points to another box
					
					//possibleBox holds the newest box found from connected box
					possibleBox = connectedBox(box, direction, size);
					//if the connected box is the previous box it will continue to check
					if(possibleBox != previousBox) {
						//returns if the newest box it points to is the previous box
						return possibleBox;
					}
					
				} 
				
				//if it passes possible edge from boxEdge, the means its direction will change
				direction++;
			}
			
			//shifts their binary representations over by 1
			//this will allow the next bit/edge to be checked
			boxEdge = boxEdge >> 1;
			boxState = boxState >> 1;
		}
		
		//return -1 if nothing was found
		//also returns this for a 1 chain in a corner
		return -1;
	}
	
	
	//finds the box connected to box in direction
	//Parameter @ box is the current box
	//Parameter @ direction is the direction to get the next box from box
			//1 is down, 2 is right, 3 is left, 4 is up
	//Return @ int that is the connected box
	public static int connectedBox(int box, int direction, int size) {
		
		//holds the new box
		int newBox;
		
		if(direction == 1) { //down
			newBox = box+size;
		} else if(direction == 2) { //right
			if(box%size == size-1) { //check to see if the box is on the board
				newBox = -1;
			} else {
				newBox = box+1;
			}
		} else if(direction == 3) { //left
			if(box%size == 0) { //check to see if the box is on the board
				newBox = -1;
			} else {
				newBox = box-1;
			}
		} else { //up
			newBox = box-size;
		}
		
		//check to see that box is within the board
		if(newBox < 0 || newBox >= size*size) {
			newBox = -1;
		}
		
		//return the box that box was pointing to in direction
		return newBox;
	}
	
	
	//gets the states of all of the boxes 
	//Parameter @ boxStates is from getBoxStates
	//Parameter @ boxEdges is from createBoxEdgesB
	//returns a binary long that has the state of each box (0 means the box is taken, 1 means it has not been taken)
		//the binary states starts with the top left box and ends with the bottom right box
	public static long getBoxesRemaining(long[] boxStates, long[] boxEdges, int size) {
		
		//create a long that is in binary 111111..... of length size*size
		long boxesRemaining = (1 << ((size*size)-1)) + (((1 << ((size*size)-1))-1));
		
		//goes through each box and sees if that box has been taken
		//if the box is taken, it puts a zero in its position in boxesRemaining
		for(int cnt = 0; cnt < boxStates.length; cnt++) {
			if(boxStates[cnt] == boxEdges[cnt]) {
				boxesRemaining = boxesRemaining - (1 << ((size*size)-cnt-1));
			}
		}
		
		//returns that states of all boxes
		return boxesRemaining;
	}
	
	
	//starting at endBox (assuming it is the beginning of a chain), it will follow the entire chain
	//Parameter @ endBox is that start of the chain
	//Parameter @ boxStates is from getBoxStates
	//Parameter @ boxEdges is from createBoxEdgesB
	//Return @ int[] that contains the numbers of all the boxes in the chain, the chain ends with a -1 in the array
	public static int[] followChain(int endBox, long[] boxStates, long[] boxEdges, int size) {
		
		int current = endBox; //holds the current box
		int pos = 1; //position of next box in chain[]
		//int[] that contains the chain, even though the size is unknown I used a array instead of arrayList because of efficiency
		int[] chain = new int[size*size];
		int previous = -1; //holds previous box
		int hold = current; 
		
		chain[0] = current;
		
		for(int cnt = 0; cnt < chain.length-1 && current != -1; cnt++) {
			
			hold = current;
			
			//use nextBox to get next box in chain and then add it to chain[]
			current = nextBox(current, previous, boxStates, boxEdges, size);
			previous = hold;
			
			chain[pos] = current;
			pos++;	
		}
		
		//returns the chain with a -1 at the end
		return chain;
	}
	
	
	//takes the boxes from a chain or a loop out of boxesRemaining
	//Parameter @ chain is a chain or loop from followLoop or followChain
	//Parameter @ boxRemaining is from getBoxesRemaining
	//Return @ long that is boxesReamining with the new chain taken out
	public static long takeOutBoxes(int[] chain, long boxesRemaining, int size) {
		long boxesRemainingNew = boxesRemaining;
		
		//takes all boxes from chain out of boxesReamining
		for(int cnt = 0; cnt < chain.length && chain[cnt] != -1; cnt++) {
			boxesRemainingNew = boxesRemainingNew - (1 << ((size*size)-chain[cnt]-1));
		}
		
		//returns the new boxesReamining
		return boxesRemainingNew;
	}
	
	
	//follows a loop starting a startBox
	//works very similar to followChain
	public static int[] followLoop(int startBox, long[] boxStates, long[] boxEdges, int size) {
		int current = startBox;
		int pos = 2;
		int[] loop = new int[size*size];
		int previous = firstBox(current, boxStates, boxEdges, size);
		int hold = current;
		
		loop[0] = previous;
		loop[1] = current;
		
		for(int cnt = 0; cnt < loop.length-1 && current != loop[0]; cnt++) {
			
			hold = current;
			
			current = nextBox(current, previous, boxStates, boxEdges, size);
			previous = hold;
			
			loop[pos] = current;
			pos++;	
		}
		
		loop[pos-1] = -1;
		
		return loop;
	}
	
	
	//finds the next box from box when there is no previousBox
	//works similarly to nextBox
	public static int firstBox(int box, long[] boxStates, long[] boxEdges, int size) {
		long boxState = boxStates[box];
		long boxEdge = boxEdges[box];
		
		int length = Long.toBinaryString(boxEdge).length();
		
		int direction = 1;
		
		for(int cnt = 0; cnt < length; cnt++) {
			
			if(boxEdge % 2 == 1) {
				
				if(boxState % 2 == 0) {
					return connectedBox(box, direction, size);
				} 
				
				direction++;
			}
			
			boxEdge = boxEdge >> 1;
			boxState = boxState >> 1;
		}
		
		return -1;
	}


}
