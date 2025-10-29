//Jack Newman.
//Date: 12/14/2024:
//Tester: Used to generate random perumations of all inputs used and files given. Gives you multplie options to brute test mutplie optinos to see if there is any errors. 

public class Tester {
	static String[] fp = {  // -f <FILENAME>  Reads the environment from the specified file; see File Format section.
			 "../457-ML-04-JN/a04-data/cliff-problem.txt",
			 "../457-ML-04-JN/a04-data/cliff-prob-small.txt",
			 "../457-ML-04-JN/a04-data/maze.txt",
			 "../457-ML-04-JN/a04-data/pipe-world.txt",
			 "../457-ML-04-JN/a04-data/simple-cliff.txt",
			 "../457-ML-04-JN/a04-data/simple-example.txt",
			 "../457-ML-04-JN/a04-data/simple-minefield.txt",
			 "../457-ML-04-JN/a04-data/simpler-example.txt",
			 "../457-ML-04-JN/a04-data/wall-hack.txt"
			};  
			static double[] epsilonValues = {0.9, .3};          		// -e <DOUBLE>    Policy randomness value eplison e [0, 1]; default 0.9.
			static double[] alphaValues = {0.9, .2};            		// -a <DOUBLE>    Learning rate (step size) alpha e [0, 1]; default 0
			static double[] gammaValues = {0.9};            				// -g <DOUBLE>    Discount rate g e [0, 1] for learning; default 0.9.
			static int[] nEpsilonValues = {200};             				// -ne <INTEGER>  Decay control for randomness threshold eplison; default 200.
			static int[] nAlphaValues = {1000};              				// -na <INTEGER>  Decay control for learning rate alpha; default 1000.
			static int[] episodesValues = {10000,1, 100};       		// -T <INTEGER>   Number of learning episodes (trials); default 10000
			static double[] successProbValues = {0.8, .7};      		// -p <DOUBLE>    Action success probability p e [0, 1]; default 0.8.
			static int[] verbosityValues = {1};              				// -v <integer>   Verbosity level for program output; default 1.
			static boolean[] useQLearningValues = {false, true};    // -q <boolean>   Toggles Q-Learning (off-policy updates); default is SARSA (on-policy updates).
			static boolean[] useUnicodeValues = {true};      				// -u <boolean>   Toggles Unicode output; disabled by default.

	

	public static void main(String[] args) throws Throwable{
		runner();
		//genPermutations();
	}
	
//runner: run a specific config in mind, just plop in the string. 	
	public static void runner() throws Throwable {
		short option = 7;
		String tmpArgs = "-f ../457-ML-04-JN/a04-data/" + switch(option) {
			case 1 -> "simple-example.txt -v 2 -u";
			case 2 -> "simple-example.txt -v 2 -q";
			case 3 -> "simpler-example.txt -v 3 -T 1200";
			case 4 -> "simple-example.txt -v 4 -q -T 200 -na 100 -ne 50";
			
			case 5 -> "cliff-problem.txt";
			case 6 -> "cliff-prob-small.txt";
			case 7 -> "maze.txt -u";
			case 9 -> "pipe-world.txt -u";
			case 10 -> "simple-cliff.txt";
			case 11 -> "simple-example.txt";
			case 12 -> "simple-minefield.txt";
			case 13 -> "wall-hack.txt";
			case 14 -> "test.txt -v 3";
			default-> "simple-example.txt -v 1";
		};
		//tmpArgs+=" -u"+" -v 2";
		System.out.println(tmpArgs);  
		double start = System.nanoTime();
		Controller.main(tmpArgs.split(" "));
		System.out.printf("\n �� Total runtime in %.3fms%n", (System.nanoTime() - start) / 1_000_000);
	}
	
//genPeruumutations: runs a bunch of different permutatiions.  
	public static void genPermutations() throws Throwable {
		double perms = 0;
		double startAll = System.nanoTime(); 
		for (String filePath : fp) {
		  for (double epsilon : epsilonValues) {
		   for (double alpha : alphaValues) {
		    for (double gamma : gammaValues) {
		     for (int nEpsilon : nEpsilonValues) {
		      for (int nAlpha : nAlphaValues) {
		       for (int episodes : episodesValues) {
		        for (double successProb : successProbValues) {
		         for (boolean useQLearning : useQLearningValues) {
		          for (boolean useUnicode : useUnicodeValues) {
		           for (int verbosity : verbosityValues) {
		            // Construct the args array
		            String[] ta = {
		             "-f", filePath,        								//0,1
		             "-e", String.valueOf(epsilon),    			//2,3
		             "-a", String.valueOf(alpha),						//4,5
		             "-g", String.valueOf(gamma),						//6,7
		             "-ne", String.valueOf(nEpsilon),				//8,9
		             "-na", String.valueOf(nAlpha),					//10,11
		             "-T", String.valueOf(episodes),				//12,13
		             "-p", String.valueOf(successProb),			//14,15
		             "-v", String.valueOf(verbosity),			 	//16
		             	"", //9 qlearning
		            };
		            if(useQLearning) ta[16]="-q";
		            System.out.print(String.join(" ", ta)+"\n");
		            double start = System.nanoTime(); Controller.main(ta);
		            System.out.printf("\n �� Total runtime in %.3fms%n\n", (System.nanoTime() - start) / 1_000_000);
		            perms++;
		           }
		          }
		         }
		        }
		       }
		      }
		     }
		    }
		   }
		  }
		 }
		 double totalTm = ((System.nanoTime() - startAll) / 1_000_000);
		 double perPerms = totalTm/perms; 
		 System.out.printf("\n �� Total runtime in %.3fms for %f perms, %.3fms per 1 perm %n\n", totalTm, perms, perPerms);
		}
		
	
}
