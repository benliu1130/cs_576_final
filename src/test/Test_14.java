import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Test_14 {

	/**
	 * this is a code to detect which blocks are border
	 * then do grouping without border
	 * then use findBackground method to find background
	 * this code can be used for moving camera only
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "C:\\temp\\moving_camera2.576v";
		int width = 480;
		int height = 368;
		double frameRate = 12;
		int kH = 48; //k in horizontal
		int kV = 8; //k in vertical
		int fastSearchLevel = 1; //how many levels when use hierarchical fast searching
		long t = 0;
		File file = new File(filePath);
		byte[] bytes = new byte[(int)file.length()];
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = bytes.length/frameDataSize;
		
		try {
			bytes = fileToBytes(file);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		//boolean[][] border = new boolean[width/16][height/16];
		boolean[][] border = CheckBorder.check(bytes, width, height);

		int[][][][] vectorsVideo = new int[numberOfFrames][2][width/16][height/16]; //an array to store vectors for the whole video
		byte[] imgByteCompared = new byte[frameDataSize];
		byte[] imgByteCurrent = new byte[frameDataSize];
		
		t = System.currentTimeMillis();
		//the vectors of frame 0 is different
		System.arraycopy(bytes, 1*frameDataSize, imgByteCompared, 0, frameDataSize);
		System.arraycopy(bytes, 0*frameDataSize, imgByteCurrent, 0, frameDataSize);
		
		vectorsVideo[0] = VectorSearch.fastSearch(imgByteCompared, imgByteCurrent, width, height, kH, kV, fastSearchLevel);
		
		//all the other frames
		for (int index = 1; index <= numberOfFrames - 1; index++){
			System.arraycopy(bytes, (index-1)*frameDataSize, imgByteCompared, 0, frameDataSize);
			System.arraycopy(bytes, index*frameDataSize, imgByteCurrent, 0, frameDataSize);
			vectorsVideo[index] = VectorSearch.fastSearch(imgByteCompared, imgByteCurrent, width, height, kH, kV, fastSearchLevel);
		}
		t = System.currentTimeMillis() - t;
		System.out.println("Total time of vectors searching: " + (t/1000) + " seconds");
		
		t = System.currentTimeMillis();
		
		BlockSet[][][] groups = new BlockSet[numberOfFrames][][];
		for (int index = 0; index <= numberOfFrames - 1; index++){
			groups[index] = Grouping.groupByVectors(vectorsVideo[index], 1, 1, border);
		}
		
		boolean[][][] backgroundInfo = new boolean[numberOfFrames][][];
		for (int index = 0; index <= numberOfFrames - 1; index++){
			boolean[][] background = LayerSeparation.findBackground(vectorsVideo[index], border);
			backgroundInfo[index] = background;
		}
		
		//make sure there is no temporally discontinuous foreground
		boolean[][][] foregroundOfVideo = invert(backgroundInfo);
		foregroundOfVideo = LayerSeparation.eliminateDiscontinuousForeground(foregroundOfVideo);
		backgroundInfo = invert(foregroundOfVideo);
		
		boolean [][][] layerInfo = new boolean[numberOfFrames][][];
		for (int index = 0; index <= numberOfFrames - 1; index++){
			boolean[][] layer = new boolean[width/16][height/16];
			for(int x = 0; x <= layer.length-1; x++){
				for(int y = 0; y <= layer[0].length-1; y++){
					layer[x][y] = !backgroundInfo[index][x][y];
				}
			}
			layerInfo[index] = layer;
		}
		
		t = System.currentTimeMillis() - t;
		System.out.println("Total time of seperating: " + (t/1000) + " seconds");
		
		
		
		DisplayWindow window1 = new DisplayWindow(bytes, bytes, width, height, frameRate, vectorsVideo, groups, layerInfo);
		
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

	//this method convert true to false and false to true
	private static boolean[][][] invert(boolean[][][] input){
		boolean[][][] output = new boolean[input.length][input[0].length][input[0][0].length];
		for(int x = 0; x <= input.length-1; x++){
			for(int y = 0; y <= input[0].length-1; y++){
				for(int z = 0; z <= input[0][0].length-1; z++){
					output[x][y][z] = !input[x][y][z];
				}
			}
		}
		return output;
	}

}
