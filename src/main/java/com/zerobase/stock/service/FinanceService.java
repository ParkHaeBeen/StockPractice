package com.zerobase.stock.service;

import com.zerobase.stock.exception.Impl.NoCompanyException;
import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.Dividend;
import com.zerobase.stock.model.ScrapedResult;
import com.zerobase.stock.model.constant.CacheKey;
import com.zerobase.stock.persisit.CompanyRepository;
import com.zerobase.stock.persisit.DividendRepository;
import com.zerobase.stock.persisit.entity.CompanyEntity;
import com.zerobase.stock.persisit.entity.DividendEntity;
import lombok.AllArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    //@Cacheable(key = "#companyName",value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName){
        //회사명 기준으로 회사 정보를 조회
        CompanyEntity company = companyRepository.findByName(companyName)
                .orElseThrow(()->new NoCompanyException());


        //조회된 회사 아아디로 배당듬 정보 조히
        List<DividendEntity> companyDividend = dividendRepository.findAllByCompanyId(company.getId());

        //결과 조합후 반환
        List<Dividend> dividends = companyDividend.stream().map(s -> Dividend.builder()
                                                                .dividend(s.getDividend())
                                                                .date(s.getDate())
                                                                .build()).collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                .name(company.getName())
                .ticker(company.getTicker())
                .build(),dividends);
    }
}
