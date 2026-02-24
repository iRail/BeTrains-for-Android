package tof.cv.mpp.bo;

import java.util.ArrayList;

public class Perturbations {
    public ArrayList<Perturbation> disturbance;

    public static class Perturbation {
        public String title;
        public String description;
        public String link;
        public long timestamp;
    }
}
