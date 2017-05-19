package yyt2xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class JktcgScrapper {

	ArrayList<String> rarities;
	String[] baseRarities = {"RR+","RR","R","UC","C","CR","CC"};
	String[] foilRarities = {"SSP","SP","RRR","SR"};
	String[] promoRarities = {"PR"};
	String[] trialRarities = {"TD"};
	
	public float progress = 0;
	public boolean foils = false;
	public boolean promos = false;
	public boolean trial = false;
	public boolean withColor = false;
	
	public boolean local = true;
	
	public void updateRarityList(){
		this.rarities = new ArrayList<String>();
		this.rarities.addAll(new ArrayList<String>(Arrays.asList(this.baseRarities)));
		if(this.foils){
			this.rarities.addAll(new ArrayList<String>(Arrays.asList(this.foilRarities)));
		}
		if(this.promos){
			this.rarities.addAll(new ArrayList<String>(Arrays.asList(this.promoRarities)));
		}
		if(this.trial){
			this.rarities.addAll(new ArrayList<String>(Arrays.asList(this.trialRarities)));
		}
	}
	
	public ArrayList<CardRow> parseJktcgPage(String cardsUrl) throws Exception{
		
		System.out.println("Parsing from Jktcg");
		
		this.updateRarityList();

		Properties properties = new Properties();
		InputStream input = new FileInputStream("enColorPairs.properties");
		properties.load(input);
	
		ArrayList<CardRow> cardRows = new ArrayList<CardRow>();
		URL url = new URL(cardsUrl);
		URI uri = url.toURI();
		
		Document doc = Jsoup.connect(cardsUrl).maxBodySize(0).get();
		/*File input = new File("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\lol.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		
		Elements headers = doc.select("h1");
		
		int count = 0;

		for(Element header : headers){
			
			String rarity = this.isRarityHeader(header.text());
			
			if(!rarity.equals("No")){
			
				Element table = header.nextElementSibling();
				
				Elements cartas = this.getTableRows(table);
				
				for(Element carta : cartas){
					//System.out.println("td: " + carta.text());
					if(!carta.text().equals("")){

						List<TextNode> texts = carta.textNodes();
						Elements htmls = carta.getElementsByTag("b");
						
						CardRow cardRow = new CardRow();
						
						cardRow.rarity = rarity;
						cardRow.cardId = this.getCleanCardId(texts.get(0).toString());
						cardRow.price = htmls.get(2).text().replace("$", "");
						cardRow.stock = htmls.get(0).textNodes().get(0).toString();
						cardRow.url = cardsUrl;
						String dirtyImgUrl = carta.getElementsByTag("img").attr("src");
						String cleanImgUrl = dirtyImgUrl.replaceAll("	", "");
						cardRow.imgUrl = uri.resolve(cleanImgUrl).toString();
						cardRow.color = properties.getProperty(cardRow.cardId);
						
						cardRows.add(cardRow);
						
						count++;
						
						this.progress = count;
						if(local){
							System.out.println(this.progress + " cartas procesadas.");
						}
					}
				}
			}

		}
		
		return cardRows;
	}
	
	
	public String getJktcgSinglesFrame(String setUrl) throws Exception{
		String frameUrl = setUrl;
		
		Document doc = Jsoup.connect(setUrl).maxBodySize(0).get();
		/*File input = new File("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\canaan.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		
		Element frame = doc.select("[name=rightside]").first();
		frameUrl = frame.attr("abs:src");
		
		return frameUrl;
	}
	
	public String isRarityHeader(String header) throws Exception{
		String actualRarity = "No";
		
		for(String rarity : this.rarities){
			if(header.startsWith(rarity + " ")){
				actualRarity = rarity;
				if(header.contains("FOIL")){
					actualRarity = "S-" + rarity;
				}
			}
		}
		return actualRarity;
	}
	
	public Elements getTableRows(Element table) throws Exception{
		
		Elements cartas = table.getElementsByTag("td");
		
		Element nextElement = table.nextElementSibling();
		if(nextElement != null){
			if(nextElement.is("table")){
				Elements masCartas = this.getTableRows(nextElement);
				cartas.addAll(masCartas);
			}
		}
		
		return cartas;
	}
	
	public String getCleanCardId(String cardId){

		String cleanId = cardId.replaceAll(" (.+/.+-.+?\\d+).* ", "$1");
		return cleanId;
	}
	
	public float getProgress(){
		return this.progress;
	}
}
