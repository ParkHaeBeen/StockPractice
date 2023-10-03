package com.zerobase.stock.service;

import com.zerobase.stock.exception.Impl.NoCompanyException;
import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.ScrapedResult;
import com.zerobase.stock.persisit.CompanyRepository;
import com.zerobase.stock.persisit.DividendRepository;
import com.zerobase.stock.persisit.entity.CompanyEntity;
import com.zerobase.stock.persisit.entity.DividendEntity;
import com.zerobase.stock.scrapper.FinanceScrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final FinanceScrapper yahooFinancecScrapper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    public Company save(String ticker){
        if(companyRepository.existsByTicker(ticker)){
            throw new RuntimeException("already exties ticker"+ticker);
        }

        return storeCompanyAndDividend(ticker);
    }

    private Company storeCompanyAndDividend(String ticker){
        //티커를 기준으로 회사를 스크래핑
        Company company = yahooFinancecScrapper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker -> "+ticker);
        }

        //회사가 존재하면 회사의 배당정보 스크래핑
        ScrapedResult scrapedResult = yahooFinancecScrapper.scrap(company);

        //스크래핑 결과
        CompanyEntity com = companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities = scrapedResult.getDividendList().stream()
                .map(e -> new DividendEntity(com.getId(), e))
                .collect(Collectors.toList());

        dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public Page<CompanyEntity> getAllCompany(final Pageable pageable){
        return companyRepository.findAll(pageable);
    }

    public void addAutoCompleteKeyword(String keyword){
        trie.put(keyword,null);
    }

    public List<String> autocomplete(String keyword){
        return (List<String>) trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword){
        trie.remove(keyword);
    }

    public List<String> getCompanyNameByKeyWord(String keyword){
        Pageable limit= PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(keyword,limit);
        return companyEntities.stream()
                .map(s->s.getName()).collect(Collectors.toList());
    }

    public String deleteCompany(String ticker) {
        CompanyEntity company = companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        dividendRepository.deleteAllByCompanyId(company.getId());
        companyRepository.delete(company);

        deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }
}
