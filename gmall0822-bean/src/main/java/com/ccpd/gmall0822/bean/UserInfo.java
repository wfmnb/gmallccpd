package com.ccpd.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

//data增加get/set方法
@Data
//增加无参构造器
@NoArgsConstructor
public class UserInfo implements Serializable {
    //id标识为 主键
    @Id
    //跟表字段对应
    @Column
    //允许字段自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column//标识为列，跟数据库字段对应
    private String loginName;
    @Column
    private String nickName;
    @Column
    private String passwd;
    @Column
    private String name;
    @Column
    private String phoneNum;
    @Column
    private String email;
    @Column
    private String headImg;
    @Column
    private String userLevel;
}
