import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_07 {

	/**
	 * @this is a code to detect if a video is static or moving
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "C:\\temp\\static_camera2.576v";
		int width = 480;
		int height = 368;
		File file = new File(filePath);
		byte[] bytes = new byte[(int)file.length()];
		
		try {
			bytes = fileToBytes(file);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		System.out.println(CheckStatic.check(bytes, width, height));
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
