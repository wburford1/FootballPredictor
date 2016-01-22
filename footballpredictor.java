import java.util.*;
import java.io.*;
import java.lang.*;

public class FootballPredictor{
	private static double[] weights;
	private static String[] meanings;
	private static Map<String, double[]> teamValues;
	private static int numberOfValues;
	private static int constant;

	public static void main(String[]args){
		/*
		*type = 0 for testing data against itself
		*type = 1 to predict future using all data
		*/
		int type = 0;
		//set meanings
		Object[] temp = FootballPredictor.readFileToArrayList("weightMeanings.txt").toArray();
		meanings = Arrays.copyOf(temp,temp.length,String[].class);
		numberOfValues = meanings.length-1;//first is the name, which is key in map
		//set weights
		ArrayList<String> tempList = new ArrayList<String>(); //= FootballPredictor.readFileToArrayList("weights.txt");
		if (tempList==null||tempList.size()!=numberOfValues*2) {
			FootballPredictor.weights = new double[numberOfValues*2];
		}
		else{
			FootballPredictor.weights = new double[numberOfValues*2];
			for (int counter=0;counter<tempList.size();counter++) {
				weights[counter] = Double.parseDouble(tempList.get(counter));
			}
		}
		System.out.println("weights = "+Arrays.toString(FootballPredictor.weights));
		//set teamValues
		teamValues = FootballPredictor.readFileToDoubleArrMap("teamData.txt");
		//System.out.println(FootballPredictor.doubleArrMapToString(teamValues));
		constant = 1;
		if (type == 1) {
			FootballPredictor machine = new FootballPredictor();
			String[][] matchups = FootballPredictor.readFileto2DStringArray("matchups.txt");
			if (matchups==null) {
				System.out.println("matchups == null");
				return;
			}
			machine.learn(matchups);

			String[][] games = FootballPredictor.readFileto2DStringArray("futureGames.txt");
			if (games==null) {
				System.out.println("games == null");
				return;
			}
			machine.seeTheFuture(games);
		}
		else if(type == 0) {
			FootballPredictor machine = new FootballPredictor();
			String[][] matchups = FootballPredictor.readFileto2DStringArray("matchups.txt");
			ArrayList<String []> sneakySnake = new ArrayList<String []>();
			for (String[] bam: matchups) {
				sneakySnake.add(bam);
			}
			Collections.shuffle(sneakySnake);
			matchups = sneakySnake.toArray(matchups);
			System.out.println("matchups[0] = "+Arrays.toString(matchups[0]));
		}
		else{
			System.out.println("type not set. exiting");
			return;
		}
	}
	public FootballPredictor(){

	}

	public void learn(String[][] matchups){
		System.out.println("teamValues keys = ");
		Set<String> keySet = FootballPredictor.teamValues.keySet();
		for (String key : keySet) {
			System.out.print(key + ", ");
		}
		System.out.println();
		//double[] weights = Arrays.copyOf(FootballPredictor.weights,FootballPredictor.weights.length);
		double[] weights = new double[FootballPredictor.numberOfValues*2];
		int numberGames = 0;
		for (int counter = 0; counter<matchups.length*8; counter++) {
			numberGames++;
			int place = counter%matchups.length;
			String[] matchup = matchups[place];
			int n = place+1;
			System.out.println("current matchup = "+Arrays.toString(matchup));
			double sum = (double)(FootballPredictor.constant);

			if (FootballPredictor.teamValues.containsKey(matchup[0])&&FootballPredictor.teamValues.containsKey(matchup[1])) {
				double[] away = Arrays.copyOf(FootballPredictor.teamValues.get(matchup[0]),FootballPredictor.numberOfValues);
				double[] home = Arrays.copyOf(FootballPredictor.teamValues.get(matchup[1]),FootballPredictor.numberOfValues);
				//System.out.println("away = "+Arrays.toString(away));
				double[] weightedAway = new double[FootballPredictor.numberOfValues];
				double[] weightedHome = new double[FootballPredictor.numberOfValues];
				for (int x = 0; x<FootballPredictor.numberOfValues; x++) {
					weightedAway[x] = away[x]*weights[x];
					weightedHome[x] = home[x]*weights[FootballPredictor.numberOfValues+x];
					sum+=weightedAway[x]+weightedHome[x];
				}
				/*
				*sum is positive and team1 wins, correct
				*sum is negative and team1 wins, wrong
				*sum is positive and team2 wins, wrong
				*sum is negative and team2 wins, correct
				*/
				if ((sum>0&&matchup[2].equals("1"))||(sum<0&&matchup[2].equals("2"))) {
					//correct
				}
				else if (sum<0&&matchup[2].equals("1")) {
					//wrong. should be positive
					//System.out.println("should be first team.");
					//System.out.println("old weights = "+Arrays.toString(weights));
					for (int x=0; x<FootballPredictor.numberOfValues; x++) {
						//System.out.println("weights[x] before = "+weights[x]);
						weights[x] = weights[x]+(1.0/n)*away[x];
						weights[FootballPredictor.numberOfValues+x] = weights[FootballPredictor.numberOfValues+x]+(1/n)*home[x];
						//System.out.println("weights[x] after = "+weights[x]);
					}
					//System.out.println("new weights = "+Arrays.toString(weights));
				}
				else if (sum>0&&matchup[2].equals("2")) {
					//wrong. should be negative
					//System.out.println("should be second team.");
					//System.out.println("old weights = "+Arrays.toString(weights));
					for (int x=0; x<FootballPredictor.numberOfValues; x++) {
						weights[x] = weights[x]-(1/n)*away[x];
						weights[FootballPredictor.numberOfValues+x] = weights[FootballPredictor.numberOfValues+x]-(1/n)*home[x];
					}
					//System.out.println("new weights = "+Arrays.toString(weights));
				}
				else {
					System.out.println("Something went wrong\nMatchup = " + Arrays.toString(matchup)+"\nSum = "+sum);
					System.out.println("\nhome = "+Arrays.toString(home)+"\naway = "+Arrays.toString(away)+"\nweights = "+Arrays.toString(weights));
					System.out.println("matchup[2] = "+matchup[2]);
				}
			}
			else {
				System.out.println("Skipping matchup: " + Arrays.toString(matchup));
				if (!FootballPredictor.teamValues.containsKey(matchup[0])) {
					System.out.println("unrecognized team name: _"+ matchup[0]+"_");
				}
				else if (!FootballPredictor.teamValues.containsKey(matchup[1])) {
					System.out.println("unrecognized team name: _"+ matchup[1]+"_");
				}
			}
		}

		PrintWriter writer;
		PrintWriter writer2;
		try{
			writer = new PrintWriter("weights.txt","UTF-8");
			writer2 = new PrintWriter("namedWeights.txt","UTF-8");
		}catch(FileNotFoundException e){
			System.out.println("Error: "+e);
			return;
		}catch(UnsupportedEncodingException e){
			System.out.println("Error: "+e);
			return;
		}
		for (double weight : weights) {
			writer.print(weight+", ");
		}
		for(int counter = 0; counter<FootballPredictor.numberOfValues; counter++){
			writer2.println(FootballPredictor.meanings[counter+1]+": "+weights[counter]);
		}
		for (int counter = FootballPredictor.numberOfValues; counter<FootballPredictor.numberOfValues*2; counter++) {
			writer2.println(FootballPredictor.meanings[counter-FootballPredictor.numberOfValues+1]+": "+weights[counter]);
		}
		writer.close();
		writer2.close();
		FootballPredictor.weights = weights;
		System.out.println("numberGames = "+numberGames);
	}

	public void seeTheFuture(String[][] games){

		PrintWriter writer;
		try{
			writer = new PrintWriter("predictions.txt","UTF-8");
		}catch(FileNotFoundException e){
			System.out.println("Error: "+e);
			return;
		}catch(UnsupportedEncodingException e){
			System.out.println("Error: "+e);
			return;
		}
		writer.println("Predictions: ");

		for (String[] game : games) {
			String awayName = game[0];
			String homeName = game[1];
			if (FootballPredictor.teamValues.containsKey(awayName)&&FootballPredictor.teamValues.containsKey(homeName)) {
				double prediction = this.predictWinner(FootballPredictor.teamValues.get(awayName), FootballPredictor.teamValues.get(homeName));
				if (prediction>0) {
					writer.println(awayName + " will beat " + homeName+ ". Sum = "+prediction);
				}
				else if (prediction<0) {
					writer.println(homeName + " will beat " + awayName + ". Sum = "+prediction);
				}
				else {
					//indeterminant
					writer.println("Could not determine winner of " + awayName + " vs. "+ homeName+ ". Sum = "+prediction);
				}
			}
		}
		writer.close();
	}

	private double predictWinner(double[] away, double[] home){
		double[] weights = FootballPredictor.weights;
		double sum = FootballPredictor.constant;
		double[] weightedHome = new double[FootballPredictor.numberOfValues];
		double[] weightedAway = new double[FootballPredictor.numberOfValues];
		for (int counter = 0; counter<FootballPredictor.numberOfValues; counter++) {
			weightedAway[counter] = weights[counter]*away[counter];
			weightedHome[counter] = weights[counter+FootballPredictor.numberOfValues]*home[counter];
			sum+=weightedAway[counter]+weightedHome[counter];
		}
		return sum;
	}

	public static ArrayList<String> readFileToArrayList(String fileName){
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    try {
		        String line = br.readLine();
		        ArrayList<String> arrList = new ArrayList<String>();

		        while (line != null) {
		            String[] split = line.split(", ");
		            for (int counter = 0; counter<split.length; counter++) {
		            	arrList.add(split[counter]);
		            }
		            line = br.readLine();
		        }
		        return arrList;
		    }catch(FileNotFoundException e){
		    	System.out.println("Error: "+e);
		   		return null;
			}	finally {
		        br.close();
		    }
		}catch(IOException e){
			System.out.println("Error: "+e);
			return null;
		}
  	}

  	public static Map<String, double[]> readFileToDoubleArrMap(String fileName){
  		Map<String, double[]> map = new HashMap<String, double[]>();
  		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    try {
		        String line = br.readLine();
		        while (line != null) {
		        	double[] doubleArr = new double[numberOfValues];
		        	while(line.trim().isEmpty()){
		        		line = br.readLine();
		        	}
		            String[] split = line.split(", ");
		            for (int counter = 1; counter<split.length; counter++) {
		            	doubleArr[counter-1]=Double.parseDouble(split[counter]);
		            }
		            //doubleArr[numberOfValues-1] = Math.random();
		            map.put(split[0],doubleArr);
		            line = br.readLine();
		        }
		        return map;
		    }catch(FileNotFoundException e){
		    	System.out.println("Error: "+e);
		   		return null;
			}	finally {
		        br.close();
		    }
		}catch(IOException e){
			System.out.println("Error: "+e);
			return null;
		}
  	}

  	public static String doubleArrMapToString(Map<String,double[]> map){
  		Set<String> keys = map.keySet();
  		String output = "";
  		for (String key : keys) {
  			output+=key+": ";
  			output+=Arrays.toString(map.get(key))+"\n";
  		}
  		return output+"\n";
  	}

  	public static String[][] readFileto2DStringArray(String fileName){
  		ArrayList<String[]> stringArrList= new ArrayList<String[]>();
  		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    try {
		        String line = br.readLine();
		        while (line != null) {
		        	while(line.trim().isEmpty()){
		        		line = br.readLine();
		        	}
		            String[] split = line.split(", ");
		            stringArrList.add(split);
		            line = br.readLine();
		        }
		        if (stringArrList.size()==0) {
		        	return null;
		        }
		        String[][] output = new String[stringArrList.size()][stringArrList.get(0).length];
		        for (int counter = 0; counter<stringArrList.size(); counter++) {
		        	output[counter]=stringArrList.get(counter);
		        }
		        return output;
		    }catch(FileNotFoundException e){
		    	System.out.println("Error: "+e);
		   		return null;
			}	finally {
		        br.close();
		    }
		}catch(IOException e){
			System.out.println("Error: "+e);
			return null;
		}
  	}
}