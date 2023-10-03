package com.zerobase.stock.scrapper;

import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.Dividend;
import com.zerobase.stock.model.ScrapedResult;
import com.zerobase.stock.model.constant.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanaceScrapper implements FinanceScrapper{

    private static final String URL="https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static  final String SUMMARY_URL="https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME=86400;
    @Override
    public ScrapedResult scrap(Company company){
        ScrapedResult scrapedResult=new ScrapedResult();
        scrapedResult.setCompany(company);
        try{

            long end=System.currentTimeMillis()/1000;
            String url = String.format(URL, company.getTicker(), START_TIME, end);
            Connection connection= Jsoup.connect(url);
            Document document=connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0);

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends=new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt=e.text();
                if(!txt.endsWith("Dividend")) continue;

                String[] splits = txt.split(" ");
                int month= Month.strTomNumber(splits[0]);
                int day=Integer.valueOf(splits[1].replace(",",""));
                int year=Integer.valueOf(splits[2]);
                String dividend=splits[3];

                if(month<0){
                    throw new RuntimeException("Unexpected Month Enum"+splits[0]);
                }

                Dividend dividendBuild = Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend)
                        .build();

                dividends.add(dividendBuild);

            }
            scrapedResult.setDividendList(dividends);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker){
        String url = String.format(SUMMARY_URL, ticker,ticker);
        try {
            Document document=Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            System.out.println(titleEle.text());
            String[] titles = titleEle.text().split(" - ");
            String title="";
            if(titles.length<=1){
                title=titles[0].replace(" -","");
            }else{
                title=titles[1].trim();
            }

            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
