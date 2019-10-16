import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_01 {
	public static void main(String[] args) {
		String filePath1 = "C:\\temp\\moving_camera4.576v";
		String filePath2 = "C:\\temp\\moving_camera4.576v";
		int width = 480;
		int height = 368;
		double frameRate = 12;
		File file1 = new File(filePath1);
		File file2 = new File(filePath2);
		byte[] bytes1 = new byte[(int)file1.length()];
		byte[] bytes2 = new byte[(int)file2.length()];
		
		//copy the data in file into byte arrays
		try {
			bytes1 = fileToBytes(file1);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		try {
			bytes2 = fileToBytes(file2);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		DisplayWindow window = new DisplayWindow(bytes1, bytes2, width, height, frameRate);
	}

	//this method take a file as an input 
	//and output the data of the file into a byte array
	public static byte[] fileToBytes(File file) throws IOException{
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[(int)file.length()];
		int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && 
	    		(numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	    return bytes;
	}//end fileToBytes

}
