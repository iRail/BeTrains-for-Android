package tof.cv.mpp.bo;

public class MaterialType {
    public String parent_type;
    public String sub_type;
    public String orientation;

    public MaterialType(String newParentType, String newSubType, String newOrientation) {
        this.parent_type=newParentType;
        this.sub_type=newSubType;
        this.orientation=newOrientation;
    }
}
