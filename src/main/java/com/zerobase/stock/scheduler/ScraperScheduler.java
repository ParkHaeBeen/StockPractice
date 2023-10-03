package com.zerobase.stock.scheduler;

import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.ScrapedResult;
import com.zerobase.stock.model.constant.CacheKey;
import com.zerobase.stock.persisit.CompanyRepository;
import com.zerobase.stock.persisit.DividendRepository;
import com.zerobase.stock.persisit.entity.CompanyEntity;
import com.zerobase.stock.persisit.entity.DividendEntity;
import com.zerobase.stock.scrapper.FinanceScrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final FinanceScrapper yahooFinanceScraper;

    //@CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {

        //저장된 회사목록을 조회
        List<CompanyEntity> companies = companyRepository.findAll();

        //회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper.scrap(Company.builder()
                    .name(company.getName())
                    .ticker(company.getTicker())
                    .build());

            //스크래핑한 배당금 정보중 데이터 베이스에 없는 값은 저장
            scrapedResult.getDividendList().stream()
                    .map(e -> new DividendEntity(company.getId(), e))
                    .forEach(e -> {
                        boolean exist = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exist) {
                            dividendRepository.save(e);
                            log.info("insert new dividend -> "+e.toString());
                        }
                    });


            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
