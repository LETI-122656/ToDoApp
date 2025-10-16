package com.example.examplefeature;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public interface ExchangeService {

    BigDecimal convert(BigDecimal amount, String source, String target);
    BigDecimal rate(String source, String target);
    Map<String, BigDecimal> multipleRates(String base, Collection<String> targets);

}
