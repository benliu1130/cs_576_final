//this class has a method: grouping
//it uses disjoint sets algorithm to group adjacent blocks into the same set
//if these two blocks have similar vectors
//the input is a three dimensional array int[][][], representing vectors of blocks
//first dimension means x or y. 0 = x and 1 = y
//second dimension means a block's order in the row
//third dimension means a block's order in the column
//it output a two dimensional BlockSet array
//the first dimension is the order of a block in row
//the second dimension is the order of a block in column
//ex. blocksets[3][5] means the BlockSet of the block on fourth column and sixth row
//if background information is provided, background blocks will not be grouped with other blocks

public class Grouping {
	public static BlockSet[][] groupByVectors(int[][][] vectors, double thresholdX, double thresholdY){ //if no border information, just assume no border
		boolean[][] border = new boolean[vectors[0].length][vectors[0][0].length];
		return groupByVectors(vectors, thresholdX, thresholdY, border);
	}
	
	public static BlockSet[][] groupByVectors(int[][][] vectors, double thresholdX, double thresholdY, boolean background[][]){
		int blocksInRow = vectors[0].length;
		int blocksInColumn = vectors[0][0].length;
		BlockSet[][] blockSets = new BlockSet[blocksInRow][blocksInColumn];
		//make each block as a BlockSet
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				blockSets[x][y] = new BlockSet(x, y, vectors[0][x][y], vectors[1][x][y]);
			}
		}
		//try to union the sets
		for(int x = 0; x <= blocksInRow-1; x++){
			for(int y = 0; y <= blocksInColumn-1; y++){
				if(!background[x][y]){//if the block is not background
					if(x != blocksInRow-1 && !background[x+1][y]){//try to union with the block on right if not right-most block
						if(!blockSets[x][y].equals(blockSets[x+1][y]) &&
						   Math.abs(blockSets[x][y].getAverageX()-blockSets[x+1][y].getAverageX()) <= thresholdX &&
						   Math.abs(blockSets[x][y].getAverageY()-blockSets[x+1][y].getAverageY()) <= thresholdY){//check if it satisfy the threshold to union
									merge(blockSets[x][y], blockSets[x+1][y], blockSets);
						}
					}
					if(y != blocksInColumn-1 && !background[x][y+1]){//try to union with the block below if not bottom block
						if(!blockSets[x][y].equals(blockSets[x][y+1]) &&
						   Math.abs(blockSets[x][y].getAverageX()-blockSets[x][y+1].getAverageX()) <= thresholdX &&
						   Math.abs(blockSets[x][y].getAverageY()-blockSets[x][y+1].getAverageY()) <= thresholdY){//check if it satisfy the threshold to union
									merge(blockSets[x][y], blockSets[x][y+1], blockSets);
						}
					}
					if(x != blocksInRow-1 && y != blocksInColumn-1 && !background[x+1][y+1]){//try to union with the block on low-right
						if(!blockSets[x][y].equals(blockSets[x+1][y+1]) &&
						   Math.abs(blockSets[x][y].getAverageX()-blockSets[x+1][y+1].getAverageX()) <= thresholdX &&
						   Math.abs(blockSets[x][y].getAverageY()-blockSets[x+1][y+1].getAverageY()) <= thresholdY){//check if it satisfy the threshold to union
									merge(blockSets[x][y], blockSets[x+1][y+1], blockSets);
						}
					}
					if(x != blocksInRow-1 && y != 0 && !background[x+1][y-1]){//try to union with the block on up-right
						if(!blockSets[x][y].equals(blockSets[x+1][y-1]) &&
						   Math.abs(blockSets[x][y].getAverageX()-blockSets[x+1][y-1].getAverageX()) <= thresholdX &&
						   Math.abs(blockSets[x][y].getAverageY()-blockSets[x+1][y-1].getAverageY()) <= thresholdY){//check if it satisfy the threshold to union
									merge(blockSets[x][y], blockSets[x+1][y-1], blockSets);
						}
					}
				}
			}
		}
		return blockSets;
	}//end groupByVectors method

	//a method to merge two set, use weighted-set heuristic
	//after two sets are merged, the pointers for the blocks in smaller set will be updated
	private static void merge(BlockSet set1, BlockSet set2, BlockSet[][] blocks){
		if(set1.size() >= set2.size()){//smaller set is merged into bigger set
			set1.union(set2);
			Block[] mergedBlocks = set2.getBlocksArray();//the blocks in smaller set
			for(int index = 0; index <= mergedBlocks.length-1; index++){
				blocks[mergedBlocks[index].getX()][mergedBlocks[index].getY()] = set1;
			}
		}
		else{//set2 bigger than set1
			set2.union(set1);
			Block[] mergedBlocks = set1.getBlocksArray();//the blocks in smaller set
			for(int index = 0; index <= mergedBlocks.length-1; index++){
				blocks[mergedBlocks[index].getX()][mergedBlocks[index].getY()] = set2;
			}
		}
	}//end union method
	
}//end Grouping
