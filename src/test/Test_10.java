import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_10 {

	/**
	 * @test reconstruct image
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "C:\\temp\\moving_camera3.576v";
		int width = 480;
		int height = 368;
		double frameRate = 12;
		int kH = 60; //k in horizontal
		int kV = 10; //k in vertical
		File file = new File(filePath);
		byte[] bytes = new byte[(int)file.length()];
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames1 = bytes.length/frameDataSize;
		
		//copy the data in file into byte arrays
		try {
			bytes = fileToBytes(file);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}

		byte[] imgByte1 = new byte[frameDataSize];
		System.arraycopy(bytes, 0*frameDataSize, imgByte1, 0, frameDataSize);
		byte[] imgByte2 = new byte[frameDataSize];
		System.arraycopy(bytes, 1*frameDataSize, imgByte2, 0, frameDataSize);
		int[][][] tempArray = VectorSearch.search(imgByte1, imgByte2, width, height, kH, kV);
		int[][][][] vectors = new int[numberOfFrames1][2][width/16][height/16];
		vectors[1] = tempArray;
		byte[] reconstructedImg = Reconstruct.reconstruct(imgByte1, width, height, vectors[1]);
		System.arraycopy(reconstructedImg, 0, bytes, 1*frameDataSize, frameDataSize);
		byte[] errImg = ErrImg.difference(imgByte2, reconstructedImg);
		System.arraycopy(errImg, 0, bytes, 2*frameDataSize, frameDataSize);
		byte[] decodedImg = ErrImg.add(reconstructedImg, errImg);
		System.arraycopy(decodedImg, 0, bytes, 3*frameDataSize, frameDataSize);
		DisplayWindow window = new DisplayWindow(bytes, bytes, width, height, frameRate, vectors);
	
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
