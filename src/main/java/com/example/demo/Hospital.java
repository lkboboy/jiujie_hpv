/**
 * Copyright 2022 json.cn
 */
package com.example.demo;

import lombok.Data;

/**
 * Auto-generated: 2022-11-03 10:16:19
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */

/**
 * 医院列表
 */
@Data
public class Hospital {
    private String hos_id;
    private int cate_id;
    private String hos_logo;
    private String hos_name;
    private String hos_address;
    private int depart_pay;
    private int treat_wait;
    private int is_report;
    private int status;
    private int list_sort;
    private int hos_type;
    private int sort;
    private String hos_longlat;
    private String stop_info;
    private int small_system;
    private int index_hide;

}