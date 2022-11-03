package com.example.demo;

import lombok.Data;

import java.util.Date;

/**
 * @Author shenyang
 * @Date 12:45
 * @Description
 */
@Data
public class Schedule {
    private long schedule_id;
    private String time_type;
    private Date sch_date;
    private int src_max;
    private int src_num;
    private String cate_name;
    private int ghf;
    private int zlf;
    private int zjf;
    private int amt;
    private String doc_id;
    private int is_datepart;
}
