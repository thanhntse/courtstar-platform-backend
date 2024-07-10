package com.example.courtstar.services.payment;

import com.example.courtstar.entity.CentreManager;
import com.example.courtstar.exception.AppException;
import com.example.courtstar.exception.ErrorCode;
import com.example.courtstar.repositories.CentreManagerRepository;
import jakarta.xml.bind.DatatypeConverter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;


@Service
public class CallBackDonateService {
    @Value("${payment.zalopay.KEY2}")
    private String KEY2;
    private Mac HmacSHA256;

    @Autowired
    private CentreManagerRepository centreManagerRepository;


    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Object doCallBack(JSONObject result, String jsonStr) throws JSONException, NoSuchAlgorithmException, InvalidKeyException {
        HmacSHA256  = Mac.getInstance("HmacSHA256");
        HmacSHA256.init(new SecretKeySpec(KEY2.getBytes(), "HmacSHA256"));

        try {
            JSONObject cbdata = new JSONObject(jsonStr);
            String dataStr = cbdata.getString("data");
            String reqMac = cbdata.getString("mac");


            byte[] hashBytes = HmacSHA256.doFinal(dataStr.getBytes());
            String mac = DatatypeConverter.printHexBinary(hashBytes).toLowerCase();


            if (!reqMac.equals(mac)) {
                result.put("return_code", -1);
                result.put("return_message", "mac not equal");
            } else {


                JSONObject data = new JSONObject(dataStr);
                logger.info("update order's status = success where app_trans_id = " + data.getString("app_trans_id"));

                //
                JSONArray jsonArray = new JSONArray(data.getString("item"));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                int id_manager_centre = jsonObject.getInt("id_manager_centre");

                CentreManager centreManager = centreManagerRepository.findById(id_manager_centre).orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_USER));
                Long amount = (long) (centreManager.getCurrentBalance()+data.getLong("amount"));
                centreManager.setCurrentBalance(amount);
                centreManagerRepository.save(centreManager);
                //
                result.put("return_code", 1);
                result.put("return_message", "success");
            }
        } catch (Exception ex) {
            result.put("return_code", 0);
            result.put("return_message", ex.getMessage());
        }


        return result;
    }
}





