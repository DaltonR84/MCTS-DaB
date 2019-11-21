import java.util.Arrays;

public class LoonyEndgame {

	
	public static void main(String[] args) {
		//variables about board taken in
		int size = 5;
		String binStr = "110111011010000011110100011111000001111011001110100001111111";
		int[][] result = getChainsLoops(binStr, size);
		
		//--------------------------------------------------------------------------------------
		
		for(int cnt = 0; cnt < result[0].length; cnt++) {
			System.out.println("Chain: " + result[0][cnt]);
		}
		for(int cnt = 0; cnt < result[1].length; cnt++) {
			System.out.println("Loop: " + result[1][cnt]);
		}	
		
		//System.out.println("Game Value: " + FindWinner.getValue(result[0],result[1]));
		
	}
	
	public static int[][] getChainsLoops(String boardStr, int size){
		
		long board = Long.parseLong(boardStr, 2);
		
		int[][] boxEdgesTemp = new int[size * size][4];
		
		for(int i = 0; i < boxEdgesTemp.length; i++){
			int first = (((i / size) * ((2 * size) + 1)) + (i % size));
			int second = first + size;
			int third = second + 1;
			int fourth = third + size;
			
			
			int[] square = {first, second, third, fourth};
			boxEdgesTemp[i] = square;	
		}
	

		long[] boxEdges = createBoxEdgesB(boxEdgesTemp, size);
		
		long[] boxStates = getBoxStates(boxEdges, board);
		
		long endBoxes = findEndBoxes(findEndBoxEdges(board, size), boxEdges, size);
		
		long boxesRemaining = (1 << ((size*size)-1)) + (((1 << ((size*size)-1))-1));
		boxesRemaining = takeOutComplete(boxesRemaining, boxStates, boxEdges, size);
		
		//
		int chainNum = 0;
		int loopNum = 0;
		
		//get all chain lengths
		int[] chains = new int[size*size];
		int posInArr = 0;
		int location = size*size - 1;
		long tempEnd = endBoxes;
		while(tempEnd != 0) {
			
			if(tempEnd%2 == 1) {
				
				int[] chain = followChain(location, boxStates, boxEdges, size);
				
				int length = 0;
				for(int cnt = 0; cnt < chain.length && chain[cnt] != -1; cnt++) {
					length++;
					//System.out.println("Chain: " + chain[cnt]);
				}
	
				chains[posInArr] = length;
				posInArr++;
				chainNum++;
				
				boxesRemaining = takeOutBoxes(chain, boxesRemaining, size);
				//System.out.println(Long.toBinaryString(boxesRemaining));
				//System.out.println("");
				
				endBoxes = endBoxes&boxesRemaining;
				tempEnd = endBoxes;	
				location = size*size-1;
				
			} else {
				
				location--;
				tempEnd = tempEnd >> 1;
				
			}
		}
		
		chains[posInArr] = -1;
			
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
	
		
		int[] finalChain = new int[chainNum];
		for(int cnt = 0; cnt < chainNum; cnt++) {
			finalChain[cnt] = chains[cnt];
		}
		int[] finalLoop = new int[loopNum];
		for(int cnt = 0; cnt < loopNum; cnt++) {
			finalLoop[cnt] = loops[cnt];
		}
		
		Arrays.sort(finalChain);
		Arrays.sort(finalLoop);
		
		int[][] result = {finalChain, finalLoop};
		
		return result;
	}
	
	// take an array of int that represent the edges of the boxes
	// Parameter @ boxEdges is the array of edges for each box [boxNumber][sideNumber]
	// Parameter @ size is the size of the board
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
	
	
	public static boolean twoSides(long[] boxStates, int size) {
		
		for(int box = 0; box < boxStates.length; box++) {
			
			if(Long.bitCount(boxStates[box]) < 2) {
				return false;
			}	
		}
		
		return true;
	}
	
	
	public static long findEndBoxEdges(long board, int size) {
		long edges = 0;
		int numOfEdges = (size * (size + 1)) + (size * (size + 1));
		long bitLoc= 1;
		
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
		
		return edges&(~board);
	}
	
	
	public static long findEndBoxes(long endEdges, long[] boxEdges, int size) {
		long endBoxes = 0;
		long bitLoc = 1;
		
		for(int cnt = 0; cnt < size*size; cnt++) {
			
			if((endEdges&boxEdges[cnt]) >= 1) {
				endBoxes = endBoxes + (bitLoc << ((size*size)-cnt-1));
			}
		}
		
		return endBoxes;
	}
	
	
	public static int nextBox(int box, int previousBox, long[] boxStates, long[] boxEdges, int size) {
		long boxState = boxStates[box];
		long boxEdge = boxEdges[box];
	
		int possibleBox = 0;
		
		int length = Long.toBinaryString(boxEdge).length();
		
		int direction = 1;
		
		for(int cnt = 0; cnt < length; cnt++) {
			
			if(boxEdge % 2 == 1) {
				
				if(boxState % 2 == 0) {
					
					possibleBox = connectedBox(box, direction, size);
					
					if(possibleBox != previousBox) {
						return possibleBox;
					}
					
				} 
				
				direction++;
			}
			
			boxEdge = boxEdge >> 1;
			boxState = boxState >> 1;
		}
		
		return -1;
	}
	
	
	public static int connectedBox(int box, int direction, int size) {
		
		int newBox;
		
		if(direction == 1) { //down
			newBox = box+size;
		} else if(direction == 2) { //right
			if(box%size == size-1) {
				newBox = -1;
			} else {
				newBox = box+1;
			}
		} else if(direction == 3) { //left
			if(box%size == 0) {
				newBox = -1;
			} else {
				newBox = box-1;
			}
		} else { //up
			newBox = box-size;
		}
		
		if(newBox < 0 || newBox >= size*size) {
			newBox = -1;
		}
		
		return newBox;
	}
	
	
	public static long takeOutComplete(long boxesRemaining, long[] boxStates, long[] boxEdges, int size) {
		
		long boxesRemainingNew = boxesRemaining;
		
		for(int cnt = 0; cnt < boxStates.length; cnt++) {
			if(boxStates[cnt] == boxEdges[cnt]) {
				boxesRemainingNew = boxesRemainingNew - (1 << ((size*size)-cnt-1));
			}
		}
		
		return boxesRemainingNew;
	}
	
	
	public static int[] followChain(int endBox, long[] boxStates, long[] boxEdges, int size) {
		
		int current = endBox;
		int pos = 1;
		int[] chain = new int[size*size];
		int previous = -1;
		int hold = current;
		
		chain[0] = current;
		
		for(int cnt = 0; cnt < chain.length-1 && current != -1; cnt++) {
			
			hold = current;
			
			current = nextBox(current, previous, boxStates, boxEdges, size);
			previous = hold;
			
			chain[pos] = current;
			pos++;	
		}
		
		return chain;
	}
	
	
	public static long takeOutBoxes(int[] chain, long boxesRemaining, int size) {
		long boxesRemainingNew = boxesRemaining;
		
		for(int cnt = 0; cnt < chain.length && chain[cnt] != -1; cnt++) {
			boxesRemainingNew = boxesRemainingNew - (1 << ((size*size)-chain[cnt]-1));
		}
		
		return boxesRemainingNew;
	}
	
	
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
