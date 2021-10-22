package tof.cv.mpp.bo;

public class Message {

    public String getUser_name() {
        return user_name;
    }

    public String getTrain_id() {
        return train_id.replace("BE.NMBS.","");
    }

    public String getEntry_date() {
        return entry_date;
    }

    public String getUser_message() {
        return user_message;
    }

    private String user_name;
    private String user_message;
    private String entry_date;
    private String train_id;
    public String pic_url;
    public String user_id;
    public boolean donator;
    public boolean beta;

    public Message(String user_name, String user_message, String entry_date, String train_id, String pic, String user,boolean don,boolean beta) {
        this.user_name = user_name;
        this.user_message = user_message;
        this.entry_date = entry_date;
        this.train_id = train_id;
        this.pic_url = pic;
        this.user_id = user;
        this.donator=don;
        this.beta=beta;
    }

    public Message() {
    }


    public void setUserMessage(String replace) {
        this.user_message=replace;

    }
}


