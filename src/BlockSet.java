import java.util.HashSet;
import java.util.Set;

//a set of blocks
public class BlockSet {
	private int sumOfX; //sum of x of all vectors associated with the blocks in the set
	private int sumOfY; //sum of y of all vectors associated with the blocks in the set
	private int size; //number of vectors in this set
	private Set<Block> set; //a set of blocks' coordinate
	
	//create a set which has only one block(blockX, blockY)
	//the vector of this block is (vectorX, vectorY)
	public BlockSet(int blockX, int blockY, int vectorX, int vectorY){
		Block block = new Block(blockX, blockY);
		set = new HashSet<Block>();
		set.add(block);
		sumOfX = vectorX;
		sumOfY = vectorY;
		size = set.size();
	}//end BlockSet constructor
	
	//return the number of blocks in this set
	public int size(){
		return size;
	}
	
	//return the set
	private Set<Block> getSet(){
		return set;
	}
	
	//return sum of x of all vectors in the set
	private int getSumOfX(){
		return sumOfX;
	}
	
	//return sum of x of all vectors in the set
	private int getSumOfY(){
		return sumOfY;
	}
	
	//Union two BlockSet
	public void union(BlockSet anotherBlockSet){
		sumOfX = sumOfX + anotherBlockSet.getSumOfX();
		sumOfY = sumOfY + anotherBlockSet.getSumOfY();
		size = size + anotherBlockSet.size();
		set.addAll(anotherBlockSet.getSet());
	}//end union
	
	//return all the blocks in an Block array
	public Block[] getBlocksArray(){
		Block[] blocks = new Block[0];
		blocks = set.toArray(blocks);
		return blocks;
	}//end getBlocksArray
	
	//return average of x of all vectors associated with the blocks in the set
	public double getAverageX(){
		return (double)sumOfX/size;
	}
	
	//return average of y of all vectors associated with the blocks in the set
	public double getAverageY(){
		return (double)sumOfY/size;
	}
	
}//end BlockSet