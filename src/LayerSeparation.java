//there are many methods
//the goal is to separate foreground & background for the video with moving camera
//


public class LayerSeparation {
	//the method "findBackground" takes motion vectors as input
	//if there is some border information, it also can take as input
	//then it return the background information
	//
	//first, it define some "corner area", then do grouping with low threshold
	//if a group which has many blocks in corner, then all blocks in this group is treated as background
	//when a big background group is found, I use the average vector of this group to find all the background blocks in image
	//before return the value, check if there is any stand-alone foreground block or background block
	public static boolean[][] findBackground(int[][][] vectors){//if no border information is provided
		int blocksInRow = vectors[0].length;
		int blocksInColumn = vectors[0][0].length;
		boolean[][] border = new boolean[blocksInRow][blocksInColumn];
		return findBackground(vectors, border);
	}//end findBackground
	
	public static boolean[][] findBackground(int[][][] vectors, boolean[][] border){
		//use low threshold
		int groupingThresholdX = 1;
		int groupingThresholdY = 1;
		int blocksInRow = vectors[0].length;
		int blocksInColumn = vectors[0][0].length;
		boolean[][] background = new boolean[blocksInRow][blocksInColumn];
		//copy border to background
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				background[x][y] = border[x][y];
			}
		}
		
		BlockSet[][] blockSets = Grouping.groupByVectors(vectors, groupingThresholdX, groupingThresholdY, border);
		
		//define the "corner"
		//there are up-right, up-left, low-right, and low-left corner
		double cornerX = 0.2;
		double cornerY = 0.2;
		int upRightCornerX = blocksInRow - (int)Math.round(cornerX * blocksInRow);
		int upRightCornerY = (int)Math.round(cornerY * blocksInColumn)-1;
		int upLeftCornerX = (int)Math.round(cornerX * blocksInRow)-1;
		int upLeftCornerY = (int)Math.round(cornerY * blocksInColumn)-1;
		int lowRightCornerX = blocksInRow - (int)Math.round(cornerX * blocksInRow * 0.7);
		int lowRightCornerY = blocksInColumn - (int)Math.round(cornerY * blocksInColumn);
		int lowLeftCornerX = (int)Math.round(cornerX * blocksInRow * 0.7)-1;
		int lowLeftCornerY = blocksInColumn - (int)Math.round(cornerY * blocksInColumn);
		
		//for each group, check how many blocks in this group are in corner
		//if this group contains many blocks(more than backgroundThreshold) in corner, this group are treated as background
		int backgroundThreshold = 5;
		boolean[][] haveDone = new boolean[blocksInRow][blocksInColumn];
		int maxSize = 0; //record the size of biggest background group
		int maxX = -1; //x of a certain block in the biggest background group
		int maxY = -1; //y of a certain block in the biggest background group
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(!haveDone[x][y]){
					int count = 0;
					BlockSet blockSet = blockSets[x][y];
					int size = blockSet.size();
					Block[] blocks = blockSet.getBlocksArray();
					for(int k = 0; k <= size-1; k++){
						if((blocks[k].getX() >= upRightCornerX && blocks[k].getY() <= upRightCornerY) || //up right corner
						   (blocks[k].getX() <= upLeftCornerX && blocks[k].getY() <= upLeftCornerY) || //up left corner
						   (blocks[k].getX() >= lowRightCornerX && blocks[k].getY() >= lowRightCornerY) || //low right corner
						   (blocks[k].getX() <= lowLeftCornerX && blocks[k].getY() >= lowLeftCornerY)){ //low left corner
							count++;
							haveDone[blocks[k].getX()][blocks[k].getY()] = true;
						}
					}
					if(count >= backgroundThreshold){//this group has many corner blocks
						if(size > maxSize){//record the biggest background group found so far
							maxSize = size;
							maxX = x;
							maxY = y;
						}
					}
				}
			}			
		}
		
		//then we use the average of the vectors of the biggest background group to represent background
		//all blocks which have similar vectors are treated as background
		if(maxX != -1 && maxSize >= 10){ // a biggest background group is found
			double backgroundX = blockSets[maxX][maxY].getAverageX();
			double backgroundY = blockSets[maxX][maxY].getAverageY();
			for(int x = 0; x <= blocksInRow-1; x++){
				for(int y = 0; y <= blocksInColumn-1; y++){
					if(Math.abs(backgroundX-vectors[0][x][y])<=groupingThresholdX && Math.abs(backgroundY-vectors[1][x][y])<=groupingThresholdY){
						background[x][y] = true;
					}
				}
			}
		}
		
		//eliminate stand-alone foreground or background
		boolean change = false; //repeating doing this until there is no stand-alone foreground/background
		do{
			change = false;
			for(int x = 0; x <= blocksInRow-1; x++){
				for(int y = 0; y <= blocksInColumn-1; y++){
					if(!background[x][y]){ //a foreground block
						int count = 0;
						int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
						int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
						int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
						int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
						for(int i = searchingLeftBound; i <= searchingRightBound; i++){
							for(int j = searchingUpBound; j <= searchingLowBound; j++){
								if(!background[i][j]){
									count++;
								}
							}
						}
						if(count==1){
							background[x][y] = true;
							change = true;
						}
					}
					else{ //a background block
						int count = 0;
						int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
						int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
						int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
						int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
						for(int i = searchingLeftBound; i <= searchingRightBound; i++){
							for(int j = searchingUpBound; j <= searchingLowBound; j++){
								if(background[i][j]){
									count++;
								}
							}
						}
						if(count==1){
							background[x][y] = false;
							change = true;
						}
					}
				}
			}
		}
		while(change);
		return background;
	}//end findBackground
	
	//the method "findForeground" takes motion vectors and background information as input
	//then it return the foreground information
	//first, it define some "central area", then do grouping with high threshold(it will ignore background blocks)
	//if a group most of whose block is in central area, then all blocks in this group is treated as foreground
	public static boolean[][] findForeground(int[][][] vectors, boolean[][] background){
		//use high threshold
		int groupingThresholdX = 5;
		int groupingThresholdY = 5;
		int blocksInRow = vectors[0].length;
		int blocksInColumn = vectors[0][0].length;
		boolean[][] foreground = new boolean[blocksInRow][blocksInColumn];
		
		BlockSet[][] blockSets = Grouping.groupByVectors(vectors, groupingThresholdX, groupingThresholdY, background);
		
		//define the "central area"
		double centralX = 0.2;
		double centralY = 0.2;
		int rightCentralX = blocksInRow - (int)Math.round(centralX * blocksInRow);
		int leftCentralX = (int)Math.round(centralX * blocksInRow)-1;
		int upCentralY = (int)Math.round(centralY * blocksInColumn)-1;
		int lowCentralY = blocksInColumn - (int)Math.round(centralY * blocksInColumn);
		
		//for each group, check how many blocks in this group are in central
		//if most(more than foregroundThreshold) of the blocks in this group are in central area, this group are treated as foreground
		double foregroundThreshold = 0.5;
		int minForegroundSize = 5; //a foreground group must have more than this number of blocks
		boolean[][] haveDone = new boolean[blocksInRow][blocksInColumn];
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(blockSets[x][y].size() >= minForegroundSize && !haveDone[x][y]){
					int count = 0;
					BlockSet blockSet = blockSets[x][y];
					int size = blockSet.size();
					Block[] blocks = blockSet.getBlocksArray();
					for(int k = 0; k <= size-1; k++){
						if(blocks[k].getX() <= rightCentralX &&
						   blocks[k].getX() >= leftCentralX && 
						   blocks[k].getY() <= lowCentralY && 
						   blocks[k].getY() >= upCentralY){
							count++;
							haveDone[blocks[k].getX()][blocks[k].getY()] = true;
						}
					}
					if((double)count/size >= foregroundThreshold){//most of the blocks are in central area
						for(int k = 0; k <= size-1; k++){
							foreground[blocks[k].getX()][blocks[k].getY()] = true;
						}
					}
				}
			}			
		}
		
		//eliminate stand-alone background
		boolean change = false; //repeating doing this until there is no stand-alone background
		do{
			change = false;
			for(int x = 0; x <= blocksInRow-1; x++){
				for(int y = 0; y <= blocksInColumn-1; y++){
					if(!foreground[x][y]){ //a background block
						int count = 0;
						int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
						int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
						int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
						int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
						for(int i = searchingLeftBound; i <= searchingRightBound; i++){
							for(int j = searchingUpBound; j <= searchingLowBound; j++){
								if(!foreground[i][j]){
									count++;
								}
							}
						}
						if(count==1){
							foreground[x][y] = true;
							change = true;
						}
					}
				}
			}
		}
		while(change);
		return foreground;
	}//end findForeground	
		
	//this method takes the foreground information of the whole video as input
	//since we know a foreground object should exist at similar location in its previous frame or next frame
	//if a block is foreground in n frame, but no foreground block at similar location in n+1 frame
	//this should be an error
	public static boolean[][][] eliminateDiscontinuousForeground(boolean[][][] foregroundOfVideo){
		int numberOfFrames = foregroundOfVideo.length;
		int blocksInRow = foregroundOfVideo[0].length;
		int blocksInColumn = foregroundOfVideo[0][0].length;
		boolean[][][] output = new boolean[numberOfFrames][blocksInRow][blocksInColumn];
				
		//first frame and last frame
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(foregroundOfVideo[0][x][y]){
					output[0][x][y] = true;
					boolean foreground = false;
					int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
					int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
					int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
					int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
					for(int i = searchingLeftBound; i <= searchingRightBound; i++){//check if there exists any foreground block at similar area in previous frame
						for(int j = searchingUpBound; j <= searchingLowBound; j++){
							if(foregroundOfVideo[1][i][j]){
								foreground = true;
							}
						}
					}
					if(!foreground){
						output[0][x][y] = false;
					}
				}	
			}
		}
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(foregroundOfVideo[numberOfFrames-1][x][y]){
					output[numberOfFrames-1][x][y] = true;
					boolean foreground = false;
					int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
					int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
					int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
					int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
					for(int i = searchingLeftBound; i <= searchingRightBound; i++){//check if there exists any foreground block at similar area in previous frame
						for(int j = searchingUpBound; j <= searchingLowBound; j++){
							if(foregroundOfVideo[numberOfFrames-2][i][j]){
								foreground = true;
							}
						}
					}
					if(!foreground){
						output[numberOfFrames-1][x][y] = false;
					}
				}	
			}
		}
		//second frame to last second frame
		for(int index = 1; index <= numberOfFrames-2; index++){
			for(int x = 0; x <= blocksInRow-1; x++){
				for(int y = 0; y <= blocksInColumn-1; y++){
					if(foregroundOfVideo[index][x][y]){
						output[index][x][y] = true;
						boolean foreground = false;
						int searchingLeftBound = (x-1 <= 0) ? 0 : (x-1);
						int searchingUpBound = (y-1 <= 0) ? 0 : (y-1);
						int searchingRightBound = (x+1 >= blocksInRow-1) ? blocksInRow-1 : (x+1);
						int searchingLowBound = (y+1 >= blocksInColumn-1) ? blocksInColumn-1 : (y+1);
						for(int i = searchingLeftBound; i <= searchingRightBound; i++){//check if there exists any foreground block at similar area in previous frame
							for(int j = searchingUpBound; j <= searchingLowBound; j++){
								if(foregroundOfVideo[index-1][i][j]){
									foreground = true;
								}
							}
						}
						for(int i = searchingLeftBound; i <= searchingRightBound; i++){//check if there exists any foreground block at similar area in next frame
							for(int j = searchingUpBound; j <= searchingLowBound; j++){
								if(foregroundOfVideo[index+1][i][j]){
									foreground = true;
								}
							}
						}
						if(!foreground){
							output[index][x][y] = false;
						}
					}	
				}
			}
		}
		return output;
	}//end eliminateDiscontinuousForeground

}//end LayerSeperation