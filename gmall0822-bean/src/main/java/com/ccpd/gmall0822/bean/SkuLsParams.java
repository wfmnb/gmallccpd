package com.ccpd.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class SkuLsParams implements Serializable {

    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=2;
}

