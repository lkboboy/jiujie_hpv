package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import net.sf.json.JSONArray;
import okhttp3.*;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;


@SpringBootApplication
public class DemoApplication {

    private static final Logger log = Logger.getLogger(DemoApplication.class);

    //基本参数，自行Fiddler抓包分析获取，必要！
    private static String hos_code = "871958";     //医院id
    private static String dep_id = "379";          //医院的总疫苗选择页id
    private static String doc_id = "1334";         //医院的预约疫苗类型的id
    private static String pat_id = "21151484";       //就诊人信息id
    private static String user_id = "3660250";       //滇医通登录用户id
    private static String Authorization = "DYT eyJhbGciOiJIUzI1NiJ9.eyJ3ZWNoYXRfaWQiOjUxNTM2NTAsInN1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZSI6MCwib3BlbmlkIjoibzdMQ1g2QXRJd0RIdklFeGQ1cTBTUnFwN1dscyIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjoxLCJuZXdfb3BlbmlkIjoibzdMQ1g2QXRJd0RIdklFeGQ1cTBTUnFwN1dscyIsInVzZXJfaWQiOjM2NjAyNTAsIndlY2hhdF9vcGVuX2lkIjoibzdMQ1g2QXRJd0RIdklFeGQ1cTBTUnFwN1dscyIsInVuaW9uX2lkIjoib05RejQwZnp6RlprUklxcGZlZ3V3UG1UcHY5WSIsIm1vY2tfb3BlbmlkIjpmYWxzZSwibWluaV9vcGVuaWQiOiIiLCJleHAiOjE2NTAyNDM4MzUsImlhdCI6MTY1MDIzODIzNX0";      //滇医通的登录认证
    private static String x_uuid = "D61EA13E10BD28D07741A836AB65DA35";      //滇医通的登录认证
    private static String acw_tc = "acw_tc=2760777f16505387202953524e32662f5b2cc00e9bc646cf8e02980418bfad";      //滇医通的登录认证

    private static boolean start = true;    //wile保险，怕重复提交表单
    private static Integer countI = 0;      //预约失败计次
    private static Integer countJ = 0;      //预约失败5次自动退出

    public static void main(String[] args) throws IOException {

        //提前声明，优化重复赋值
        Request request;
        Response response;
        String tempResponse;
        JSONObject json;
        JSONArray jsonArray;

        int jsonArraySize;
        int j;

        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

        //while暴力循环监测疫苗号源，程序请在疫苗开枪前30s内开启，请勿长时间使用程序监测号源，避免后台把该接口封死。
        while (start) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            request = new Request.Builder()
                    .url("https://newdytapi.ynhdkc.com/index/schedule?hos_code=" + hos_code +
                            "&dep_id=" + dep_id +
                            "&doc_id=" + doc_id)
                    .method("GET", null)
                    .build();
            response = client.newCall(request).execute();

            //需要中转一下，才能toString出body值
            tempResponse = response.body().string();
            json = JSONObject.parseObject(tempResponse);
            jsonArray = JSONArray.fromObject(json.getJSONArray("data"));
            jsonArraySize = jsonArray.size();    //优化for循环的重复计算，减少遍历时间。

            log.info("昆明市妇幼保健院(华山西路院区)-疫苗- 九价号探测");

            //log.info("现在系统时间：" + df.format(new Date()));
            //log.info("==============================================================================");

            log.info(tempResponse);


            //显示打印+疫苗号源列表遍历,从后往前遍历（抢），增加抢到概率。
            for (int i = 1; i <= jsonArraySize; i++) {
                j = jsonArraySize - i; //优化for循环的重复计算，减少遍历时间。

                log.info("日期：" + jsonArray.getJSONObject(j).getString("sch_date") +
                        "\t时间：" + jsonArray.getJSONObject(j).getString("cate_name") +
                        "\t时段：" + jsonArray.getJSONObject(j).getString("time_type").replace("1", "早上").replace("2", "下午") +
                        "\t总号源：" + jsonArray.getJSONObject(j).getString("src_max") +
                        "\t剩余号源：" + jsonArray.getJSONObject(j).getString("src_num")

                );

                //判断以上数组遍历是否存在剩余号源
                if (Integer.parseInt(jsonArray.getJSONObject(j).getString("src_num")) != 0) {

                    //尝试提交，如果提交成功，退出wile，程序结束！
                    Get(jsonArray.getJSONObject(j).getString("schedule_id"), jsonArray.getJSONObject(j).getString("sch_date"), jsonArray.getJSONObject(j).getString("time_type"));
                    break;

                }
            }

            log.info("==============================================================================");
            //生成jar包，挂机，dos下刷屏显示用的！
//                new ProcessBuilder("cmd", "/c", "cls")
//                        .inheritIO()
//                        .start()
//                        .waitFor(); // 清屏命令
        }

    }

    //抢九价疫苗，已拼接,换医院的话 改哈doc_name、hos_name 2个字段，其他不变，level_name字段页面 “疫苗接种预约|  ” 后面是否有 “疫苗” 两个字！
    public static boolean Get(String schedule_id, String sch_date, String time_type) throws IOException {
        log.info(schedule_id + "==========" + sch_date + "==========" + time_type + "==========" + hos_code + "==========" + dep_id + "==========" + doc_id + "==========" + pat_id);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse(" application/json");
        RequestBody body = RequestBody.create(mediaType, "{" +
                "\"doc_name\": \"进口九价宫颈癌疫苗\"," +
                "\"hos_name\": \"昆明市妇幼保健院(华山西路院区)-疫苗\"," +
                "\"hos_code\": " + "\"" + hos_code + "\"," +                                    //医院id
                "\"dep_name\": \"疫苗接种预约\"," +
                "\"level_name\": \"疫苗\"," +
                "\"dep_id\": " + "\"" + dep_id + "\"," +                                        //医院的总疫苗选择页id
                "\"doc_id\": " + "\"" + doc_id + "\"," +                                        //医院的预约疫苗类型的id
                "\"pat_id\": " + pat_id + "," +                                                //就诊人信息id
                "\"schedule_id\": " + schedule_id + "," +                                      //排班id
                "\"jz_card\": \"\"," +
                "\"sch_date\": " + "\"" + sch_date + "\"," +                                    //排班时间
                "\"time_type\": " + "\"" + time_type + "\"," +                                  //1为早上，2为下午
                "\"info\": \"\"," +
                "\"ghf\": 0," +
                "\"zlf\": 0," +
                "\"zjf\": 0," +
                "\"jz_start_time\": 0," +
                "\"amt\": 0," +
                "\"jz_card_type\": 0," +
                "\"queue_sn_id\": \"\"," +
                "\"wechat_login\": \"\"}");
        Request request = new Request.Builder()
                .url("https://dytapi.ynhdkc.com/v1/appoint?hos_code=" + hos_code +
                        "&dep_id=" + dep_id +
                        "&doc_id=" + doc_id +
                        "&pat_id=" + pat_id +
                        "&user_id=" + user_id +
                        "&schedule_id=" + schedule_id +
                        "&cate_name=")
                .method("POST", body)
                .addHeader("Connection", " keep-alive")
                .addHeader("Accept", " application/json, text/plain, */*")
                .addHeader("Authorization", Authorization)
                .addHeader("User-Agent", " Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x6305002e)")
                .addHeader("x-uuid", x_uuid)
                .addHeader("Cookie", acw_tc)
                .addHeader("Content-Type", " application/json")
                .build();
        Response response = client.newCall(request).execute();
        String tempResponse = response.body().string();
        JSONObject json = JSONObject.parseObject(tempResponse);

        log.info(tempResponse);
        if (json.getString("msg").equals("预约成功")) {
            log.info("预约成功！");
            start = false;
            return true;
        } else {

            log.info("预约失败！");
            countI++; //预约失败计次
            if (countI >= countJ) { //预约失败5次自动退出
                start = false;
            }
            return false;
        }

    }

}
