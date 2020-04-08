/**
 * The goal of this code was to explore the use of the bitshift operator 
 * 
 * 
 * 
 * 
 * 
 */
import java.util.Arrays;

public class RotationTesting {

	public static void main(String[] args) {
		
		
		long data = 0b111110111011L;
		//            111011111011
		long data2 = 0b101010010101101010010101L;
		long data3 = 0b101010111111010101000000L;
		
//		System.out.println(Long.toBinaryString(Long.rotateRight(data, 1)));
//		System.out.println(64-Long.numberOfLeadingZeros(data));
		
//
//		System.out.println(0b1000L>2);
//		System.out.println();
//		System.out.println();
		System.out.println(Long.toBinaryString(data));
		System.out.println(Long.toBinaryString(reflect(data,12,2)));
//		System.out.println(reflect(data,12,2));

		System.out.println();
		System.out.println();
		System.out.println(Long.toBinaryString(data));
		System.out.println(Long.toBinaryString(circularShift(data, 12)));
		System.out.println(Long.toBinaryString(circularShift(circularShift(data, 12),12)));
		System.out.println(Long.toBinaryString(data3));
		System.out.println(Long.toBinaryString(circularShift(data3, 24)));
		System.out.println(Long.toBinaryString(circularShift(circularShift(data3, 24),24)));
	}
	
	public static long circularShift(long binaryBoard, int size) {
		int shift = size/4;
		return (((binaryBoard<<shift)|(binaryBoard>>>(size-shift)))%(1<<size));
	}
	
	public static long reflect(long board, int size, int sideLen) {
		int shift = size/4;
		int move = 0;
		
		long res = 0;
		for(int i = 0; i<4; i++) {
			long quad = 0;
			int hold = 0;
			for(int j = 1; j<=sideLen; j++) {
				hold = hold + j;
				int loc = (hold)+(i*shift);
				System.out.println();
				long a = board<<(Long.SIZE-loc);
				//long b = board<<(Long.SIZE-loc+move);
				long b = Long.reverse(a);
//				long c = b&(long)Math.pow(sideLen, (int)((j*j+j)/2));
				//long c = b&(((long)Math.pow(2, j+1)-1)<<(Long.SIZE-j));
				long c = b<<(Long.SIZE-move);
				
				quad += c;
				
				System.out.println(Long.SIZE-loc);
				System.out.println("a: " + Long.toBinaryString(a));
				System.out.println("b: " + Long.toBinaryString(b));
				//System.out.println(Long.toBinaryString((((long)Math.pow(2, j+1)-1)<<(Long.SIZE-j+1))));
				System.out.println("move: " + move);
				System.out.println("c: " + Long.toBinaryString(c));
				
				move = move + j;
//				System.out.println("res: " + Long.toBinaryString(c));
//		
//				
//				System.out.println(Long.toBinaryString((Long.reverse(board<<(Long.SIZE-loc-1)))&(1<<((j*j+j)/2))));
//				System.out.println(loc);
//				System.out.println(Long.toBinaryString(Long.reverse(board<<(Long.SIZE-loc-1))));
//				quad+=((Long.reverse(board<<(Long.SIZE-loc-1)))&((long)Math.pow(2, j+1)-1))<<loc;
//				System.out.println(Long.toBinaryString(Long.reverse(board<<(Long.SIZE-loc-1)))&(1<<((j*j+j)/2)));
		
			}
			System.out.println("quad " + Long.toBinaryString(quad));
//			res+=Long.toBinaryString(quad);
			int extra = sideLen*(sideLen+1)/2;
			switch(i) {
				case 0: res = quad;break;
				case 1: res += quad<<(extra*3);
				case 2: res += quad<<extra;
				case 3: res += quad<<(extra*2);
			}
			System.out.println("res: " + Long.toBinaryString(res));
		}
		return res;
	}
	public static int findEdge(int index, int sideLen) {
		// len of the side of the edge
		int s = sideLen;
		// num of edges
		int e = 2*(s*(s+1));
		// quadrant 
		int quadrant = index/(e/4);
		// whether the square is even or odd
		boolean parity = s%2==0;
		int n = s/2;
		// num of all boxes in the quadrant
		int boxes = parity?(n*n+n):((n+1)*(n+1));
		int i = 0;
		while(e-index+i-1>0) 
			i++;
		return -1;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static long toLong(int[] board) {
		long res = 0;
		
		for(int i = 0; i<board.length; i++) {
			if(board[i]==1)
				res+=Math.pow(2, board.length-i-1);
		}
		return res;
	}
	public static String toString(int[] board) {
		String res = "";
		int len = board.length;
		
		for(int i = 0; i<len; i++) {
				res+=board[i];
		}
		return res;
	}
	public static int[] toArray(long board) {
		String str = Long.toBinaryString(board);
		int len = str.length();
		int[] res = new int[len];
		
		for(int i = 0; i<len; i++) {
				res[i] = str.charAt(i);
		}
		return res;
	}
	public static int[] toArray(String board) {
		String str = board;
		int len = str.length();
		int[] res = new int[len];
		
		for(int i = 0; i<len; i++) {
				res[i] = Integer.parseInt(str.charAt(i)+"");
		}
		return res;
	}
	
	public static int[] newFormToTraditional(int[] board) {
		 int numEdges = (int)Math.floor(Math.sqrt(1+(2*board.length))/2);
		 
		 int[] newBoard = new int[board.length];
		 
		 for(int i = 0; i<board.length; i++) {
			 newBoard[i] = board[i];
		 }
		 
		 return newBoard;
	}

	public static long toBinary(boolean[] board) {
		long res = 0b0L;
		for(int i = 0; i<board.length; i++) {
			res*=10;
			if(board[i])
				res+=1;
		}
		return res;
	}
	
	public static long toBinary(int[] board) {
		long res = 0b0;
		for(int i = 0; i<board.length; i++) {
			res*=10;
			if(board[i]==1)
				res+=1;
		}
		return res;
	}
	
	public static String circularShift(String binaryBoard) {
		int len = binaryBoard.length();
		int split = len/4;
		return binaryBoard.substring(split, len)+binaryBoard.substring(0, split);
	}
	
	
	public static String rotate(String state){
		
		String newState = "";
		
		int height = (int)Math.floor(Math.sqrt(1+(2*state.length()))/2);

		for(int i = 0; i < state.length(); i++){
			newState = newState + state.charAt(DotsAndBoxes.rotationMap[height-1][i]);
			
		}
		
		return newState;
	}	
	
	
	
	
}
