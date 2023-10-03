package com.zerobase.stock.persisit.entity;

import com.zerobase.stock.model.Company;
import lombok.*;

import javax.persistence.*;

@Entity(name = "COMPANY")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true)
    private String ticker;

    public CompanyEntity(Company company){
        this.ticker= company.getTicker();
        this.name=company.getName();
    }
}
