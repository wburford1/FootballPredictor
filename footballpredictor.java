import java.util.*;
import java.io.*;
import java.lang.*;

public class FootballPredictor{
	private static int[] weights;
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
		if (tempList==null||tempList.size()!=numberOfValues) {
			FootballPredictor.weights = new int[numberOfValues];
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

		FootballPredictor baller = new FootballPredictor();
		baller.findWeights("matchups.txt");
	}
	public FootballPredictor(){

	}

	public void findWeights(String dataFileName){
		String[][] matchups = FootballPredictor.readFileto2DStringArray(dataFileName);
		if (matchups==null) {
			return;
		}

		//make thing find weights here

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
		writer.close();

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
		        String[][] output = new String[stringArrList.size()][3];
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