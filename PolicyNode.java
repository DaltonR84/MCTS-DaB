import java.util.Random;

/* Created by Jared Prince
 * 1/31/2017
 * You may add to this note, but not remove it */

/* This is an implementation of a node in a Monte Carlo Tree Search
 */

public class PolicyNode{

	public static Random r = new Random();
	
	public GameState state; // the state as an int
	public int timesReached; // the times this node was reached - N(s)
	public int moves; // the depth in the tree of this node
	
	public ActionLink[] links;
	
	public PolicyNodeTree tree;

	public PolicyNode(GameState state, int moves, int[] actions, PolicyNodeTree tree) {
		this.tree = tree;
		
		this.state = state;
		this.moves = moves;
		
		timesReached = 1;
		
		links = new ActionLink[actions.length];
		
		for(int i = 0; i < links.length; i++){
			links[i] = new ActionLink(actions[i], null);
		}
	}

	// gets the next action based on the averages and the uncertainty bonus
	public int getNextAction(double c) {
		
		if(c > 0){
			return links[0].action;
		}
		
		int action = -1;
		double max = -50;
		
		for(int i = 0; i < links.length; i++){
			double val = links[i].getValue(false);
			if(val > max || (val == max && r.nextDouble() < .5)){
				max = val;
				action = links[i].action;
			}
		}
		
		return action;
	}

	// returns the child node picked based on uncertainty bonus and average
	public PolicyNode getNode(int action, PolicyNode root, boolean override) {
		
		// for every action
		for (int i = 0; i < links.length; i++) {
			// if the action is the one given, return the child at i
			if (links[i].action == action){
				if(links[i].child != null){
					return links[i].child;
				}
				
				else if(moves == 0 || override){
					
					//otherwise, make a new node and return it
					PolicyNode newNode = getNextNode(action);
					PolicyNode oldNode = tree.findNode(newNode);
					
					if(oldNode == null){
						links[i].child = newNode;
						tree.numNodes++;
						tree.totalDepth += moves;
						
						tree.nodeTable.put(newNode.state.getString(), newNode);
					} else {
						if(!newNode.equals(oldNode)){
							System.out.println("Hash Table Error");
						}
						
						links[i].child = oldNode;
					}
					
					return links[i].child;
				}
				
				else{
					//return no node
					if(links[i].timesChosen < PolicyNodeTree.NODE_CREATION_COUNT){
						return null;
					}
					
					//make a new node
					else if(links[i].timesChosen == PolicyNodeTree.NODE_CREATION_COUNT){
						
						PolicyNode newNode = getNextNode(action);
						PolicyNode oldNode = tree.findNode(newNode);
						
						//add a connection to the old node if it was found
						if(oldNode != null){
							if(!newNode.equals(oldNode)){
								System.out.println("Hash Table Error");
							}
							
							links[i].child = oldNode;
						}
						
						//return the new node
						else{
							tree.numNodes++;
							tree.totalDepth += moves;
							links[i].child = newNode;
							
							tree.nodeTable.put(newNode.state.getString(), newNode);
						}
						
						return links[i].child;
					}
				}
			}
		}

		return null;
	}

	// return the child equivalent to p, if it exists
	/*Deprecated with the addition of the Hashtable*/
	public PolicyNode findChild(PolicyNode p) {
		if (equals(p)) {
			return this;
		}
		
		// if p can't be a child, skip
		if (!tree.game.possibleChild(this.state, p.state)) {
			return null;
		}

		// if p has <= moves of this node, it can't be a child
		if (p.moves <= moves) {
			return null;
		}

		// for every child
		for (int i = 0; i < links.length; i++) {
			
			if(links[i].child != null){
				
				try{
					// find p in the subtree of child i
					PolicyNode p2 = links[i].child.findChild(p);
					
					// if found, return
					if (p2 != null)
						return p2;
				} catch (StackOverflowError e) {e.printStackTrace();}
			}
		}

		//null by default
		return null;
	}

	// checks if this node is equivalent to p
	public boolean equals(PolicyNode p) {
		if (p.state.equals(this.state)) {
			return true;
		}

		return false;
	}
	
	public int hashCode(){
		return state.getString().hashCode();
	}

	// add a value to the average for this action
	public void addValue(int action, int value, double c) {
		timesReached++;
		
		//find the correct index
		int index = -1;
		for (int i = 0; i < links.length; i++) {		
			if (links[i].action == action) {
				index = i;
				break;
			}
		}
		
		links[index].update(value);
		
		//update the bonuses and reorder the list
		for(int i = 0; i < links.length; i++){
			links[i].updateBonus(timesReached, c);
			
			int t = i;
			
			//move link up the queue while it's value is greater than the link before it
			while(t > 0 && links[t].getValue(true) > links[t - 1].getValue(true)){
				ActionLink tempLink = links[t];
				links[t] = links[t - 1];
				links[t - 1] = tempLink;
				
				t--;
			}
			
			//move link down the queue while it's value is less than the link after it
			while(t < links.length - 1 && links[t].getValue(true) < links[t + 1].getValue(true)){
				ActionLink tempLink = links[t];
				links[t] = links[t + 1];
				links[t + 1] = tempLink;
				
				t++;
			}
		}
	}
	
	//returns a newly created node that is the successor of the current node given action
	public PolicyNode getNextNode(int action){
		
		if(state instanceof GameStateScored){
			GameStateScored newState = tree.game.getSuccessorState((GameStateScored)state, action);
			return new PolicyNode(newState, moves + 1, tree.game.getActions(newState), tree);
		} else{
			//get the next state
			GameState newState = tree.game.getSuccessorState(state, action);
			return new PolicyNode(newState, moves + 1, tree.game.getActions(newState), tree);
		}
	}
}

class ActionLink {
	
	int action;
	int timesChosen = 0;
	
	double rewards;
	double bonus = 10;
	
	PolicyNode child;
	
	public ActionLink(int action, PolicyNode child){
		this.child = child;
		this.action = action;
	}
	
	public void update(int reward){
		this.rewards += reward;
		timesChosen++;
	}
	
	public void updateBonus(int timesReached, double c){
		this.bonus = c * Math.sqrt(Math.log(timesReached) / timesChosen);
	}
	
	public double getValue(boolean applyBonus){
		if(timesChosen == 0){
			return bonus;
		}
		
		return (rewards / timesChosen) + (applyBonus ? bonus : 0);
	}
}