import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_06 {

	/**
	 * @test "VectorSearch.search method"
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String filePath1 = "C:\\temp\\static_camera3.576v";
		String filePath1 = "C:\\temp\\moving_camera3.576v";
		int width = 480;
		int height = 368;
		double frameRate = 12;
		int kH = 60; //k in horizontal
		int kV = 10; //k in vertical
		File file1 = new File(filePath1);
		byte[] bytes1 = new byte[(int)file1.length()];
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames1 = bytes1.length/frameDataSize;
		
		//copy the data in file into byte arrays
		try {
			bytes1 = fileToBytes(file1);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}

		byte[] imgByte1 = new byte[frameDataSize];
		System.arraycopy(bytes1, 0*frameDataSize, imgByte1, 0, frameDataSize);
		byte[] imgByte2 = new byte[frameDataSize];
		System.arraycopy(bytes1, 1*frameDataSize, imgByte2, 0, frameDataSize);
		int[][][] tempArray = VectorSearch.fastSearch(imgByte1, imgByte2, width, height, kH, kV, 1);
		int[][][][] temp = new int[numberOfFrames1][2][width/16][height/16];
		temp[1] = tempArray;
		DisplayWindow window = new DisplayWindow(bytes1, bytes1, width, height, frameRate, temp);
	
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
