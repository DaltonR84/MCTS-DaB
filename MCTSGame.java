/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it
 */

/* This is the abstract of a MCTS game implementation */

public abstract class MCTSGame {
	public abstract int[] getActions(GameState state);
	public abstract GameState getSuccessorState(GameState state, int action);
	public abstract GameStateScored getSuccessorState(GameStateScored state, int action);
	public abstract boolean possibleChild(GameState state1, GameState state2);
}
