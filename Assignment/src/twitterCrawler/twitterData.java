package twitterCrawler;

public class twitterData {
	
	String twitterText = "";
	String twitterId = "";
	String emotion = "";
	
	public twitterData(String twitterText, String twitterId, String emotion) {
		this.twitterId = twitterId;
		this.twitterText = twitterText;
		this.emotion = emotion;
	}

	public String getEmotion() {
		return emotion;
	}

	public void setEmotion(String emotion) {
		this.emotion = emotion;
	}

	public String getTwitterText() {
		return twitterText;
	}

	public void setTwitterText(String twitterText) {
		this.twitterText = twitterText;
	}

	public String getTwitterId() {
		return twitterId;
	}

	public void setTwitterId(String twitterId) {
		this.twitterId = twitterId;
	}

}
