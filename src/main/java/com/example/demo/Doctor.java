package com.example.demo;

import lombok.Data;

import java.util.List;

/**
 * @Author shenyang
 * @Date 11:50
 * @Description
 */
@Data
public class Doctor {

    private int doc_id;
    private int hospital_id;
    private int dep_id;
    private String doc_name;
    private String doctor_info;
    private String doc_avatar;
    private String doc_good;
    private String level_name;
    private int status;
    private int sort;
    private String offline_payment_tips;
    private String depart_name;
    private String hos_id;
    private int amt;
    private int is_doctor;
    private int src_num;
    private List<String> date_sch_num;
    private String sch_date;
    private int hospital_type;
}
