package com.example.demo;

import lombok.Data;

import java.util.List;

/**
 * @Author shenyang
 * @Date 11:27
 * @Description 科室信息
 */
@Data
public class Department {
    private int dep_get_doc_type;
    private List<DepList> dep_list;

    @Data
    public static class DepList {
        private int dep_id;
        private String dep_name;
        private String first_char;
        private String dep_addr;
        private int dep_status;
        private int reservation_type;
        private int pid;
        private int hos_type;
        private String hos_code;
        private String dep_intro;
    }
}
