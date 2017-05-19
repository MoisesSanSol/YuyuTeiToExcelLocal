package yyt2xls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YuyuteiScrapper {
	
	public float progress = 0;
	public boolean foils = false;
	public boolean promos = false;
	public boolean trial = false;
	public boolean withColor = false;
	
	public boolean local = false;
	
	public ArrayList<CardRow> parseYuyuteiPage(String pageUrl) throws Exception{
		ArrayList<CardRow> cardRows = new ArrayList<CardRow>();
		
		Document doc = Jsoup.connect(pageUrl).maxBodySize(0).get();
		/*File input = new File("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\canaan.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		
		Elements cards = doc.select("[class^=card_unit]");
		
		int count = 0;

		for(Element card : cards){
			
			CardRow cardRow = new CardRow();
			
			cardRow.rarity = card.className().replace("card_unit rarity_", "");
			
			if(this.filter(cardRow.rarity)){
			
				Element name = card.select(".id").first();
				cardRow.cardId = name.text();
				
				Element price = card.select(".price").first();
				cardRow.price = price.text().replace("円", "");
	
				if(price.html().contains("sale")){
					String[] prices = cardRow.price.split(" ");
					cardRow.sale = "Sale (" + prices[0] + ")";
					cardRow.price = prices[1];
				}
				
				Element stock = card.select(".stock").first();
				cardRow.stock = stock.text().replace("残：", "").replace("◯", "O");
				
				Element url = card.select("a[href]").first();
				cardRow.url = "http://yuyu-tei.jp" + url.attr("href");
				
				Element img = card.select("img").first();
				cardRow.imgUrl = "http://yuyu-tei.jp" + img.attr("src");
				
				if(this.withColor){
					cardRow.color = this.getCardColor(cardRow.url);
				}
				
				cardRows.add(cardRow);
			}
			
			count++;
			
			this.progress = (float)(((float)count / (float)cards.size()) * 100);
			
			if(local){
				System.out.println(this.progress + "%");
			}
		}
		
		return cardRows;
	}
	
	public String getCardColor(String cardUrl) throws Exception{
		
		String color = "Desconocido";
		
		Document doc = Jsoup.connect(cardUrl).maxBodySize(0).get();
		/*File input = new File("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\canaan.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		
		Element colorTd = doc.select("[data-colors").first();
		String colorJp = colorTd.text();
		
		if(colorJp.equals("黄色")){
			color = "Amarillo";
		} else if(colorJp.equals("緑色")){
			color = "Verde";
		} else if(colorJp.equals("赤色")){
				color = "Rojo";
		} else if(colorJp.equals("青色")){
			color = "Azul";
		} else if(colorJp.equals("紫色")){
			color = "Morado";
		}
		
		return color;
		
	}
	
	public float getProgress(){
		return this.progress;
	}
	
	public boolean filter(String rarity){
		
		boolean filter = false;
			
		if(rarity.startsWith("S") || rarity.equals("RRR") || rarity.equals("XR")){
			if(this.foils){
				filter = true;
			}
		}
		else if(rarity.equals("PR")){
			if(this.promos){
				filter = true;
			}
		}
		else if(rarity.equals("TD")){
			if(this.trial){
				filter = true;
			}
		}
		else{
			filter = true;
		}
		
		return filter;
	}
	
}
