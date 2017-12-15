/**
 * Created by temp on 12/14/2017.
 */
public class PrivSuper {
    String database;
    double eps;
    double eps1, eps2, eps3;
    int k;

    public PrivSuper(String database, double eps, int k) {
        this.database = database;
        this.eps = eps;
        this.k = k;
    }

    public void run() {
    	
    }

    public void splitBudget() {
        eps1 = eps * 0.15;
        eps2 = eps * 0.5;
        eps3 = eps * 0.35;
    }
}
