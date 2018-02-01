import pacsim.PacAction;
import pacsim.PacFace;
import pacsim.PacSim;

public class PacSimRNNA implements PacAction {

    //constructor should be formatted this way
    public PacSimRNNA(String fname) {
        PacSim sim = new PacSim( fname );
        sim.init(this);
    }

    // instantiate class instants
    public static void main( String[] args ) {
        new PacSimRNNA( args[ 0 ] );
    }

    //called every time pacman moves
    //TODO Implement RNNA here
    @Override
    public PacFace action(Object o) {

        //TODO compute RNNA path

        //TODO determine next move
        return null;
    }

    //reset variables here
    //gets called at every new run
    @Override
    public void init() {

    }
}
