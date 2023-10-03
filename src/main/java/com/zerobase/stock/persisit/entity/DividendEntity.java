package com.zerobase.stock.persisit.entity;

import com.zerobase.stock.model.Dividend;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name="DIVIDEND")
@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"companyId","date"}
                )
        }
)
public class DividendEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long companyId;
    private LocalDateTime date;
    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend){
        this.companyId=companyId;
        this.date=dividend.getDate();
        this.dividend=dividend.getDividend();
    }
}
