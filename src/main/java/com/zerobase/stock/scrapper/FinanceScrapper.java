package com.zerobase.stock.scrapper;

import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.ScrapedResult;

public interface FinanceScrapper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
