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
    private static String doc_id = "2488";         //医院的预约疫苗类型的id
    private static String pat_id = "1111111";       //就诊人信息id
    private static String user_id = "1111111";      //滇医通登录用户id
    private static String Authorization = "DYT eyJhbGciOiJiUzI1NiW9.eyJ3ZWNoYXRfaWQiOjQ0MTU4MTQsInW1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZWI6MCwib3BlbmlkIjoib19VWzZzeUxJQm12bF9pZm5HWkF3S0wya1ZFYyIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjoxLCJuZXdfb3BlbmlkIjoibzdMQ1g2QXN3SW9WdFNKd29qQ1pibDczLWd1VSIsImR6al9vcGVuaWQiOiIiLCJ1c2VyX2lkIjozNTI1MDYxLCJ3ZWNoYXRfb3Blbl9pZCI6Im9fVTM2c3lMSUJtdmxfaWZuR1pBd0tMMmtWRWMiLCJ1bmlvbl9pZCI6Im9OUXo0MFJBYlNPRjhQcUlndERFc3VSWkFmNzAiLCJtb2NrX29wZW5pZCI6ZmFsc2UsIm1pbmlfb3BlbmlkIjoib2lBNFA1SklNQzZYMjNPSUlrcHkweWJpdDN4QSIsImV4cCI6MTY0OTUyNDQzOSwiaWF0IjoxNjQ5NTE4ODM5fQ.jjb_fN7FimScVtL8moTeyHqEWbyHLbaKhmAaW4gaAJc";      //滇医通的登录认证
    private static String x_uuid = " DCW7472807CD3951507515C7B5C03B9F";      //滇医通的登录认证

    public static void main(String[] args) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://newdytapi.ynhdkc.com/index/schedule?hos_code=" + hos_code +
                        "&dep_id=" + dep_id +
                        "&doc_id=" + doc_id )
                .method("GET", null)
                .addHeader("Cookie", "acw_tc=2760776e16470724890577588ea7df0ea7204aa757b51b2e59ed3b745302df")
                .build();
        Response response = client.newCall(request).execute();
        //需要中转一下，才能toString出body值
        String tempResponse = response.body().string();
        JSONObject json = JSONObject.parseObject(tempResponse);
        JSONArray jsonArray = JSONArray.fromObject(json.getJSONArray("data"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

        //while暴力循环监测疫苗号源，程序请在疫苗开枪前30s内开启，请勿长时间使用程序监测号源，避免后台把该接口封死。
        while (true) {
            System.out.println("昆明市妇幼保健院（华山西路院区）- 九价号探测");
            String data = df.format(new Date());
            System.out.println("现在系统时间：" + data);
            System.out.println("==============================================================================");

            //显示打印+疫苗号源列表遍历,从后往前遍历（抢），增加抢到概率。
            for (int i = 1; i < jsonArray.size(); i++) {
                System.out.println("日期：" + jsonArray.getJSONObject(jsonArray.size()-i).getString("sch_date") +
                        "\t时间：" + jsonArray.getJSONObject(jsonArray.size()-i).getString("cate_name") +
                        "\t时段：" + jsonArray.getJSONObject(jsonArray.size()-i).getString("time_type").replace("1", "早上").replace("2", "下午") +
                        "\t总号源：" + jsonArray.getJSONObject(jsonArray.size()-i).getString("src_max") +
                        "\t剩余号源：" + jsonArray.getJSONObject(jsonArray.size()-i).getString("src_num")
                );

                //判断以上数组遍历是否存在剩余号源
                if (jsonArray.getJSONObject(jsonArray.size()-i).getString("src_num") != "0") {

                    //尝试提交，如果提交成功，退出wile，程序结束！如果过时段没提示抢到成功请手动关闭程序！
                    if (Get(jsonArray.getJSONObject(jsonArray.size()-i).getString("schedule_id"), jsonArray.getJSONObject(jsonArray.size()-i).getString("sch_date"), jsonArray.getJSONObject(jsonArray.size()-i).getString("time_type"))) {
                        System.out.println("预约成功！");
                        break;
                    }
                }

                //生成jar包，挂机，dos下刷屏显示用的！
//                System.out.println("==============================================================================");
//                new ProcessBuilder("cmd", "/c", "cls")
//                        .inheritIO()
//                        .start()
//                        .waitFor(); // 清屏命令
            }

        }
    }

    //抢九价疫苗，已拼接，不用改！
    public static boolean Get(String schedule_id, String sch_date, String time_type) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse(" application/json");
        RequestBody body = RequestBody.create(mediaType, "{" +
                "\"doc_name\": \"进口九价宫颈癌疫苗\"," +
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
                .addHeader("Cookie", "acw_tc=2760824616495046547086623eed142e936eea65ecd682e927ff1428e2ecc1")
                .build();
        Response response = client.newCall(request).execute();
        String tempResponse = response.body().string();
        JSONObject json = JSONObject.parseObject(tempResponse);
        if (json.getString("msg").equals("预约成功")) {
            return true;
        }
        return false;
    }

}
