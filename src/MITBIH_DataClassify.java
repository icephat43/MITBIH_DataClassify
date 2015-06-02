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
		
	}
}
