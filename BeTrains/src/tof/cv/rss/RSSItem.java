package tof.cv.rss;

public class RSSItem {

	private String title = null;
	private String description = null;
	private String link = null;
	private String pubdate = null;

	RSSItem() {
	}

	void setTitle(String value) {
		title = value;
	}

	void setDescription(String value) {
		description = value;
	}

	void setLink(String value) {
		link = value;
	}

	void setPubdate(String value) {
		pubdate = value;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public String getPubdate() {
		return pubdate;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return title;
	}
}
