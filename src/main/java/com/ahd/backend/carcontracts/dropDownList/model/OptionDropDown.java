package com.ahd.backend.carcontracts.dropDownList.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class OptionDropDown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String label;
    private String value;
    private Long dropDownId;
    private Long root;
    private Long sub;
}

//ALTER TABLE dbo.option_drop_down
//ADD root BIGINT NULL;
//ALTER TABLE dbo.option_drop_down
//ADD sub BIGINT NULL;

//ALTER TABLE dbo.car
//ADD [description] NVARCHAR(500) NULL;