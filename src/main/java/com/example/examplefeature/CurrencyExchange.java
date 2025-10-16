package com.example.examplefeature;

import org.springframework.stereotype.Service;

import javax.money.*;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CurrencyExchange implements ExchangeService {

    private final ExchangeRateProvider provider = MonetaryConversions.getExchangeRateProvider("ECB");

    @Override
    public BigDecimal convert(BigDecimal amount, String source, String target) {
        if (source.equalsIgnoreCase(target)) return amount;

        CurrencyUnit from = Monetary.getCurrency(source);
        CurrencyUnit to   = Monetary.getCurrency(target);

        MonetaryAmount input = Monetary.getDefaultAmountFactory()
                .setCurrency(from)
                .setNumber(amount)
                .create();

        MonetaryAmount result = input.with(provider.getCurrencyConversion(to));
        return result.getNumber().numberValueExact(BigDecimal.class)
                .setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    @Override
    public BigDecimal rate(String source, String target) {
        if (source.equalsIgnoreCase(target)) return BigDecimal.ONE;

        ExchangeRate rate = provider.getExchangeRate(Monetary.getCurrency(source), Monetary.getCurrency(target));
        return rate.getFactor().numberValueExact(BigDecimal.class)
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    @Override
    public Map<String, BigDecimal> multipleRates(String base, Collection<String> targets) {
        Map<String, BigDecimal> results = new LinkedHashMap<>();
        for (String code : targets) {
            results.put(code, rate(base, code));
        }
        return results;
    }
}
