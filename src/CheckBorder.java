//check if there exists a border
//this can be used only when the camera is moving
//it returns a boolean array showing which blocks are border
//if a block is border, then the value is true

public class CheckBorder {
	public static boolean[][] check(byte[] videoBytes, int width, int height){
		int kH = 4; //horizontal searching range k
		int kV = 4; //vertical searching range k
		int fastSearchLevel = 1; //how many levels when use hierarchical fast searching
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = videoBytes.length/frameDataSize;
		byte[] imgBytesFirst = new byte[frameDataSize]; //first frame
		byte[] imgBytesLast = new byte[frameDataSize]; //last frame
		System.arraycopy(videoBytes, 0*frameDataSize, imgBytesFirst, 0, frameDataSize);
		System.arraycopy(videoBytes, (numberOfFrames-1)*frameDataSize, imgBytesLast, 0, frameDataSize);
		int[][][] vectors = VectorSearch.fastSearch(imgBytesFirst, imgBytesLast, width, height, kH, kV, fastSearchLevel);
		int blocksInRow = width/16; //number of blocks in a row
		int blocksInColumn = height/16; //number of blocks in a row 
		boolean border[][] = new boolean[blocksInRow][blocksInColumn];

		//check horizontal border
		for(int y = 0; y <= blocksInColumn-1; y++){
			int count = 0;
			for(int x = 0; x <= blocksInRow-1; x++){
				if(vectors[1][x][y] == 0){
					count++;
				}
			}
			if(count > blocksInRow*9/10){ //if 90 percent of blocks have vector (?,0)
				for(int x = 0; x <= blocksInRow-1; x++){
					border[x][y] = true;
				}
			}
		}
		//check vertical border
		for(int x = 0; x <= blocksInRow-1; x++){
			int count = 0;
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(vectors[0][x][y] == 0){
					count++;
				}
			}
			if(count > blocksInColumn*8/10){ //if 90 percent of blocks have vector (?,0)
				for(int y = 0; y <= blocksInColumn-1; y++){
					border[x][y] = true;
				}
			}
		}
		return border;
	}//end check
}//end CheckBorder
