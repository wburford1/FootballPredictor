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
		//set meanings
		Object[] temp = FootballPredictor.readFileToArrayList("weightMeanings.txt").toArray();
		meanings = Arrays.copyOf(temp,temp.length,String[].class);
		numberOfValues = meanings.length-1;//first is the name, which is key in map
		//set weights
		ArrayList<String> tempList = FootballPredictor.readFileToArrayList("weights.txt");
		if (tempList==null||tempList.size()!=numberOfValues*2) {
			FootballPredictor.weights = new double[numberOfValues*2];
		}
		else{
			for (int counter=0;counter<tempList.size();counter++) {
				weights[counter] = Integer.parseInt(tempList.get(counter));
			}
		}
		System.out.println("weights = "+Arrays.toString(FootballPredictor.weights));
		//set teamValues
		teamValues = FootballPredictor.readFileToDoubleArrMap("teamData.txt");
		//System.out.println(FootballPredictor.doubleArrMapToString(teamValues));
		constant = 1;

		FootballPredictor machine = new FootballPredictor();
		FootballPredictor.weights = machine.learn("matchups.txt");
		System.out.println("Found new weights. Check weights.txt");
	}
	public FootballPredictor(){

	}

	public void learn(String dataFileName){
		String[][] matchups = FootballPredictor.readFileto2DStringArray(dataFileName);
		if (matchups==null) {
			return;
		}
		//double[] weights = Arrays.copyOf(FootballPredictor.weights,FootballPredictor.weights.length);
		double[] weights = new double[FootballPredictor.numberOfValues*2];
		for (int counter = 0; counter<matchups.length; counter++) {
			String[] matchup = matchups[counter];
			double sum = (double)(FootballPredictor.constant);

			if (FootballPredictor.teamValues.containsKey(matchup[0])&&FootballPredictor.teamValues.containsKey(matchup[1])) {
				double[] away = Arrays.copyOf(FootballPredictor.teamValues.get(matchup[0]),FootballPredictor.numberOfValues);
				double[] home = Arrays.copyOf(FootballPredictor.teamValues.get(matchup[1]),FootballPredictor.numberOfValues);
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
				if ((sum>0&&matchup[2].equals(1))||(sum<0&&matchup[2].equals(2))) {
					//correct
				}
				else if (sum<0&&matchup[2].equals(1)) {
					//wrong. should be positive
					for (int x=0; x<FootballPredictor.numberOfValues; x++) {
						weights[x] = weights[x]+(1/counter)*away[x];
						weights[FootballPredictor.numberOfValues+x] = weights[FootballPredictor.numberOfValues+x]+(1/counter)*home[x];
					}
				}
				else if (sum>0&&matchup[2].equals(2)) {
					//wrong. should be negative
					for (int x=0; x<FootballPredictor.numberOfValues; x++) {
						weights[x] = weights[x]-(1/counter)*away[x];
						weights[FootballPredictor.numberOfValues+x] = weights[FootballPredictor.numberOfValues+x]-(1/counter)*home[x];
					}
				}
				else {
					System.out.println("Something went wrong\nMatchup = " + Arrays.toString(matchup)+"\nSum = "+sum);
					System.out.println("\nhome = "+Arrays.toString(home)+"\naway = "+Arrays.toString(away)+"\nweights = "+Arrays.toString(weights));
				}
			}
			else {
				System.out.println("Skipping matchup: " + Arrays.toString(matchup));
			}
		}

		PrintWriter writer;
		try{
			writer = new PrintWriter("weights.txt","UTF-8");
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
		writer.close();
		//FootballPredictor.weights = weights;
		return weights;
	}

	public void seeTheFuture(String dataFileName){
		String[][] games = FootballPredictor.readFileto2DStringArray(dataFileName);
		if (games==null) {
			return;
		}

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
				int prediction = this.predictWinner(FootballPredictor.teamValues.get(awayName), FootballPredictor.teamValues.get(homeName));
				if (prediction>0) {
					writer.println(awayName + "will beat" + homeName);
				}
				else if (prediction<0) {
					writer.println(homeName + "will beat" + awayName);
				}
				else {
					//indeterminant
					writer.println("Could not determine winner of " + awayName + " vs. "+ homeName);
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