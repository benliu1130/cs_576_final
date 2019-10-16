import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_08 {

	/**
	 * showing motion vectors of a video
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String filePath1 = "C:\\temp\\moving_camera2.576v";
		int width = 480;
		int height = 368;
		double frameRate = 12;
		int kH = 50; //k in horizontal
		int kV = 10; //k in vertical
		long t = 0;
		File file1 = new File(filePath1);
		byte[] bytes = new byte[(int)file1.length()];
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = bytes.length/frameDataSize;
		
		//copy the data in file into byte arrays
		try {
			bytes = fileToBytes(file1);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}

		int[][][][] vectorsVideo = new int[numberOfFrames][2][width/16][height/16]; //an array to store vectors for the whole video
		byte[] imgByteCompared = new byte[frameDataSize];
		byte[] imgByteCurrent = new byte[frameDataSize];
		
		t = System.currentTimeMillis();
		//the vectors of frame 0 is different
		System.arraycopy(bytes, 1*frameDataSize, imgByteCompared, 0, frameDataSize);
		System.arraycopy(bytes, 0*frameDataSize, imgByteCurrent, 0, frameDataSize);
		
		vectorsVideo[0] = VectorSearch.fastSearch(imgByteCompared, imgByteCurrent, width, height, kH, kV, 1);
		
		//all the other frames
		for (int index = 1; index <= numberOfFrames - 1; index++){
			System.arraycopy(bytes, (index-1)*frameDataSize, imgByteCompared, 0, frameDataSize);
			System.arraycopy(bytes, index*frameDataSize, imgByteCurrent, 0, frameDataSize);
			vectorsVideo[index] = VectorSearch.fastSearch(imgByteCompared, imgByteCurrent, width, height, kH, kV, 1);
		}
		t = System.currentTimeMillis() - t;
		System.out.println("Total time of vectors searching: " + (t/1000) + " seconds");
		
				
		DisplayWindow window = new DisplayWindow(bytes, bytes, width, height, frameRate, vectorsVideo);

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
