import java.util.Random;
import java.math.*;

public class Distribution {
	/**
	 * 
	 * @param pro
	 * @return
	 */
	static Random rd = new Random(System.currentTimeMillis());


	/**
	 * 
	 * @param pro
	 * @param k
	 * @return
	 */
	public static double laplace(double pro, int k) {
		pro = k / pro;
		double _para = 0.5;

		double a = rd.nextDouble();
		double result = 0;
		double temp = 0;
		if (a < _para) {
			temp = pro * Math.log(2 * a);
			result = temp;
		} else if (a > _para) {
			temp = -pro * Math.log(2 - 2 * a);
			result = temp;
		} else
			result = 0;

		return result;
	}
	
	
	/**
	 * ¼ÆËã½×³ËC(max, i) //iejr: ¼ÆËã×éºÏC(max, i)
	 * 
	 * @param maxCardinality
	 * @param i
	 * @return
	 */
	public static int calculateFCT(int maxCardinality, int i) {
		if (i >= maxCardinality || i == 0)
			return 1;
		long num = 1;
		
		if( 2*i > maxCardinality ){
			
			for( int j = maxCardinality;j >= i + 1;j-- ){
				num *= j;
			}
			
			for(; maxCardinality - i > 1; i++){
				num /= (maxCardinality - i);
			}
			
		}else{
		
			for (int j = maxCardinality; j >= maxCardinality - i + 1; j--)
				num *= j;
			for (; i > 1; i--) {
				num /= i;
			}
			
		}
	
		return (int) (num);
	}
	
}
