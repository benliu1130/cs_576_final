
public class Test_04 {

	/**
	 * test zigzag
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Zigzag zigzag = new Zigzag();
		System.out.println(zigzag.getX() + " " + zigzag.getY());
		while(!(zigzag.getX() == 7 && zigzag.getY() == 7)){
			zigzag.go();
			System.out.println(zigzag.getX() + " " + zigzag.getY());
		}
	}

}
