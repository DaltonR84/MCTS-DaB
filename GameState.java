import java.math.BigInteger;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this not, but not remove it */

/* This class represents a single state of a game consisting only of a whole number
 * BigInteger is used when a long is too short
 */

public class GameState {

	public long longState;			//the game state represented by a long (when small enough)
	BigInteger bigState;	//the game state represented by a bigint (when too big for a long)
	
	public GameState(long state){
		this.longState = state;
	}
	
	public GameState(BigInteger state){
		this.bigState = state;
	}
	
	public GameState(String state, boolean inBinary){
		if(inBinary){
			try{
				longState = new Long(Long.parseLong(state,2));
			} catch (Exception e) {
				//parse binary to big int
				bigState = new BigInteger(state, 2);
			}
		}
		
		else{
			try{
				longState = new Long(state);
			} catch (NumberFormatException e) {
				bigState = new BigInteger(state);
			}
		}
	}
	
	public boolean equals(GameState secondState){		
		if(longState == secondState.longState){
			if(bigState == null && secondState.bigState == null){
				return true;
			}
			
			if(bigState != null && secondState.bigState != null){
				if(bigState.equals(secondState.bigState)){
					return true;
				}
			}
			
			return false;
		}
		
		return false;
	}
	
	
	//returns the state as a binary string
	public String getBinaryString(){
		if(bigState != null){
			return bigState.toString(2);
		}
		
		return Long.toBinaryString(longState);
	}
	
	//returns the state as a string in base 10
	public String getString(){
		if(bigState != null){
			return bigState.toString();
		}
		
		return Long.toString(longState);
	}
	
	public long getLongState() {
		return longState;
	}
}
