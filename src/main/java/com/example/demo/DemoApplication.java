package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import net.sf.json.JSONArray;
import okhttp3.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class DemoApplication {

    //基本参数，自行Fiddler抓包分析获取，必要！
    private static String hos_code = "871958";     //医院id
    private static String dep_id = "379";          //医院的总疫苗选择页id
    private static String doc_id = "1334";         //医院的预约疫苗类型的id
    private static String pat_id = "123345";       //就诊人信息id
    private static String user_id = "12345";      //滇医通登录用户id
    private static String Authorization = "DYT 11111111111.eyJ3ZWNoYXRfaWQiOjQ0MTU4MTQsInN1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZSI6MCwib3BlbmlkIjoib19VMzZzeUxJQm12bF9pZm5HWkF3S0wya1ZFYyIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjoxLCJuZXdfb3BlbmlkIjoibzdMQ1g2QXN3SW9WdFNKd29qQ1pibDczLWd1VSIsImR6al9vcGVuaWQiOiIiLCJ1c2VyX2lkIjozNTI1MDYxLCJ3ZWNoYXRfb3Blbl9pZCI6Im9fVTM2c3lMSUJtdmxfaWZuR1pBd0tMMmtWRWMiLCJ1bmlvbl9pZCI6Im9OUXo0MFJBYlNPRjhQcUlndERFc3VSWkFmNzAiLCJtb2NrX29wZW5pZCI6ZmFsc2UsIm1pbmlfb3BlbmlkIjoib2lBNFA1SklNQzZYMjNPSUlrcHkweWJpdDN4QSIsImV4cCI6MTY0OTcyNjY0MiwiaWF0IjoxNjQ5NzIxMDQyfQ.nRl2jIba9wZju_IxNvfq4-968RWbO7qAIJ64Iyt-SAw";      //滇医通的登录认证
    private static String x_uuid = "1111111111111111111";      //滇医通的登录认证

    public static void main(String[] args) throws IOException {

        //提前声明，优化重复赋值
        Request request;
        Response response;
        String tempResponse;
        JSONObject json;
        JSONArray jsonArray;

        int jsonArraySize;
        int j;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");


        //while暴力循环监测疫苗号源，程序请在疫苗开枪前30s内开启，请勿长时间使用程序监测号源，避免后台把该接口封死。
        while (true) {
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
            System.out.println("昆明市妇幼保健院(华山西路院区)-疫苗- 九价号探测");
            System.out.println("现在系统时间：" + df.format(new Date()));
            System.out.println("==============================================================================");
            System.out.println(tempResponse);

            //显示打印+疫苗号源列表遍历,从后往前遍历（抢），增加抢到概率。
            for (int i = 1; i <= jsonArraySize; i++) {
                j = jsonArraySize - i; //优化for循环的重复计算，减少遍历时间。
                System.out.println("日期：" + jsonArray.getJSONObject(j).getString("sch_date") +
                        "\t时间：" + jsonArray.getJSONObject(j).getString("cate_name") +
                        "\t时段：" + jsonArray.getJSONObject(j).getString("time_type").replace("1", "早上").replace("2", "下午") +
                        "\t总号源：" + jsonArray.getJSONObject(j).getString("src_max") +
                        "\t剩余号源：" + jsonArray.getJSONObject(j).getString("src_num")
                );

                //判断以上数组遍历是否存在剩余号源
                if (Integer.parseInt(jsonArray.getJSONObject(j).getString("src_num")) != 0) {
                    //尝试提交，如果提交成功，退出wile，程序结束！如果过时段没提示抢到成功请手动关闭程序！
                    if (Get(jsonArray.getJSONObject(j).getString("schedule_id"), jsonArray.getJSONObject(j).getString("sch_date"), jsonArray.getJSONObject(j).getString("time_type"))) {
                        break;
                    }
                }
            }
            System.out.println("==============================================================================");
            //生成jar包，挂机，dos下刷屏显示用的！
//                new ProcessBuilder("cmd", "/c", "cls")
//                        .inheritIO()
//                        .start()
//                        .waitFor(); // 清屏命令
        }
    }

    //抢九价疫苗，已拼接,换医院的话 改哈doc_name、hos_name 2个字段，其他不变，level_name字段页面 疫苗接种预约|  后面是否有 疫苗 两个字！
    public static boolean Get(String schedule_id, String sch_date, String time_type) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse(" application/json");
        RequestBody body = RequestBody.create(mediaType, "{" +
                "\"doc_name\": \"九价宫颈癌疫苗\"," +
                "\"hos_name\": \"昆明市妇幼保健院(华山西路院区)-疫苗\"," +
                "\"hos_code\": " + "\"" + hos_code + "\"," +                                 //医院id
                "\"dep_name\": \"疫苗接种预约\"," +
                "\"level_name\": \"疫苗\"," +
                "\"dep_id\": " + "\"" + dep_id + "\"," +                                       //医院的总疫苗选择页id
                "\"doc_id\": " + "\"" + doc_id + "\"," +                                       //医院的预约疫苗类型的id
                "\"pat_id\": " + "\"" + pat_id + "\"," +                                       //就诊人信息id
                "\"schedule_id\": " + "\"" + schedule_id + "\"," +                                   //排班id
                "\"jz_card\": \"\"," +
                "\"sch_date\": " + "\"" + sch_date + "\"," +                              //排班时间
                "\"time_type\": " + "\"" + time_type + "\"," +                                      //1为早上，2为下午
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
                .addHeader("Content-Type", " application/json")
                .build();
        Response response = client.newCall(request).execute();
        String tempResponse = response.body().string();
        JSONObject json = JSONObject.parseObject(tempResponse);
        System.out.println(tempResponse);
        if (json.getString("msg").equals("预约成功")) {
            System.out.println("预约成功！");
            return true;
        }
        System.out.println("预约失败！");
        return false;
    }

}
