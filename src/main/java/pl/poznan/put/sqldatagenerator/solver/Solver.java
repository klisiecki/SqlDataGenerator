package pl.poznan.put.sqldatagenerator.solver;

import org.apache.log4j.Logger;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;

public class Solver {
    private static final Logger logger = Logger.getLogger(Solver.class);

    private Restrictions restrictions;

    public Solver(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    public void solve() {
        logger.info("Solving " + restrictions);
    }
}
