import java.math.BigInteger;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it*/

public class GameStateScored extends GameState{

	int playerNetScore;		//the net score for the player whose turn it is
	
	public GameStateScored(BigInteger state, int score) {
		super(state);
		playerNetScore = score;
	}
	
	public GameStateScored(long state, int score){
		super(state);
		this.playerNetScore = score;
	}
	
	public GameStateScored(String state, int score, boolean inBinary){
		super(state, inBinary);
		playerNetScore = score;
	}

	public boolean equals(GameStateScored secondState){
		
		if(secondState.playerNetScore != playerNetScore){
			return false;
		}
		
		if(!super.equals(secondState)){
			return false;
		}
		
		return true;
	}
	
	public boolean equals(GameState secondState){
		if(secondState instanceof GameStateScored){
			return equals((GameStateScored) secondState);
		}
		
		if(!super.equals(secondState)){
			return false;
		}
		
		return true;
	}
	
	public int getScore(){
		return playerNetScore;
	}
	
	public String getString(){
		String str = null;
		
		if(bigState != null){
			str = bigState.toString();
		} else {
			str = Long.toString(longState);
		}
		
		if(this.playerNetScore < 0){
			str = str + playerNetScore;
		} else {
			str = str + "+" + playerNetScore;
		}
		
		return str;
	}
}
