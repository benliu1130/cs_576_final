import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;


public class Test_03 {

	/**
	 *test layeredImageCodec
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath1 = "C:\\temp\\moving_camera3.576v";
		int width = 480;
		int height = 368;
		int blocksInRow = width/8;
		int blocksInColumn = height/8;
		File file1 = new File(filePath1);
		byte[] bytes1 = new byte[width*height*3];
		byte[] bytes2 = new byte[width*height*3];

		
		//copy the data in file into byte arrays
		try {
			bytes1 = fileToBytes(file1);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		//up-left corner is foreground
		boolean[][] layer = new boolean[blocksInRow][blocksInColumn];
		for (int x = 0; x <= blocksInRow/2; x++){
			for (int y = 0; y <= blocksInColumn/2; y++){
				layer[x][y] = true;
			}
		}
		
		//int[] temp = LayeredImageCodec.encoder(bytes1, width, height, 0, 7, layer);
		int[][][] imgArray = new int[3][width][height];
		bytesToArray(bytes1, imgArray);
		int[] temp = LayeredImageCodec.encoderErr(imgArray, 0, 7, layer);
		
		imgArray = LayeredImageCodec.decoderErr(temp, width, height, 0, 7, layer);
		for(int color = 0; color <=2; color++){
			for(int i = 0; i <=width-1; i++){
				for(int j = 0; j <=height-1; j++){
					imgArray[color][i][j] = (imgArray[color][i][j]>255)?255:imgArray[color][i][j];
					imgArray[color][i][j] = (imgArray[color][i][j]<0)?0:imgArray[color][i][j];
				}
			}
		}
		bytes2 = arrayToBytes(imgArray);
				
		DisplayWindow window = new DisplayWindow(bytes1, bytes2, width, height, 12);
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

	//this method copy the data in a byte array into a 3-dimensional integer array
	//first dimension means color, (RGB) 0=R, 1=G, 2=B
	//second dimension means column, 0=first column, 1=second column, and so on
	//third dimension means row, 0=first row, 1=second row, and so on
	//the values range from 0 to 255. 0 means most dark, 255 means most bright
	private static void bytesToArray(byte[] bytes, int[][][] imageArray){
		int width = imageArray[0].length;
		int height = imageArray[0][0].length;
		int index = 0;
	    for (int color = 0; color <= 2; color++){
	    	for (int y = 0; y <= height - 1; y++){
	    		for (int x = 0; x <= width - 1; x++){
	    			int k = bytes[index];
	    			imageArray[color][x][y] = (k + ((k < 0)?256:0)); //k = k + 256 if x < 0
	    			index++;
	    		}
	    	}
	    }
	}//end bytesToArray

	//this method convert a 3-dimensional image array into a byte array
	private static byte[] arrayToBytes(int[][][] imageArray){
		int width = imageArray[0].length;
		int height = imageArray[0][0].length;
		byte[] bytes = new byte[3*height*width];
		int index = 0;
	    for (int color = 0; color <= 2; color++){
	    	for (int y = 0; y <= height - 1; y++){
	    		for (int x = 0; x <= width - 1; x++){
	    			bytes[index] = (byte)imageArray[color][x][y];
	    			index++;
	    		}
	    	}
	    }
	    return bytes;
	}//end arrayToBytes
}