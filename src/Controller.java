//Jack Newman
//Date: 12/14/2024.
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//Controller: 
//This program implements Sarsa and Q-learning algorithms to navigate a simple grid-world environment within a single class.
//It parses an input file, initializes necessary data structures, learns the environment through iterative training, and tests the learned results.
//Custum features/deviations: I pad my statespace with borders, and my statespace is is represented by rewards. This way, i can do less boolean logic for cell checking, and directly access rewards. 
//Furthermore, I do not have to check out bounds errors, as I can just fundmentally treat the same as block cells. Things im proud of: I like my prime map solution for doing the directions. 

public class Controller { 
	static boolean SpacedPrint = false; //-B makes the learned greedy policy print better, deafault true
	static int DEBUG = 0; 	// -D <INT>: default = 0 = nothing. 
									      	//1= after greedy policy evaultion print, it prints the environment grid with each cell showing its type and StSpace index.
													//2= Border will appear now in grid print. 
													//3= Makes q values print first. Then calls special debug of greedy policy anylsis. 
													//4= Which prints paths off each action in a epsoide and its end reward. Will need special table to understand. 
	
	static double[][] ActVals; 	//Parallel 2d array. StSpace[c][r] maps to states[(CLen)*r+(currentC)][0-3], 0=up, 1=left, 2=right, 3=down. 
	static double[][] StSpace; 	//Reward Representation of the grid, padded with -10,s. Made it a double to avoid implicit casting, if maze was large then it would a byte
	static char[][] StRef; 	   	//Char version of StSpace for printing and debugging reference. 
	static int SX, SY; 		   		//The start states x and y index
	static int StSz; 						//The size of the statespace without the border. Used for early termination of epsoide. 
	static int RLen, 						//Total num of rows w/th borders, frequently used for loops. 
						 CLen;		  			//Total # of columns w/th borders, frequently used for index calculations in Actvals.
	//static double outBounds = -1; If you want to not punish out of bounds or treat it differenlty. Make border =-3, 
	//and uncommment all lines //else if(rwrd==-3){ apX=aX; apY=aY; rwrd=outBounds;} 
	
	

 //==============================================================================================	
 //Set up
	//Main: parses input commands, and then calls, generateEnviroment, train, finalEval, printGreedyPolicy, and printQvalues. 
	
	static void main(String[] args) throws Throwable {
		String fileName = "";              // -f <FILENAME>  Reads the environment from the specified file; see File Format section.
		double epsilon = 0.9;              // -e <DOUBLE>    Policy randomness value eplison e [0, 1]; default 0.9.
		double alpha = 0.9;                // -a <DOUBLE>    Learning rate (step size) alpah e [0, 1]; default 0.9
		double gamma = 0.9;                // -g <DOUBLE>    Discount rate g e [0, 1] for learning; default 0.9.
		int nEpsilon = 200;                // -ne <INTEGER>  Decay control for randomness threshold ε; default 200.
		int nAlpha = 1000;                 // -na <INTEGER>  Decay control for learning rate α; default 1000.
		int episodes = 10_000;              // -T <INTEGER>   Number of learning episodes (trials); default 10000
		double successProb = 0.8;          // -p <DOUBLE>    Action success probability p e [0, 1]; default 0.8.
		boolean useQLearning = false;      // -q <boolean>   Toggles Q-Learning (off-policy updates); default is SARSA (on-policy updates).
		boolean useUnicode = false;        // -u <boolean>   Toggles Unicode output; disabled by default.
		int verbosity = 1;                 // -v <integer>   Verbosity level for program output; default 1. 															 
		//I SWITCHED BACK TO USING INT, because to do comparsions you need to implicity CAST IT. 

		for (int i = 0; i < args.length; i++) {
	    switch (args[i]) {
	        case "-f" -> fileName = args[++i];
	        case "-e" -> epsilon = Double.parseDouble(args[++i]);
	        case "-a" -> alpha = Double.parseDouble(args[++i]);
	        case "-g" -> gamma = Double.parseDouble(args[++i]);
	        case "-ne" -> nEpsilon = Integer.parseInt(args[++i]);
	        case "-na" -> nAlpha = Integer.parseInt(args[++i]);
	        case "-T" -> episodes = Integer.parseInt(args[++i]);
	        case "-p" -> successProb = Double.parseDouble(args[++i]);
	        case "-v" -> verbosity = Integer.parseInt(args[++i]);
	        case "-q" -> useQLearning = true;
	        case "-u" -> useUnicode = true;
	        case "-B" -> SpacedPrint = true;
	        case "-D" -> DEBUG = Integer.parseInt(args[++i]);
	    }
		}
		if(!fileName.equals("")) {
			generateEnviorment(fileName); //Intiilization of data structures. 
			train(epsilon, alpha, gamma, nEpsilon, nAlpha, successProb, episodes, verbosity, useQLearning); 
			if(DEBUG<2) finalEval(successProb); 
			if(!SpacedPrint) System.out.print("\n* Learned greedy policy: \n"); 
			else System.out.print("\n* Learned greedy policy: with spaced print on\n\n");
			printGreedyPolicy(useUnicode);
			if(verbosity>1) printQValues();
			if (DEBUG>2) evaluatePolicyDebug(successProb);
		}
		else System.out.print("The file name was incorrect");
	}
	
	//Sets up my global varibles and data structures needed to parse this program. 
	
	private static void generateEnviorment(String fileName) throws Throwable { 
		BufferedReader br = new BufferedReader(new FileReader(fileName)); //Buffered reader to read 
		System.out.println("* Reading"+fileName);
		ArrayList<String> parser = new ArrayList<>();  			 //Keeps, track of every row of data.   
		String ln;																						//tmp string, to traverse the data. 

		while(( (ln = br.readLine()) !=null )) {
			if(!ln.contains("#") && !ln.equals("")) parser.add("N"+ln.toUpperCase()+"N"); //I read each row at a time. I pad in the ends. 
		}
		RLen = parser.size()+2; CLen = parser.get(0).length(); 		 
		parser.addFirst("N".repeat(CLen)); 
		parser.add("N".repeat(CLen));
		StSz = (RLen-2)*(CLen-2);
		StRef = new char[RLen][CLen];
		StSpace = new double[RLen][CLen];
		ActVals = new double[RLen*CLen][4];
		

		for(int r=0; r < RLen; r++) { 
			ln = parser.removeLast(); 
			int sy = ln.indexOf("S");
			if(sy!=-1) { SX = r; SY=sy; } 
			char[] contents = ln.toCharArray();
			for(int i=0; i<CLen; i++){
				char point = contents[i];
				StRef[r][i]=point;
				StSpace[r][i] = switch(point){
					case'M'->-100; //explosive mine: terminates episode when entered 
					case'G'-> 10;	 //goal: terminates episode when entered 
					case'C'->-20;	 //cliff: sends you back to start when exiting it. 
					case'B'->-2;   //Border
					case'N'->-2;   
					default->-1;
					//case 'S': 
				};
			}
			
		}
	}
	

 //==============================================================================================	
 //Learning and Evaulating. 
	
	//train: 
	//Description: This program trains the modeel for x episodes. An epsoide ends if it has done enough moves = to the search space, or if it reaches a terminal state. 
	//Q-learning and Sarsa Learning is controlled by the varible called actionTaken. Which just represents q′, which it either equals the previoussly found q′, or it takes the greedy action. Based off if 
	//if offPolicy is true or not. I move via x and y cordinates, amd access each x and y cordinates values by calcuatling [(CLen)*r+(currentC)][0-3]
	
	private static void train(double epsilon, double alpha, double gamma, int nE, int nA, final double p, final int episodes, final int v, final boolean offPolicy) {
		int print = episodes/10;
		Random rand = new Random(); 
		int aUpdate= episodes/nA, eUpdate=episodes/nE;
		String tmp = offPolicy? "Q-learning...": "SARSA...";
		System.out.printf("* Beginning %,d learning episodes with %s\n", episodes, tmp);
		if(v>2){
			System.out.println("  * After     Avg. Total Reward for");
			System.out.println("  * Episode   Current Greedy Policy");
		} 
		for(double t=0; t < episodes;){
			boolean toCliff = false, outCliff = false;  //Controlls specific case for cliffs. 
			int aX=SX, aY=SY, aState = aX*CLen+aY; 		//This grabs the starting postions. 
			int action = rand.nextDouble() > epsilon? greedy(aState, rand): rand.nextInt(4);
			double aQ = ActVals[aState][action]; 	    //The chosen Q value in current cell. 
			for(int c=0; c < StSz; c++){
				int apX=aX, apY=aY;	//Represent action a′ new locatiion, T=NA, TT=R/U, 3=D/L FF=L/DOWN				
				int driftDir =  p > rand.nextDouble() ? 0 : rand.nextBoolean() ? 1 : -1; 
				switch(action){ //Up, left, right, down. 
					case 0 -> { apX++; apY+=driftDir;} //up    + drift=Right/none/left
					case 1 -> { apY--; apX+=driftDir;} //left  + drift=Up/none/Down
					case 2 -> { apY++; apX+=driftDir;} //right + drift=Up/none/Down
					default ->{ apX--; apY+=driftDir;} //down  + drift=Right/none/left
				 };
				double rwrd; 
				if(!toCliff) rwrd = StSpace[apX][apY]; //If we did not previously enter cliff, standard reward. 
				else { rwrd = -10; outCliff=true; }    //If we entered the cliff, subsquent reward is -10	 
				if(rwrd!=10.0 && rwrd!=-100.0){		   //No we enter, if it was a cliff,block,path,border. 
					if(rwrd == -20) toCliff = true; 
					else if(rwrd==-2){ apX=aX; apY=aY; rwrd=-1.0;} 			//if block reset and change val. 
					//else if(rwrd==-3){ apX=aX; apY=aY; rwrd=outBounds;} //if border reset positon. 
					int apState = apX*CLen+apY; 	
					int actionPrime = rand.nextDouble() > epsilon? greedy(apState,rand): rand.nextInt(4);
					int	actionTaken = offPolicy? greedy(apState,rand): actionPrime; 
					ActVals[aState][action] = aQ + alpha*(rwrd + (gamma*(ActVals[apState][actionTaken])-aQ) );
					if(outCliff){ //if your moving out of a cliff
						toCliff = outCliff = false; 		   //Reset the booleans. 
						apX=SX; apY=SY; apState=aX*CLen+aY; //reset to starting positon. 
						actionPrime = rand.nextDouble() > epsilon? greedy(apState,rand): rand.nextInt(4); //find next best actionprime
					}
					action = actionPrime; 					//a ← a′ 	
					aX=apX; aY=apY; aState = apState;  		//s ← s′	
					aQ = ActVals[apState][actionTaken]; 	//q = q′
				} 
				else { //Direction ponits towards terimnal state. 
					ActVals[aState][action] = aQ + alpha*(rwrd-aQ); //we update values. 
					break; 
				}
			}
			t++;
			if(v>2 && t%print==0) System.out.printf("        %.0f      %.3f\n",t,evaluatePolicy(p));
			if(t%aUpdate==0) {
				alpha = alpha/( 1 + (t/nA));
				if(v==4) System.out.printf( "    (after episode %.0f, alpha to %.5f)\n",t,alpha);
			}
			if(t%eUpdate==0){
				epsilon = epsilon/(1 + (t/nE));
				if(v==4) System.out.printf( "    (after episode %.0f, eplison to %.5f)\n",t,epsilon);
			}
		}
		System.out.println("  Done with learning!");
	}
							
	//evaulatePolicy
	//Description: Does only whats needed to evaulute the policy. I wanted to seperate them because it was easier and faster. 
	
	private static double evaluatePolicy(final double p){
		double reward = 0;
		Random rand = new Random(); 
		for(int t=0; t < 50; t++){
			boolean toCliff = false, outCliff = false; 
			int aX=SX, aY=SY, aState = aX*CLen+aY; 
			int action = greedy(aState,rand);
			int c =0;
			for(c =0; c < StSz; c++){ 
				int apX=aX, apY=aY; //T=NA, TT=R/U, 3=D/L FF=L/DOW
				int driftDir =  p > rand.nextDouble() ? 0 : rand.nextBoolean() ? 1 : -1; 
				switch(action){ //Up, left, right, down. 
					case 0 -> { apX++; apY+=driftDir;} //up    +- drift=Right/none/left
					case 1 -> { apY--; apX+=driftDir;} //left  +- drift=Up/none/Down
					case 2 -> { apY++; apX+=driftDir;} //right +- drift=Up/none/Down
					default ->{ apX--; apY+=driftDir;} //down  +- drift=Right/none/left
				 };
				double rwrd; 
				if(!toCliff) rwrd = StSpace[apX][apY]; //If we did not previously enter cliff, standard reward. 
				else { rwrd = -10; outCliff=true; }    //If we entered the cliff, subsquent reward is -10	   
				if(rwrd==-2){ apX=aX; apY=aY; rwrd=-1;} //if border or null, reset and change val. 
				//else if(rwrd==-3){ apX=aX; apY=aY; rwrd=outBounds;}
				reward+=rwrd;
				if(rwrd!=10.0 && rwrd!=-100.0){		   //No we enter, if it was a cliff,block,path,border. 
					if(rwrd == -20) toCliff = true; 
					int apState = apX*CLen+apY; 
					if(outCliff){
						toCliff = outCliff = false;      	//Reset the booleans. 
						apX=SX; apY=SY; apState=aX*CLen+aY; //reset to starting positon. 		
					}
					action = greedy(apState,rand); 			    //a ← a′ 		
					aX=apX; aY=apY; aState = aX*CLen+aY;  	//s ← s′
				}
				else break;
			}
		}	
		return reward/50.0; 
	}
	
	//greedy: Finds the maxium q value, and breaks ties at random.
	
	private static int greedy(int loc, Random rand){
		double[] actionValues = ActVals[loc];
		double max= actionValues[0]; 
		int maxLocation = 0; 
		for(int i=0; i < 4; i++){ //finds min value
			double qValue = actionValues[i]; 
			if(max==qValue && rand.nextBoolean()){ //breaking ties at random.
				max=qValue; 
				maxLocation = i; 
			}
			else if(max<qValue) { //switching to new q value. 
				max=qValue; 
				maxLocation = i; 
		}}
		return maxLocation;
	}

 //==============================================================================================	
 //Printing Greedy policy and Learned Q values. 

	//Final eval, calls the eevulatePolicy with some extra print statements. 
	private static void finalEval(final double p) {
		System.out.println("* Beginning 50 evaluation episodes...");
		double reward = evaluatePolicy(p);
		System.out.printf("  Avg. Total Reward of Learned Policy: %.3f",(reward));
	}

	//printGreedyPolicy: prints the enviroment and the best learned policy for each cell. It does by evulating each q value and their direction. It finds the max one and any other ones equal to it. 
	//Each varition of combintions of up,left,right,down makee a special symbol. Check my baseAction map to understand. This method, creates my baseActionMap, and then parses through the data and retrieves 
	//an action code and uses that code to retriive the correct symbol. 
	
	private static void printGreedyPolicy(boolean useUni){
		int[] mvtMap = new int[]{1,3,5,11};  //Each q value is tied to a specific movement. u=0,l=1,r=2,d=3
		HashMap<Integer, Character> actionMap = baseActionMap(useUni); //creates a base action map. 
		for(int r=RLen-2; r > 0; r--){ //parses through the array backwards to correctly print it
			String row = SpacedPrint? "  ": ""; //extra spacing. 
			for(int c=1; c < CLen-1; c++){
				char type = StRef[r][c]; //If its not a 
				if(type=='M'||type=='G'||type=='B') row+=type;
				else row+=actionMap.get(getActionCode(mvtMap,  r*CLen+c));
				if(SpacedPrint) row+=" ";
			}
			System.out.println(row);
		}
		if(DEBUG>0) debugMap();
	}

	//baseActionMap. 
	//Creates a map, which holds access to characters i use for printing. The codes are derived, by first mapping q values direction to a primee number. Then combinations of directions as sums of prime numbers. 
	//A list of prime numbers, and their combinations give a unique list of sums. So, I map those sums to a list of characters either unicode or Ascci.	
	private static HashMap<Integer, Character> baseActionMap(boolean useUni) {
		/** 	
			Q values locations:      UP=0,   Left=1,  Right=2,   Down=4
			Prime map to Q PM :      0=1,    1=3,     2=5,       3=11
			PM   Base Action Set         ASCII   Java Char   Unicode   
			 1   Up                      ^       ↑           \u2191     
			 3   Left                    <       ←           \u2190     
			 5   Right                   >       →           \u2192     
			 11  Down                    v       ↓           \u2193     
			 8   Left, Right             -       ↔           \u2194     
			 12  Up, Down                |       ↕           \u2195     
			 4   Up, Left                \       ↖           \u2196     
			 6   Up, Right               /       ↗           \u2197     
			 16  Down, Right             \       ↘           \u2198     
			 14  Down, Left              /       ↙           \u2199     
			 17  Up, Down, Right         >       ⊢           \u22a2     
			 15  Up, Down, Left          <       ⊣           \u22a3     
			 19  Down, Left, Right       v       ⊤           \u22a4     
			 9   Up, Left, Right         ^       ⊥           \u22a5     
			 20  Up, Down, Left, Right   +       +           +     

		*/
		char[] chars;	
		HashMap<Integer, Character> baseActionMap = new HashMap<>(15);
		int[] keys = {1, 3, 5, 11, 8, 12, 4, 6, 16, 14, 17, 15, 19, 9, 20}; //Unique combinations of all prime numbers. 
		if(useUni) chars = new char[]{'↑','←','→', '↓', '↔', '↕', '↖', '↗', '↘', '↙', '⊢', '⊣', '⊤', '⊥', '+'};
		else chars = new char[]{'^','<','>','v','-','|','\\','/','\\','/','>','<','v','^','+'};	
		for (int i = 0; i < keys.length; i++) baseActionMap.put(keys[i], chars[i]);
		return baseActionMap;
		//{'\u2191', '\u2190', '\u2192', '\u2193', '\u2194', '\u2195', '\u2196', '\u2197', '\u2198', '\u2199', '\u22a2', '\u22a3', '\u22a4', '\u22a5', '+'};
	}

	//GetActionCode
	//Q values Directions (Up, Down, Left, Right) are mapped to indices (0-3) in a prime array {1, 3, 5, 11}. 
	//I find min q index and other indexs == min q index value.  I then sum the primes corresponding to the directions and use that sum as a code, and then access it. Ez
	
	private static int getActionCode(int[] movementMap, int actionLocation){
		double[] ActionValues = ActVals[actionLocation]; 
		double max = ActionValues[0];
		int actionCode = 0; 
		for(int i=1; i < 4; i++){ //finds min value
			if(max<ActionValues[i]) { 
				max=ActionValues[i]; 
				}
		}
		for(int i=0; i < 4; i++){
			if(max==ActionValues[i]){
				actionCode+=movementMap[i];
			} 
		}
		return actionCode;
	}

	//printQValues: prints all my Q values for each cell.  
	
	private static void printQValues(){
		System.out.println("* Learned Q values:");
		String border = "" ;		
		for(int i=1; i < CLen-1; i++) border+="-----------";
		String[] formats = new String[]{ "  %6.1f  |","%-6.1f    |","    %6.1f|","  %6.1f  |"};
		for(int r=RLen-2; r > 0 ; r--){  //Go through enviroment. Above: format mapping: 0=up, 1=left, 2=right, 3=down. 
			System.out.println(border+"-");			
			for(int q=0; q<4; q++){
				int actionLoc=r*CLen+0; //border location.
				String format = formats[q]; //grab typee of formatt. 
				StringBuilder row = new StringBuilder((CLen-2)*12).append("|"); //size of cell=12 with ||, 
				for(int i=1; i < CLen-1; i++) { //Move across an entire row grabbing all up q valuee. 
					row.append(String.format(format, ActVals[++actionLoc][q]));
				}
				System.out.println(row.toString()); //print them. 
			}
		}
		System.out.println(border+"-");	//print final border. 
	
	}

	
	//==============================================================================================	
	//Extra debuggers/more information. 
	
	//Debugmap: After greedy policy evaultion print, 
	 	//1=it prints the environment grid with each cell showing its type and StSpace index.
		//2= Border will appear now in grid print. 
	
	private static void debugMap() {
		System.out.println("\nArray perspective");
		List<String> swap = new ArrayList<String>(StRef.length);
		for(int r=0, v=0; r < RLen; r++){
			String row = "   ";
			for(int c=0; c < CLen; c++, v++) {
				char val = StRef[r][c];
				if(val!='N' || DEBUG==2) row += String.format("%s%-6s", val,v);
			}	
			swap.add(row+"\n");
		}
		for(int i=swap.size()-1; i >= 0; i--) System.out.print(swap.get(i));
		System.out.println("");
	}

	
 //EvulatePolicyDebug
	//3= Makes q values print first. Then calls special debug of greedy policy anylsis. 
	//4= Which prints paths off each action in a epsoide and its end reward. Will need special table to understand.
	
	private static double evaluatePolicyDebug(final double p){
		double reward = 0;
		Random rand = new Random(); 
		
		for(int t=0; t < 50; t++){
			boolean toCliff = false, outCliff = false; 
			int aX=SX, aY=SY, aState = aX*CLen+aY; 
			int action = greedy(aState,rand);
			int c =0;
			if(DEBUG==4) System.out.printf("%-2s %d: ", "Ep", t);
			for(c =0; c < StSz; c++){ 
				double r = rand.nextDouble();//driftdir: 1=L/DOWN, 2=0, 3=R/UP
				int driftDir =  (p > r) ? 0 : (r >= (p - 1.0) / 2.0) ? 1 : -1;
				int apX=aX, apY=aY;
				switch(action){ //Up, left, right, down. 
					case 0 -> { apX++; apY+=driftDir;} //up    +- drift=Right/none/left
					case 1 -> { apY--; apX+=driftDir;} //left  +- drift=Up/none/Down
					case 2 -> { apY++; apX+=driftDir;} //right +- drift=Up/none/Down
					default ->{ apX--; apY+=driftDir;} //down  +- drift=Right/none/left
				 };
				double rwrd; 
				if(DEBUG==4) System.out.print(""+aState+"→"+(apX*CLen+apY));
				if(!toCliff) rwrd = StSpace[apX][apY]; //If we did not previously enter cliff, standard reward. 
				else { rwrd = -10; outCliff=true; }    //If we entered the cliff, subsquent reward is -10	   
				if(rwrd==-2){ apX=aX; apY=aY; rwrd=-1;} //if border or null, reset and change val. 
				//else if(rwrd==-3){ apX=aX; apY=aY; rwrd=outBounds;} //if border reset positon. 
				reward+=rwrd;
				if(rwrd!=10.0 && rwrd!=-100.0){		   //No we enter, if it was a cliff,block,path,border. 
					if(rwrd == -20) toCliff = true; 
					int apState = apX*CLen+apY; 
					if(outCliff){
						toCliff = outCliff = false;      	//Reset the booleans. 
						apX=SX; apY=SY; apState=aX*CLen+aY; //reset to starting positon. 
						if(DEBUG==4) System.out.print("→"+apState);
					}
					if(DEBUG==4) System.out.print(",");
					action = greedy(apState,rand); 			    //a ← a′ 		
					aX=apX; aY=apY; aState = aX*CLen+aY;  	//s ← s′
				}
				else break;
			}
			if(DEBUG==4) {
				System.out.printf("\n•› Moved: %d Avg Rwrd: (%.2f)\n\n", c, reward / c);
			}
		}	
		return reward/50.0; 
	}
	

	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
