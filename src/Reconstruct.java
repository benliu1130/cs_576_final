//this class has a method: reconstruct
//it takes previous frame and motion vectors as input
//and reconstruct an image
public class Reconstruct {
	public static byte[] reconstruct(byte[] previousImg, int width, int height, int[][][] vectors){
		int blocksInRow = width/16; //number of blocks in a row
		int blocksInColumn = height/16; //number of blocks in a column
		int[][][] previousImageArray = new int[3][width][height];
		bytesToArray(previousImg, previousImageArray);
		int[][][] reconstructedImageArray = new int[3][width][height];
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				int vectorX = vectors[0][x][y];
				int vectorY = vectors[1][x][y];
				for(int i = x*16; i <= x*16+15; i++){
					for(int j = y*16; j <= y*16+15; j++){
						reconstructedImageArray[0][i][j] = previousImageArray[0][i+vectorX][j+vectorY];
						reconstructedImageArray[1][i][j] = previousImageArray[1][i+vectorX][j+vectorY];
						reconstructedImageArray[2][i][j] = previousImageArray[2][i+vectorX][j+vectorY];
					}
				}
			}
		}
		byte[] reconstructedImg = arrayToBytes(reconstructedImageArray);
		return reconstructedImg;
	}//end reconstruct
	
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
}//end Reconstruct

