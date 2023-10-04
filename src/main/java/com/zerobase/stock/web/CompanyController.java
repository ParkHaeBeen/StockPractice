package com.zerobase.stock.web;

import com.zerobase.stock.model.Company;
import com.zerobase.stock.model.constant.CacheKey;
import com.zerobase.stock.persisit.entity.CompanyEntity;
import com.zerobase.stock.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    private final CacheManager redisCacheManager;
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autoComplete(@RequestParam String keyword){
        // List<String> autocomplete = companyService.autocomplete(keyword);
        List<String> autocomplete = companyService.getCompanyNameByKeyWord(keyword);
        return ResponseEntity.ok(autocomplete);
    }

    @GetMapping()
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable){
        Page<CompanyEntity> companies = companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping()
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request){
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {

            throw new RuntimeException("ticker is Empty");
        }

        Company com = companyService.save(ticker);
        companyService.addAutoCompleteKeyword(com.getName());
        return ResponseEntity.ok(com);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker){
        String company = companyService.deleteCompany(ticker);
        clearFinanceCache(company);
        return ResponseEntity.ok(company);
    }

    public void clearFinanceCache(String companyName){
        redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
