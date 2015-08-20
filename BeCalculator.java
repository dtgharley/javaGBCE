import java.lang.Boolean;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.lang.Math;

enum Cmpf {
    Common, Preferred; 
}

enum Bysl {
    Buy, Sell; 
}

class Stock {
	public Stock(String code, Cmpf cop, int ld, int fd, int pv) {
		this.code = code;
                this.cop = cop;
                this.lastDividend = ld;
                this.fixedDividend = fd;
                this.parValue = pv;
		this.trades = new ArrayList<TradeDetails>();
	}
	String code;
	Cmpf cop;
	int lastDividend;
	int fixedDividend;
	int parValue;
        List<TradeDetails> trades;
}

class CodePrice {
        public CodePrice(int code, int price) {
		this.code = code;
		this.price = price;
	}
	int code;
        int price;
}

class TradeDetails {
        public TradeDetails(int code, Date ts, int quantity, Bysl bs, int price) {
		this.code = code;
		this.timeStamp = ts;
		this.quantity = quantity;
		this.buySell = bs;
		this.price = price;
	}
	int code;
	Date timeStamp;
        int quantity;
	Bysl buySell;
        int price;
}

public class BeCalculator {

	static BufferedReader br;
	static List<Stock> stocks;
	static HashMap<String, Integer> codemap;
        static CodePrice cp;
        static TradeDetails td;

	public static void main(String[] args) {
		stocks = new ArrayList<Stock>();
		stocks.add(new Stock("TEA", Cmpf.Common, 0, -1, 100));
		stocks.add(new Stock("POP", Cmpf.Common, 8, -1, 100));
		stocks.add(new Stock("ALE", Cmpf.Common, 23, -1, 60));
		stocks.add(new Stock("GIN", Cmpf.Preferred, 8, 2, 100));
		stocks.add(new Stock("JOE", Cmpf.Common, 13, -1, 250));
                int i = 0;
		codemap = new HashMap<String, Integer>();
		for (Stock ts : stocks) {
			codemap.put(ts.code.toLowerCase(), new Integer(i++));
		}
        	cp = new CodePrice(-1, -1);
		Date cd = new Date();
        	td = new TradeDetails(-1, cd, -1, Bysl.Buy, -1);
		br = new BufferedReader(new InputStreamReader(System.in));
		String quit_pattern = "^q(uit)*$"; 
		Pattern qp = Pattern.compile(quit_pattern);
		Matcher qm = qp.matcher("");
		String request_pattern = "^[dprvg]$"; 
		Pattern rp = Pattern.compile(request_pattern);
		Matcher rm;
		String str; 
		try {
			do { 
				print_input_message();
				print_input_prompt();
				str = br.readLine().toLowerCase();
				rm = rp.matcher(str);
				if (rm.find()) {
					switch (str) {
						case "d":
						 	read_stock_price();
							if (cp.code != -1) {
								Stock tstock = stocks.get(cp.code);
								int dy;
								if (tstock.cop == Cmpf.Common) {
									dy = (100 * tstock.lastDividend) / cp.price;
								} else {
									dy = (tstock.fixedDividend * tstock.parValue) / cp.price;
								}
								System.out.printf("\nThe Dividend Yield for %s at %dp is %d%%\n\n", tstock.code, cp.price, dy);
							}
							break;
						case "p":
						 	read_stock_price();
							if (cp.code != -1) {
								Stock tstock = stocks.get(cp.code);
								float pe;
								if (tstock.cop == Cmpf.Common) {
									pe = (float) cp.price / tstock.lastDividend;
								} else {
									pe = (float) cp.price / ((tstock.fixedDividend * tstock.parValue) / 100);
								}
								System.out.printf("\nThe Price to Earnings Ratio for %s at %dp is %.2f\n\n", tstock.code, cp.price, pe);
							}
							break;
						case "r":
						 	read_trade_details();
							if (td.code != -1) {
								Stock tstock = stocks.get(td.code);
								tstock.trades.add(new TradeDetails(td.code, td.timeStamp, td.quantity, td.buySell, td.price));
								String tbs = (td.buySell == Bysl.Buy) ? "purchase" : "sale";
								System.out.printf("\nTrade recorded for %s of %d %s shares at %dp on %s\n\n", tbs, td.quantity, tstock.code, td.price, td.timeStamp.toString());
							}
							break;
						case "v":
						 	read_stock_code();
							if (cp.code != -1) {
								Stock tstock = stocks.get(cp.code);
								int vwsb = calc_vwi(cp.code);
								System.out.printf("\nThe Volume Weighted Stock Price for %s is %dp\n\n", tstock.code, vwsb);
							}
							break;
						case "g":
							int slsz = stocks.size();
							int li = 0;
							double ttp = 1.0;
							int apc = 0;
							while (li < slsz) {
								int cvwsb = calc_vwi(li);
								if (cvwsb != 0) {
									ttp *= (double) cvwsb;
									apc++;
								}
								li++;
							}
							double gbce = (apc == 0) ? 0.0 : Math.pow(ttp, (double) 1.0 / (double) apc);	
							System.out.printf("\nThe GBCE All Share Index is currently %dp\n\n", (int) gbce);
							break;
					}
                                        continue;
				}
      				qm = qp.matcher(str);
                                if (qm.find()) break;
			} while(!qm.find());
		}
		catch(IOException e)
		{
			System.out.printf("IO exception: %s", e.toString());
		}
	} 
	static void print_input_message () {
		System.out.println("Request: D)ividend Yield"); 
		System.out.println("         P)/E Ratio"); 
		System.out.println("         R)ecord Trade");
		System.out.println("         V)olume Weighted Stock Price");
		System.out.println("         G)BCE All Share Index");
		System.out.println("or type \"q)uit\" to terminate the program.");
	}
        static void print_input_prompt () {
		System.out.print("> ");
	}
	static void read_stock_price() {
		String stockprice_pattern = "^(?<c>[a-z]{3}+)\\s*(?<p>\\d{1,5})p?$"; 
		Pattern spp = Pattern.compile(stockprice_pattern);
		Matcher spm = spp.matcher("");
		String stockback_pattern = "^b(ack)*$"; 
		Pattern sbp = Pattern.compile(stockback_pattern);
		Matcher sbm = sbp.matcher("");
		String str;
		try {
			boolean ok = true;
 			do {
				System.out.println("Please enter the stock code and price.");
				print_input_prompt();
				str = br.readLine().toLowerCase();
				spm = spp.matcher(str);
				if (spm.find()) {
					String tc = spm.group("c");
					String tp = spm.group("p");
					cp.code = codemap.containsKey(tc) ? codemap.get(tc) : -1;
					cp.price  = Integer.parseInt(tp);
					if (cp.code != -1) {
						return;
					}
				}
				sbm = sbp.matcher(str);
				if (sbm.find()) {
					cp.code = -1;
					cp.price = -1;
					return;
				}
					
			} while (ok);
					
		}
		catch(IOException e)
		{
			System.out.printf("IO exception: %s", e.toString());
		}
	}
	static void read_stock_code() {
		String stockcode_pattern = "^(?<c>[a-z]{3}+)$"; 
		Pattern scp = Pattern.compile(stockcode_pattern);
		Matcher scm = scp.matcher("");
		String stockback_pattern = "^b(ack)*$"; 
		Pattern sbp = Pattern.compile(stockback_pattern);
		Matcher sbm = sbp.matcher("");
		String str;
		try {
			boolean ok = true;
 			do {
				System.out.println("Please enter the stock code.");
				print_input_prompt();
				str = br.readLine().toLowerCase();
				scm = scp.matcher(str);
				if (scm.find()) {
					String tc = scm.group("c");
					cp.code = codemap.containsKey(tc) ? codemap.get(tc) : -1;
					cp.price  = -1;
					if (cp.code != -1) {
						return;
					}
				}
				sbm = sbp.matcher(str);
				if (sbm.find()) {
					cp.code = -1;
					cp.price = -1;
					return;
				}
					
			} while (ok);
					
		}
		catch(IOException e)
		{
			System.out.printf("IO exception: %s", e.toString());
		}
	}
	static void read_trade_details() {
		String tradedetails_pattern = "^(?<b>(buy|sell))\\s*(?<q>\\d{1,5})\\s*(?<c>[a-z]{3}+)\\s*(at|@)\\s*(?<p>\\d{1,5})p?$"; 
		Pattern tdp = Pattern.compile(tradedetails_pattern);
		Matcher tdm = tdp.matcher("");
		String tradeback_pattern = "^b(ack)*$"; 
		Pattern tbp = Pattern.compile(tradeback_pattern);
		Matcher tbm = tbp.matcher("");
		String str;
		try {
			boolean ok = true;
 			do {
				System.out.println("Please enter either \"Buy\" or \"Sell\" followed by the quantity, and share code followed by \"at\" or \"@\" and the price of the shares required.");
				print_input_prompt();
				str = br.readLine().toLowerCase();
				tdm = tdp.matcher(str);
				if (tdm.find()) {
					String tc = tdm.group("c");
					String tq = tdm.group("q");
					String tb = tdm.group("b");
					tb = tb.substring(0, 1).toUpperCase() + tb.substring(1);
					String tp = tdm.group("p");
					td.code = codemap.containsKey(tc) ? codemap.get(tc) : -1;
					td.timeStamp = new Date();
					td.quantity  = Integer.parseInt(tq);
					td.buySell  = Bysl.valueOf(tb);
					td.price  = Integer.parseInt(tp);
					if (td.code != -1) {
						return;
					}
				}
				tbm = tbp.matcher(str);
				if (tbm.find()) {
					cp.code = -1;
					cp.price = -1;
					return;
				}
					
			} while (ok);
					
		}
		catch(IOException e)
		{
			System.out.printf("IO exception: %s", e.toString());
		}
	}
	static int calc_vwi(int code) {
		Stock tstock = stocks.get(code);
		int tlsz = tstock.trades.size();
		int li = tlsz - 1;
		Date ld = new Date();
		long ct = ld.getTime();
		int ttpq = 0;
		int ttq = 0;
		while (li >= 0) {
			TradeDetails ctd = tstock.trades.get(li);
			long ctt = ctd.timeStamp.getTime();
			boolean lt15 = ct - ctt < 15 * 60 * 1000;
			if (lt15) {
				ttpq += ctd.quantity * ctd.price;
				ttq += ctd.quantity;
				li--;
			} else {
				break;
			}
		}
		int vwsp = ttq == 0 ? 0 : ttpq / ttq;	
		return vwsp;
	}

}
