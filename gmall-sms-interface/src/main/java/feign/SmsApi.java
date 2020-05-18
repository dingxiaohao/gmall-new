package feign;

import com.atguigu.core.bean.Resp;
import dto.SaleDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface SmsApi {

    @PostMapping("sms/skubounds/skusale/save")  //post新增，put修改更新
    public Resp<Object> saveSkuSaleInfo(@RequestBody SaleDTO saleDTO);

}
