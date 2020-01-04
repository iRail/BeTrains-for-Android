package tof.cv.mpp.bo;

import java.util.ArrayList;

public class TrainComposition {
    public Composition composition;

    public class Composition {
        public Segments segments;

        public class Segments {
            public ArrayList<Segment> segment;

            public class Segment {
                public SegmentComposition composition;

                public class SegmentComposition {
                    public Units units;

                    public class Units {
                        public ArrayList<Unit> unit;

                        public class Unit {
                            public int seatsFirstClass;
                            public MaterialType materialType;
                        }
                    }
                }
            }
        }
    }
}
