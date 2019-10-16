//the method check will check if a video is static or not
//it take a byte array of a video as input
//return a boolean value that tells if a video is static

public class CheckStatic {
	public static boolean check(byte[] videoBytes, int width, int height){
		int kH = 4; //horizontal searching range k
		int kV = 4; //vertical searching range k
		int fastSearchLevel = 1; //how many levels when use hierarchical fast searching
		boolean staticOrNot = false; //true = static, false = moving
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = videoBytes.length/frameDataSize;
		byte[] imgBytesFirst = new byte[frameDataSize]; //first frame
		byte[] imgBytesLast = new byte[frameDataSize]; //last frame
		System.arraycopy(videoBytes, 0*frameDataSize, imgBytesFirst, 0, frameDataSize);
		System.arraycopy(videoBytes, (numberOfFrames-1)*frameDataSize, imgBytesLast, 0, frameDataSize);
		int[][][] vectors = VectorSearch.fastSearch(imgBytesFirst, imgBytesLast, width, height, kH, kV, fastSearchLevel);
		int numberOfStaticBlock = 0; // count number of blocks which have vector (0,0)
		int blocksInRow = width/16; //number of blocks in a row
		int blocksInColumn = height/16; //number of blocks in a row 
		for(int x = 0; x <= blocksInRow - 1; x++){
			for(int y = 0; y <= blocksInColumn - 1; y++){
				if (vectors[0][x][y] == 0 && vectors[1][x][y] == 0){
					numberOfStaticBlock++;
				}
			}
		}
		int numberOfBlocks = blocksInRow * blocksInColumn;
		if(numberOfStaticBlock > numberOfBlocks/2){ //if more than half of the blocks are static
			staticOrNot = true;
		}
		return staticOrNot;
	}//end check method
}//end CheckStatic
