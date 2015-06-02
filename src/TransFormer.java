import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class TransFormer {
	private String ECGDataAtrPath = "./Data/106_atr.txt";
	private String OutputLibSVMPath = "./Data/106_extracted.txt";
	private String OutputWekaPath = "./Data/106_extracted.arff";
	private File ECGAtrFile = new File(ECGDataAtrPath);
	private File OutputLibSVMFile = new File(OutputLibSVMPath);
	private File OutputWekaFile = new File(OutputWekaPath);
	private double RRinterval = 1/360;
	private ArrayList<String[]> FullList;
	//extract time,sample,type,aux, where array[0,1,2,6]
	private ArrayList<String[]> NoNoiseList;//not include type ~,+
	private ArrayList<String[]> ArrythmiaList;//only include type N,V
	private ArrayList<String[]> RRiperData;
	
	public TransFormer() throws IOException {
		if(!OutputLibSVMFile.exists())
			OutputLibSVMFile.createNewFile();
		if(!OutputWekaFile.exists())
			OutputWekaFile.createNewFile();
	}
	
	public void TransForm() throws IOException, ParseException {
		FullList = tranformRawData();
		NoNoiseList = getNoNoiseData(FullList);
		ArrythmiaList = getArrythmiaList(NoNoiseList);
		RRiperData = getRRiperData();
		OutputSVMData();
		OutputWekaData();
	}
	
	private ArrayList<String[]> tranformRawData() throws IOException{
		FileReader fr = new FileReader(ECGAtrFile);
		BufferedReader bfr = new BufferedReader(fr);
		String line = bfr.readLine();
		ArrayList<String[]> list = new ArrayList<String[]>();
		String seperate[];
		line = bfr.readLine();
		while(line!=null){
			seperate = getToken(line);
			list.add(seperate);
			line = bfr.readLine();
		}
		return list;
	}
	
	private void OutputSVMData() throws IOException{
		FileWriter fw = new FileWriter(OutputLibSVMFile);
		String temp ="";
		String[] t;
		for(int i=1;i<ArrythmiaList.size();i++){
			t = ArrythmiaList.get(i);
			if(t[2].equalsIgnoreCase("N"))
				temp = temp + "0 ";
			else
				temp = temp + "1 ";
			temp = temp + "1:" + (Double.parseDouble(RRiperData.get(i-1)[0])/1000) + " ";
			temp = temp + "2:" + (Double.parseDouble(RRiperData.get(i-1)[1])/1000) + "\n";
		}
		fw.write(temp);
		fw.close();
	}
	
	private void OutputWekaData() throws IOException{
		FileWriter fw = new FileWriter(OutputWekaFile);
		String temp = "@relation ECGFeature" + ECGDataAtrPath.substring(7, 10) + "\n";
		temp = temp + "@attribute RRinterval_current numeric\n";
		temp = temp + "@attribute RRinterval_previous numeric\n";
		temp = temp + "@attribute class {N,V}\n\n@data\n";
		String t[];
		for(int i=1;i<ArrythmiaList.size();i++){
			t = ArrythmiaList.get(i);
			temp = temp + (Double.parseDouble(RRiperData.get(i-1)[0])/1000) + ",";
			temp = temp + (Double.parseDouble(RRiperData.get(i-1)[1])/1000) + ",";
			temp = temp + t[2] + "\n";
		}
		fw.write(temp);
		fw.close();
	}
	
	private String[] getToken(String input){
		StringTokenizer str = new StringTokenizer(input, " ");
		StringTokenizer lastToken;
		int tokencount = str.countTokens();
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0;i<tokencount;i++)
			list.add(str.nextToken());
		lastToken = new StringTokenizer(list.get(list.size()-1),"\t");
		list.remove(list.size()-1);
		String origin = lastToken.nextToken();
		list.add(origin);
		if(lastToken.hasMoreTokens()){
			String last = lastToken.nextToken();
			list.add(last);
		}
		else{
			list.add("none");
		}
		return list.toArray(new String[0]);
	}
	
	private ArrayList<String[]> getNoNoiseData(ArrayList<String[]> list){
		ArrayList<String[]> arrList = new ArrayList<String[]>();
		for(int i=0;i<list.size();i++){
			String dataSet[] = list.get(i);
			if(!(dataSet[2].equalsIgnoreCase("~") || dataSet[2].equalsIgnoreCase("+")))
				arrList.add(dataSet);
		}
		return arrList;
	}
	
	private ArrayList<String[]> getArrythmiaList(
			ArrayList<String[]> NNList) {
		ArrayList<String[]> arrList = new ArrayList<String[]>();
		for(String[] t : NNList){
			if(t[2].equalsIgnoreCase("N") || 
					t[2].equalsIgnoreCase("V")){
				arrList.add(t);
			}
		}
		return arrList;
	}
	
	private ArrayList<String[]> getRRiperData() throws ParseException{
		ArrayList<String[]> arrList= new ArrayList<String[]>();
		String[] temp;
		for(int i=1;i<ArrythmiaList.size();i++){
			temp = new String[2];
			temp[0] = getTimeDifference(ArrythmiaList.get(i)[0], ArrythmiaList.get(i-1)[0]);
			if(i==1)
				temp[1] = getTimeDifference(ArrythmiaList.get(i-1)[0], "00:00.000");
			else
				temp[1] = arrList.get(arrList.size()-1)[0];
			arrList.add(temp);
		}
		return arrList;
	}
	
	private String getTimeDifference(String current, String previous) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        if(current.split(":")[0].length()==1){
        	current = "0" +  current;
        }
        Date currentDate = sdf.parse("1970-01-01 " + current);
        Date previousDate = sdf.parse("1970-01-01 " + previous);
		return Long.toString((currentDate.getTime()-previousDate.getTime()));
	}
}
