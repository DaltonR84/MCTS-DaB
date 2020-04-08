import java.util.Hashtable;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it*/

/* This is a tree class that contains the root node of the policy tree */

public class PolicyNodeTree {

	int numNodes = 1;
	long totalDepth = 0;
	public final static int NODE_CREATION_COUNT = 1;
	public MCTSGame game;
	PolicyNode root;
	
	public Hashtable<String, PolicyNode> nodeTable = new Hashtable<String, PolicyNode>();

	public PolicyNodeTree(MCTSGame game, GameState state) {
		this.game = game;
		
		//initialize the root
		root = new PolicyNode(state, 0, game.getActions(state), this);
		nodeTable.put(root.state.getString(), root);
	}

	// returns the node equivalent to p, if it exists
	public PolicyNode findNode(PolicyNode p) {
		return nodeTable.get(p.state.getString());
	}
}
