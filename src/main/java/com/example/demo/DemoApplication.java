package com.example.demo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import okhttp3.*;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@SpringBootApplication
public class DemoApplication {

    private static final Logger log = Logger.getLogger(DemoApplication.class);

    //基本参数，自行Fiddler抓包分析获取，必要！
    //就诊人信息id
    private static String pat_id = "22191585";
    //滇医通登录用户id
    private static String user_id = "3812265";


    //可以预约九价的医院
    private static Integer cate_id = 23;
    private static Integer hos_type = 4;


    //滇医通的登录认证
    private static String Authorization = "DYT eyJhbGciOiJIUzI1NiJ9.eyJ3ZWNoYXRfaWQiOjUzMzAyNDEsInN1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZSI6MCwib3BlbmlkIjoib19VMzZzejYtc0l5QmszQ1VzNnYtRXpLOW9MQSIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjowLCJuZXdfb3BlbmlkIjoiIiwiZHpqX29wZW5pZCI6IiIsInVzZXJfaWQiOjM4MTIyNjUsIndlY2hhdF9vcGVuX2lkIjoib19VMzZzejYtc0l5QmszQ1VzNnYtRXpLOW9MQSIsInVuaW9uX2lkIjoib05RejQwWG80WktTOFViMzZzOG1Ma0ktTTRhcyIsIm1vY2tfb3BlbmlkIjpmYWxzZSwibWluaV9vcGVuaWQiOiJvaUE0UDVDNVRKaXlJWEptT2ptb1JXQ0tMbWxrIiwiZXhwIjoxNjY3NDU4NDE5LCJpYXQiOjE2Njc0NTI4MTl9.RMva7iPTLqV-goStJtxFtmPLjhbY3cvby-e1ZLT-774";
    //滇医通的登录认证
    private static String x_uuid = "96A772EA12987A3BEE5290838920BE4B";
    //浏览器Cookie
    private static String acw_tc = "_ga=GA1.2.93876309.1667452550; _gid=GA1.2.842777932.1667452550; acw_tc=2760776f16674529881496790e676c75d6086962e61a502b8d4164871ef8ad";


    public static void main(String[] args) throws IOException {
        //查询出有九价育苗的医院
        List<Hospital> allhospital = allhospital(cate_id, hos_type);
        //是否指定医院？排除某些离我远的医院 去医院.JSON里面找
//        allhospital = allhospital.stream().filter(all -> !Arrays.asList("","","",).contains(all.getHos_id())).collect(Collectors.toList());


        //针对九价育苗的医院筛选出有号的列表
        for (Hospital hospital : allhospital) {
            //拿到了所有的科室
            Department.DepList department = department(Integer.valueOf(hospital.getHos_id()));
            if (Objects.isNull(department)) {
                continue;
            }
            //拿到九价信息
            Doctor doctor = doctor(Integer.valueOf(hospital.getHos_id()), department.getDep_id());
            if (doctor == null) {
                continue;
            }
            //探测是否有育苗
            List<Schedule> scheduleList = schedule(Integer.valueOf(hospital.getHos_id()), department.getDep_id(), doctor.getDoc_id());
            if (CollUtil.isEmpty(scheduleList)) {
                continue;
            }
            log.info(StrUtil.format("{}-疫苗- 九价号探测", hospital.getHos_name()));
            log.info("现在系统时间：" + DateUtil.format(DateUtil.date(), DatePattern.NORM_DATETIME_MS_PATTERN));
            log.info("==============================================================================");
            for (Schedule schedule : scheduleList) {
                log.info("日期：" + schedule.getSch_date() +
                        "\t时间：" + schedule.getCate_name() +
                        "\t时段：" + schedule.getTime_type().replace("1", "早上").replace("2", "下午") +
                        "\t总号源：" + schedule.getSrc_max() +
                        "\t剩余号源：" + schedule.getSrc_num()
                );
                //判断以上数组遍历是否存在剩余号源
                if (schedule.getSrc_num() != 0) {
                    //尝试提交，如果提交成功，退出wile，程序结束！
                    boolean b = YuY(schedule, hospital, doctor);
                    if (b){
                        break;
                    }
                }
            }
        }
    }

    public static boolean YuY(Schedule schedule, Hospital hospital, Doctor doctor) throws IOException {
        HttpRequest request = HttpUtil.createPost(StrUtil.format("https://dytapi.ynhdkc.com/v1/appoint?hos_code={}&dep_id={}&doc_id={}&pat_id={}&user_id={}&schedule_id={}&cate_name=", doctor.getHos_id(), doctor.getDep_id(), pat_id, user_id, schedule.getSchedule_id()))
                .header("Connection", " keep-alive")
                .header("Accept", " application/json, text/plain, */*")
                .header("Authorization", Authorization)
                .header("x-uuid", x_uuid)
                .header("Cookie", acw_tc)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat")
                .header("Content-Type", " application/json")
                .header("Referer", "https://appv2.ynhdkc.com/registration_order_confirm")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "en-us,en");
//        {"doc_name":"进口成人流感疫苗","hos_name":"昆明市第一人民医院北院(甘美医院)-疫苗","hos_code":"871908","dep_name":"成人疫苗预约","level_name":"","dep_id":"1063","doc_id":"3798","pat_id":22191585,"schedule_id":1634295,"jz_card":"",
//        "sch_date":"2022-11-04","time_type":"1","info":"","ghf":0,"zlf":0,"zjf":0,"jz_start_time":0,"amt":0,"jz_card_type":0,"queue_sn_id":"","wechat_login":"dytminiapp"}
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("doc_name", doctor.getDoc_name());
        paramMap.put("hos_name", hospital.getHos_name());
        paramMap.put("hos_code", hospital.getHos_id());
        paramMap.put("dep_name", doctor.getDepart_name());
        paramMap.put("level_name", doctor.getLevel_name());
        paramMap.put("dep_id", doctor.getDep_id());
        paramMap.put("doc_id", doctor.getDoc_id());
        paramMap.put("pat_id", pat_id);
        paramMap.put("schedule_id", schedule.getSchedule_id());
        paramMap.put("jz_card", "");
        paramMap.put("sch_date", schedule.getSch_date());
        paramMap.put("time_type", schedule.getTime_type());
        paramMap.put("info", "");
        paramMap.put("ghf", schedule.getGhf());
        paramMap.put("zlf", schedule.getZlf());
        paramMap.put("zjf", schedule.getZjf());
        paramMap.put("jz_start_time", 0);
        paramMap.put("amt", schedule.getAmt());
        paramMap.put("jz_card_type", 0);
        paramMap.put("queue_sn_id", "");
        paramMap.put("wechat_login", "dytminiapp");
        try {
            HttpResponse execute = request.body(JSON.toJSONString(paramMap), "application/json").execute();
            if (Objects.nonNull(execute) && execute.isOk()) {
                //预约成功  退出程序
                log.info(StrUtil.format("预约成功，返回结果:{}", execute.body()));
                return Boolean.TRUE;
            } else {
                log.info(StrUtil.format("预约失败，返回结果:{}", execute.body()));
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.info("接口调用失败！");
            return Boolean.FALSE;
        }
    }


    /**
     * 查询每个可以预约九价的医院
     */
    public static List<Hospital> allhospital(Integer cate_id, Integer hos_type) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cate_id", cate_id);
        paramMap.put("hos_type", hos_type);
        String s = HttpUtil.get("https://newdytapi.ynhdkc.com/index/allhospital", paramMap);
        ResultMsg resultMsg = JSON.parseObject(s, ResultMsg.class);
        List<Hospital> hospitals = JSON.parseArray(JSON.toJSONString(resultMsg.getData()), Hospital.class);
        return hospitals;
    }

    /**
     * 查询医院的科室列表 单个科室直接取  多个科室匹配名称 成人疫苗/宫颈癌疫苗
     */
    public static Department.DepList department(Integer hos_code) {
        String s = HttpUtil.get(StrUtil.format("https://newdytapi.ynhdkc.com/index/hospital/{}/depart", hos_code));
        ResultMsg resultMsg = JSON.parseObject(s, ResultMsg.class);
        Department department = JSON.parseArray(JSON.toJSONString(resultMsg.getData()), Department.class).get(0);
        List<Department.DepList> depList = department.getDep_list();
        if (depList.size() > 1) {
            depList = department.getDep_list().stream().filter(dep -> StrUtil.contains(dep.getDep_name(), "成人疫苗") || StrUtil.contains(dep.getDep_name(), "宫颈癌疫苗")).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(depList)) {
            return null;
        }
        return depList.get(0);
    }

    /**
     * 查询医院的科室可预约九价的列表
     */
    public static Doctor doctor(Integer hos_code, Integer depId) {
        //时间动态获取7天
        String startTime = DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_PATTERN);
        String endTime = DateUtil.format(DateUtil.nextWeek(), DatePattern.NORM_DATE_PATTERN);
        String s = HttpUtil.get(StrUtil.format("https://newdytapi.ynhdkc.com/index/doctor?hos_code={}&dep_id={}&from_date={}&end_date={}&reg_date=2017-2-20&vip=0&from_dyt=0", hos_code, depId, startTime, endTime));
        ResultMsg resultMsg = JSON.parseObject(s, ResultMsg.class);
        List<Doctor> doctors = JSON.parseArray(JSON.toJSONString(resultMsg.getData()), Doctor.class);
        return doctors.stream().filter(doc -> StrUtil.contains(doc.getDoc_name(), "九价")).findFirst().orElse(null);
    }

    /**
     * 探测是否有育苗
     *
     * @param hos_code
     * @param depId
     * @return
     */
    public static List<Schedule> schedule(Integer hos_code, Integer depId, Integer doc_id) {
        //时间动态获取7天
        String startTime = DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_PATTERN);
        String endTime = DateUtil.format(DateUtil.nextWeek(), DatePattern.NORM_DATE_PATTERN);
        String s = HttpUtil.get(StrUtil.format("https://newdytapi.ynhdkc.com/index/schedule?hos_code={}&dep_id={}&doc_id={}}&from_date={}&end_date={}&reg_date=2017-2-20&hyid=&vip=0&other_info=undefined", hos_code, depId, doc_id, startTime, endTime));
        ResultMsg resultMsg = JSON.parseObject(s, ResultMsg.class);
        if (resultMsg.getData() == null || StrUtil.isBlank(String.valueOf(resultMsg.getData()))) {
            return null;
        }
        List<Schedule> schedules = JSON.parseArray(JSON.toJSONString(resultMsg.getData()), Schedule.class);
        return schedules;
    }

}
