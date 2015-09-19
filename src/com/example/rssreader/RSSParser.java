package com.example.rssreader;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class RSSParser {
	// RSS XML document CHANNEL tag
	private static String TAG_CHANNEL = "channel";
	private static String TAG_TITLE = "title";
	private static String TAG_LINK = "link";
	private static String TAG_DESRIPTION = "description";
	private static String TAG_LANGUAGE = "language";
	private static String TAG_ITEM = "item";
	private static String TAG_PUB_DATE = "pubDate";
	private static String TAG_GUID = "guid";

	public void RSSParser() {

	}

	public RSSFeed getRSSFeed(String url) {
		RSSFeed rssFeed = null;
		String rss_feed_xml = null;
		String rss_url;
		// getting rss link from html source code
		if (isRSSLink(url)) {
			rss_url = url;
		} else {
			rss_url = this.getRSSLinkFromURL(url);
		}
		Log.i("RSSParser", "rss_url--->" + rss_url);
		if (rss_url != null) {
			rss_feed_xml = this.getXmlFromUrl(rss_url);
			if (rss_feed_xml != null) {
				// successfully fetched rss xml
				// parse the xml
				try {
					Document doc = this.getDomElement(rss_feed_xml);
					NodeList nodeList = (NodeList) doc
							.getElementsByTagName(TAG_CHANNEL);
					Element e = (Element) nodeList.item(0);

					// RSS nodes
					String title = this.getValue(e, TAG_TITLE);
					String link = this.getValue(e, TAG_LINK);
					String description = this.getValue(e, TAG_DESRIPTION);
					String language = this.getValue(e, TAG_LANGUAGE);

					rssFeed = new RSSFeed(title, description, link, rss_url,
							language);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {

			}
		}
		return rssFeed;
	}

	private boolean isRSSLink(String url) {
		// TODO Auto-generated method stub
		String[] substr = url.split("/");
		int length = substr.length;
		String str = substr[length - 1];
		Log.i("AddNewSite", "last substr:" + str);
		if (str.equalsIgnoreCase("feed") || str.equalsIgnoreCase("xml")
				|| str.equalsIgnoreCase("rss")) {
			Log.i("RSSParser", "isRSSLink true");
			return true;
		} else {
			Log.i("RSSParser", "isRSSLink false");
			return false;
		}
	}

	public List<RSSItem> getRSSFeedItems(String rss_url) {
		List<RSSItem> itemsList = new ArrayList<RSSItem>();
		String rss_feed_xml;

		rss_feed_xml = this.getXmlFromUrl(rss_url);
		if (rss_feed_xml != null) {
			try {
				Document doc = this.getDomElement(rss_feed_xml);

				NodeList nodeList = doc.getElementsByTagName(TAG_CHANNEL);
				Element e = (Element) nodeList.item(0);

				NodeList items = e.getElementsByTagName(TAG_ITEM);
				for (int i = 0; i < items.getLength(); i++) {
					Element e1 = (Element) items.item(i);
					String title = this.getValue(e1, TAG_TITLE);
					String link = this.getValue(e1, TAG_LINK);
					String description = this.getValue(e1, TAG_DESRIPTION);
					String pubdate = this.getValue(e1, TAG_PUB_DATE);
					String guid = this.getValue(e1, TAG_GUID);

					RSSItem rssItem = new RSSItem(title, link, description,
							pubdate, guid);

					// adding item to list
					itemsList.add(rssItem);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return itemsList;
	}

	private String getValue(Element item, String str) {
		// TODO Auto-generated method stub
		NodeList n = item.getElementsByTagName(str);
		return this.getElementValue(n.item(0));
	}

	private String getElementValue(Node elem) {
		// TODO Auto-generated method stub
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE
							|| (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Getting XML DOM element
	 * 
	 * @param XML
	 *            string
	 * */
	private Document getDomElement(String xml) {
		// TODO Auto-generated method stub
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = (Document) db.parse(is);
		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		return doc;
	}

	private String getXmlFromUrl(String url) {
		// TODO Auto-generated method stub
		String xml = null;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("RSSParser", "xml--->" + xml);
		return xml;
	}

	private String getRSSLinkFromURL(String url) {
		// TODO Auto-generated method stub
		String rss_url = null;
		try {
			// Using JSoup library to parse the html source code
			org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
			Log.i("RSSParser", doc.toString());
			// finding rss links which are having link[type=application/rss+xml]
			Elements links = doc.select("link[type=application/rss+xml]");
			Log.d("No of RSS links found", " " + links.size());

			// check if urls found or not
			if (links.size() > 0) {
				rss_url = links.get(0).attr("href").toString();

			} else {
				org.jsoup.select.Elements links1 = doc
						.select("link[type=application/atom+xml]");
				if (links1.size() > 0) {
					rss_url = links1.get(0).attr("href").toString();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rss_url;
	}
}
