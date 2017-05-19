package yyt2xlsUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import yyt2xls.JktcgScrapper;
import yyt2xls.CardRow;

public class JktcgHelper {
	
	public String enMenuUrl = "http://jktcg.com/MenuLeftEN.html";
	public String wstcgBaseUrl = "http://ws-tcg.com/en/cardlist/list/?cardno=";
	
	public void generateSeriesUrlsFile() throws Exception{
		
		Document doc = Jsoup.connect(this.enMenuUrl).maxBodySize(0).get();
		/*File input = new File(LocalConf.defaultFolder + "enmenu.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		
		Elements anchors = doc.select("a[href]");
		
		String fullPathWrite = LocalConf.defaultFolder + "allSeries.txt";
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPathWrite), "UTF-8"));

		for(Element anchor : anchors){
			
			writer.write(anchor.attr("abs:href"));
			writer.write("\r\n");
			
		}
		writer.close();
	}
	
	public void generateAllCardsFile() throws Exception{
		
		String fullPathRead = LocalConf.defaultFolder + "allSeries.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fullPathRead), "UTF-8"));
		
		String fullPathWrite = LocalConf.defaultFolder + "allCards.txt";
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPathWrite), "UTF-8"));
		
		JktcgScrapper scrapper = new JktcgScrapper();
		scrapper.foils = true;
		scrapper.promos = true;
		scrapper.trial = true;
		scrapper.updateRarityList();
		
		while(reader.ready()){
			
			String singlesUrl = scrapper.getJktcgSinglesFrame(reader.readLine());
			Document doc = Jsoup.connect(singlesUrl).maxBodySize(0).get();
			//System.out.println(doc.html());
			System.out.println(doc.location());
			
			Elements headers = doc.select("h1");

			for(Element header : headers){
				
				System.out.println("Header: " + header.text());

				String rarity = scrapper.isRarityHeader(header.text());
				
				if(!rarity.equals("No")){
				
					Element table = header.nextElementSibling();
					
					Elements cartas = scrapper.getTableRows(table);
					
					for(Element carta : cartas){
						
						System.out.println("Carta: " + carta.text());
						//System.out.println("td: " + carta.text());
						if(!carta.text().equals("")){

							List<TextNode> texts = carta.textNodes();
							String cardId = texts.get(0).toString();
							
							writer.write(cardId);
							writer.write("\r\n");
							
						}
					}
				}
			}
		}
		
		reader.close();
		writer.close();
	}
	
	public void generateUniqueCardsFile() throws Exception{
		
		String fullPathRead = LocalConf.defaultFolder + "allCards.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fullPathRead), "UTF-8"));

		String fullPathWrite = LocalConf.defaultFolder + "uniqueCards.txt";
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPathWrite), "UTF-8"));
		
		Set<String> uniqueCards = new HashSet<String>();
				
		while(reader.ready()){
			
			String line = reader.readLine();
			
			String cleanId = line.replaceAll("(.+/.+-.+?\\d+).* ", "$1");
			
			uniqueCards.add(cleanId);
		}

		reader.close();
		
		for(String carta : uniqueCards){

			writer.write(carta);
			writer.write("\r\n");

		}

		writer.close();
	}
	
	public void generateColorPairsFile() throws Exception{
		
		String fullPathRead = LocalConf.defaultFolder + "uniqueCards.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fullPathRead), "UTF-8"));

		Set<String> cards = new HashSet<String>();
		
		while(reader.ready()){
		
			String cardId = reader.readLine();
			cards.add(cardId);
		}

		reader.close();

		int count = 0; 
		
		Properties prop = new Properties();
		
		for(String cardId : cards){
			
			count++;
		
			String color = this.getColorFromWstcg(cardId);
			prop.setProperty(cardId, color);
			
	        float progress = (float)(((float)count / (float)cards.size()) * 100);

	        System.out.println(progress + "% by card");
		}
		
		OutputStream output = new FileOutputStream(LocalConf.defaultFolder + "colorPairs.properties");
		prop.store(output, null);
		output.close();
	}
	
	public String getColorFromWstcg(String cardId) throws Exception{
		String color = "Desconocido";
		
		Document doc = Jsoup.connect(this.wstcgBaseUrl + cardId).maxBodySize(0).get();
		/*File input = new File(LocalConf.defaultFolder + "enmenu.html");
		Document doc = Jsoup.parse(input, "UTF-8");*/
		//System.out.println(doc.html());
		//System.out.println(doc.location());
		
		try{
			Element colorTh = doc.select("th:contains(color)").first();
			Element colorTd = colorTh.nextElementSibling();
			Element colorImg = colorTd.children().first();
			String colorSrc = colorImg.attr("src");
			
			switch(colorSrc){
					case "../partimages/yellow.gif":
				color = "Amarillo";
				break;
				case "../partimages/green.gif":
					color = "Verde";
					break;
				case "../partimages/red.gif":
					color = "Rojo";
					break;
				case "../partimages/blue.gif":
					color = "Azul";
					break;
			}
		}
		catch(Exception ex){
			System.out.println(cardId + " " + ex.toString());
		}
		return color;
	}
	
	public ArrayList<CardRow> getAllCardsWithoutColor() throws Exception{

		ArrayList<CardRow> cards = new ArrayList<CardRow>();
		
		String fullPathRead = LocalConf.defaultFolder + "allSeries.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fullPathRead), "UTF-8"));
		
		String fullPathWrite = LocalConf.defaultFolder + "allCards.txt";
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPathWrite), "UTF-8"));
		
		JktcgScrapper scrapper = new JktcgScrapper();
		scrapper.foils = true;
		scrapper.promos = true;
		scrapper.trial = true;
		scrapper.updateRarityList();
		
		while(reader.ready()){
			
			String singlesUrl = scrapper.getJktcgSinglesFrame(reader.readLine());
			Document doc = Jsoup.connect(singlesUrl).maxBodySize(0).get();
			//System.out.println(doc.html());
			System.out.println(doc.location());
			
			Elements headers = doc.select("h1");

			for(Element header : headers){
				
				System.out.println("Header: " + header.text());

				String rarity = scrapper.isRarityHeader(header.text());
				
				if(!rarity.equals("No")){
				
					Element table = header.nextElementSibling();
					
					Elements cartas = scrapper.getTableRows(table);
					
					for(Element carta : cartas){
						
						System.out.println("Carta: " + carta.text());
						//System.out.println("td: " + carta.text());
						if(!carta.text().equals("")){

							List<TextNode> texts = carta.textNodes();
							String cardId = texts.get(0).toString();
							
							writer.write(cardId);
							writer.write("\r\n");
							
						}
					}
				}
			}
		}
		
		reader.close();
		writer.close();
		
		return null;
	}
}
