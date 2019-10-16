import java.awt.Point;


public class Test_11 {

	/**
	 * @test BlockSet
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BlockSet set1 = new BlockSet(1,3, 5,8);
		BlockSet set2 = new BlockSet(5,5, 6,-9);
		BlockSet set3 = new BlockSet(2,4, -3,2);
		set1.union(set2);
		set1.union(set3);
		System.out.println(set1.getAverageX());
		System.out.println(set1.getAverageY());
		System.out.println(set1.size());
		Block[] blocks = set1.getBlocksArray();
		for(int i = 0; i <= blocks.length-1; i++){
			System.out.println(blocks[i].getX() + " ");
			System.out.println(blocks[i].getY() + " ");
		}
		blocks = set2.getBlocksArray();
		for(int i = 0; i <= blocks.length-1; i++){
			System.out.println(blocks[i].getX() + " ");
			System.out.println(blocks[i].getY() + " ");
		}
	}

}
