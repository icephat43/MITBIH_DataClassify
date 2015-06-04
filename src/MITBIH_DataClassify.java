import java.io.IOException;
import java.text.ParseException;

public class MITBIH_DataClassify {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		TransFormer trans = new TransFormer();
		trans.TransForm();
		SVM();
	}
	
	private static void SVM() throws IOException{
		String[] trainpara = {
				"./Data/106_training.txt", //training data
				"./Data/106_model.txt"};//output model
		String[] testpara = {
				"./Data/106_testing.txt", //testing data, though I know I shouldn't use training data as testing data
				"./Data/106_model.txt", //using model
				"./Data/out_consiquence.txt"}; //consequence
		svm_train.main(trainpara);
		svm_predict.main(testpara);
	}
}
