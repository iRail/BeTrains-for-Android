package tof.cv.mpp.bo;

import java.util.ArrayList;

public class TrainComposition {
    public Composition composition;

    public static class Composition {
        public Segments segments;

        public static class Segments {
            public ArrayList<Segment> segment;

            public static class Segment {
                public SegmentComposition composition;

                public static class SegmentComposition {
                    public Units units;

                    public static class Units {
                        public ArrayList<Unit> unit;

                        public static class Unit {
                            public int seatsFirstClass;
                            public MaterialType materialType;
                        }
                    }
                }
            }
        }
    }
}
