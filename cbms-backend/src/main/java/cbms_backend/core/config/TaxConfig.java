package cbms_backend.core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TaxConfig {

    public static final double DEFAULT_TAX_RATE = 0.10;

    @Value("${cbms.tax-rate:0.10}")
    private double taxRate = DEFAULT_TAX_RATE;
}
