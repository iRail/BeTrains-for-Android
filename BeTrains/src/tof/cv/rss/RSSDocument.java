package tof.cv.rss;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RSSDocument {

	private URL rssURL;
	private Document document;
	private RSSFeed feed;
	
	public RSSDocument(URL rssURL) {
		this.rssURL = rssURL;
		makeDocument();
	}

	private void makeDocument() {

		URLConnection urlconn;
		try {
			/*
			 * we make the connection and a document is build , through parsing the xml stream
			 */
			urlconn = rssURL.openConnection();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(urlconn.getInputStream());
			makeRSSFeed();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void makeRSSFeed(){
		this.feed = new RSSFeed();
		NodeList root = document.getElementsByTagName("rss");
		Node rootrss = root.item(0); // <rss ...>
		NodeList listOfRssChildren = rootrss.getChildNodes();
		/*
		 * we are only interested in the children with tag name "item"
		 */
		for(int i = 0; i < listOfRssChildren.getLength();i++){
			Node child = listOfRssChildren.item(i);
			/*
			 * if you look at the xml structure you would see that there is no other child than
			 * channel, but apparently some #text slipped in the xml stream
			 * so that is also considered a child
			 */
			if(child.getNodeName().equals("channel")){
				NodeList listOfChannelChildren = child.getChildNodes();
				System.out.println("channelnode found !!");
				/*
				 * here we are at the level of title, link, etc we need the ones with item
				 */
				for(int j=0; j < listOfChannelChildren.getLength();j++){
					Node child2 = listOfChannelChildren.item(j);
					String title = null;
					String pubDate = null;
					String description = null;
					String link = null;
					if(child2.getNodeName().equals("item")){
						System.out.println("item found !!");
						NodeList propertiesOfItem = child2.getChildNodes();
						
						/*
						 * we have now a list of nodes : description, title, link, and pubdate						
						 */
						for(int k = 0; k < propertiesOfItem.getLength();k++){
							Node property = propertiesOfItem.item(k);
							if(property.getNodeName().contains("title")){
								title = property.getFirstChild().getNodeValue();
							}else if(property.getNodeName().contains("link")){
								pubDate = property.getFirstChild().getNodeValue();
							}else if(property.getNodeName().contains("description")){
								description = property.getFirstChild().getNodeValue();
							}else if(property.getNodeName().contains("pubDate")){
								pubDate = property.getFirstChild().getNodeValue();
							}
						}
						/*
						 *TODO : make proper RSSItem constructor lol =D
						 */
						RSSItem item = new RSSItem();
						item.setDescription(description);
						item.setTitle(title);
						item.setLink(link);
						item.setPubdate(pubDate);
						this.feed.getList().add(item);
					}
				}
			}
		}
	}
	
	public RSSFeed getRSSFeed(){
		return this.feed;
	}

}
