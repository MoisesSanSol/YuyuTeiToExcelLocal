package yyt2xlsUtilities;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import yyt2xls.CardRow;
import yyt2xls.JktcgScrapper;
import yyt2xls.ToExcel;

public class Main {

	public static void main(String[] args) throws Exception{

		System.out.println("*** Starting ***");
		
		/*JktcgScrapper jktcgScrapper = new JktcgScrapper();
		jktcgScrapper.foils = true;
		jktcgScrapper.trial = true;
		jktcgScrapper.promos = true;
		jktcgScrapper.local = true;
		
		String frameUrl = jktcgScrapper.getJktcgSinglesFrame("http://jktcg.com/WS_EN/EN_AB_W31/EN_AB_W31_DL.html");
		ArrayList<CardRow> allCards = jktcgScrapper.parseJktcgPage(frameUrl);
		*/
		//ArrayList<CardRow> allCards = jktcgScrapper.parseJktcgPage("");

		/*YuyuteiScrapper yytscrapper = new YuyuteiScrapper();
    	yytscrapper.foils = true;
    	ArrayList<CardRow> allCards = yytscrapper.parseYuyuteiPage("http://yuyu-tei.jp/game_ws/sell/sell_price.php?ver=konosuba");
		*/
		
		JktcgHelper helper = new JktcgHelper();
		//helper.generateSeriesUrlsFile();
		//helper.generateAllCardsFile();
		//helper.generateUniqueCardsFile();
		//helper.getColorFromWstcg("P4/EN-S01-088");
		helper.generateColorPairsFile();
		
		/*
		ToExcel formatter = new ToExcel();
    	formatter.withImages = false;
    	formatter.initialCant = 0;
    	formatter.local = true;
		byte[] bytes = formatter.generateExcel(allCards);
		
		FileOutputStream stream = new FileOutputStream("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\something.xls");
		try {
		    stream.write(bytes);
		} finally {
		    stream.close();
		}
		Desktop.getDesktop().open(new File("C:\\Users\\Moises BSS\\Desktop\\PruebasTemporales\\something.xls"));
		*/
		
		System.out.println("*** Finished ***");
	}
}
