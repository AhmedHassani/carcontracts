package com.ahd.backend.carcontracts.dropDownList.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class DropDown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

}
//
//
//CREATE TABLE drop_down (
//        id BIGINT IDENTITY(1,1) PRIMARY KEY,
//name NVARCHAR(255) NOT NULL,
//created_at DATETIME DEFAULT GETDATE(),
//updated_at DATETIME DEFAULT GETDATE()
//);
//
//
//CREATE TABLE option_drop_down (
//        id BIGINT IDENTITY(1,1) PRIMARY KEY,
//label NVARCHAR(255) NOT NULL,
//value NVARCHAR(255) NOT NULL,
//drop_down_id BIGINT NOT NULL,
//created_at DATETIME DEFAULT GETDATE(),
//updated_at DATETIME DEFAULT GETDATE(),
//
//CONSTRAINT fk_option_dropdown
//FOREIGN KEY (drop_down_id)
//REFERENCES drop_down(id)
//ON DELETE CASCADE
//);
//
//
//
//
//ALTER TABLE drop_down
//ALTER COLUMN name NVARCHAR(255) ;
//
//-- 修改 option_drop_down 表的 label 字段
//ALTER TABLE option_drop_down
//ALTER COLUMN label NVARCHAR(255) COLLATE Arabic_100_CI_AS_SC_UTF8 NOT NULL;
//
//-- 修改 option_drop_down 表的 value 字段
//ALTER TABLE option_drop_down
//ALTER COLUMN value NVARCHAR(255) COLLATE Arabic_100_CI_AS_SC_UTF8 NOT NULL;
//
//
//
//-- 正确：使用 N 前缀表示 Unicode 字符串
//INSERT INTO drop_down (name) VALUES
//(N'نوع السيارة'),
//        (N'اسم السيارة'),
//        (N'الموديل'),
//        (N'اللون'),
//        (N'رقم المحافظة'),
//        (N'نوع اللوحة'),
//        (N'نوع المحرك'),
//        (N'عدد الركاب'),
//        (N'عدد الاسطوانات');
//
//select * from  drop_down
//
//
//ALTER TABLE dbo.car
//ADD init_price NVARCHAR(50) NULL;
/// ///////////////////////////////////////////////////////////////////////////////////



